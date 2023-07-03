package app.shosetsu.android.backend.workers.onetime

import android.content.Context
import android.database.sqlite.SQLiteException
import android.net.Uri
import android.util.Base64
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import app.shosetsu.android.R
import app.shosetsu.android.backend.workers.CoroutineWorkerManager
import app.shosetsu.android.backend.workers.NotificationCapable
import app.shosetsu.android.common.NullContentResolverException
import app.shosetsu.android.common.consts.LogConstants
import app.shosetsu.android.common.consts.Notifications
import app.shosetsu.android.common.consts.Notifications.ID_RESTORE
import app.shosetsu.android.common.consts.VERSION_BACKUP
import app.shosetsu.android.common.consts.WorkerTags.RESTORE_WORK_ID
import app.shosetsu.android.common.ext.*
import app.shosetsu.android.common.utils.backupJSON
import app.shosetsu.android.domain.model.local.*
import app.shosetsu.android.domain.model.local.backup.*
import app.shosetsu.android.domain.repository.base.*
import app.shosetsu.android.domain.usecases.AddCategoryUseCase
import app.shosetsu.android.domain.usecases.InstallExtensionUseCase
import app.shosetsu.android.domain.usecases.StartRepositoryUpdateManagerUseCase
import app.shosetsu.lib.Novel
import app.shosetsu.lib.Version
import app.shosetsu.lib.exceptions.InvalidMetaDataException
import kotlinx.coroutines.delay
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromStream
import org.acra.ACRA
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.instance
import java.io.BufferedInputStream
import java.io.IOException
import java.util.zip.GZIPInputStream

/*
 * This file is part of Shosetsu.
 *
 * Shosetsu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Shosetsu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Shosetsu.  If not, see <https://www.gnu.org/licenses/>.
 */

/**
 * 21 / 01 / 2021
 */
class RestoreBackupWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(
	appContext,
	params
), DIAware, NotificationCapable {
	override val di: DI by closestDI(applicationContext)

	private val backupRepo by instance<IBackupRepository>()
	private val extensionsRepoRepo by instance<IExtensionRepoRepository>()
	private val initializeExtensionsUseCase by instance<StartRepositoryUpdateManagerUseCase>()
	private val extensionsRepo by instance<IExtensionsRepository>()
	private val extensionEntitiesRepo by instance<IExtensionEntitiesRepository>()
	private val installExtension: InstallExtensionUseCase by instance()
	private val novelsRepo by instance<INovelsRepository>()
	private val novelPinsRepo by instance<INovelPinsRepository>()
	private val novelsSettingsRepo by instance<INovelSettingsRepository>()
	private val chaptersRepo by instance<IChaptersRepository>()
	private val chapterHistoryRepo by instance<ChapterHistoryRepository>()
	private val backupUriRepo by instance<IBackupUriRepository>()
	private val categoriesRepo by instance<ICategoryRepository>()
	private val novelCategoriesRepo by instance<INovelCategoryRepository>()
	private val addCategoryUseCase by instance<AddCategoryUseCase>()
	override val baseNotificationBuilder: NotificationCompat.Builder
		get() = notificationBuilder(applicationContext, Notifications.CHANNEL_BACKUP)
			.setSubText(getString(R.string.restore_notification_subtitle))
			.setSmallIcon(R.drawable.restore)
			.setOnlyAlertOnce(true)
			.setOngoing(true)

	override val notificationManager: NotificationManagerCompat by notificationManager()
	override val notifyContext: Context = appContext
	override val defaultNotificationID: Int = ID_RESTORE

	@Throws(IOException::class)
	private fun unGZip(content: ByteArray) =
		GZIPInputStream(content.inputStream())

	/**
	 * Loads a backup via the [Uri] provided by Androids file selection
	 */
	@Throws(NullContentResolverException::class)
	private fun loadBackupFromUri(uri: Uri): BackupEntity {
		val contentResolver =
			applicationContext.contentResolver ?: throw NullContentResolverException()

		val inputStream = contentResolver.openInputStream(uri)
		val bis = BufferedInputStream(inputStream)
		return BackupEntity(bis.readBytes())
	}

	@OptIn(ExperimentalSerializationApi::class)
	@Throws(IOException::class)
	override suspend fun doWork(): Result {
		logI("Starting restore")
		val backupName = inputData.getString(BACKUP_DATA_KEY)
		val isExternal = inputData.getBoolean(BACKUP_DIR_KEY, false)

		if (!isExternal && backupName == null) {
			logE("null backupName, Internal Restore requires backupName")
			return Result.failure()
		}

		notify(R.string.restore_notification_content_starting)
		val backupEntity = try {
			if (isExternal) {
				backupUriRepo.take()?.let { loadBackupFromUri(it) }
			} else {
				backupRepo.loadBackup(backupName!!)
			}
		} catch (e: Exception) {//TODO specify
			with(e) {
				logE(" $message", e)
				notify("$message $e") {
					setNotOngoing()
					addReportErrorAction(
						applicationContext,
						defaultNotificationID,
						e
					)
				}
				return Result.failure()
			}
		}
		if (backupEntity == null) {
			logE("Received empty, impossible")
			notify(R.string.restore_notification_content_unexpected_empty) {
				setNotOngoing()
			}
			return Result.failure()
		}


		// Decode encrypted string to bytes via Base64
		notify(R.string.restore_notification_content_decoding_string)
		val decodedBytes: ByteArray = Base64.decode(backupEntity.content, Base64.DEFAULT)

		// Unzip bytes to a string via gzip
		notify(R.string.restore_notification_content_unzipping_bytes)


		unGZip(decodedBytes).use { stream ->
			val metaInfo = backupJSON.decodeFromStream<MetaBackupEntity>(stream)

			// Reads the version line from the json, if it does not exist the process fails

			val metaVersion: String =
				metaInfo.version ?: return Result.failure()
					.also {
						logE(MESSAGE_LOG_JSON_MISSING)
						notify(R.string.restore_notification_content_missing_key) { setNotOngoing() }
					}


			// Checks if the version is compatible
			logV("Version in backup: $metaVersion")

			if (Version(VERSION_BACKUP).major != Version(metaVersion).major) {
				logE(MESSAGE_LOG_JSON_OUTDATED)
				notify(R.string.restore_notification_content_text_outdated) { setNotOngoing() }
				return Result.failure()
			}
		}

		unGZip(decodedBytes).use { stream ->
			val backup = backupJSON.decodeFromStream<FleshedBackupEntity>(stream)

			notify("Adding categories")
			val currentCategories = categoriesRepo.getCategories()
			val categoryOrderToCategoryIds =
				backup.categories.sortedBy { it.order }.associate { backupCategory ->
					val currentCategory = currentCategories.find { backupCategory.name == it.name }
					backupCategory.order to if (currentCategory != null) {
						currentCategory.id!!
					} else {
						addCategoryUseCase(backupCategory.name)
					}
				}

			notify("Adding repositories")
			// Adds the repositories
			backup.repos.forEach { (url, name) ->
				notify("") {
					setContentTitle(getString(R.string.restore_notification_title_adding_repos))
					setContentText("$name\n$url")
				}
				try {
					extensionsRepoRepo.addRepository(
						url,
						name,
					)
				} catch (e: SQLiteException) {
					logE("Failed to add repo", e)
					// its likely constraint, we can ignore it
				}
			}

			notify("Loading repository data")
			// Load the data from the repositories
			initializeExtensionsUseCase()

			// Install the extensions
			val extensions = extensionsRepo.loadRepositoryExtensions()

			backup.extensions.forEach {
				restoreExtension(
					extensions,
					it,
					categoryOrderToCategoryIds
				)
			}
		}


		System.gc() // Politely ask for a garbage collection
		delay(5000) // Wait for gc to occur (maybe), also helps with the next notification

		notify(R.string.restore_notification_content_completed)
		{
			setNotOngoing()
		}
		logI("Completed restore")
		return Result.success()
	}

	private suspend fun restoreExtension(
		extensions: List<GenericExtensionEntity>,
		backupExtensionEntity: BackupExtensionEntity,
		categoryOrderToCategoryIds: Map<Int, Int>
	) {
		val extensionID = backupExtensionEntity.id
		val backupNovels = backupExtensionEntity.novels
		logI("$extensionID")
		extensions.find { it.id == extensionID }?.let { extensionEntity ->
			// Install the extension
			if (!extensionsRepo.isExtensionInstalled(extensionEntity)) {
				logI("Installing extension $extensionID via repo ${extensionEntity.repoID}")
				notify(getString(R.string.installing) + " ${extensionEntity.id} | ${extensionEntity.name}")
				try {
					installExtension(extensionEntity)
				} catch (e: InvalidMetaDataException) {
					notify(
						getString(R.string.worker_extension_install_error_lua) + " ${extensionEntity.id} | ${extensionEntity.name}",
						notificationId = extensionID
					)
					return
				} catch (e: Exception) {
					notify(
						getString(R.string.worker_extension_install_error_lua) + " ${extensionEntity.id} | ${extensionEntity.name}",
						notificationId = extensionID
					)
					ACRA.errorReporter.handleSilentException(e)
					return
				}
			} else {
				logI("Extension is installed, moving on")
			}

			logI("Restoring extension novels")
			backupNovels.forEach novelLoop@{ novelEntity ->
				try {
					restoreNovel(
						extensionID,
						novelEntity,
						categoryOrderToCategoryIds
					)
				} catch (e: Exception) {
					e.printStackTrace()
				}
			}
		}
	}

	@Throws(Exception::class)
	private suspend fun restoreNovel(
		extensionID: Int,
		backupNovelEntity: BackupNovelEntity,
		categoryOrderToCategoryIds: Map<Int, Int>
	) {
		logV("$extensionID, ${backupNovelEntity.url}")
		val bNovelURL = backupNovelEntity.url
		val name = backupNovelEntity.name
		val bChapters = backupNovelEntity.chapters
		val bSettings = backupNovelEntity.settings

		logI(name)

		var targetNovelID = novelsRepo.loadNovelId(bNovelURL, extensionID) ?: -1
		if (targetNovelID == -1) {
			notify(R.string.restore_notification_content_novel_load) {
				setContentTitle(name)
			}

			val siteNovel = NovelEntity(
				id = null,
				url = backupNovelEntity.url,
				extensionID = extensionID,
				bookmarked = backupNovelEntity.bookmarked,
				loaded = backupNovelEntity.loaded,
				title = backupNovelEntity.name,
				imageURL = backupNovelEntity.imageURL,
				description = backupNovelEntity.description,
				language = backupNovelEntity.language,
				genres = backupNovelEntity.genres,
				authors = backupNovelEntity.authors,
				artists = backupNovelEntity.artists,
				tags = backupNovelEntity.tags,
				status = backupNovelEntity.status
			)

			notify(R.string.restore_notification_content_novel_save) {
				setContentTitle(name)
			}
			novelsRepo.insertReturnStripped(
				siteNovel
			)?.let { (id) ->
				targetNovelID = id
			}

			notify(R.string.restore_notification_content_novel_chapters_save) {
				setContentTitle(name)
			}
			try {
				chaptersRepo.handleChapters(
					novelID = targetNovelID,
					extensionID = extensionID,
					list = backupNovelEntity.chapters.mapIndexed { index, chapter ->
						Novel.Chapter(
							title = chapter.name,
							link = chapter.url,
							release = chapter.releaseDate.orEmpty(),
							order = chapter.order?.takeUnless { it.isNaN() } ?: index.toDouble()
						)
					}
				)
			} catch (e: Exception) {//TODO Specify
				logE("Failed to handle chapters", e)
				ACRA.errorReporter.handleSilentException(e)
			}
			logI("Inserted new chapters")
		}

		// if the ID is still -1, return
		if (targetNovelID == -1) {
			logE("Could not find novel, even after injecting, aborting")
			return
		}

		notify(R.string.restore_notification_content_chapters_load)
		{
			setContentTitle(name)
		}

		// get the chapters
		val repoChapters = chaptersRepo.getChapters(targetNovelID)


		val chapterMap = buildMap {
			bChapters.forEach { backupChapter ->
				repoChapters.find { it.url == backupChapter.url }?.let { repoChapter ->
					this[backupChapter] = repoChapter
				}
			}
		}

		chaptersRepo.restoreBackup(chapterMap)
		chapterHistoryRepo.restoreBackup(chapterMap)

		notify(R.string.restore_notification_content_settings_restore)
		{
			setContentTitle(name)
			removeProgress()
		}

		val settingEntity = try {
			novelsSettingsRepo.get(targetNovelID)
		} catch (e: Exception) {// TODO specify
			logE("Failed to load novel settings")
			ACRA.errorReporter.handleSilentException(e)
			return
		}

		if (settingEntity == null) {
			logI("Inserting novel settings")
			novelsSettingsRepo.insert(
				NovelSettingEntity(
					targetNovelID,
					sortType = bSettings.sortType,
					showOnlyReadingStatusOf = bSettings.showOnlyReadingStatusOf,
					showOnlyBookmarked = bSettings.showOnlyBookmarked,
					showOnlyDownloaded = bSettings.showOnlyDownloaded,
					reverseOrder = bSettings.reverseOrder,
				)
			)
		} else {
			novelsSettingsRepo.update(
				settingEntity.copy(
					sortType = bSettings.sortType,
					showOnlyReadingStatusOf = bSettings.showOnlyReadingStatusOf,
					showOnlyBookmarked = bSettings.showOnlyBookmarked,
					showOnlyDownloaded = bSettings.showOnlyDownloaded,
					reverseOrder = bSettings.reverseOrder,
				)
			)
		}

		notify(R.string.restore_notification_content_categories_restore) {
			setContentTitle(name)
		}
		val novelCategories = try {
			novelCategoriesRepo.getNovelCategoriesFromNovel(targetNovelID)
		} catch (e: Exception) {// TODO specify
			logE("Failed to load novel categories")
			ACRA.errorReporter.handleSilentException(e)
			return
		}

		if (novelCategories.isEmpty()) {
			logI("Inserting novel categories")
			novelCategoriesRepo.setNovelCategories(
				backupNovelEntity.categories.map {
					NovelCategoryEntity(
						targetNovelID,
						categoryOrderToCategoryIds[it]!!
					)
				}
			)
		} else {
			val existingNovelCategories = novelCategories.map { it.categoryID }
			novelCategoriesRepo.setNovelCategories(
				backupNovelEntity.categories.mapNotNull {
					val category = categoryOrderToCategoryIds[it]
					if (category == null || it in existingNovelCategories)
						return@mapNotNull null

					NovelCategoryEntity(
						targetNovelID,
						categoryOrderToCategoryIds[it]!!
					)
				}
			)
		}

		if (backupNovelEntity.pinned) {
			try {
				novelPinsRepo.updateOrInsert(NovelPinEntity(targetNovelID, true))
			} catch (ignored: SQLiteException) {
				// TODO how to handle this issue?
			}
		}

		delay(500) // Delay things a bit
	}

	/**
	 * Manager of [BackupWorker]
	 */
	class Manager(context: Context) : CoroutineWorkerManager(context) {

		override suspend fun getCount(): Int =
			getWorkerInfoList().size

		/**
		 * Returns the status of the service.
		 *
		 * @return true if the service is running, false otherwise.
		 */
		override suspend fun isRunning(): Boolean = try {
			// Is this running
			val a = (getWorkerState() == WorkInfo.State.RUNNING)

			// Don't run if update is being installed
			val b = !AppUpdateInstallWorker.Manager(context).isRunning()
			a && b
		} catch (e: Exception) {
			false
		}

		override suspend fun getWorkerState(index: Int): WorkInfo.State =
			getWorkerInfoList()[index].state

		override suspend fun getWorkerInfoList(): List<WorkInfo> =
			workerManager.getWorkInfosForUniqueWork(RESTORE_WORK_ID).await()

		/**
		 * Starts the service. It will be started only if there isn't another instance already
		 * running.
		 */
		override fun start(data: Data) {
			launchIO {
				logI(LogConstants.SERVICE_NEW)
				workerManager.enqueueUniqueWork(
					RESTORE_WORK_ID,
					ExistingWorkPolicy.REPLACE,
					OneTimeWorkRequestBuilder<RestoreBackupWorker>(
					).setInputData(data).build()
				)
				logI(
					"Worker State ${
						workerManager.getWorkInfosForUniqueWork(RESTORE_WORK_ID)
							.await()[0].state
					}"
				)
			}
		}

		/**
		 * Stops the service.
		 */
		override fun stop(): Operation =
			workerManager.cancelUniqueWork(RESTORE_WORK_ID)
	}

	companion object {
		private const val MESSAGE_LOG_JSON_MISSING = "BACKUP JSON DOES NOT CONTAIN KEY 'version'"


		private const val MESSAGE_LOG_JSON_OUTDATED = "BACKUP JSON MISMATCH"


		/**
		 * Path / name of file
		 */
		const val BACKUP_DATA_KEY = "BACKUP_NAME"

		/**
		 * If true, the [BACKUP_DATA_KEY] is a full path pointing to a specific file, other wise
		 * it is an internal path
		 */
		const val BACKUP_DIR_KEY = "BACKUP_DIR"
	}
}