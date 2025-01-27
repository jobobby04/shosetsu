package app.shosetsu.android.viewmodel.abstracted

import android.content.Context
import androidx.lifecycle.LiveData
import app.shosetsu.android.common.enums.AppThemes
import app.shosetsu.android.view.uimodels.model.NovelReaderSettingUI
import app.shosetsu.android.view.uimodels.model.reader.ReaderUIItem
import app.shosetsu.android.view.uimodels.model.reader.ReaderUIItem.ReaderChapterUI
import app.shosetsu.android.viewmodel.base.ExposedSettingsRepoViewModel
import app.shosetsu.android.viewmodel.base.ShosetsuViewModel
import app.shosetsu.android.viewmodel.base.SubscribeViewModel
import app.shosetsu.lib.Novel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

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
 */
abstract class AChapterReaderViewModel :
	SubscribeViewModel<ImmutableList<ReaderUIItem>?>,
	ShosetsuViewModel(),
	ExposedSettingsRepoViewModel {

	/**
	 * Has the user been reading for too long?
	 *
	 * If so, then the user will be notified
	 */
	abstract val isReadingTooLong: StateFlow<Boolean>

	/**
	 * Whether or not to track if the user is reading too long or not
	 */
	abstract val trackLongReading: StateFlow<Boolean>

	/**
	 * Called when the user reads for too long
	 */
	abstract fun userIsReadingTooLong()

	/**
	 * Dismiss reading for too long
	 */
	abstract fun dismissReadingTooLong()

	abstract val appThemeLiveData: SharedFlow<AppThemes>

	abstract fun retryChapter(item: ReaderChapterUI)

	class TTSText(val id: String, val text: String)
	sealed class ChapterPassage {
		data object Loading : ChapterPassage()
		data class Error(val throwable: Throwable?) : ChapterPassage()
		data class Success(val content: String, val ttsElements: List<TTSText>) : ChapterPassage()
	}

	abstract fun getChapterStringPassage(item: ReaderChapterUI): Flow<ChapterPassage>

	abstract fun getChapterHTMLPassage(item: ReaderChapterUI): Flow<ChapterPassage>

	abstract fun setCurrentPage(page: Int)

	abstract val isFirstFocusFlow: StateFlow<Boolean>
	abstract val isSwipeInverted: StateFlow<Boolean>

	abstract fun getChapterProgress(chapter: ReaderChapterUI): Flow<Double>

	abstract fun onFirstFocus()

	abstract val currentPage: StateFlow<Int?>

	abstract val isCurrentChapterBookmarked: StateFlow<Boolean>

	abstract val chapterType: StateFlow<Novel.ChapterType?>

	abstract val ttsSpeed: StateFlow<Float>
	abstract val ttsPitch: StateFlow<Float>

	abstract val ttsLanguage: StateFlow<String>
	abstract val ttsEngine: StateFlow<String>
	abstract val ttsVoice: StateFlow<String>

	/**
	 * Is tap to scroll enabled
	 */
	abstract val tapToScroll: StateFlow<Boolean>

	/**
	 * Is text selection disabled?
	 */
	abstract val disableTextSelection: StateFlow<Boolean>

	/**
	 * Double tap required to focus/unfocus the reader
	 */
	abstract val isFocused: StateFlow<Boolean>
	abstract val isSystemVisible: StateFlow<Boolean>
	abstract val enableFullscreen: StateFlow<Boolean>
	abstract val matchFullscreenToFocus: StateFlow<Boolean>

	abstract fun toggleFocus()
	abstract fun toggleSystemVisible()

	abstract fun onReaderClicked(item: String?)
	abstract fun onReaderDoubleClicked()

	/**
	 * Should the screen be locked
	 */
	abstract val liveIsScreenRotationLocked: StateFlow<Boolean>

	/**
	 * Should the reader keep the screen on
	 */
	abstract val liveKeepScreenOn: StateFlow<Boolean>

	/**
	 * The current chapter ID that is being read
	 */
	abstract val currentChapterID: StateFlow<Int>

	abstract val textColor: StateFlow<Int>
	abstract val backgroundColor: StateFlow<Int>

	abstract val liveTextSize: StateFlow<Float>


	/**
	 * false    -> vertical paging
	 * true     -> horizontal paging
	 */
	abstract val isHorizontalReading: StateFlow<Boolean>

	/**
	 * The state that should be used by default for newly created views
	 * This also is the way to easily get current state without async calls
	 */
	abstract val isVolumeScrollEnabled: StateFlow<Boolean>

	/** Set the novelID */
	abstract fun setNovelID(novelID: Int)

	/**
	 * Toggle the bookmark of the current chapter
	 */
	abstract fun toggleBookmark()

	/** Update [chapter] as Read, this will also clear all reading progress */
	abstract fun updateChapterAsRead(chapter: ReaderChapterUI)

	/** Called when a [chapter] is viewed by the user */
	abstract fun onViewed(chapter: ReaderChapterUI)

	/**
	 * Called when a [chapter] is scrolled
	 */
	abstract fun onScroll(chapter: ReaderChapterUI, readingPosition: Double)

	abstract val chapterHistory: StateFlow<ImmutableList<ReaderChapterUI>>
	abstract fun popHistory()

	abstract val pageJumper: SharedFlow<Int>
	abstract suspend fun jumpToChapter(url: String): Boolean

	/**
	 * Loads a [LiveData] reflection of the global custom css
	 */
	abstract fun loadChapterCss(): Flow<String>

	/**
	 * Loads the settings list for the bottom bar
	 */
	abstract fun getSettings(): StateFlow<NovelReaderSettingUI>

	abstract fun updateSetting(novelReaderSettingEntity: NovelReaderSettingUI)

	/**
	 * Toggle the screen lock state
	 */
	abstract fun toggleScreenRotationLock()

	abstract fun setCurrentChapterID(chapterId: Int, initial: Boolean = false)
	abstract fun incrementProgress()
	abstract fun depleteProgress()

	abstract fun clearMemory()

	sealed class TtsPlayback {
		data object Playing : TtsPlayback()
		data object Paused : TtsPlayback()
		data object Stopped : TtsPlayback()
	}
	abstract val ttsProgress: StateFlow<String?>
	abstract val ttsPlayback: StateFlow<TtsPlayback>
	abstract fun onPlayTts(context: Context)
	abstract fun onPauseTts()
	abstract fun onStopTts()
}