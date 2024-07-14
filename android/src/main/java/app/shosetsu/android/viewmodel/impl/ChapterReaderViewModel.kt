package app.shosetsu.android.viewmodel.impl

import android.content.Context
import android.database.sqlite.SQLiteException
import android.graphics.Color
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import app.shosetsu.android.R
import app.shosetsu.android.common.SettingKey
import app.shosetsu.android.common.SettingKey.ReaderDisableTextSelection
import app.shosetsu.android.common.SettingKey.ReaderDoubleTapFocus
import app.shosetsu.android.common.SettingKey.ReaderDoubleTapSystem
import app.shosetsu.android.common.SettingKey.ReaderEnableFullscreen
import app.shosetsu.android.common.SettingKey.ReaderEngine
import app.shosetsu.android.common.SettingKey.ReaderHorizontalPageSwap
import app.shosetsu.android.common.SettingKey.ReaderHtmlCss
import app.shosetsu.android.common.SettingKey.ReaderIndentSize
import app.shosetsu.android.common.SettingKey.ReaderIsFirstFocus
import app.shosetsu.android.common.SettingKey.ReaderIsInvertedSwipe
import app.shosetsu.android.common.SettingKey.ReaderIsTapToScroll
import app.shosetsu.android.common.SettingKey.ReaderKeepScreenOn
import app.shosetsu.android.common.SettingKey.ReaderLanguage
import app.shosetsu.android.common.SettingKey.ReaderMarkReadAsReading
import app.shosetsu.android.common.SettingKey.ReaderMatchFullscreenToFocus
import app.shosetsu.android.common.SettingKey.ReaderNextChapter
import app.shosetsu.android.common.SettingKey.ReaderParagraphSpacing
import app.shosetsu.android.common.SettingKey.ReaderPitch
import app.shosetsu.android.common.SettingKey.ReaderShowChapterDivider
import app.shosetsu.android.common.SettingKey.ReaderSpeed
import app.shosetsu.android.common.SettingKey.ReaderStringToHtml
import app.shosetsu.android.common.SettingKey.ReaderTableHack
import app.shosetsu.android.common.SettingKey.ReaderTextSize
import app.shosetsu.android.common.SettingKey.ReaderTheme
import app.shosetsu.android.common.SettingKey.ReaderTrackLongReading
import app.shosetsu.android.common.SettingKey.ReaderUserThemes
import app.shosetsu.android.common.SettingKey.ReaderVoice
import app.shosetsu.android.common.SettingKey.ReaderVolumeScroll
import app.shosetsu.android.common.SettingKey.ReadingMarkingType
import app.shosetsu.android.common.enums.AppThemes
import app.shosetsu.android.common.enums.MarkingType
import app.shosetsu.android.common.enums.MarkingType.ONSCROLL
import app.shosetsu.android.common.enums.MarkingType.ONVIEW
import app.shosetsu.android.common.enums.ReadingStatus.READ
import app.shosetsu.android.common.enums.ReadingStatus.READING
import app.shosetsu.android.common.ext.launchIO
import app.shosetsu.android.common.ext.logD
import app.shosetsu.android.common.ext.logE
import app.shosetsu.android.common.ext.logI
import app.shosetsu.android.common.ext.logV
import app.shosetsu.android.common.ext.onIO
import app.shosetsu.android.common.ext.toast
import app.shosetsu.android.common.utils.asHtml
import app.shosetsu.android.common.utils.copy
import app.shosetsu.android.domain.model.local.ColorChoiceData
import app.shosetsu.android.domain.repository.base.IChaptersRepository
import app.shosetsu.android.domain.repository.base.INovelReaderSettingsRepository
import app.shosetsu.android.domain.repository.base.INovelsRepository
import app.shosetsu.android.domain.repository.base.ISettingsRepository
import app.shosetsu.android.domain.usecases.RecordChapterIsReadUseCase
import app.shosetsu.android.domain.usecases.RecordChapterIsReadingUseCase
import app.shosetsu.android.domain.usecases.delete.DeleteChapterPassageUseCase
import app.shosetsu.android.domain.usecases.get.GetChapterPassageUseCase
import app.shosetsu.android.domain.usecases.get.GetChapterUIsUseCase
import app.shosetsu.android.domain.usecases.get.GetExtensionUseCase
import app.shosetsu.android.domain.usecases.get.GetLastReadChapterUseCase
import app.shosetsu.android.domain.usecases.get.GetReaderChaptersUseCase
import app.shosetsu.android.domain.usecases.get.GetReaderSettingUseCase
import app.shosetsu.android.domain.usecases.load.LoadDeletePreviousChapterUseCase
import app.shosetsu.android.domain.usecases.load.LoadLiveAppThemeUseCase
import app.shosetsu.android.ui.reader.customSpeak
import app.shosetsu.android.view.uimodels.model.NovelReaderSettingUI
import app.shosetsu.android.view.uimodels.model.reader.ReaderUIItem
import app.shosetsu.android.view.uimodels.model.reader.ReaderUIItem.ReaderChapterUI
import app.shosetsu.android.view.uimodels.model.reader.ReaderUIItem.ReaderDividerUI
import app.shosetsu.android.viewmodel.abstracted.AChapterReaderViewModel
import app.shosetsu.lib.IExtension
import app.shosetsu.lib.IExtension.Companion.KEY_CHAPTER_URL
import app.shosetsu.lib.Novel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CompletableDeferred
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
import kotlinx.coroutines.flow.collect
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
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import org.acra.ACRA
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.util.Locale
import java.util.UUID
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
	override val settingsRepo: ISettingsRepository,
	private val chapterRepository: IChaptersRepository,
	private val novelRepo: INovelsRepository,
	private val readerSettingsRepo: INovelReaderSettingsRepository,
	private var loadLiveAppThemeUseCase: LoadLiveAppThemeUseCase,
	private val loadReaderChaptersUseCase: GetReaderChaptersUseCase,
	private val loadChapterPassageUseCase: GetChapterPassageUseCase,
	private val getReaderSettingsUseCase: GetReaderSettingUseCase,
	private val recordChapterIsReading: RecordChapterIsReadingUseCase,
	private val recordChapterIsRead: RecordChapterIsReadUseCase,
	private val getChapters: GetChapterUIsUseCase,
	private val getExt: GetExtensionUseCase,
	private val getLastReadChapter: GetLastReadChapterUseCase,
	private val loadDeletePreviousChapterUseCase: LoadDeletePreviousChapterUseCase,
	private val deleteChapterPassageUseCase: DeleteChapterPassageUseCase,
) : AChapterReaderViewModel() {

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

	override val appThemeLiveData: SharedFlow<AppThemes> by lazy {
		loadLiveAppThemeUseCase()
			.onIO()
			.shareIn(viewModelScopeIO, SharingStarted.Lazily, replay = 1)
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

	private val tableHackEnabledFlow: Flow<Boolean> by lazy {
		settingsRepo.getBooleanFlow(ReaderTableHack)
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
	private val refreshMap = HashMap<Int, MutableStateFlow<Boolean>>()

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
		refreshMap.getOrPut(item.id) { MutableStateFlow(false) }

	override fun retryChapter(item: ReaderChapterUI) {
		//logV("$item")
		val flow = getRefreshFlow(item)
		flow.value = !flow.value
	}

	private var cleanStringMapJob: Job? = null

	override fun getChapterStringPassage(item: ReaderChapterUI): Flow<ChapterPassage> {
		//logV("$item")
		val mutableFlow = stringMap.getOrPut(item.id) {
			getRefreshFlow(item)
				.transformLatest {
					emit(ChapterPassage.Loading)
					val bytes = getChapterPassage(item)
						?: throw Exception("No content received")

					emitAll(
						indentSizeFlow.combine(
							paragraphSpacingFlow
						) { indentSize, paragraphSpacing ->
							val unformattedText = bytes.decodeToString()

							val replaceSpacing = StringBuilder("\n")
							// Calculate changes to \n
							for (x in 0 until paragraphSpacing.toInt())
								replaceSpacing.append("\n")

							// Calculate changes to \t
							for (x in 0 until indentSize)
								replaceSpacing.append("\t")

							// Set new text formatted
							ChapterPassage.Success(
								unformattedText.replace(
									"\n".toRegex(),
									replaceSpacing.toString()
								),
								listOf(
									TTSText(
										UUID.randomUUID().toString(),
										unformattedText,
									)
								)
							)
						}
					)
				}
				.catch { emit(ChapterPassage.Error(it)) }
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

		return mutableFlow
	}

	override fun getChapterHTMLPassage(item: ReaderChapterUI): Flow<ChapterPassage> {
		val mutableFlow = stringMap.getOrPut(item.id) {
			getRefreshFlow(item)
				.transformLatest {
					emit(ChapterPassage.Loading)
					val bytes = getChapterPassage(item)
						?: throw Exception("No content received")

					var result = bytes.decodeToString()

					@Suppress("DEPRECATION")
					val convert = convertStringToHtml.firstOrNull() ?: false
					val chapterType = extensionChapterTypeFlow.firstOrNull()

					if (chapterType == Novel.ChapterType.STRING && convert) {
						result = asHtml(result, item.title)
					}

					val document = Jsoup.parse(result)

					val textElements = document.body().select("*:not(:has(*))")

					val textItems = mutableListOf<TTSText>()
					textElements.forEach { element ->
						var actualElement = element
						var parent = element.parent()
						do {
							if (!parent?.ownText().isNullOrEmpty()) {
								actualElement = parent!!
							}
							parent = actualElement.parent()
						} while (!parent?.ownText().isNullOrEmpty())

						val text = actualElement.wholeText().trim()
						if (text.isNotEmpty()) {
							val uuid = UUID.randomUUID()
							actualElement.attr("id", "textElement$uuid")
							textItems.add(TTSText(uuid.toString(), text))
						}
					}

					emitAll(
						shosetsuCss.combine(userCssFlow) { shoCSS, useCSS ->
							fun update(id: String, css: String) {
								var style: Element? = document.getElementById(id)

								if (style == null) {
									style =
										document.createElement("style") ?: return

									style.id(id)
									style.attr("type", "text/css")

									document.head().appendChild(style)
								}

								style.text(css)
							}

							update("shosetsu-style", shoCSS)
							update("user-style", useCSS)

							ChapterPassage.Success(
								document.toString(),
								textItems.toList()
							)
						}
					)
				}
				.catch { emit(ChapterPassage.Error(it)) }
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

		return mutableFlow
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

	private val convertStringToHtml by lazy {
		settingsRepo.getBooleanFlow(ReaderStringToHtml)
	}

	private val extensionChapterTypeFlow: SharedFlow<Novel.ChapterType?> by lazy {
		extFlow.map { it?.chapterType }
			.onIO()
			.shareIn(viewModelScopeIO, SharingStarted.Lazily, 1)
	}

	/**
	 * Specifies what chapter type the reader should render.
	 *
	 * Upon [ReaderStringToHtml] being true, will clear out any previous strings if the prevType was
	 * not html, causing the content to regenerate.
	 */
	override val chapterType: StateFlow<Novel.ChapterType?> by lazy {
		extensionChapterTypeFlow.filterNotNull().flatMapLatest { type ->
			var prevType: Novel.ChapterType? = null

			convertStringToHtml.mapLatest { convert ->
				@Suppress("DEPRECATION")
				if (convert && type == Novel.ChapterType.STRING) {
					if (prevType != Novel.ChapterType.HTML)
						clearMaps()

					prevType = Novel.ChapterType.HTML
					Novel.ChapterType.HTML
				} else {
					if (prevType != type)
						clearMaps()

					prevType = type
					type
				}
			}
		}.onIO().stateIn(viewModelScopeIO, SharingStarted.Lazily, null)
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

	private val themeFlow: StateFlow<Pair<Int, Int>> by lazy {
		settingsRepo.getIntFlow(ReaderTheme).mapLatest { id: Int ->
			settingsRepo.getStringSet(ReaderUserThemes)
				.map { ColorChoiceData.fromString(it) }
				.find { it.identifier == id.toLong() }
				?.let { (_, _, textColor, backgroundColor) ->
					(textColor to backgroundColor)
				} ?: (Color.BLACK to Color.WHITE)
		}.onIO().stateIn(viewModelScopeIO, SharingStarted.Lazily, Color.BLACK to Color.WHITE)
	}

	override val textColor: StateFlow<Int> by lazy {
		themeFlow.map { it.first }.onIO()
			.stateIn(viewModelScopeIO, SharingStarted.Lazily, themeFlow.value.first)
	}

	override val backgroundColor: StateFlow<Int> by lazy {
		themeFlow.map { it.second }.onIO()
			.stateIn(viewModelScopeIO, SharingStarted.Lazily, themeFlow.value.second)
	}

	override val liveTextSize: StateFlow<Float> by lazy {
		settingsRepo.getFloatFlow(ReaderTextSize)
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
			recordChapterIsRead(chapter)
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
		}
	}

	private val readingMarkingTypeFlow by lazy {
		settingsRepo.getStringFlow(ReadingMarkingType).map {
			MarkingType.valueOf(it)
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

				recordChapterIsReading(chapter)

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
						recordChapterIsReading(chapter)
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

				recordChapterIsRead(chapter)

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

	override val chapterHistory: MutableStateFlow<ImmutableList<ReaderChapterUI>> =
		MutableStateFlow(persistentListOf())

	private val mutex = Mutex()
	override fun popHistory() {
		viewModelScopeIO.launch {
			mutex.withLock {
				this@ChapterReaderViewModel.logD(chapterHistory.value.toString())
				val items = liveData.first { it != null } ?: return@launch
				val history = chapterHistory.value
				if (history.size >= 2) {
					val chapter = history[history.lastIndex - 1]
					this@ChapterReaderViewModel.logD(chapter.toString())
					chapterHistory.value = chapterHistory.value.dropLast(1).toImmutableList()
					pageJumper.emit(items.indexOf(chapter))
				}
			}
		}
	}

	override val pageJumper: MutableSharedFlow<Int> = MutableSharedFlow(replay = 0)
	override suspend fun jumpToChapter(url: String): Boolean = onIO {
		val chapters = getChapters(novelIDLive.value).first()
			.map { it.copy(link = it.link.removeSuffix("/")) }
		val ext = extFlow.first() ?: return@onIO false

		val shrunkUrl = ext.shrinkURL(url, KEY_CHAPTER_URL).removeSuffix("/")
		val noAnchorUrl = shrunkUrl.substringBefore('#')
		val chapterId =  chapters.find {
			it.link == shrunkUrl || it.link == noAnchorUrl
		}?.id ?: return@onIO false

		val items = liveData.first { it != null } ?: return@onIO false
		val newChapter =  items
			.find { it is ReaderChapterUI && it.id == chapterId } as? ReaderChapterUI
			?: return@onIO false
		chapterHistory.value = chapterHistory.value.plus(newChapter).toImmutableList()

		pageJumper.emit(items.indexOf(newChapter))
		true
	}

	override fun loadChapterCss(): Flow<String> =
		settingsRepo.getStringFlow(ReaderHtmlCss)

	override fun updateSetting(novelReaderSettingEntity: NovelReaderSettingUI) {
		launchIO {
			readerSettingsRepo.update(novelReaderSettingEntity.convertTo())
		}
	}

	override fun getSettings(): StateFlow<NovelReaderSettingUI> = readerSettingsFlow

	override val tapToScroll: StateFlow<Boolean> by lazy {
		settingsRepo.getBooleanFlow(ReaderIsTapToScroll)
	}

	override val disableTextSelection: StateFlow<Boolean> by lazy {
		settingsRepo.getBooleanFlow(ReaderDisableTextSelection)
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
		if (item != null && ttsPlayback.value == TtsPlayback.Paused) {
			ttsProgress.value = item.substringAfter("textElement")
			ttsPlayback.value = TtsPlayback.Playing
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

	data class ShosetsuCSSBuilder(
		val backgroundColor: Int = Color.WHITE,
		val foregroundColor: Int = Color.BLACK,
		val textSize: Float = ReaderTextSize.default,
		val indentSize: Int = ReaderIndentSize.default,
		val paragraphSpacing: Float = ReaderParagraphSpacing.default,
		val tableHackEnabled: Boolean = ReaderTableHack.default,
		val disableTextSelection: Boolean = ReaderDisableTextSelection.default
	)

	private val shosetsuCss: Flow<String> by lazy {
		themeFlow.combine(liveTextSize) { (fore, back), textSize ->
			ShosetsuCSSBuilder(
				backgroundColor = back,
				foregroundColor = fore,
				textSize = textSize
			)
		}.combine(indentSizeFlow) { builder, indent ->
			builder.copy(
				indentSize = indent
			)
		}.combine(paragraphSpacingFlow) { builder, space ->
			builder.copy(
				paragraphSpacing = space
			)
		}.combine(tableHackEnabledFlow) { builder, enabled ->
			builder.copy(
				tableHackEnabled = enabled
			)
		}.combine(disableTextSelection) { builder, enabled ->
			builder.copy(
				disableTextSelection = enabled
			)
		}.map {
			val shosetsuStyle: HashMap<String, HashMap<String, String>> = hashMapOf()

			fun setShosetsuStyle(elem: String, action: HashMap<String, String>.() -> Unit) =
				shosetsuStyle.getOrPut(elem) { hashMapOf() }.apply(action)

			fun Int.cssColor(): String = "rgb($red,$green,$blue)"

			if (it.disableTextSelection) {
				setShosetsuStyle("*") {
					this["-webkit-user-select"] = "none"
					this["user-select"] = "none"
				}
			}

			setShosetsuStyle("body") {
				this["background-color"] = it.backgroundColor.cssColor()
				this["color"] = it.foregroundColor.cssColor()
				this["font-size"] = "${it.textSize / HTML_SIZE_DIVISION}pt"
				this["scroll-behavior"] = "smooth"
				this["text-indent"] = "${it.indentSize}em"
				this["overflow-wrap"] = "break-word"
				this["padding"] = "0.5em" // ensure everything stays away from the edge
			}

			setShosetsuStyle("p") {
				this["margin-top"] = "${it.paragraphSpacing}em"
			}

			setShosetsuStyle("img") {
				this["max-width"] = "100%"
				this["height"] = "initial !important"
			}

			setShosetsuStyle(".tts-border-style") {
				this["border"] = "2px solid red"
			}

			if (it.tableHackEnabled)
				setShosetsuStyle("table") {
					this["overflow-x"] = "auto"
					this["display"] = "block"
					this["white-space"] = "nowrap"
				}

			shosetsuStyle.map { elem ->
				"${elem.key} {" + elem.value.map { rule -> "${rule.key}:${rule.value}" }
					.joinToString(";", postfix = ";") + "}"
			}.joinToString("")
		}.onIO()
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
				val selectedChapter =  items
					.find { it is ReaderChapterUI && it.id == chapterId } as ReaderChapterUI
				if (chapterHistory.value.isEmpty()) {
					chapterHistory.value = persistentListOf(selectedChapter)
				}
				currentPage.value = items.indexOf(selectedChapter)
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

	override val ttsProgress = MutableStateFlow<String?>(null)
	val ttsDone = MutableStateFlow<String?>(null)
	override val ttsPlayback = MutableStateFlow<TtsPlayback>(TtsPlayback.Stopped)
	data class TTSBuilder(
		val context: Context,
		val engine: String,
		val language: String,
		val voice: String,
	)
	private val context = MutableStateFlow<Context?>(null)
	private val tts = ttsEngine.combine(context) { engine, context ->
		context ?: return@combine null
		TTSBuilder(context, engine, "", "")
	}.filterNotNull().combine(ttsLanguage) { builder, language ->
		builder.copy(language = language)
	}.combine(ttsVoice) { builder, voice ->
		builder.copy(voice = voice)
	}.map { builder ->
		val ttsResult = CompletableDeferred<Int>()
		val tts = if (builder.engine.isEmpty()) {
			TextToSpeech(builder.context) { ttsResult.complete(it) }
		} else {
			TextToSpeech(builder.context, { ttsResult.complete(it) }, builder.engine)
		}
		when (ttsResult.await()) {
			TextToSpeech.SUCCESS -> tts to builder
			else -> {
				builder.context.toast(R.string.reader_test_invalid_engine)
				null
			}
		}
	}.filterNotNull()
		.filter { (tts, builder) ->
			val languageSuccess: Boolean
			val locale: Locale
			if (builder.language.isEmpty()) {
				locale = Locale.getDefault()
				val result = tts.setLanguage(Locale.getDefault())
				languageSuccess = when (result) {
					TextToSpeech.LANG_AVAILABLE -> true
					TextToSpeech.LANG_COUNTRY_AVAILABLE -> true
					TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE -> true
					else -> false
				}
			} else {
				val ttsLocale = tts.availableLanguages.find { it.toLanguageTag() == builder.language }
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
					locale = Locale.getDefault()
					languageSuccess = false
				}
			}
			if (!languageSuccess) {
				builder.context.toast(R.string.reader_test_invalid_language)
				return@filter false
			}
			val voiceSuccess: Boolean
			if (builder.voice.isNotEmpty()) {
				val ttsVoice = tts.voices.filter { it.locale == locale }
					.find { it.name == builder.voice }
				if (ttsVoice != null) {
					val result = tts.setVoice(ttsVoice)
					voiceSuccess = when (result) {
						TextToSpeech.SUCCESS -> true
						else -> false
					}
				} else {
					voiceSuccess = false
				}
			} else {
				voiceSuccess = tts.defaultVoice != null
			}
			if (!voiceSuccess) {
				builder.context.toast(R.string.reader_test_invalid_voice)
				return@filter false
			}
			true
		}
		.combine(
			ttsPitch.combine(ttsSpeed) { a, b -> a to b }
				.distinctUntilChanged()
		) { (tts, _), (pitch, speed) ->
			tts.setPitch(pitch / 10)
			tts.setSpeechRate(speed / 10)
			tts
		}
		.distinctUntilChanged()
		.onEach {
			it.setOnUtteranceProgressListener(
				object : UtteranceProgressListener() {
					override fun onStart(utteranceId: String?) {
						if (ttsPlayback.value != TtsPlayback.Stopped) {
							ttsProgress.value = utteranceId?.substringBefore('|')
						}
					}

					override fun onDone(utteranceId: String?) {
						ttsDone.value = utteranceId
					}
					@Deprecated("Deprecated in Java")
					override fun onError(utteranceId: String?) {}
					override fun onError(utteranceId: String?, errorCode: Int) {
						this@ChapterReaderViewModel.logE("TTS Error code: $errorCode")
						ttsPlayback.value = TtsPlayback.Paused
					}
				}
			)
		}
		.stateIn(viewModelScopeIO, SharingStarted.Eagerly, null)

	init {
		viewModelScopeIO.launch {
			var oldTts: TextToSpeech? = null
			currentChapterID.collectLatest { chapterId ->
				coroutineScope {
					oldTts?.stop()
					oldTts = null
					val chapters = liveData.first { it != null }
					val item = chapters
						?.find { (it as? ReaderChapterUI)?.id == chapterId }
							as? ReaderChapterUI ?: return@coroutineScope

					val passage = when (chapterType.first { it != null }) {
						null -> return@coroutineScope
						Novel.ChapterType.HTML -> getChapterHTMLPassage(item)
						Novel.ChapterType.STRING -> getChapterStringPassage(item)
					}.firstOrNull { it is ChapterPassage.Success } as? ChapterPassage.Success
						?: return@coroutineScope

					launch nextChapterTts@{
						val lastTts = passage.ttsElements.lastOrNull() ?: return@nextChapterTts
						ttsNextChapter.collectLatest nextChapterTts2@{
							if (!it) {
								return@nextChapterTts2
							}
							ttsDone.collectLatest nextChapterTts3@{ id ->
								if (id != null && id == lastTts.id) {
									val index = chapters.indexOfFirst {
										(it as? ReaderChapterUI)?.id == chapterId
									}
									if (index > 0) {
										val nextChapter = chapters.getOrNull(index + 2)
											as? ReaderChapterUI
											?: return@nextChapterTts3

										pageJumper.emit(chapters.indexOf(nextChapter))
										viewModelScopeIO.launch {
											onViewed(nextChapter)
											setCurrentChapterID(nextChapter.id)
											withTimeoutOrNull(5.seconds) {
												if (
													ttsPlayback.firstOrNull { it == TtsPlayback.Stopped } != null
												) {
													onPlayTts(
														context.value ?: return@withTimeoutOrNull
													)
												}
											}
										}
									}
								}
							}
						}
					}

					tts.collectLatest { tts ->
						if (tts == null) {
							oldTts?.stop()
							oldTts = null
							@Suppress("LABEL_NAME_CLASH")
							return@collectLatest
						}
						oldTts?.stop()
						oldTts = tts
						ttsPlayback.collectLatest { playback ->
							if (playback != TtsPlayback.Playing) {
								tts.stop()
								@Suppress("LABEL_NAME_CLASH")
								return@collectLatest
							}
							coroutineScope {
								var ttsElements = passage.ttsElements
								val ttsState = ttsProgress.value
								if (ttsState != null) {
									val index = ttsElements.indexOfFirst { it.id == ttsState }
									if (index >= 0) {
										ttsElements = ttsElements.drop(index)
									}
								}
								ttsElements.forEach {
									customSpeak(
										tts,
										it.text,
										it.id
									)
								}
							}
						}
					}
				}
			}
		}
	}

	override fun onPlayTts(context: Context) {
		this.context.value = context.applicationContext
		ttsPlayback.value = TtsPlayback.Playing
	}

	override fun onPauseTts() {
		ttsPlayback.value = TtsPlayback.Paused
	}

	override fun onStopTts() {
		ttsPlayback.value = TtsPlayback.Stopped
		ttsProgress.value = null
	}

	override fun onCleared() {
		tts.value?.stop()
	}

	companion object {
		const val HTML_SIZE_DIVISION = 1.25
		const val INCREMENT_PERCENTAGE = 0.05
	}
}