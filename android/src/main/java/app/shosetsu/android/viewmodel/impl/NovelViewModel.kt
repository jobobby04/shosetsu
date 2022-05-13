package app.shosetsu.android.viewmodel.impl

import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import app.shosetsu.android.common.enums.ChapterSortType
import app.shosetsu.android.common.enums.ReadingStatus
import app.shosetsu.android.common.ext.*
import app.shosetsu.android.common.utils.share.toURL
import app.shosetsu.android.domain.usecases.DownloadChapterPassageUseCase
import app.shosetsu.android.domain.usecases.IsOnlineUseCase
import app.shosetsu.android.domain.usecases.StartDownloadWorkerAfterUpdateUseCase
import app.shosetsu.android.domain.usecases.delete.DeleteChapterPassageUseCase
import app.shosetsu.android.domain.usecases.delete.TrueDeleteChapterUseCase
import app.shosetsu.android.domain.usecases.get.*
import app.shosetsu.android.domain.usecases.load.LoadDeletePreviousChapterUseCase
import app.shosetsu.android.domain.usecases.settings.LoadChaptersResumeFirstUnreadUseCase
import app.shosetsu.android.domain.usecases.start.StartDownloadWorkerUseCase
import app.shosetsu.android.domain.usecases.update.UpdateChapterUseCase
import app.shosetsu.android.domain.usecases.update.UpdateNovelSettingUseCase
import app.shosetsu.android.domain.usecases.update.UpdateNovelUseCase
import app.shosetsu.android.view.AndroidQRCodeDrawable
import app.shosetsu.android.view.uimodels.model.ChapterUI
import app.shosetsu.android.view.uimodels.model.NovelUI
import app.shosetsu.android.viewmodel.abstracted.ANovelViewModel
import app.shosetsu.common.enums.ChapterSortType
import app.shosetsu.common.enums.ChapterSortType.SOURCE
import app.shosetsu.common.enums.ReadingStatus
import app.shosetsu.common.utils.copy
import app.shosetsu.android.view.uimodels.NovelSettingUI
import app.shosetsu.lib.share.ExtensionLink
import app.shosetsu.lib.share.NovelLink
import app.shosetsu.lib.share.RepositoryLink
import io.github.g0dkar.qrcode.QRCode
import io.github.g0dkar.qrcode.render.QRCodeCanvasFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking

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
 * shosetsu
 * 24 / 04 / 2020
 *
 * @author github.com/doomsdayrs
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NovelViewModel(
	private val getChapterUIsUseCase: GetChapterUIsUseCase,
	private val loadNovelUIUseCase: GetNovelUIUseCase,
	private val updateNovelUseCase: UpdateNovelUseCase,
	private val getContentURL: GetURLUseCase,
	private val loadRemoteNovel: GetRemoteNovelUseCase,
	private var isOnlineUseCase: IsOnlineUseCase,
	private val updateChapterUseCase: UpdateChapterUseCase,
	private val downloadChapterPassageUseCase: DownloadChapterPassageUseCase,
	private val deleteChapterPassageUseCase: DeleteChapterPassageUseCase,
	private val isChaptersResumeFirstUnread: LoadChaptersResumeFirstUnreadUseCase,
	private val getNovelSettingFlowUseCase: GetNovelSettingFlowUseCase,
	private val updateNovelSettingUseCase: UpdateNovelSettingUseCase,
	private val loadDeletePreviousChapterUseCase: LoadDeletePreviousChapterUseCase,
	private val startDownloadWorkerUseCase: StartDownloadWorkerUseCase,
	private val startDownloadWorkerAfterUpdateUseCase: StartDownloadWorkerAfterUpdateUseCase,
	private val getLastReadChapter: GetLastReadChapterUseCase,
	private val getTrueDelete: GetTrueDeleteChapterUseCase,
	private val trueDeleteChapter: TrueDeleteChapterUseCase,
	private val getInstalledExtensionUseCase: GetInstalledExtensionUseCase,
	private val getRepositoryUseCase: GetRepositoryUseCase
) : ANovelViewModel() {

	override val chaptersException: MutableStateFlow<Throwable?> = MutableStateFlow(null)

	override val novelException: MutableStateFlow<Throwable?> = MutableStateFlow(null)

	override val otherException: MutableStateFlow<Throwable?> = MutableStateFlow(null)

	override val chaptersLive: Flow<List<ChapterUI>> by lazy {
		chaptersFlow.onIO()
	}

	private val selectedChapters = MutableStateFlow<Map<Int, Boolean>>(mapOf())

	private suspend fun copySelected(): HashMap<Int, Boolean> =
		selectedChapters.first().copy()

	override fun clearSelection() {
		launchIO {
			clearSelectedSuspend()
		}
	}

	private suspend fun clearSelectedSuspend() {
		selectedChapters.emit(mapOf())
	}

	private val chaptersFlow: Flow<List<ChapterUI>> by lazy {
		novelIDLive.transformLatest { id: Int ->
			emitAll(
				getChapterUIsUseCase(id)
					.combineBookmarked()
					.combineDownloaded()
					.combineStatus()
					.combineSort()
					.combineReverse()
					.combineSelection()
			)
		}.catch {
			chaptersException.emit(it)
		}
	}

	override val novelSettingFlow: Flow<NovelSettingUI?> by lazy {
		novelSettingsFlow.onIO()
	}

	override fun getIfAllowTrueDelete(): Flow<Boolean> =
		flow {
			emit(getTrueDelete())
		}.onIO()

	override fun getQRCode(): Flow<ImageBitmap?> =
		flow {
			emitAll(novelFlow.transformLatest { novel ->
				if (novel != null) {
					emitAll(getNovelURL().transformLatest { novelURL ->
						if (novelURL != null) {
							emitAll(getInstalledExtensionUseCase(novel.extID).transformLatest { ext ->
								if (ext != null) {
									val repo = getRepositoryUseCase(ext.repoID)
									if (repo != null) {
										val url = NovelLink(
											novel.title,
											novel.imageURL,
											novelURL,
											ExtensionLink(
												novel.extID,
												ext.name,
												ext.imageURL,
												RepositoryLink(
													repo.name,
													repo.url
												)
											)
										).toURL()
										val code = QRCode(url)
										val encoding = code.encode()

										QRCodeCanvasFactory.AVAILABLE_IMPLEMENTATIONS["android.graphics.Bitmap"] =
											{ width, height ->
												AndroidQRCodeDrawable(
													width,
													height
												)
											}

										val size = code.computeImageSize(
											QRCode.DEFAULT_CELL_SIZE,
											QRCode.DEFAULT_MARGIN,
										)
										val bytes = code.render(
											qrCodeCanvas = AndroidQRCodeDrawable(size, size),
											rawData = encoding,
											brightColor = Color.WHITE,
											darkColor = Color.BLACK,
											marginColor = Color.WHITE
										).toByteArray()

										emit(
											BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
												.asImageBitmap()
										)
									} else emit(null)
								} else emit(null)
							})
						} else emit(null)
					})
				} else emit(null)
			})
		}.onIO()

	private val novelFlow: Flow<NovelUI?> by lazy {
		novelIDLive.transformLatest {
			emit(null)
			emitAll(loadNovelUIUseCase(it))
		}.catch {
			novelException.emit(it)
		}
	}

	override val novelLive: Flow<NovelUI?> by lazy {
		novelFlow.onIO()
	}

	private val _isRefreshing by lazy {
		MutableStateFlow(false)
	}

	override val isRefreshing: Flow<Boolean> = _isRefreshing.onIO()

	private val novelSettingsFlow: Flow<NovelSettingUI?> by lazy {
		novelIDLive.transformLatest { emitAll(getNovelSettingFlowUseCase(it)) }
	}

	private val novelIDLive: MutableStateFlow<Int> by lazy { MutableStateFlow(-1) }


	private val _showOnlyStatusOfFlow: Flow<ReadingStatus?> =
		novelSettingsFlow.mapLatest { it?.showOnlyReadingStatusOf }

	private val _onlyDownloadedFlow: Flow<Boolean> =
		novelSettingsFlow.mapLatest { it?.showOnlyDownloaded ?: false }

	private val _onlyBookmarkedFlow: Flow<Boolean> =
		novelSettingsFlow.mapLatest { it?.showOnlyBookmarked ?: false }

	private val _sortTypeFlow: Flow<ChapterSortType> =
		novelSettingsFlow.mapLatest { it?.sortType ?: ChapterSortType.SOURCE }

	private val _reversedSortFlow: Flow<Boolean> =
		novelSettingsFlow.mapLatest { it?.reverseOrder ?: false }

	private fun Flow<List<ChapterUI>>.combineBookmarked(): Flow<List<ChapterUI>> =
		combine(_onlyBookmarkedFlow) { result, onlyBookmarked ->
			if (onlyBookmarked)
				result.filter { ui -> ui.bookmarked }
			else result
		}

	private fun Flow<List<ChapterUI>>.combineDownloaded(): Flow<List<ChapterUI>> =
		combine(_onlyDownloadedFlow) { result, onlyDownloaded ->
			if (onlyDownloaded)
				result.filter { it.isSaved }
			else result
		}

	@ExperimentalCoroutinesApi
	private fun Flow<List<ChapterUI>>.combineStatus(): Flow<List<ChapterUI>> =
		combine(_showOnlyStatusOfFlow) { result, readingStatusOf ->
			readingStatusOf?.let { status ->
				if (status != ReadingStatus.UNREAD)
					result.filter { it.readingStatus == status }
				else result.filter {
					it.readingStatus == status || it.readingStatus == ReadingStatus.READING
				}

			} ?: result
		}

	@ExperimentalCoroutinesApi
	private fun Flow<List<ChapterUI>>.combineSort(): Flow<List<ChapterUI>> =
		combine(_sortTypeFlow) { chapters, sortType ->
			when (sortType) {
				ChapterSortType.SOURCE -> {
					chapters.sortedBy { it.order }
				}
				ChapterSortType.UPLOAD -> {
					chapters.sortedBy { it.releaseDate }
				}
			}
		}

	@ExperimentalCoroutinesApi
	private fun Flow<List<ChapterUI>>.combineReverse(): Flow<List<ChapterUI>> =
		combine(_reversedSortFlow) { result, reverse ->
			if (reverse)
				result.reversed()
			else result
		}

	private fun Flow<List<ChapterUI>>.combineSelection(): Flow<List<ChapterUI>> =
		combine(selectedChapters) { chapters, selection ->
			chapters.map {
				it.copy(
					isSelected = selection.getOrElse(it.id) { false }
				)
			}
		}

	override fun deletePrevious() {
		logI("Deleting previous chapters")
		launchIO {
			loadDeletePreviousChapterUseCase().let { chaptersBackToDelete ->
				if (chaptersBackToDelete != -1) {
					val lastUnread =
						getLastReadChapter(novelFlow.first { it != null }!!.id)

					if (lastUnread == null) {
						logE("Received empty when trying to get lastUnreadResult")
						return@launchIO
					}

					val chapters = chaptersFlow.first().sortedBy { it.order }

					val indexOfLast = chapters.indexOfFirst { it.id == lastUnread.chapterId }

					if (indexOfLast == -1) {
						logE("Index of last read chapter turned up negative")
						return@launchIO
					}

					if (indexOfLast - chaptersBackToDelete < 0) {
						return@launchIO
					}

					deleteChapterPassageUseCase(chapters[indexOfLast - chaptersBackToDelete])
				}
			}

		}
	}

	override fun destroy() {
		novelIDLive.tryEmit(-1) // Reset view to nothing
		itemIndex.tryEmit(0)
		_isRefreshing.tryEmit(false)

		novelException.tryEmit(null)
		chaptersException.tryEmit(null)
		otherException.tryEmit(null)
		runBlocking {
			clearSelectedSuspend()
		}
	}

	private fun downloadChapter(vararg chapterUI: ChapterUI, startManager: Boolean = false) {
		launchIO {
			downloadChapterPassageUseCase(*chapterUI)

			if (startManager)
				startDownloadWorkerUseCase()

		}
	}

	override fun isOnline(): Boolean = isOnlineUseCase()

	override fun openLastRead(): Flow<ChapterUI?> =
		flow {
			val array = chaptersFlow.first()

			val sortedArray = array.sortedBy { it.order }
			val result = isChaptersResumeFirstUnread()

			val index: Int = if (!result)
				sortedArray.indexOfFirst { it.readingStatus != ReadingStatus.READ }
			else sortedArray.indexOfFirst { it.readingStatus == ReadingStatus.UNREAD }


			emit(
				if (index == -1) {
					null
				} else {
					itemIndex.emit(index + 1) // +1 to account for header
					sortedArray[index]
				}
			)
		}.onIO()

	override fun getNovelURL(): Flow<String?> =
		flow {
			emit(getContentURL(novelFlow.first { it != null }!!))
		}.onIO()

	override fun getShareInfo(): Flow<NovelShareInfo?> =
		flow {
			emit(novelFlow.first { it != null }!!.let {
				getContentURL(it)?.let { url ->
					NovelShareInfo(it.title, url)
				}
			})
		}.onIO()

	override fun getChapterURL(chapterUI: ChapterUI): Flow<String?> =
		flow {
			emit(getContentURL(chapterUI))
		}.onIO()

	override fun refresh(): Flow<Unit> =
		flow {
			_isRefreshing.emit(true)
			try {
				loadRemoteNovel(novelIDLive.value, true)?.let {
					startDownloadWorkerAfterUpdateUseCase(it.updatedChapters)
				}
			} finally {
				emit(Unit)
				_isRefreshing.emit(false)
			}
		}.onIO()

	override fun setNovelID(novelID: Int) {
		when {
			novelIDLive.value == -1 -> logI("Setting NovelID")
			novelIDLive.value != novelID -> logI("NovelID not equal, resetting")
			novelIDLive.value == novelID -> {
				logI("NovelID equal, ignoring")
				return
			}
		}
		novelIDLive.tryEmit(novelID)
	}

	override fun toggleNovelBookmark() {
		launchIO {
			val novel = novelFlow.first { it != null }!!
			updateNovelUseCase(novel.copy(bookmarked = !novel.bookmarked))
		}
	}

	override fun isBookmarked(): Flow<Boolean> = flow {
		emit(novelFlow.first()?.bookmarked ?: false)
	}.onIO()

	private fun markChapterAsRead(chapterUI: ChapterUI) {
		launchIO {
			updateChapterUseCase(
				chapterUI.copy(
					readingStatus = ReadingStatus.READ
				)
			)
		}
	}

	private fun markChapterAsReading(chapterUI: ChapterUI) {
		launchIO {
			updateChapterUseCase(
				chapterUI.copy(
					readingStatus = ReadingStatus.READING
				)
			)
		}
	}

	private fun markChapterAsUnread(chapterUI: ChapterUI) {
		launchIO {
			updateChapterUseCase(
				chapterUI.copy(
					readingStatus = ReadingStatus.UNREAD
				)
			)
		}
	}

	override fun downloadNextChapter() {
		launchIO {
			val array = chaptersFlow.first().sortedBy { it.order }
			val r = array.indexOfFirst { it.readingStatus != ReadingStatus.READ }
			if (r != -1) downloadChapter(array[r])
			startDownloadWorkerUseCase()
		}
	}

	override fun downloadNextCustomChapters(max: Int) {
		launchIO {
			val array = chaptersFlow.first().sortedBy { it.order }
			val r = array.indexOfFirst { it.readingStatus != ReadingStatus.READ }
			if (r != -1) {
				val list = arrayListOf<ChapterUI>()
				list.add(array[r])
				var count = 1
				while ((r + count) < array.size && count <= max) {
					list.add(array[r + count])
					count++
				}
				downloadChapter(*list.toTypedArray())
			}
			startDownloadWorkerUseCase()
		}
	}

	override fun downloadNext5Chapters() = downloadNextCustomChapters(5)

	override fun downloadNext10Chapters() = downloadNextCustomChapters(10)

	override fun downloadAllUnreadChapters() {
		launchIO {
			downloadChapter(
				*chaptersFlow.first().filter { it.readingStatus == ReadingStatus.UNREAD }
					.toTypedArray())
			startDownloadWorkerUseCase()
		}
	}

	override fun downloadAllChapters() {
		launchIO {
			downloadChapter(*chaptersFlow.first().toTypedArray())
			startDownloadWorkerUseCase()
		}
	}

	override fun updateNovelSetting(novelSettingUI: NovelSettingUI) {
		logD("Launching update")
		launchIO {
			updateNovelSettingUseCase(novelSettingUI)
		}
	}

	override var isFromChapterReader: Boolean = false
		get() = if (field) {
			val value = field
			field = !value
			value
		} else field

	override val itemIndex: MutableStateFlow<Int> = MutableStateFlow(0)
	override fun setItemAt(index: Int) {
		itemIndex.tryEmit(index)
	}

	override val hasSelected: Flow<Boolean> by lazy {
		chaptersFlow.mapLatest { chapters -> chapters.any { it.isSelected } }
	}

	override fun bookmarkSelected() {
		launchIO {
			val list = chaptersFlow.first()
			list.filter { it.isSelected }.forEach {
				updateChapterUseCase(it.copy(bookmarked = true))
			}
			clearSelectedSuspend()
		}
	}

	override fun deleteSelected() {
		launchIO {
			val list = chaptersFlow.first()
			list.filter { it.isSelected && it.isSaved }.forEach {
				deleteChapterPassageUseCase(it)
			}
			clearSelectedSuspend()
		}
	}

	override fun downloadSelected() {
		launchIO {
			val list = chaptersFlow.first()
			list.filter { it.isSelected && !it.isSaved }.forEach {
				downloadChapterPassageUseCase(it)
			}
			clearSelectedSuspend()
		}
	}

	override fun invertSelection() {
		launchIO {
			val list = chaptersFlow.first()
			val selection = copySelected()

			list.forEach {
				selection[it.id] = !it.isSelected
			}

			selectedChapters.emit(selection)
		}
	}

	override fun markSelectedAs(readingStatus: ReadingStatus) {
		launchIO {
			val list =
				chaptersFlow.first().filter { it.isSelected && it.readingStatus != readingStatus }
			when (readingStatus) {
				ReadingStatus.UNREAD -> list.forEach {
					markChapterAsUnread(it)
				}
				ReadingStatus.READING -> list.forEach {
					markChapterAsReading(it)
				}
				ReadingStatus.READ -> list.forEach {
					markChapterAsRead(it)
				}
				else -> {
					logE("Invalid input")
				}
			}
			clearSelectedSuspend()
		}
	}

	override fun removeBookmarkFromSelected() {
		launchIO {
			val list = chaptersFlow.first()
			list.filter { it.isSelected }.forEach {
				updateChapterUseCase(it.copy(bookmarked = false))
			}
			selectAll()
			clearSelectedSuspend()
		}
	}

	override fun selectAll() {
		launchIO {
			val list = chaptersFlow.first()
			val selection = copySelected()

			list.forEach {
				selection[it.id] = true
			}

			selectedChapters.emit(selection)
		}
	}

	override fun selectBetween() {
		launchIO {
			val list = chaptersFlow.first()
			val selection = copySelected()

			val firstSelected = list.indexOfFirst { it.isSelected }
			val lastSelected = list.indexOfLast { it.isSelected }

			if (listOf(firstSelected, lastSelected).any { it == -1 }) {
				logE("Received -1 index")
				return@launchIO
			}

			if (firstSelected == lastSelected) {
				logE("Ignoring select between, requires more then 1 selected item")
				return@launchIO
			}

			if (firstSelected + 1 == lastSelected) {
				logE("Ignoring select between, requires gap between items")
				return@launchIO
			}

			list.subList(firstSelected + 1, lastSelected).forEach {
				selection[it.id] = true
			}

			selectedChapters.emit(selection)
		}
	}

	override fun trueDeleteSelected() {
		launchIO {
			val list = chaptersFlow.first()
			list.filter { it.isSelected && it.isSaved }.forEach {
				trueDeleteChapter(it)
			}
			clearSelectedSuspend()
		}
	}

	override fun scrollTo(predicate: (ChapterUI) -> Boolean): Flow<Boolean> =
		flow {
			val chapters = chaptersFlow.first()
			chapters.indexOfFirst(predicate).takeIf { it != -1 }?.let {
				itemIndex.emit(it)
				emit(true)
			} ?: emit(false)
		}

	override fun toggleSelection(it: ChapterUI) {
		logV("$it")
		launchIO {
			val selection = copySelected()

			selection[it.id] = !it.isSelected

			selectedChapters.emit(selection)
		}
	}

	override fun getChapterCount(): Flow<Int> =
		flow {
			emit(chaptersFlow.first().size)
		}

}