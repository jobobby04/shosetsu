package app.shosetsu.android.viewmodel.impl

import android.app.Application
import android.database.sqlite.SQLiteException
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import app.shosetsu.android.R
import app.shosetsu.android.common.SettingKey
import app.shosetsu.android.common.SettingKey.ReaderDoubleTapFocus
import app.shosetsu.android.common.SettingKey.ReaderDoubleTapSystem
import app.shosetsu.android.common.SettingKey.ReaderEnableFullscreen
import app.shosetsu.android.common.SettingKey.ReaderEngine
import app.shosetsu.android.common.SettingKey.ReaderHorizontalPageSwap
import app.shosetsu.android.common.SettingKey.ReaderHtmlCss
import app.shosetsu.android.common.SettingKey.ReaderIsFirstFocus
import app.shosetsu.android.common.SettingKey.ReaderIsInvertedSwipe
import app.shosetsu.android.common.SettingKey.ReaderIsTapToScroll
import app.shosetsu.android.common.SettingKey.ReaderKeepScreenOn
import app.shosetsu.android.common.SettingKey.ReaderLanguage
import app.shosetsu.android.common.SettingKey.ReaderMarkReadAsReading
import app.shosetsu.android.common.SettingKey.ReaderMatchFullscreenToFocus
import app.shosetsu.android.common.SettingKey.ReaderNextChapter
import app.shosetsu.android.common.SettingKey.ReaderPitch
import app.shosetsu.android.common.SettingKey.ReaderShowChapterDivider
import app.shosetsu.android.common.SettingKey.ReaderSpeed
import app.shosetsu.android.common.SettingKey.ReaderTrackLongReading
import app.shosetsu.android.common.SettingKey.ReaderVoice
import app.shosetsu.android.common.SettingKey.ReaderVolumeScroll
import app.shosetsu.android.common.SettingKey.ReadingMarkingType
import app.shosetsu.android.common.enums.MarkingType
import app.shosetsu.android.common.enums.MarkingType.ONSCROLL
import app.shosetsu.android.common.enums.MarkingType.ONVIEW
import app.shosetsu.android.common.enums.ReadingStatus.READ
import app.shosetsu.android.common.enums.ReadingStatus.READING
import app.shosetsu.android.common.ext.launchIO
import app.shosetsu.android.common.ext.launchUI
import app.shosetsu.android.common.ext.logD
import app.shosetsu.android.common.ext.logE
import app.shosetsu.android.common.ext.logI
import app.shosetsu.android.common.ext.logV
import app.shosetsu.android.common.utils.asHtml
import app.shosetsu.android.common.utils.copy
import app.shosetsu.android.common.utils.transformCatching
import app.shosetsu.android.domain.repository.base.IChaptersRepository
import app.shosetsu.android.domain.repository.base.INovelReaderSettingsRepository
import app.shosetsu.android.domain.repository.base.INovelsRepository
import app.shosetsu.android.domain.repository.base.ISettingsRepository
import app.shosetsu.android.domain.usecases.RecordChapterIsReadUseCase
import app.shosetsu.android.domain.usecases.RecordChapterIsReadingUseCase
import app.shosetsu.android.domain.usecases.delete.DeleteChapterPassageUseCase
import app.shosetsu.android.domain.usecases.get.GetChapterPassageUseCase
import app.shosetsu.android.domain.usecases.get.GetExtensionUseCase
import app.shosetsu.android.domain.usecases.get.GetReaderChaptersUseCase
import app.shosetsu.android.domain.usecases.get.GetReaderSettingUseCase
import app.shosetsu.android.domain.usecases.load.LoadDeletePreviousChapterUseCase
import app.shosetsu.android.domain.usecases.load.LoadLiveAppThemeUseCase
import app.shosetsu.android.ui.reader.customSpeak
import app.shosetsu.android.ui.reader.page.ShosetsuStyle
import app.shosetsu.android.ui.theme.FallbackColorScheme
import app.shosetsu.android.view.uimodels.model.ExceptionSnackbarModel
import app.shosetsu.android.view.uimodels.model.NovelReaderSettingUI
import app.shosetsu.android.view.uimodels.model.reader.ChapterPassage
import app.shosetsu.android.view.uimodels.model.reader.ElementToTTSTextIterator
import app.shosetsu.android.view.uimodels.model.reader.LazyTTSText
import app.shosetsu.android.view.uimodels.model.reader.ReaderUIItem
import app.shosetsu.android.view.uimodels.model.reader.ReaderUIItem.ReaderChapterUI
import app.shosetsu.android.view.uimodels.model.reader.ReaderUIItem.ReaderDividerUI
import app.shosetsu.android.view.uimodels.model.reader.RewindableMutableListIterator
import app.shosetsu.android.view.uimodels.model.reader.TTSPlayback
import app.shosetsu.android.view.uimodels.model.reader.TTSText
import app.shosetsu.android.viewmodel.abstracted.AChapterReaderViewModel
import app.shosetsu.android.viewmodel.abstracted.ShosetsuCssViewModelComponent
import app.shosetsu.lib.IExtension
import app.shosetsu.lib.Novel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.acra.ACRA
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.util.Locale
import kotlin.time.Duration.Companion.seconds

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
 * 06 / 05 / 2020
 *
 * TODO delete previous chapter
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ChapterReaderViewModel(
	private val application: Application,
	override val settingsRepo: ISettingsRepository,
	private val chapterRepository: IChaptersRepository,
	private val novelRepo: INovelsRepository,
	private val readerSettingsRepo: INovelReaderSettingsRepository,
	override var loadLiveAppThemeUseCase: LoadLiveAppThemeUseCase,
	private val loadReaderChaptersUseCase: GetReaderChaptersUseCase,
	private val loadChapterPassageUseCase: GetChapterPassageUseCase,
	private val getReaderSettingsUseCase: GetReaderSettingUseCase,
	private val recordChapterIsReading: RecordChapterIsReadingUseCase,
	private val recordChapterAsRead: RecordChapterIsReadUseCase,
	private val getExt: GetExtensionUseCase,
	private val loadDeletePreviousChapterUseCase: LoadDeletePreviousChapterUseCase,
	private val deleteChapterPassageUseCase: DeleteChapterPassageUseCase,
) : AChapterReaderViewModel() {

	private val css = object : ShosetsuCssViewModelComponent() {
		override val settingsRepo: ISettingsRepository
			get() = this@ChapterReaderViewModel.settingsRepo
		override val viewModelScopeIO: CoroutineScope
			get() = this@ChapterReaderViewModel.viewModelScopeIO
		override val indentSizeFlow: Flow<Int>
			get() = this@ChapterReaderViewModel.indentSizeFlow
		override val paragraphSpacingFlow: Flow<Float>
			get() = this@ChapterReaderViewModel.paragraphSpacingFlow
		override val colorSchemeFlow: Flow<ColorScheme>
			get() = this@ChapterReaderViewModel.colorScheme
		override val paddingValuesFlow: Flow<PaddingValues>
			get() = this@ChapterReaderViewModel.paddingValues
	}

	override val exceptions: MutableSharedFlow<ExceptionSnackbarModel> = MutableSharedFlow()

	override val isReadingTooLong: MutableStateFlow<Boolean> by lazy {
		MutableStateFlow(false)
	}

	override val trackLongReading: StateFlow<Boolean> =
		settingsRepo.getBooleanFlow(ReaderTrackLongReading)

	override fun userIsReadingTooLong() {
		isReadingTooLong.value = true
	}

	override fun dismissReadingTooLong() {
		isReadingTooLong.value = false
	}

	private val isHorizontalPageSwapping by lazy {
		settingsRepo.getBooleanFlow(ReaderHorizontalPageSwap)
	}

	private val indentSizeFlow: Flow<Int> by lazy {
		readerSettingsFlow.mapLatest { result ->
			result.paragraphIndentSize
		}
	}

	private val paragraphSpacingFlow: Flow<Float> by lazy {
		readerSettingsFlow.mapLatest { result ->
			result.paragraphSpacingSize
		}
	}

	private val doubleTapSystemFlow: StateFlow<Boolean> by lazy {
		settingsRepo.getBooleanFlow(ReaderDoubleTapSystem)
			.let {
				it
					.combine(enableFullscreen) { doubleTapSystem, enableFullscreen ->
						doubleTapSystem || !enableFullscreen
					}
					.combine(matchFullscreenToFocus) { doubleTapSystem, matchFullscreenToFocus ->
						doubleTapSystem && !matchFullscreenToFocus
					}
					.onIO()
					.stateIn(
						viewModelScopeIO,
						SharingStarted.Lazily,
						(it.value || !enableFullscreen.value) && !matchFullscreenToFocus.value
					)
			}

	}

	/**
	 * Lets explain what goes on here
	 *
	 * Say the User reads a chapter,
	 * the action that is then taken by the code is to mark the chapter as read and 0 it out.
	 * But when it 0s out the progress, and the user refreshes the UI, the user sees the UI reset.
	 *
	 * The user will view this as an "error" because they expect things to remain the way they left
	 * it while reading. (Object permanence).
	 *
	 * To correct this,
	 */
	private val progressMapFlow = MutableStateFlow(HashMap<Int, Double>())

	override val ttsPitch by lazy {
		settingsRepo.getFloatFlow(ReaderPitch)
	}
	override val ttsLanguage: StateFlow<String> by lazy {
		settingsRepo.getStringFlow(ReaderLanguage)
	}
	override val ttsEngine: StateFlow<String> by lazy {
		settingsRepo.getStringFlow(ReaderEngine)
	}
	override val ttsVoice: StateFlow<String> by lazy {
		settingsRepo.getStringFlow(ReaderVoice)
	}
	override val ttsSpeed by lazy {
		settingsRepo.getFloatFlow(ReaderSpeed)
	}
	val ttsNextChapter: StateFlow<Boolean> by lazy {
		settingsRepo.getBooleanFlow(ReaderNextChapter)
	}

	private val stringMap = HashMap<Int, Flow<ChapterPassage>>()
	private val refreshMap = HashMap<Int, MutableSharedFlow<Unit>>()

	override val isFirstFocusFlow: StateFlow<Boolean> by lazy {
		settingsRepo.getBooleanFlow(ReaderIsFirstFocus)
	}

	override val isSwipeInverted: StateFlow<Boolean> by lazy {
		settingsRepo.getBooleanFlow(ReaderIsInvertedSwipe)
	}

	override fun onFirstFocus() {
		//logV("")
		launchIO {
			settingsRepo.setBoolean(ReaderIsFirstFocus, false)
		}
	}

	/**
	 * Trim out the strings present around the current page
	 *
	 * Ensures there is only 3~ flows at a time in memory
	 */
	private fun cleanStringMap(currentIndex: Int) {
		val excludedKeys = arrayListOf<Int>()
		val keys = stringMap.keys.toList()

		excludedKeys.add(keys[currentIndex])

		for (i in 1..3) {
			keys.getOrNull(currentIndex - i)?.let {
				excludedKeys.add(it)
			}
			keys.getOrNull(currentIndex + i)?.let {
				excludedKeys.add(it)
			}
		}

		keys.filterNot { excludedKeys.contains(it) }.forEach { key ->
			stringMap.remove(key)
		}
	}

	/**
	 * Clear all maps
	 */
	@Suppress("NOTHING_TO_INLINE") // We need every ns
	private inline fun clearMaps() {
		stringMap.clear()
	}

	@Suppress("NOTHING_TO_INLINE") // We need every ns
	private inline fun getRefreshFlow(item: ReaderChapterUI) =
		refreshMap.getOrPut(item.id) {
			MutableSharedFlow<Unit>(replay = 1).apply {
				viewModelScopeIO.launch { emit(Unit) }
			}
		}

	override fun retryChapter(item: ReaderChapterUI) {
		val flow = getRefreshFlow(item)
		viewModelScopeIO.launch { flow.emit(Unit) }
	}

	private var cleanStringMapJob: Job? = null

	/**
	 * Try to deduplicate titles in the given [document]
	 *
	 * @param document The document to process
	 */
	private suspend fun tryToDedupTitles(document: Document) {
		if (settingsRepo.getBoolean(SettingKey.ReaderDeDupChapterTitle)) {
			logD("Trying to remove duplicate titles...")
			try {
				val titles = document.select("h1");
				if (titles.size > 1) {
					// Remove duplicate titles, in reverse
					for (title in titles.subList(1, titles.size).reversed()) {
						title.remove()
					}
				}
			} catch (e: Exception) {
				logE("Failed to remove duplicate titles", e)
				exceptions.emit(
					ExceptionSnackbarModel(
						application.getString(
							R.string.reader_error_dedup_titles,
							e.message ?: "unknown"
						)
					)
				)
			}
		}
	}

	override fun getChapterPassageHTML(item: ReaderChapterUI): Flow<ChapterPassage> {
		val mutableFlow = stringMap.getOrPut(item.id) {
			getRefreshFlow(item)
				.transformCatching(exceptional = {
					emit(
						ChapterPassage.Error(
							it
						)
					)
				}) {
					emit(ChapterPassage.Loading)
					val bytes = getChapterPassage(item)
						?: throw Exception("No content received")

					var result = bytes.decodeToString()

					val chapterType = extensionChapterTypeFlow.firstOrNull()

					if (chapterType == Novel.ChapterType.STRING) {
						logI("Converting text to HTML")
						result = asHtml(result, item.title)
					}

					val document = Jsoup.parse(result)

					tryToDedupTitles(document)

					val ttsElements = document.body().select("*:not(:has(*)):not(br)")

					// we need to generate the ids here
					// as to ensure they stay here when the html is rendered
					logV("Generating ids for views")
					ttsElements.parallelStream().map(::LazyTTSText).forEach {
						it.id
					}
					logV("Finished generating ids for views")

					// run GC as we just created a lot of objects
					// TODO see how to optimize this by not creating so many objects
					System.gc()

					// keep a single backing store of the iterator,
					//  as to prevent it from being recreated
					val ttsIterator = ElementToTTSTextIterator {
						ttsElements.listIterator()
					}

					emitAll(cssStyle.map { cssStyle ->
						cssStyle.insert(document)
						@Suppress("UNCHECKED_CAST")
						ChapterPassage.Success(
							document.toString(),
							// this is fine
							ttsIterator as RewindableMutableListIterator<TTSText>
						)
					})
				}
				.onIO()
				.shareIn(viewModelScopeIO, SharingStarted.Lazily, 1)
		}

		if (cleanStringMapJob == null && stringMap.size > 10) {
			cleanStringMapJob =
				launchIO {
					cleanStringMap(stringMap.keys.indexOf(item.id))
					cleanStringMapJob = null
				}
		}

		return mutableFlow.catch { e ->
			exceptions.emit(createGenericExceptionModel(e))
		}
	}

	private fun createGenericExceptionModel(e: Throwable) =
		ExceptionSnackbarModel(
			e.message ?: application.getString(R.string.reader_error_unknown),
			e
		)

	override val cssStyle: SharedFlow<ShosetsuStyle> by lazy {
		css.shosetsuCss.combine(userCssFlow) { shoCSS, useCSS ->
			ShosetsuStyle(shoCSS, useCSS)
		}.onIO().shareIn(viewModelScopeIO, SharingStarted.Lazily, 1)
	}

	override val isCurrentChapterBookmarked: StateFlow<Boolean> by lazy {
		currentChapterID.flatMapLatest { id ->
			chapterRepository.getChapterBookmarkedFlow(id).map {
				it ?: false
			}
		}.onIO().stateIn(viewModelScopeIO, SharingStarted.Lazily, false)
	}

	private val extFlow: SharedFlow<IExtension?> by lazy {
		novelIDLive.mapLatest { id ->
			val novel = novelRepo.getNovel(id) ?: return@mapLatest null
			getExt(novel.extensionID)
		}.shareIn(viewModelScopeIO, SharingStarted.Lazily, 1)
	}

	private val extensionChapterTypeFlow: SharedFlow<Novel.ChapterType?> by lazy {
		extFlow.map { it?.chapterType }
			.onIO()
			.shareIn(viewModelScopeIO, SharingStarted.Lazily, 1)
	}

	private val chaptersFlow: SharedFlow<List<ReaderChapterUI>> by lazy {
		novelIDLive.flatMapLatest { nId ->
			System.gc() // Run GC to try and mitigate OOM
			loadReaderChaptersUseCase(nId)
		}.onIO().shareIn(viewModelScopeIO, SharingStarted.Lazily, 1)
	}

	override fun getChapterProgress(chapter: ReaderChapterUI): Flow<Double> =
		progressMapFlow.transformLatest { progressMap ->
			if (progressMap.containsKey(chapter.id))
				emit(progressMap[chapter.id]!!)
			else
				emitAll(chapterRepository.getChapterProgress(chapter.convertTo()))
		}.onIO()

	override val liveData: StateFlow<ImmutableList<ReaderUIItem>?> by lazy {
		chaptersFlow
			.combineDividers() // Add dividers
			.map { it.toImmutableList() }
			.catch { createGenericExceptionModel(it) }
			.onIO()
			.stateIn(viewModelScopeIO, SharingStarted.Lazily, null)
	}

	override val currentPage: MutableStateFlow<Int?> = MutableStateFlow(null)

	private fun Flow<List<ReaderChapterUI>>.combineDividers(): Flow<List<ReaderUIItem>> =
		combine(settingsRepo.getBooleanFlow(ReaderShowChapterDivider)) { list, value ->
			if (value && list.isNotEmpty()) {
				val modified = ArrayList<ReaderUIItem>(list)
				// Adds the "No more chapters" marker
				modified.add(modified.size, ReaderDividerUI(prev = list.last()))

				/**
				 * Loops down the list, adding in the seperators
				 */
				val startPoint = modified.size - 2
				for (index in startPoint downTo 1)
					modified.add(
						index, ReaderDividerUI(
							(modified[index - 1] as ReaderChapterUI),
							(modified[index] as ReaderChapterUI)
						)
					)

				modified
			} else {
				list
			}
		}

	override fun setCurrentPage(page: Int) {
		//logV("$page")
		currentPage.value = page
	}

	private val readerSettingsFlow: StateFlow<NovelReaderSettingUI> by lazy {
		novelIDLive.flatMapLatest {
			getReaderSettingsUseCase(it)
		}.onIO().stateIn(viewModelScopeIO, SharingStarted.Lazily, NovelReaderSettingUI(-1))
	}

	override val liveKeepScreenOn: StateFlow<Boolean> by lazy {
		settingsRepo.getBooleanFlow(ReaderKeepScreenOn)
	}

	override val currentChapterID: MutableStateFlow<Int> = MutableStateFlow(-1)

	private val novelIDLive: MutableStateFlow<Int> = MutableStateFlow(-1)

	private var _isHorizontalReading: Boolean = ReaderHorizontalPageSwap.default

	override val isVolumeScrollEnabled by lazy {
		settingsRepo.getBooleanFlow(ReaderVolumeScroll)
	}

	override val isHorizontalReading: StateFlow<Boolean> by lazy {
		isHorizontalPageSwapping
			.onEach { _isHorizontalReading = it }
			.launchIn(viewModelScopeIO)
		isHorizontalPageSwapping
	}

	override fun setNovelID(novelID: Int) {
		logV("novelID=$novelID")
		when {
			novelIDLive.value == -1 -> {
				//logD("Setting NovelID")
			}

			novelIDLive.value != novelID -> {
				//logD("NovelID not equal, resetting")
			}

			novelIDLive.value == novelID -> {
				//logD("NovelID equal, ignoring")
				return
			}
		}
		novelIDLive.value = novelID
	}

	@Suppress("NOTHING_TO_INLINE") // We need every ns
	private suspend inline fun getChapterPassage(readerChapterUI: ReaderChapterUI): ByteArray? =
		loadChapterPassageUseCase(readerChapterUI)

	override fun toggleBookmark() {
		launchIO {
			val id = currentChapterID.first()
			val chapter = chapterRepository.getChapter(id) ?: return@launchIO

			chapterRepository.updateChapter(
				chapter.copy(
					bookmarked = !chapter.bookmarked
				)
			)
		}
	}

	override fun updateChapterAsRead(chapter: ReaderChapterUI) {
		launchIO {
			_recordChapterAsRead(chapter)

			try {
				chapterRepository.getChapter(chapter.id)?.let {
					chapterRepository.updateChapter(
						it.copy(
							readingStatus = READ,
							readingPosition = 0.0
						)
					)
				}
			} catch (e: SQLiteException) {
				logE("Failed to update chapter as read", e)
				ACRA.errorReporter.handleSilentException(e)
			}

			deletePrevious(chapter)

			NotificationManagerCompat.from(application).cancel(
				"update/${novelIDLive.value}/${chapter.id}",
				10000 + novelIDLive.value
			)
		}
	}

	private val readingMarkingTypeFlow by lazy {
		settingsRepo.getStringFlow(ReadingMarkingType).map {
			MarkingType.valueOf(it)
		}
	}

	/**
	 * Wrapper of [recordChapterIsReading] for exceptions.
	 */
	private suspend fun _recordChapterIsReading(chapter: ReaderChapterUI) {
		try {
			recordChapterIsReading(chapter)
		} catch (e: CancellationException) {
			logE("Job to record chapter as being read was cancelled...", e)
			// We do not want to report the error in this case, its a common on.
			exceptions.emit(ExceptionSnackbarModel(application.getString(R.string.reader_error_chapter_reading_cancelled)))
		} catch (e: Exception) {
			logE("Failed to record chapter as being read.", e)
			ACRA.errorReporter.handleSilentException(e)
			exceptions.emit(ExceptionSnackbarModel(application.getString(R.string.reader_error_chapter_reading)))
		}
	}

	/**
	 * Wrapper of [recordChapterAsRead] for exceptions.
	 */
	private suspend fun _recordChapterAsRead(chapter: ReaderChapterUI) {
		try {
			recordChapterAsRead(chapter)
		} catch (e: CancellationException) {
			logE("Job to record chapter as read was cancelled...", e)
			// We do not want to report the error in this case, its a common on.
			exceptions.emit(ExceptionSnackbarModel(application.getString(R.string.reader_error_chapter_read_cancelled)))
		} catch (e: Exception) {
			logE("Failed to record chapter as read.", e)
			ACRA.errorReporter.handleSilentException(e)
			exceptions.emit(ExceptionSnackbarModel(application.getString(R.string.reader_error_chapter_read)))
		}
	}

	override fun onViewed(chapter: ReaderChapterUI) {
		//logV("$chapter")
		launchIO {
			settingsRepo.getBoolean(ReaderMarkReadAsReading).let { markReadAsReading ->
				val chapterEntity = chapterRepository.getChapter(chapter.id) ?: return@launchIO
				/*
				 * If marking chapters that are read as reading is disabled
				 * and the chapter's readingStatus is read, return to prevent further IO.
				 */
				if (!markReadAsReading && chapterEntity.readingStatus == READ) return@launchIO

				/*
				 * If the reading marking type does not equal on view, then return
				 */
				if (readingMarkingTypeFlow.first() != ONVIEW) return@launchIO

				_recordChapterIsReading(chapter)

				chapterRepository.updateChapter(
					chapterEntity.copy(readingStatus = READING)
				)
			}
		}
	}

	override fun onScroll(chapter: ReaderChapterUI, readingPosition: Double) {
		launchIO {
			val chapterEntity = chapterRepository.getChapter(chapter.id) ?: return@launchIO

			// If the chapter reaches 90% read, we can assume the reader already sees it all :P
			if (readingPosition <= 0.90) {
				settingsRepo.getBoolean(ReaderMarkReadAsReading).let { markReadAsReading ->
					/**
					 * If marking chapters that are read as reading is disabled
					 * and the chapter's readingStatus is read, save progress temporarily.
					 */
					if (!markReadAsReading && chapterEntity.readingStatus == READ) {
						progressMapFlow.value = progressMapFlow.value.copy().apply {
							put(chapter.id, readingPosition)
						}
						return@launchIO
					}

					/*
							 * If marking type is on scroll, record as reading
							 */
					val markingType = readingMarkingTypeFlow.first()
					if (markingType == ONSCROLL) {
						_recordChapterIsReading(chapter)
					}

					// Remove temp progress
					progressMapFlow.value = progressMapFlow.value.copy().apply {
						remove(chapter.id)
					}

					chapterRepository.updateChapter(
						chapterEntity.copy(
							readingStatus = if (markingType == ONSCROLL) {
								READING
							} else chapterEntity.readingStatus,
							readingPosition = readingPosition
						)
					)
				}
			} else {
				// User probably sees everything at this point

				_recordChapterAsRead(chapter)

				// Temp remember the progress
				progressMapFlow.value = progressMapFlow.value.copy().apply {
					put(chapter.id, readingPosition)
				}

				chapterRepository.updateChapter(
					chapterEntity.copy(
						readingStatus = READ,
						readingPosition = 0.0
					)
				)
			}
		}
	}

	override fun updateSetting(novelReaderSettingEntity: NovelReaderSettingUI) {
		launchIO {
			readerSettingsRepo.update(novelReaderSettingEntity.convertTo())
		}
	}

	override fun getSettings(): StateFlow<NovelReaderSettingUI> = readerSettingsFlow

	override val tapToScroll: StateFlow<Boolean> by lazy {
		settingsRepo.getBooleanFlow(ReaderIsTapToScroll)
	}

	private val doubleTapFocus: StateFlow<Boolean> by lazy {
		settingsRepo.getBooleanFlow(ReaderDoubleTapFocus)
	}

	override val enableFullscreen by lazy {
		settingsRepo.getBooleanFlow(ReaderEnableFullscreen)
	}

	override val matchFullscreenToFocus: StateFlow<Boolean> by lazy {
		settingsRepo.getBooleanFlow(ReaderMatchFullscreenToFocus)
	}

	override val isFocused: MutableStateFlow<Boolean> = MutableStateFlow(false)

	private val _isSystemVisible = MutableStateFlow(true)
	override val isSystemVisible: StateFlow<Boolean> by lazy {
		_isSystemVisible.combine(enableFullscreen) { isSystemVisible, enableFullscreen ->
			isSystemVisible || !enableFullscreen
		}.onIO().stateIn(viewModelScopeIO, SharingStarted.Lazily, true)
	}


	override fun toggleFocus() {
		isFocused.value = !isFocused.value
	}

	override fun toggleSystemVisible() {
		isFocused.value = _isSystemVisible.value
		_isSystemVisible.value = !_isSystemVisible.value
	}

	override fun onReaderClicked(item: String?) {
		if (item != null && ttsPlayback.value == TTSPlayback.Paused) {
			ttsProgress.value = item.substringAfter("textElement")
			ttsPlayback.value = TTSPlayback.Playing
		} else if (!doubleTapFocus.value) {
			val newValue = !isFocused.value
			isFocused.value = newValue
			if (newValue || matchFullscreenToFocus.value)
				_isSystemVisible.value = !newValue
		}
	}

	override fun onReaderDoubleClicked() {
		if (doubleTapFocus.value) {
			val newValue = !isFocused.value
			isFocused.value = newValue
			if (newValue || matchFullscreenToFocus.value)
				_isSystemVisible.value = !newValue
		} else if (doubleTapSystemFlow.value) {
			toggleSystemVisible()
		}
	}

	private val userCssFlow: StateFlow<String> by lazy {
		settingsRepo.getStringFlow(ReaderHtmlCss)
	}

	override val liveIsScreenRotationLocked = MutableStateFlow(false)

	override fun toggleScreenRotationLock() {
		liveIsScreenRotationLocked.value = !liveIsScreenRotationLocked.value
	}

	override fun setCurrentChapterID(chapterId: Int, initial: Boolean) {
		//logV("$chapterId, $initial")
		currentChapterID.value = chapterId

		if (initial)
			launchIO {
				val items = liveData.first { it != null }!!
				currentPage.value = items
					.indexOfFirst { it is ReaderChapterUI && it.id == chapterId }
			}
	}

	override fun incrementProgress() {
		launchIO {

			val chapterId = currentChapterID.first()

			val chapter = chaptersFlow.first().find { it.id == chapterId } ?: return@launchIO
			val chapterEntity = chapterRepository.getChapter(chapter.id) ?: return@launchIO

			/*
			 * Increment 5% at a time, let us hope this does not back fire
			 */
			if ((chapterEntity.readingPosition + INCREMENT_PERCENTAGE) < 1)
				onScroll(chapter, chapterEntity.readingPosition + INCREMENT_PERCENTAGE)
		}
	}

	override fun depleteProgress() {
		launchIO {
			val chapterId = currentChapterID.first()

			val chapter = chaptersFlow.first().find { it.id == chapterId } ?: return@launchIO
			val chapterEntity = chapterRepository.getChapter(chapter.id) ?: return@launchIO

			/*
			 * Increment 5% at a time, let us hope this does not back fire
			 */
			if ((chapterEntity.readingPosition - INCREMENT_PERCENTAGE) > 0)
				onScroll(chapter, chapterEntity.readingPosition - INCREMENT_PERCENTAGE)
		}
	}

	override fun clearMemory() {
		logV("Application called to clear memory")
		launchIO {
			run {
				val excludedKeys = arrayListOf<Int>()
				val keys = stringMap.keys.toList()
				val currentChapter = currentChapterID.value

				excludedKeys.add(currentChapter)

				keys.filterNot { excludedKeys.contains(it) }.forEach { key ->
					stringMap.remove(key)
				}
			}

			run {
				val excludedKeys = arrayListOf<Int>()
				val map = progressMapFlow.value
				val keys = map.keys.toList()
				val currentChapter = currentChapterID.value

				excludedKeys.add(currentChapter)

				keys.filterNot { excludedKeys.contains(it) }.forEach { key ->
					map.remove(key)
				}

				progressMapFlow.value = map
			}

			run {
				val excludedKeys = arrayListOf<Int>()
				val map = refreshMap
				val keys = map.keys.toList()
				val currentChapter = currentChapterID.value

				excludedKeys.add(currentChapter)

				keys.filterNot { excludedKeys.contains(it) }.forEach { key ->
					map.remove(key)
				}
			}
		}
	}

	suspend fun deletePrevious(readChapter: ReaderChapterUI) {
		logI("Deleting previous chapters")
		loadDeletePreviousChapterUseCase().let { chaptersBackToDelete ->
			if (chaptersBackToDelete != -1) {

				val chapters = chaptersFlow.first()

				val indexOfLast = chapters.indexOfFirst { it.id == readChapter.id }

				if (indexOfLast == -1) {
					logE("Index of last read chapter turned up negative")
					return
				}

				if (indexOfLast - chaptersBackToDelete < 0) {
					return
				}

				val targetToDelete = indexOfLast - chaptersBackToDelete

				deleteChapterPassageUseCase(
					if (targetToDelete == 0) {
						listOf(chapters[targetToDelete])
					} else {
						chapters.subList(0, targetToDelete + 1)
					}
						// Convert reader to
						.mapNotNull {
							try {
								chapterRepository.getChapter(it.id)
							} catch (e: SQLiteException) {
								null
							}
						}
						.filter { it.isSaved } // only delete downloaded chapters
				)
			}
		}
	}

	override val pageJumper: MutableSharedFlow<Int> = MutableSharedFlow<Int>(replay = 0)
	override val ttsProgress = MutableStateFlow<String?>(null)
	val ttsDone = MutableStateFlow<String?>(null)
	override val ttsPlayback = MutableStateFlow<TTSPlayback>(TTSPlayback.Stopped)

	data class TTSBuilder(
		val engine: String,
		val language: String,
		val voice: String,
	)

	/**
	 * Provides a TTS to use
	 */
	private val tts = ttsEngine.map { engine ->
		TTSBuilder(engine, "", "")
	}.filterNotNull().combine(ttsLanguage) { builder, language ->
		builder.copy(language = language)
	}.combine(ttsVoice) { builder, voice ->
		builder.copy(voice = voice)
	}.map { builder ->
		val ttsResult = CompletableDeferred<Int>()

		val tts = if (builder.engine.isEmpty()) {
			TextToSpeech(application, ttsResult::complete)
		} else {
			TextToSpeech(application, ttsResult::complete, builder.engine)
		}

		// Wait for the TTS to initialize
		when (val result = ttsResult.await()) {
			TextToSpeech.SUCCESS -> tts to builder
			else -> {
				handleTTSError(result, R.string.reader_test_invalid_engine)
				null
			}
		}
	}.filterNotNull()
		.filter { (tts, builder) ->
			/** Has a language been set */
			val languageSuccess: Boolean
			val locale: Locale

			if (builder.language.isEmpty()) {
				// If language not set, assume the default language
				locale = Locale.getDefault()
				val result = tts.setLanguage(locale)
				languageSuccess = when (result) {
					TextToSpeech.LANG_AVAILABLE -> true
					TextToSpeech.LANG_COUNTRY_AVAILABLE -> true
					TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE -> true
					else -> false
				}
			} else {
				// Find the local from languages
				val ttsLocale =
					tts.availableLanguages.find { it.toLanguageTag() == builder.language }
				if (ttsLocale != null) {
					locale = ttsLocale
					val result = tts.setLanguage(locale)
					languageSuccess = when (result) {
						TextToSpeech.LANG_AVAILABLE -> true
						TextToSpeech.LANG_COUNTRY_AVAILABLE -> true
						TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE -> true
						else -> false
					}
				} else {
					// Failed to find the locale, strange
					locale = Locale.getDefault() // need to set this, else warning
					languageSuccess = false
				}
			}

			// Do not continue if a language has not been set successfully
			if (!languageSuccess) {
				exceptions.emit(ExceptionSnackbarModel(application.getString(R.string.reader_test_invalid_language)))
				return@filter false
			}

			/** Has the voice been set */
			val voiceSuccess: Boolean
			if (builder.voice.isNotEmpty()) {
				// Find the voice from voices
				val ttsVoice = tts.voices
					.filter { it.locale == locale }
					.find { it.name == builder.voice }

				if (ttsVoice != null) {
					// Attempt to set the voice if found
					val result = tts.setVoice(ttsVoice)
					voiceSuccess = when (result) {
						TextToSpeech.SUCCESS -> true
						else -> {
							handleTTSError(result)
							false
						}
					}
				} else {
					voiceSuccess = false
				}
			} else {
				// is fine if there is a default voice
				voiceSuccess = tts.defaultVoice != null
			}

			// do not proceed if voice was not successful
			if (!voiceSuccess) {
				exceptions.emit(ExceptionSnackbarModel(application.getString(R.string.reader_test_invalid_voice)))
				return@filter false
			}
			true
		}
		.combine(
			ttsPitch
				.combine(ttsSpeed) { a, b -> a to b }
				.distinctUntilChanged()
		) { (tts, _), (pitch, speed) ->
			tts.apply {
				setPitch(pitch / 10).let(::launchHandleTTSError)
				setSpeechRate(speed / 10).let(::launchHandleTTSError)
			}
		}
		.distinctUntilChanged()
		.onEach {
			it.setOnUtteranceProgressListener(
				object : UtteranceProgressListener() {
					override fun onStart(utteranceId: String?) {
						// Only set progress if not stopped
						if (ttsPlayback.value != TTSPlayback.Stopped) {
							ttsProgress.value = utteranceId?.substringBefore('|')
						}
					}

					override fun onDone(utteranceId: String?) {
						ttsDone.value = utteranceId
					}

					@Deprecated("Deprecated in Java")
					override fun onError(utteranceId: String?) {
					}

					override fun onError(utteranceId: String?, errorCode: Int) {
						this@ChapterReaderViewModel.logE("TTS Error code: $errorCode")
						ttsPlayback.value = TTSPlayback.Paused
					}
				}
			).let(::launchHandleTTSError)
		}
		.stateIn(viewModelScopeIO, SharingStarted.Eagerly, null)

	init {
		viewModelScopeIO.launch {
			var oldTts: TextToSpeech? = null
			currentChapterID.collectLatest { chapterId ->
				// Child scope is cancelled when the chapter is changed
				coroutineScope {
					// Clear out old TTS
					oldTts?.stop() // we ignore error codes here, its going away
					oldTts = null

					// Find the current chapter
					val item = liveData.first { it != null }
						?.find { (it as? ReaderChapterUI)?.id == chapterId }
						as? ReaderChapterUI ?: return@coroutineScope

					// Clean up memory asap
					System.gc()

					// Get the text of the chapter
					val passage = getChapterPassageHTML(item)
						.firstOrNull { it is ChapterPassage.Success } as? ChapterPassage.Success
						?: return@coroutineScope

					launch nextChapterTts@{
						val lastTts =
							passage.ttsElements.lastOrNull()
								?: return@nextChapterTts

						// If the user enables the setting while in the reader, we can listen in
						ttsNextChapter.collectLatest nextChapterTts2@{ ttsNextChapter ->
							// skip if disabled
							if (!ttsNextChapter) {
								return@nextChapterTts2
							}

							// Wait for the last TTS line to be spoken to move to the next chapter
							ttsDone.firstOrNull { it != null && it == lastTts.id }
								?: return@nextChapterTts2

							// Get current readerUIItems
							val readerUIItems =
								liveData.first { it != null } ?: return@nextChapterTts2

							val chapterItems = readerUIItems.filterIsInstance<ReaderChapterUI>()

							// Find index of the current chapter
							val index = chapterItems.indexOfFirst { it.id == chapterId }

							// ensure we got a valid index
							if (index >= 0) {
								// Find next chapter
								val nextChapter = chapterItems
									.getOrNull(index + 1) // Attempt to get next chapter
									?: return@nextChapterTts2

								// Jump to the next chapter
								pageJumper.emit(readerUIItems.indexOf(nextChapter))
								viewModelScopeIO.launch {
									System.gc() // Clear out heavy operation (above)
									onViewed(nextChapter)
									setCurrentChapterID(nextChapter.id)
									// Start the TTS again
									withTimeoutOrNull(5.seconds) {
										if (
											ttsPlayback.firstOrNull { it == TTSPlayback.Stopped } != null
										) {
											onPlayTts()
										}
									}
								}
							}
						}
					}

					tts.collectLatest { tts ->
						if (tts == null) {
							oldTts?.stop() // ignore result
							oldTts = null
							@Suppress("LABEL_NAME_CLASH")
							return@collectLatest
						}
						oldTts?.stop() // ignore result
						oldTts = tts

						// Are we playing TTS?
						ttsPlayback.collectLatest { playback ->
							// if we are not playing, make sure the TTS is stopped
							if (playback != TTSPlayback.Playing) {
								tts.stop().let(::launchHandleTTSError)
								@Suppress("LABEL_NAME_CLASH")
								return@collectLatest
							}

							// child scope is killed off if the parent dies
							coroutineScope {
								syncTTSIterator(passage.ttsElements)
								// For each element, lets speak it out
								passage.ttsElements.forEachRemaining {
									if (!it.ignore)
										customSpeak(
											tts,
											it.text,
											it.id,
											::launchHandleTTSError
										)
								}
							}
						}
					}
				}
			}
		}
	}

	@Throws(NoSuchElementException::class)
	private fun syncTTSIterator(ttsElements: RewindableMutableListIterator<TTSText>) {
		val ttsState = ttsProgress.value

		// Recreate!
		ttsElements.recreate()

		// check if the tts was playing something
		if (ttsState != null) {
			logV("Attempting to sync TTS to $ttsState")
			// we were in fact playing something
			// we need to ensure we are at the right position
			var found = false

			while (ttsElements.hasNext()) {
				if (ttsElements.next().id == ttsState) {
					val currentIndex = ttsElements.nextIndex()
					try {
						ttsElements.previous() // make current next
					} catch (_: NoSuchElementException) {
						if (currentIndex == 0) {
							logD("We are at the first element, Recreating the iterator-")
						}
						ttsElements.recreate()
					}
					found = true
					break
				}
			}

			if (!found) {
				logE("Failed to syncc TTS to $ttsState")
				onStopTts()
			}
		}
	}

	override fun onPlayTts() {
		ttsPlayback.value = TTSPlayback.Playing
	}

	override fun onPauseTts() {
		ttsPlayback.value = TTSPlayback.Paused
	}

	override fun onStopTts() {
		ttsPlayback.value = TTSPlayback.Stopped
		ttsProgress.value = null
	}

	override val colorScheme: MutableStateFlow<ColorScheme> = MutableStateFlow(FallbackColorScheme)
	override val paddingValues: MutableStateFlow<PaddingValues> = MutableStateFlow(PaddingValues(0.dp))

	override fun onCleared() {
		tts.value?.stop()?.let(::launchHandleTTSError)
	}

	/**
	 * Performs [handleTTSError] on a new UI coroutine
	 *
	 * @param result The result code.
	 * @param genericMessage The generic message to display
	 */
	private fun launchHandleTTSError(
		result: Int,
		@StringRes genericMessage: Int = R.string.reader_error_tts_generic
	) {
		launchUI {
			handleTTSError(result, genericMessage)
		}
	}

	/**
	 * Run over returned result codes from TTS to display appropriate errors.
	 *
	 * @param result The result code.
	 * @param genericMessage The generic message to display
	 */
	private suspend fun handleTTSError(
		result: Int,
		@StringRes genericMessage: Int = R.string.reader_error_tts_generic
	) {
		when (result) {
			TextToSpeech.ERROR_SYNTHESIS -> exceptions.emit(ExceptionSnackbarModel(application.getString(R.string.reader_error_tts_synthesis)))
			TextToSpeech.ERROR_SERVICE -> exceptions.emit(ExceptionSnackbarModel(application.getString(R.string.reader_error_tts_service)))
			TextToSpeech.ERROR_OUTPUT -> exceptions.emit(ExceptionSnackbarModel(application.getString(R.string.reader_error_tts_output)))
			TextToSpeech.ERROR_NOT_INSTALLED_YET -> exceptions.emit(ExceptionSnackbarModel(application.getString(R.string.reader_error_tts_not_installed)))
			TextToSpeech.ERROR_NETWORK_TIMEOUT -> exceptions.emit(ExceptionSnackbarModel(application.getString(R.string.reader_error_network_timeout)))
			TextToSpeech.ERROR_NETWORK -> exceptions.emit(ExceptionSnackbarModel(application.getString(R.string.reader_error_tts_network)))
			TextToSpeech.ERROR_INVALID_REQUEST -> exceptions.emit(ExceptionSnackbarModel(application.getString(R.string.reader_error_tts_invalid_request)))
			TextToSpeech.ERROR -> exceptions.emit(ExceptionSnackbarModel(application.getString(genericMessage)))
		}
	}

	companion object {
		const val HTML_SIZE_DIVISION = 1.25
		const val INCREMENT_PERCENTAGE = 0.05
	}
}
