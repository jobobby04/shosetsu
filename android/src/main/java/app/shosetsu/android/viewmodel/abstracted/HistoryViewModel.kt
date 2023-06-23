package app.shosetsu.android.viewmodel.abstracted

import androidx.paging.PagingData
import app.shosetsu.android.view.uimodels.model.ChapterHistoryUI
import app.shosetsu.android.viewmodel.base.ShosetsuViewModel
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
 * Shosetsu
 *
 * View model for History view
 *
 * @since 12 / 02 / 2023
 * @author Doomsdayrs
 */
abstract class HistoryViewModel : ShosetsuViewModel() {
	/**
	 * History items
	 */
	abstract val items: Flow<PagingData<ChapterHistoryUI>>

	/**
	 * Show clear before dialog or not
	 */
	abstract val isClearBeforeDialogShown: StateFlow<Boolean>

	/**
	 * Show clear before dialog
	 */
	abstract fun showClearBeforeDialog()

	/**
	 * Hide clear before dialog
	 */
	abstract fun hideClearBeforeDialog()

	/**
	 * Remove all history
	 */
	abstract fun clearAll()

	/**
	 * Clear out all history before the date provided
	 *
	 * @param date, in system time format
	 */
	abstract fun clearBefore(date: Long)
}