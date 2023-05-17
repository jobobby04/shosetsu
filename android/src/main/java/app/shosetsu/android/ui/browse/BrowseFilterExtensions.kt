package app.shosetsu.android.ui.browse

import androidx.appcompat.widget.SearchView
import app.shosetsu.android.viewmodel.abstracted.ABrowseViewModel

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
 * Shosetsu
 * 9 / June / 2019
 *
 * @author github.com/doomsdayrs
 */
class BrowseFilterExtensions(
	private val viewModel: ABrowseViewModel
) :
	SearchView.OnQueryTextListener {
	override fun onQueryTextSubmit(s: String): Boolean = false

	override fun onQueryTextChange(s: String): Boolean {
		viewModel.setSearch(s)
		return true
	}
}