package app.shosetsu.android.viewmodel.abstracted

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.ImageBitmap
import app.shosetsu.android.common.enums.ReadingStatus
import app.shosetsu.android.view.uimodels.NovelSettingUI
import app.shosetsu.android.view.uimodels.model.CategoryUI
import app.shosetsu.android.view.uimodels.model.ChapterUI
import app.shosetsu.android.view.uimodels.model.NovelUI
import app.shosetsu.android.viewmodel.base.IsOnlineCheckViewModel
import app.shosetsu.android.viewmodel.base.ShosetsuViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import javax.security.auth.Destroyable

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
 * 29 / 04 / 2020
 *
 * @author github.com/doomsdayrs
 */
abstract class ANovelViewModel
	: ShosetsuViewModel(), IsOnlineCheckViewModel, Destroyable {

	abstract val hasSelected: StateFlow<Boolean>
	abstract fun clearSelection()

	abstract val itemIndex: StateFlow<Int>
	abstract fun setItemAt(index: Int)

	abstract val isRefreshing: StateFlow<Boolean>

	abstract val novelLive: StateFlow<NovelUI?>
	abstract val chaptersLive: StateFlow<ImmutableList<ChapterUI>>
	abstract val selectedChaptersState: StateFlow<SelectedChaptersState>

	abstract val otherException: StateFlow<Throwable?>
	abstract val novelException: StateFlow<Throwable?>
	abstract val chaptersException: StateFlow<Throwable?>

	abstract val novelSettingFlow: SharedFlow<NovelSettingUI?>

	abstract val categories: StateFlow<ImmutableList<CategoryUI>>
	abstract val novelCategories: StateFlow<ImmutableList<Int>>

	abstract val isCategoriesDialogVisible: StateFlow<Boolean>

	abstract val toggleBookmarkResponse: StateFlow<ToggleBookmarkResponse>

	abstract val isChapterJumpDialogVisible: StateFlow<Boolean>

	enum class JumpState { UNKNOWN, FAILURE }

	abstract val jumpState: StateFlow<JumpState>

	abstract fun showChapterJumpDialog()

	abstract fun hideChapterJumpDialog()

	abstract fun showCategoriesDialog()

	abstract fun hideCategoriesDialog()

	/** Set's the value to be loaded */
	abstract fun setNovelID(novelID: Int)

	/**
	 * Set the categories of the novel
	 */
	abstract fun setNovelCategories(categories: IntArray): Unit

	/**
	 * Toggles the bookmark of this ui
	 * @return ToggleBookmarkResponse of what the UI should react with
	 */
	abstract fun toggleNovelBookmark()

	/**
	 * Response to toggling the novel bookmark
	 */
	sealed class ToggleBookmarkResponse {
		/**
		 * UI can ignore response
		 */
		object Nothing : ToggleBookmarkResponse()

		/**
		 * The user should be informed that chapters can be deleted
		 * @param chapters how many chapters to delete
		 */
		data class DeleteChapters(val chapters: Int) : ToggleBookmarkResponse()
		// TODO Possibly warn if a matching novel is in the library or not
	}

	/**
	 * Return the novelURL to utilize in some way
	 */
	abstract val novelURL: StateFlow<String?>

	data class NovelShareInfo(
		val novelTitle: String,
		val novelURL: String
	)

	abstract fun getShareInfo(): Flow<NovelShareInfo?>

	/**
	 * Return the chapterURL to utilize in some way
	 */
	abstract fun getChapterURL(chapterUI: ChapterUI): Flow<String?>

	/**
	 * Will return the next chapter to read & scroll to said chapter
	 *
	 * @return Next chapter to read uwu
	 */
	abstract fun openLastRead(): Flow<ChapterUI?>

	/** Refresh media */
	abstract fun refresh(): Flow<Unit>

	/**
	 * Is the novel bookmarked?
	 */
	abstract fun isBookmarked(): Flow<Boolean>

	/** Download the next unread chapters */
	abstract fun downloadNextChapter()

	/** Download the next 5 unread chapters */
	abstract fun downloadNext5Chapters()

	/** Download the next 10 unread chapters */
	abstract fun downloadNext10Chapters()

	/** Download the next [max] unread chapters */
	abstract fun downloadNextCustomChapters(max: Int)

	/** Download all unread chapters */
	abstract fun downloadAllUnreadChapters()

	/** Download all chapters */
	abstract fun downloadAllChapters()

	abstract fun updateNovelSetting(novelSettingUI: NovelSettingUI)

	abstract fun getIfAllowTrueDelete(): Flow<Boolean>

	abstract fun getQRCode(): Flow<ImageBitmap?>

	abstract fun bookmarkSelected()
	abstract fun removeBookmarkFromSelected()

	abstract fun selectAll()

	abstract fun invertSelection()
	abstract fun downloadSelected()
	abstract fun deleteSelected()
	abstract fun markSelectedAs(readingStatus: ReadingStatus)
	abstract fun selectBetween()

	abstract fun trueDeleteSelected()

	abstract fun toggleSelection(it: ChapterUI)

	/**
	 * Get the count of chapters as declared from [chaptersLive]
	 */
	abstract fun getChapterCount(): Int

	/**
	 * Delete downloaded chapters
	 */
	abstract fun deleteChapters()
	abstract fun jump(query: String, byTitle: Boolean)

	/**
	 * @param showRemoveBookmark If any chapters are bookmarked, show the remove bookmark logo
	 * @param showBookmark If any chapters are not bookmarked, show bookmark
	 * @param showDelete  If any are downloaded, show delete
	 * @param showDownload  If any are not downloaded, show download option
	 * @param showMarkAsRead If any are unread, show read option
	 * @param showMarkAsUnread If any are read, show unread option
	 */
	@Immutable
	data class SelectedChaptersState(
		val showRemoveBookmark: Boolean = false,
		val showBookmark: Boolean = false,
		val showDelete: Boolean = false,
		val showDownload: Boolean = false,
		val showMarkAsRead: Boolean = false,
		val showMarkAsUnread: Boolean = false
	)
}