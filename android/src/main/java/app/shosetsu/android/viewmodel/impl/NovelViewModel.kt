package app.shosetsu.android.viewmodel.impl

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.viewModelScope
import app.shosetsu.android.R
import app.shosetsu.android.common.ChapterLoadException
import app.shosetsu.android.common.NovelLoadException
import app.shosetsu.android.common.OfflineException
import app.shosetsu.android.common.RefreshException
import app.shosetsu.android.common.SettingKey
import app.shosetsu.android.common.enums.ChapterSortType
import app.shosetsu.android.common.enums.ReadingStatus
import app.shosetsu.android.common.ext.launchIO
import app.shosetsu.android.common.ext.logD
import app.shosetsu.android.common.ext.logE
import app.shosetsu.android.common.ext.logI
import app.shosetsu.android.common.ext.logV
import app.shosetsu.android.common.utils.copy
import app.shosetsu.android.common.utils.share.toURL
import app.shosetsu.android.domain.repository.base.IChaptersRepository
import app.shosetsu.android.domain.repository.base.ISettingsRepository
import app.shosetsu.android.domain.usecases.DownloadChapterPassageUseCase
import app.shosetsu.android.domain.usecases.IsOnlineUseCase
import app.shosetsu.android.domain.usecases.SetNovelCategoriesUseCase
import app.shosetsu.android.domain.usecases.StartDownloadWorkerAfterUpdateUseCase
import app.shosetsu.android.domain.usecases.delete.DeleteChapterPassageUseCase
import app.shosetsu.android.domain.usecases.delete.TrueDeleteChapterUseCase
import app.shosetsu.android.domain.usecases.get.GetCategoriesUseCase
import app.shosetsu.android.domain.usecases.get.GetChapterUIsUseCase
import app.shosetsu.android.domain.usecases.get.GetInstalledExtensionUseCase
import app.shosetsu.android.domain.usecases.get.GetNovelCategoriesUseCase
import app.shosetsu.android.domain.usecases.get.GetNovelSettingFlowUseCase
import app.shosetsu.android.domain.usecases.get.GetNovelUIUseCase
import app.shosetsu.android.domain.usecases.get.GetRemoteNovelUseCase
import app.shosetsu.android.domain.usecases.get.GetRepositoryUseCase
import app.shosetsu.android.domain.usecases.get.GetURLUseCase
import app.shosetsu.android.domain.usecases.settings.LoadChaptersResumeFirstUnreadUseCase
import app.shosetsu.android.domain.usecases.start.StartDownloadWorkerUseCase
import app.shosetsu.android.domain.usecases.update.UpdateNovelSettingUseCase
import app.shosetsu.android.domain.usecases.update.UpdateNovelUseCase
import app.shosetsu.android.view.uimodels.NovelSettingUI
import app.shosetsu.android.view.uimodels.model.CategoryUI
import app.shosetsu.android.view.uimodels.model.ChapterUI
import app.shosetsu.android.view.uimodels.model.NovelUI
import app.shosetsu.android.view.uimodels.model.QRCodeData
import app.shosetsu.android.viewmodel.abstracted.ANovelViewModel
import app.shosetsu.lib.share.ExtensionLink
import app.shosetsu.lib.share.NovelLink
import app.shosetsu.lib.share.RepositoryLink
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import qrcode.QRCode
import kotlin.collections.set

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
	private val chapterRepo: IChaptersRepository,
	private val downloadChapterPassageUseCase: DownloadChapterPassageUseCase,
	private val deleteChapterPassageUseCase: DeleteChapterPassageUseCase,
	private val isChaptersResumeFirstUnread: LoadChaptersResumeFirstUnreadUseCase,
	private val getNovelSettingFlowUseCase: GetNovelSettingFlowUseCase,
	private val updateNovelSettingUseCase: UpdateNovelSettingUseCase,
	private val startDownloadWorkerUseCase: StartDownloadWorkerUseCase,
	private val startDownloadWorkerAfterUpdateUseCase: StartDownloadWorkerAfterUpdateUseCase,
	private val settingsRepo: ISettingsRepository,
	private val trueDeleteChapter: TrueDeleteChapterUseCase,
	private val getInstalledExtensionUseCase: GetInstalledExtensionUseCase,
	private val getRepositoryUseCase: GetRepositoryUseCase,
	private val getCategoriesUseCase: GetCategoriesUseCase,
	private val getNovelCategoriesUseCase: GetNovelCategoriesUseCase,
	private val setNovelCategoriesUseCase: SetNovelCategoriesUseCase
) : ANovelViewModel() {

	override val error = MutableSharedFlow<Throwable>()

	override val isRefreshing = MutableStateFlow(false)

	private val novelIDLive = MutableStateFlow(-1)

	override val chaptersLive: StateFlow<ImmutableList<ChapterUI>> by lazy {
		novelIDLive.flatMapLatest { id: Int ->
			getChapterUIsUseCase(id).shareIn(viewModelScopeIO, SharingStarted.Lazily, 1)
				.combineBookmarked().combineDownloaded().combineStatus().combineSort()
				.combineReverse().combineSelection().map { it.toImmutableList() }
		}.catch {
			error.emit(ChapterLoadException(it))
		}.onIO().stateIn(viewModelScopeIO, SharingStarted.Lazily, persistentListOf())
	}

	override val selectedChaptersState: StateFlow<SelectedChaptersState> by lazy {
		chaptersLive.map { rawChapters ->
			val chapters = rawChapters.filter { it.isSelected }
			SelectedChaptersState(showRemoveBookmark = chapters.any { it.bookmarked },
				showBookmark = chapters.any { !it.bookmarked },
				showDelete = chapters.any { it.isSaved },
				showDownload = chapters.any { !it.isSaved },
				showMarkAsRead = chapters.any { it.readingStatus != ReadingStatus.READ },
				showMarkAsUnread = chapters.any { it.readingStatus != ReadingStatus.UNREAD })
		}.onIO().stateIn(viewModelScopeIO, SharingStarted.Lazily, SelectedChaptersState())
	}

	private val selectedChapters = MutableStateFlow<Map<Int, Boolean>>(emptyMap())

	private fun copySelected(): HashMap<Int, Boolean> = selectedChapters.value.copy()

	private fun getSelectedIds(): List<Int> =
		selectedChapters.value.filter { it.value }.map { it.key }

	override fun clearSelection() {
		launchIO {
			clearSelected()
		}
	}

	private fun clearSelected() {
		selectedChapters.value = emptyMap()
	}

	override val novelSettingFlow: StateFlow<NovelSettingUI?> by lazy {
		novelIDLive.flatMapLatest { getNovelSettingFlowUseCase(it) }.onIO()
			.stateIn(viewModelScopeIO, SharingStarted.Eagerly, null)
	}

	override val categories: StateFlow<ImmutableList<CategoryUI>> by lazy {
		getCategoriesUseCase().map { it.toImmutableList() }
			.stateIn(viewModelScopeIO, SharingStarted.Lazily, persistentListOf())
	}

	override val novelCategories: StateFlow<ImmutableList<Int>> by lazy {
		novelIDLive.transformLatest { id: Int ->
			emitAll(getNovelCategoriesUseCase(id))
		}.map { it.toImmutableList() }
			.stateIn(viewModelScopeIO, SharingStarted.Lazily, persistentListOf())
	}
	override val isCategoriesDialogVisible = MutableStateFlow(false)

	override val toggleBookmarkResponse: MutableStateFlow<ToggleBookmarkResponse> =
		MutableStateFlow(ToggleBookmarkResponse.Nothing)
	override val isChapterJumpDialogVisible = MutableStateFlow<Boolean>(false)

	override val jumpState = MutableStateFlow<JumpState>(JumpState.UNKNOWN)

	override fun showChapterJumpDialog() {
		isChapterJumpDialogVisible.value = true
	}

	override fun hideChapterJumpDialog() {
		isChapterJumpDialogVisible.value = false
	}

	override fun showCategoriesDialog() {
		isCategoriesDialogVisible.value = true
	}

	override fun hideCategoriesDialog() {
		isCategoriesDialogVisible.value = false
	}

	override val showTrueDelete: StateFlow<Boolean> =
		settingsRepo.getBooleanFlow(SettingKey.ExposeTrueChapterDelete)

	override val qrCode: Flow<QRCodeData?> by lazy {
		novelLive.transformLatest { novel ->
			if (novel != null) {
				emitAll(novelURL.transformLatest { novelURL ->
					if (novelURL != null) {
						emitAll(getInstalledExtensionUseCase(novel.extID).transformLatest { ext ->
							if (ext != null) {
								val repo = getRepositoryUseCase(ext.repoID)
								if (repo != null) {
									val url = NovelLink(
										novel.title, novel.imageURL, novelURL, ExtensionLink(
											novel.extID, ext.name, ext.imageURL, RepositoryLink(
												repo.name, repo.url
											)
										)
									).toURL()
									val code = QRCode(url)

									val bytes = code.render().getBytes()

									val bitmap = BitmapFactory
										.decodeByteArray(bytes, 0, bytes.size)
										.asImageBitmap()

									emit(QRCodeData(bitmap, url))
								} else emit(null)
							} else emit(null)
						})
					} else emit(null)
				})
			} else emit(null)
		}.shareIn(viewModelScopeIO, SharingStarted.Lazily, 1).onIO()
	}

	override val novelLive: StateFlow<NovelUI?> by lazy {
		novelIDLive.flatMapLatest {
			loadNovelUIUseCase(it)
		}.catch {
			error.emit(NovelLoadException(it))
		}.onIO().stateIn(viewModelScopeIO, SharingStarted.Lazily, null)
	}


	private val _showOnlyStatusOfFlow: Flow<ReadingStatus?> =
		novelSettingFlow.mapLatest { it?.showOnlyReadingStatusOf }

	private val _onlyDownloadedFlow: Flow<Boolean> =
		novelSettingFlow.mapLatest { it?.showOnlyDownloaded ?: false }

	private val _onlyBookmarkedFlow: Flow<Boolean> =
		novelSettingFlow.mapLatest { it?.showOnlyBookmarked ?: false }

	private val _sortTypeFlow: Flow<ChapterSortType> =
		novelSettingFlow.mapLatest { it?.sortType ?: ChapterSortType.SOURCE }

	private val _reversedSortFlow: Flow<Boolean> =
		novelSettingFlow.mapLatest { it?.reverseOrder ?: false }

	private fun Flow<List<ChapterUI>>.combineBookmarked(): Flow<List<ChapterUI>> =
		combine(_onlyBookmarkedFlow) { result, onlyBookmarked ->
			if (onlyBookmarked) result.filter { ui -> ui.bookmarked }
			else result
		}

	private fun Flow<List<ChapterUI>>.combineDownloaded(): Flow<List<ChapterUI>> =
		combine(_onlyDownloadedFlow) { result, onlyDownloaded ->
			if (onlyDownloaded) result.filter { it.isSaved }
			else result
		}

	@ExperimentalCoroutinesApi
	private fun Flow<List<ChapterUI>>.combineStatus(): Flow<List<ChapterUI>> =
		combine(_showOnlyStatusOfFlow) { result, readingStatusOf ->
			readingStatusOf?.let { status ->
				if (status != ReadingStatus.UNREAD) result.filter { it.readingStatus == status }
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
			if (reverse) result.reversed()
			else result
		}

	private fun Flow<List<ChapterUI>>.combineSelection(): Flow<List<ChapterUI>> =
		combine(selectedChapters) { chapters, selection ->
			chapters.map {
				it.copy(isSelected = selection.getOrElse(it.id) { false })
			}
		}

	private suspend fun downloadChapter(chapters: Array<ChapterUI>, startManager: Boolean = false) {
		if (chapters.isEmpty()) return
		downloadChapterPassageUseCase(chapters)

		if (startManager) startDownloadWorkerUseCase()
	}

	private val isOnline: StateFlow<Boolean> =
		isOnlineUseCase.getFlow().stateIn(viewModelScopeIO, SharingStarted.Eagerly, false)

	override fun openLastRead() {
		viewModelScopeIO.launch {
			val array = chaptersLive.value

			val sortedArray = array.sortedBy { it.order }
			val result = isChaptersResumeFirstUnread()

			val item =
				if (!result) sortedArray.firstOrNull { it.readingStatus != ReadingStatus.READ }
				else sortedArray.firstOrNull { it.readingStatus == ReadingStatus.UNREAD }



			if (item == null) {
				openLastReadResult.emit(LastOpenResult.Complete)
			} else {
				itemIndex.emit(array.indexOf(item) + 1) // +1 to account for header
				openLastReadResult.emit(LastOpenResult.Open(item))
			}
		}
	}

	override val novelURL: StateFlow<String?> = flow {
		emit(novelLive.first { it != null }?.let {
			getContentURL(it)
		})
	}.onIO().stateIn(viewModelScope, SharingStarted.Eagerly, null)

	override val shareInfo: StateFlow<NovelShareInfo?> =
		novelLive.combine(novelURL) { it, url ->
			if (it != null && url != null)
				NovelShareInfo(it.title, url)
			else null
		}.stateIn(viewModelScope, SharingStarted.Eagerly, null)

	override fun getChapterURL(chapterUI: ChapterUI): Flow<String?> = flow {
		emit(getContentURL(chapterUI))
	}.onIO()

	override fun refresh() {
		viewModelScopeIO.launch {
			if (isOnline.value) {
				logI("Refreshing the novel data")
				isRefreshing.emit(true)
				try {
					loadRemoteNovel(novelIDLive.value, true)?.let {
						startDownloadWorkerAfterUpdateUseCase(it.updatedChapters)
					}
					logI("Successfully reloaded novel")
				} catch (t: Throwable) {
					logE("Failed refreshing the novel data", t)
					error.emit(RefreshException(t))
				} finally {
					isRefreshing.emit(false)
				}
			} else {
				error.emit(
					OfflineException(R.string.fragment_novel_snackbar_cannot_inital_load_offline)
				)
			}
		}
	}

	override fun setNovelID(novelID: Int) {
		when {
			novelIDLive.value == -1 -> logI("Setting NovelID")
			novelIDLive.value != novelID -> logI("NovelID not equal, resetting")
			novelIDLive.value == novelID -> {
				logI("NovelID equal, ignoring")
				return
			}
		}
		novelIDLive.value = novelID
	}

	override fun setNovelCategories(categories: IntArray) {
		launchIO {
			val novel = novelLive.first { it != null }!!
			if (!novel.bookmarked) {
				updateNovelUseCase(novel.copy(bookmarked = true))
			}
			setNovelCategoriesUseCase(novel.id, categories)
		}
	}

	override fun toggleNovelBookmark() {
		launchIO {
			logI("toggleNovelBookmarkFlow")
			val novel = novelLive.first { it != null }!!
			val newState = !novel.bookmarked
			updateNovelUseCase(novel.copy(bookmarked = newState))

			if (!newState) {

				val savedChapters = chaptersLive.value.filter { it.isSaved }.size

				if (savedChapters != 0) toggleBookmarkResponse.value =
					ToggleBookmarkResponse.DeleteChapters(savedChapters)

				delay(100)
			}
			toggleBookmarkResponse.value = ToggleBookmarkResponse.Nothing
		}
	}

	override fun isBookmarked(): Flow<Boolean> = flow {
		emit(novelLive.value?.bookmarked ?: false)
	}.onIO()

	override fun downloadNextChapter() {
		launchIO {
			val array = chaptersLive.value.sortedBy { it.order }
			val r = array.indexOfFirst { it.readingStatus != ReadingStatus.READ }
			if (r != -1) downloadChapter(arrayOf(array[r]))
			startDownloadWorkerUseCase()
		}
	}

	override fun downloadNextCustomChapters(max: Int) {
		launchIO {
			val array = chaptersLive.value.sortedBy { it.order }
			val r = array.indexOfFirst { it.readingStatus != ReadingStatus.READ }
			if (r != -1) {
				val list = arrayListOf<ChapterUI>()
				list.add(array[r])
				var count = 1
				while ((r + count) < array.size && count <= max) {
					list.add(array[r + count])
					count++
				}
				downloadChapter(list.toTypedArray())
			}
			startDownloadWorkerUseCase()
		}
	}

	override fun downloadNext5Chapters() = downloadNextCustomChapters(5)

	override fun downloadNext10Chapters() = downloadNextCustomChapters(10)

	override fun downloadAllUnreadChapters() {
		launchIO {
			downloadChapter(chaptersLive.value.filter { it.readingStatus == ReadingStatus.UNREAD && !it.isSaved }
				.toTypedArray())
			startDownloadWorkerUseCase()
		}
	}

	override fun downloadAllChapters() {
		launchIO {
			downloadChapter(chaptersLive.value.filter { !it.isSaved }.toTypedArray())
			startDownloadWorkerUseCase()
		}
	}

	override fun updateNovelSetting(novelSettingUI: NovelSettingUI) {
		logD("Launching update")
		launchIO {
			updateNovelSettingUseCase(novelSettingUI)
		}
	}

	override val itemIndex: MutableStateFlow<Int> = MutableStateFlow(0)
	override fun setItemAt(index: Int) {
		itemIndex.value = index
	}

	override val openLastReadResult = MutableSharedFlow<LastOpenResult>()

	override val hasSelected: StateFlow<Boolean> by lazy {
		this.chaptersLive.mapLatest { chapters -> chapters.any { it.isSelected } }.onIO()
			.stateIn(viewModelScopeIO, SharingStarted.Lazily, false)
	}

	override fun bookmarkSelected() {
		launchIO {
			chapterRepo.updateChapterBookmark(getSelectedIds(), true)

			clearSelected()
		}
	}

	override fun deleteSelected() {
		launchIO {
			val list = chaptersLive.value.filter { it.isSelected && it.isSaved }
			deleteChapterPassageUseCase.invokeUI(list)
			clearSelected()
		}
	}

	override fun downloadSelected() {
		launchIO {
			val list = chaptersLive.value.filter { it.isSelected && !it.isSaved }
			downloadChapterPassageUseCase(list.toTypedArray())
			clearSelected()
			startDownloadWorkerUseCase()
		}
	}

	override fun invertSelection() {
		launchIO {
			val list = chaptersLive.value
			val selection = copySelected()

			list.forEach {
				selection[it.id] = !it.isSelected
			}

			selectedChapters.value = selection
		}
	}

	override fun markSelectedAs(readingStatus: ReadingStatus) {
		launchIO {
			chapterRepo.updateChapterReadingStatus(getSelectedIds(), readingStatus)

			clearSelected()
		}
	}

	override fun removeBookmarkFromSelected() {
		launchIO {
			chapterRepo.updateChapterBookmark(getSelectedIds(), false)
			clearSelected()
		}
	}

	override fun selectAll() {
		launchIO {
			val list = chaptersLive.value
			val selection = copySelected()

			list.forEach {
				selection[it.id] = true
			}

			selectedChapters.value = selection
		}
	}

	override fun selectBetween() {
		launchIO {
			val list = chaptersLive.value
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

			selectedChapters.value = selection
		}
	}

	override fun trueDeleteSelected() {
		launchIO {
			val list = chaptersLive.value
			trueDeleteChapter(list.filter { it.isSelected })
			clearSelected()
		}
	}

	private fun scrollTo(predicate: (ChapterUI) -> Boolean): Boolean {
		chaptersLive.value.indexOfFirst(predicate).takeIf { it != -1 }?.let {
			itemIndex.value = it
			return true
		}
		return false
	}

	override fun toggleSelection(it: ChapterUI) {
		logV("$it")
		launchIO {
			val selection = copySelected()

			selection[it.id] = !it.isSelected

			selectedChapters.value = selection
		}
	}

	override fun getChapterCount(): Int = chaptersLive.value.size

	override fun deleteChapters() {
		launchIO {
			deleteChapterPassageUseCase.invokeUI(chaptersLive.value.filter { it.isSaved })
		}
	}

	override fun jump(query: String, byTitle: Boolean) {
		launchIO {
			/**
			 * The predicate to use to find the chapter to scroll to
			 */
			val predicate: (ChapterUI) -> Boolean

			@Suppress("LiftReturnOrAssignment") if (byTitle) {
				predicate = { it.title.contains(query) }
			} else {
				predicate = { it.order == query.toDouble() }
			}

			if (!scrollTo(predicate)) {
				jumpState.value = JumpState.FAILURE
				delay(100)
				jumpState.value = JumpState.UNKNOWN
			}
		}
	}

	override val isQRCodeVisible = MutableStateFlow(false)
	override fun showQRCodeDialog() {
		isQRCodeVisible.value = true
	}

	override fun hideQRCodeDialog() {
		isQRCodeVisible.value = false
	}

	override val isShareMenuVisible = MutableStateFlow(false)

	override fun openShareMenu() {
		isShareMenuVisible.value = true
	}

	override fun hideShareMenu() {
		isShareMenuVisible.value = false
	}

	override val isFilterMenuVisible = MutableStateFlow(false)

	override fun showFilterMenu() {
		isFilterMenuVisible.value = true
	}

	override fun hideFilterMenu() {
		isFilterMenuVisible.value = false
	}

	override val isDownloadDialogVisible = MutableStateFlow(false)

	override fun showDownloadDialog() {
		isDownloadDialogVisible.value = true
	}

	override fun hideDownloadDialog() {
		isDownloadDialogVisible.value = false
	}
}