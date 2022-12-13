package app.shosetsu.android.viewmodel.impl

import android.database.sqlite.SQLiteException
import androidx.work.WorkInfo
import app.shosetsu.android.backend.workers.onetime.ExtensionInstallWorker
import app.shosetsu.android.backend.workers.onetime.RepositoryUpdateWorker
import app.shosetsu.android.common.IncompatibleExtensionException
import app.shosetsu.android.common.SettingKey
import app.shosetsu.android.common.ext.generify
import app.shosetsu.android.common.ext.launchIO
import app.shosetsu.android.common.ext.logI
import app.shosetsu.android.common.ext.logV
import app.shosetsu.android.domain.model.local.InstalledExtensionEntity
import app.shosetsu.android.domain.model.local.NovelEntity
import app.shosetsu.android.domain.model.local.RepositoryEntity
import app.shosetsu.android.domain.repository.base.*
import app.shosetsu.android.domain.usecases.RequestInstallExtensionUseCase
import app.shosetsu.android.domain.usecases.StartRepositoryUpdateManagerUseCase
import app.shosetsu.android.domain.usecases.get.GetURLUseCase
import app.shosetsu.android.viewmodel.abstracted.AAddShareViewModel
import app.shosetsu.lib.IExtension.Companion.KEY_NOVEL_URL
import app.shosetsu.lib.exceptions.InvalidMetaDataException
import app.shosetsu.lib.share.ExtensionLink
import app.shosetsu.lib.share.NovelLink
import app.shosetsu.lib.share.RepositoryLink
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.acra.ACRA

/*
 * This file is part of shosetsu.
 *
 * shosetsu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * shosetsu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with shosetsu.  If not, see <https://www.gnu.org/licenses/>.
 */

/**
 * Shosetsu
 *
 * @since 07 / 03 / 2022
 * @author Doomsdayrs
 */
