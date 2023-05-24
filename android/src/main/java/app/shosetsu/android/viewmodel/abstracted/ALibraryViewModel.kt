package app.shosetsu.android.viewmodel.abstracted

import androidx.compose.ui.state.ToggleableState
import app.shosetsu.android.common.enums.NovelCardType
import app.shosetsu.android.common.enums.NovelSortType
import app.shosetsu.android.view.uimodels.model.LibraryNovelUI
import app.shosetsu.android.view.uimodels.model.LibraryUI
import app.shosetsu.android.viewmodel.base.IsOnlineCheckViewModel
import app.shosetsu.android.viewmodel.base.ShosetsuViewModel
import app.shosetsu.android.viewmodel.base.StartUpdateManagerViewModel
import app.shosetsu.android.viewmodel.base.SubscribeViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow
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
 * 29 / 04 / 2020
 *
 * @author github.com/doomsdayrs
 */
abstract class ALibraryViewModel :
	SubscribeViewModel<LibraryUI?>,
	ShosetsuViewModel(),
	IsOnlineCheckViewModel,
	StartUpdateManagerViewModel {

	/**
	 * View state of if the category dialog is open or not.
	 *
	 * This is here due to legacy non-compose frameworks.
	 */
	abstract val isCategoryDialogOpen: StateFlow<Boolean>

	/**
	 * Show the category dialog
	 */
	abstract fun showCategoryDialog()

	/**
	 * Hide the category dialog
	 */
	abstract fun hideCategoryDialog()

	abstract val isEmptyFlow: StateFlow<Boolean>
	abstract val hasSelection: StateFlow<Boolean>

	/** All genres from all [LibraryNovelUI] combined*/
	abstract val genresFlow: Flow<ImmutableList<String>>

	/** All tags from all [LibraryNovelUI] combined*/
	abstract val tagsFlow: Flow<ImmutableList<String>>

	/** All authors from all [LibraryNovelUI] combined*/
	abstract val authorsFlow: Flow<ImmutableList<String>>

	/** All artists from all [LibraryNovelUI] combined*/
	abstract val artistsFlow: Flow<ImmutableList<String>>

	abstract val novelCardTypeFlow: StateFlow<NovelCardType>

	abstract val columnsInH: StateFlow<Int>
	abstract val columnsInV: StateFlow<Int>
	abstract val badgeUnreadToastFlow: StateFlow<Boolean>

	abstract fun cycleUnreadFilter(currentState: ToggleableState)
	abstract fun getUnreadFilter(): Flow<ToggleableState>

	abstract fun cycleDownloadedFilter(currentState: ToggleableState)
	abstract fun getDownloadedFilter(): Flow<ToggleableState>

	abstract fun getSortType(): Flow<NovelSortType>
	abstract fun setSortType(novelSortType: NovelSortType)

	abstract fun isSortReversed(): Flow<Boolean>
	abstract fun setIsSortReversed(reversed: Boolean)

	abstract fun isPinnedOnTop(): Flow<Boolean>
	abstract fun setPinnedOnTop(onTop: Boolean)

	abstract fun cycleFilterGenreState(genre: String, currentState: ToggleableState)
	abstract fun getFilterGenreState(name: String): Flow<ToggleableState>

	abstract fun cycleFilterAuthorState(author: String, currentState: ToggleableState)
	abstract fun getFilterAuthorState(name: String): Flow<ToggleableState>

	abstract fun cycleFilterArtistState(artist: String, currentState: ToggleableState)
	abstract fun getFilterArtistState(name: String): Flow<ToggleableState>

	abstract fun cycleFilterTagState(tag: String, currentState: ToggleableState)
	abstract fun getFilterTagState(name: String): Flow<ToggleableState>

	abstract fun resetSortAndFilters()
	abstract fun setViewType(cardType: NovelCardType)

	abstract fun setCategories(categories: IntArray)

	abstract fun removeSelectedFromLibrary()

	abstract fun getSelectedIds(): Flow<IntArray>
	abstract fun deselectAll()
	abstract fun selectAll()
	abstract fun invertSelection()
	abstract fun selectBetween()
	abstract fun toggleSelection(item: LibraryNovelUI)

	abstract val queryFlow: StateFlow<String>
	abstract fun setQuery(s: String)

	abstract val activeCategory: StateFlow<Int>
	abstract fun setActiveCategory(category: Int)

	/**
	 * Toggle pin of selected novels
	 */
	abstract fun togglePinSelected()

}