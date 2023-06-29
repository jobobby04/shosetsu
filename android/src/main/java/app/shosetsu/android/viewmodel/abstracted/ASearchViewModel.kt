package app.shosetsu.android.viewmodel.abstracted

import androidx.paging.PagingData
import app.shosetsu.android.view.uimodels.model.catlog.ACatalogNovelUI
import app.shosetsu.android.view.uimodels.model.search.SearchRowUI
import app.shosetsu.android.viewmodel.base.ShosetsuViewModel
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
 */
abstract class ASearchViewModel : ShosetsuViewModel() {

	abstract val query: StateFlow<String?>

	abstract val listings: StateFlow<ImmutableList<SearchRowUI>>

	abstract val isCozy: StateFlow<Boolean>

	abstract fun initQuery(string: String?)
	abstract fun setQuery(query: String)
	abstract fun applyQuery(query: String)


	abstract fun searchLibrary(): Flow<PagingData<ACatalogNovelUI>>

	/**
	 * Gets the search flow of an extension
	 */
	abstract fun searchExtension(extensionId: Int): Flow<PagingData<ACatalogNovelUI>>

	/**
	 * Refresh all rows
	 */
	abstract fun refresh()

	/**
	 * Refresh a specific row
	 */
	abstract fun refresh(id: Int)

	/**
	 * Get the exception that occurred in a certain row
	 */
	abstract fun getException(id: Int): Flow<Throwable?>

}