class AddShareViewModel(
	private val extRepo: IExtensionsRepository,
	private val repoRepo: IExtensionRepoRepository,
	private val novelRepo: INovelsRepository,
	private val getContentURL: GetURLUseCase,
	private val installExtension: RequestInstallExtensionUseCase,
	private val startRepositoryUpdateWorker: StartRepositoryUpdateManagerUseCase,
	private val installManager: ExtensionInstallWorker.Manager,
	private val updateManager: RepositoryUpdateWorker.Manager,
	private val settingRepo: ISettingsRepository,
	private val iExtRepo: IExtensionEntitiesRepository
) : AAddShareViewModel() {
	override val isAdding: MutableStateFlow<Boolean> = MutableStateFlow(false)
	override val isComplete: MutableStateFlow<Boolean> = MutableStateFlow(false)
	override val isNovelOpenable: MutableStateFlow<Boolean> = MutableStateFlow(false)

	override val isNovelAlreadyPresent: MutableStateFlow<Boolean> = MutableStateFlow(false)
	override val isStyleAlreadyPresent: MutableStateFlow<Boolean> = MutableStateFlow(false)
	override val isExtAlreadyPresent: MutableStateFlow<Boolean> = MutableStateFlow(false)
	override val isRepoAlreadyPresent: MutableStateFlow<Boolean> = MutableStateFlow(false)

	override val isProcessing: MutableStateFlow<Boolean> = MutableStateFlow(false)
	override val isURLValid: MutableStateFlow<Boolean> = MutableStateFlow(false)

	override val extLink: MutableStateFlow<ExtensionLink?> = MutableStateFlow(null)
	override val novelLink: MutableStateFlow<NovelLink?> = MutableStateFlow(null)
	override val repoLink: MutableStateFlow<RepositoryLink?> = MutableStateFlow(null)

	private var data: MutableStateFlow<String?> = MutableStateFlow(null)

	override val exception: MutableStateFlow<Exception?> = MutableStateFlow(null)

	override val showURLInput: MutableStateFlow<Boolean> = MutableStateFlow(true)

	override val url: MutableStateFlow<String> = MutableStateFlow("")

	override fun setURL(url: String) {
		this.url.value = url
		val http = url.toHttpUrlOrNull()
		isURLValid.value = http != null
	}

	override fun applyURL() {
		takeData(url.value)
	}

	private var repoEntity: RepositoryEntity? = null
	private var extEntity: InstalledExtensionEntity? = null
	private var novelEntity: NovelEntity? = null

	init {
		launchIO {
			data.collectLatest { url ->
				if (url == null) return@collectLatest

				isProcessing.value = true

				fun invalidate() {
					isProcessing.value = false
					isURLValid.value = false
				}

				val http = url.toHttpUrlOrNull()

				if (http != null) {
					val linkType = http.pathSegments.firstOrNull()

					if (linkType != null) {
						when (linkType) {
							"novel" -> {
								val names = http.queryParameterNames

								if (!names.containsAll(
										listOf(
											"name",
											"url",
											"imageURL",
											"extID",
											"extURL",
											"extName",
											"repoName",
											"repoURL"
										)
									)
								) {
									invalidate()
									return@collectLatest
								}


								val repo = RepositoryLink(
									http.queryParameter("repoName")!!,
									http.queryParameter("repoURL")!!.toHttpUrl().toUri().normalize()
										.toString()
								)

								val ext = ExtensionLink(
									http.queryParameter("extID")!!.toInt(),
									http.queryParameter("extName")!!,
									http.queryParameter("extURL")!!.toHttpUrl().toUri().normalize()
										.toString(),
									repo
								)
								val novel = NovelLink(
									http.queryParameter("name")!!,
									http.queryParameter("imageURL")!!.toHttpUrl().toUri()
										.normalize().toString(),
									http.queryParameter("url")!!.toHttpUrl().toUri().normalize()
										.toString(),
									ext
								)

								logI("Checking if repository is present")
								repoEntity = try {
									repoRepo.loadRepositories().find {
										val entityUrl = it.url.toHttpUrl().toUri().normalize()
										val repoLinkUrl = repo.url.toHttpUrl().toUri()

										logV(entityUrl.toString())
										logV(entityUrl.toString())

										entityUrl == repoLinkUrl
									}
								} catch (e: SQLiteException) {
									null
								}

								logI("Checking if extension is installed")
								extEntity = try {
									extRepo.getInstalledExtension(ext.id)
								} catch (e: SQLiteException) {
									null
								}

								logI("Checking for matching novel in database")
								novelEntity = novelRepo.loadNovels().find { entity ->
									val contentUrl =
										try {
											getContentURL(entity)?.toHttpUrl()?.toUri()?.normalize()
										} catch (e: IllegalArgumentException) {
											// Something is wrong with the extension
											// TODO how to report extension errors
											ACRA.errorReporter.handleSilentException(e)
											return@find false
										}
									val novelLinkUrl = novel.url.toHttpUrl().toUri().normalize()

									logV(contentUrl.toString())
									logV(novelLinkUrl.toString())

									contentUrl == novelLinkUrl
								}?.takeIf { it.bookmarked }

								repoLink.value = repo
								extLink.value = ext
								novelLink.value = novel

								if (repoEntity != null)
									isRepoAlreadyPresent.value = true

								if (extEntity != null)
									isExtAlreadyPresent.value = true

								if (novelEntity != null)
									isNovelAlreadyPresent.value = true

								isProcessing.value = false
								isURLValid.value = true
							}
							"repository" -> {
								invalidate()
							}
							"extension" -> {
								invalidate()
							}
							"style" -> {
								invalidate()
							}
						}
					} else {
						invalidate()
					}
				} else {
					invalidate()
				}
			}
		}
	}

	override fun takeData(url: String) {
		logV(url)
		showURLInput.value = false
		data.value = url
	}

	override fun setUserCancelled() {
		showURLInput.value = false
		setInvalidURL()
	}

	override fun setInvalidURL() {
		isURLValid.value = false
		isProcessing.value = false
	}

	override fun add() {
		launchIO {
			isAdding.value = true

			// Add repository if not present
			if (!isRepoAlreadyPresent.value) {
				val link = repoLink.value!!
				try {
					repoRepo.addRepository(link.url, link.name)
				} catch (e: SQLiteException) {
					exception.value = e
					return@launchIO
				}
				startRepositoryUpdateWorker()

				delay(1000)

				while (updateManager.getWorkerState().let {
						it == WorkInfo.State.ENQUEUED || it == WorkInfo.State.RUNNING || it == WorkInfo.State.BLOCKED
					}) {
					logI("Waiting for repo to finish updating")
					delay(200)
				}
			}

			// Add ext if not present
			if (!isExtAlreadyPresent.value) {
				val link = extLink.value!!

				if (repoEntity == null)
					repoEntity = try {
						repoRepo.loadRepositories().first { it.url == link.repo.url }
					} catch (e: SQLiteException) {
						exception.value = e
						return@launchIO
					} catch (e: NoSuchElementException) {
						exception.value = e
						return@launchIO
					}



				installExtension(link.id, repoEntity!!.id)

				delay(1000)

				while (installManager.getWorkerState().let {
						it == WorkInfo.State.ENQUEUED || it == WorkInfo.State.RUNNING || it == WorkInfo.State.BLOCKED
					}) {
					logI("Waiting for extension to install")
					delay(200)
				}
			}

			// Add style if not present
			@Suppress("ControlFlowWithEmptyBody")
			if (!isStyleAlreadyPresent.value) {
				// TODO Style
			}

			// Add novel if not present
			if (!isNovelAlreadyPresent.value) {
				val link = novelLink.value!!

				if (novelEntity == null)
					novelEntity = novelRepo.loadNovels().find {
						getContentURL(it)?.toHttpUrl()?.toUri()?.normalize() ==
								link.url.toHttpUrl().toUri().normalize()
					}

				try {
					if (extEntity == null)
						extEntity = extRepo.getInstalledExtension(link.extensionQRCode.id)
				} catch (e: SQLiteException) {
					exception.value = e
					return@launchIO
				}

				val iExt = try {
					iExtRepo.get(extEntity!!.generify())
				} catch (e: InvalidMetaDataException) {
					exception.value = e
					return@launchIO
				} catch (e: IncompatibleExtensionException) {
					exception.value = e
					return@launchIO
				}

				try {
					val autoBookmark = settingRepo.getBoolean(SettingKey.AutoBookmarkFromQR)

					if (novelEntity == null) {
						val stripped = novelRepo.insertReturnStripped(
							NovelEntity(
								url = iExt.shrinkURL(link.url, KEY_NOVEL_URL),
								imageURL = link.imageURL,
								title = link.name,
								extensionID = link.extensionQRCode.id,
								bookmarked = autoBookmark
							)
						)

						if (stripped != null)
							novelEntity = novelRepo.getNovel(stripped.id)!!
					} else {
						if (autoBookmark)
							novelRepo.update(
								novelEntity!!.copy(
									bookmarked = true
								)
							)
					}

					isNovelOpenable.value = true
				} catch (e: SQLiteException) {
					exception.value = e
					return@launchIO
				}
			}

			isAdding.value = false
			isComplete.value = true
		}
	}

	override fun destroy() {
		isAdding.value = false
		isComplete.value = false
		isNovelOpenable.value = false

		isNovelAlreadyPresent.value = false
		isStyleAlreadyPresent.value = false
		isExtAlreadyPresent.value = false
		isRepoAlreadyPresent.value = false
		isProcessing.value = false
		isURLValid.value = false

		extLink.value = null
		novelLink.value = null
		repoLink.value = null
		data.value = null
		showURLInput.value = true
		url.value = ""

		exception.value = null
		repoEntity = null
		extEntity = null
		novelEntity = null
	}

	override fun retry() {
		isAdding.value = false
		isProcessing.value = false
		isURLValid.value = false
		showURLInput.value = true
	}

	override fun getNovel(): NovelEntity? =
		novelEntity
}