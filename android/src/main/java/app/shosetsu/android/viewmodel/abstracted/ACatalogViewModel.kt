package app.shosetsu.android.viewmodel.abstracted

import androidx.paging.PagingData
import app.shosetsu.android.common.enums.NovelCardType
import app.shosetsu.android.view.uimodels.ListingSelectionData
import app.shosetsu.android.view.uimodels.StableHolder
import app.shosetsu.android.view.uimodels.model.CategoryUI
import app.shosetsu.android.view.uimodels.model.catlog.ACatalogNovelUI
import app.shosetsu.android.viewmodel.base.ShosetsuViewModel
import app.shosetsu.lib.Filter
import javax.security.auth.Destroyable
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
 * 01 / 05 / 2020
 * Used for showing the specific listing of a novel
 */
abstract class ACatalogViewModel :
	ShosetsuViewModel(),
	Destroyable {

	/**
	 * What is currently being displayed to the user
	 */
	abstract val itemsLive: Flow<PagingData<ACatalogNovelUI>>

	/**
	 * Any exceptions collected internally to be shown to the user
	 */
	abstract val exceptionFlow: Flow<Throwable>

	/**
	 * The list of items that will be presented as the filter menu
	 */
	abstract val filterItemsLive: StateFlow<ImmutableList<StableHolder<Filter<*>>>>

	/**
	 * If this extension has filters or not.
	 *
	 * Controls if the filter button is visible or not
	 */
	abstract val hasFilters: StateFlow<Boolean>

	/**
	 * enable or disable searching
	 */
	abstract val hasSearchLive: StateFlow<Boolean>

	/**
	 * Name of the extension that is used for its catalogue
	 */
	abstract val extensionName: StateFlow<String>

	/**
	 * Provides the selection data for which listing is shown
	 */
	abstract val listingSelectionData: StateFlow<ListingSelectionData?>

	/**
	 * What type of card to display
	 */
	abstract val novelCardTypeLive: StateFlow<NovelCardType>

	/**
	 * If images are to be shown or not
	 */
	abstract val showImages: StateFlow<Boolean>

	/**
	 * How many columns horizontally
	 */
	abstract val columnsInH: StateFlow<Int>

	/**
	 * How many columns vertically
	 */
	abstract val columnsInV: StateFlow<Int>

	/**
	 * The categories available to add a novel to
	 */
	abstract val categories: StateFlow<ImmutableList<CategoryUI>>

	/**
	 * Sets the extension to load.
	 *
	 * This will reset the view completely when called.
	 *
	 * @param extensionID The id of the extension.
	 */
	abstract fun setExtensionID(extensionID: Int)

	/**
	 * Apply a query.
	 *
	 * This will reload the view.
	 *
	 * @param newQuery The new query to load.
	 */
	abstract fun applyQuery(newQuery: String)

	/**
	 * Resets the view back to what it was when it first opened.
	 */
	abstract fun resetView()

	/**
	 * Bookmarks and loads the specific novel in the background.
	 *
	 * @param item ID of novel to load.
	 * @param categories The categories to add the novel to.
	 */
	abstract fun backgroundNovelAdd(item: ACatalogNovelUI, categories: IntArray = intArrayOf())

	/**
	 * The current state of adding a novel in the background.
	 */
	abstract val backgroundAddState: StateFlow<BackgroundNovelAddProgress>

	/**
	 * Represents the state of adding a novel in the background.
	 */
	sealed class BackgroundNovelAddProgress {
		/**
		 * Default state / Unknown state
		 */
		object Unknown : BackgroundNovelAddProgress()

		/**
		 * When a novel is being added...
		 */
		object Adding : BackgroundNovelAddProgress()

		/**
		 * The novel has been added.
		 *
		 * @param title The title of the novel that has been added.
		 */
		class Added(val title: String) : BackgroundNovelAddProgress()

		/**
		 * Failed to add the novel.
		 *
		 * @param error The reason why the novel failed to be added
		 */
		class Failure(val error: Exception) : BackgroundNovelAddProgress()
	}

	/**
	 * Apply filters
	 *
	 * This will reset [itemsLive]
	 */
	abstract fun applyFilter()

	/**
	 * Reset the filter data to nothing
	 *
	 * This will reset [itemsLive]
	 */
	abstract fun resetFilter()

	/**
	 * The type of novel card shown to the user.
	 *
	 * @param cardType The type of card.
	 */
	abstract fun setViewType(cardType: NovelCardType)

	abstract fun getFilterStringState(id: Filter<String>): Flow<String>
	abstract fun setFilterStringState(id: Filter<String>, value: String)

	abstract fun getFilterBooleanState(id: Filter<Boolean>): Flow<Boolean>
	abstract fun setFilterBooleanState(id: Filter<Boolean>, value: Boolean)

	abstract fun getFilterIntState(id: Filter<Int>): Flow<Int>
	abstract fun setFilterIntState(id: Filter<Int>, value: Int)

	/**
	 * Get the URL to open web view for the extension
	 */
	abstract val baseURL: StateFlow<String?>

	/**
	 * Clear the cookies
	 */
	abstract fun clearCookies()

	/**
	 * Controls if the filter menu is visible or not
	 */
	abstract val isFilterMenuVisible: StateFlow<Boolean>

	/**
	 * When called, the filter will be shown.
	 */
	abstract fun showFilterMenu()

	/**
	 * When called, the filter will be hidden.
	 */
	abstract fun hideFilterMenu()

	/**
	 * The current query set to the extension.
	 */
	abstract val queryFlow: StateFlow<String>

	/**
	 * Set the selected listing to use and display
	 * @param value Selection as per [listingSelectionData].
	 */
	abstract fun setSelectedListing(value: Int)
}
