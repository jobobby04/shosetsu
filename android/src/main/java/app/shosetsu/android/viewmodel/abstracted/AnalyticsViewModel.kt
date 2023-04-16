package app.shosetsu.android.viewmodel.abstracted

import app.shosetsu.android.view.uimodels.model.AnalyticsNovelUI
import app.shosetsu.android.viewmodel.base.ShosetsuViewModel
import kotlinx.coroutines.flow.Flow

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
 * @since 10 / 04 / 2023
 * @author Doomsdayrs
 */
abstract class AnalyticsViewModel : ShosetsuViewModel() {
	/**
	 * Days that the user has spent reading
	 */
	abstract val days: Flow<Int>

	/**
	 * Hours that the user has spent reading minus [days]
	 */
	abstract val hours: Flow<Int>

	/**
	 * Minutes that the user has spend reading minus [hours]
	 */
	abstract val minutes: Flow<Int>

	/**
	 * Total count of novels in the users library
	 */
	abstract val totalLibraryNovelCount: Flow<Int>

	/**
	 * Total count of unread novels in the users library
	 */
	abstract val totalUnreadNovelCount: Flow<Int>

	/**
	 * Total count of novels currently being read in the users library
	 */
	abstract val totalReadingNovelCount: Flow<Int>

	/**
	 * Total count of read novels in the users library
	 */
	abstract val totalReadNovelCount: Flow<Int>

	/**
	 * Total count of chapters in the users library
	 */
	abstract val totalChapterCount: Flow<Int>

	/**
	 * Total count of unread chapters in the users library
	 */
	abstract val totalUnreadChapterCount: Flow<Int>

	/**
	 * Total count of chapters currently being read in the users library
	 */
	abstract val totalReadingChapterCount: Flow<Int>

	/**
	 * Total count of read chapters in the users library
	 */
	abstract val totalReadChapterCount: Flow<Int>

	/**
	 * Top 3 genres in the users library
	 */
	abstract val topGenres: Flow<List<String>>

	/**
	 * Top 3 extensions used in the users library
	 */
	abstract val topExtensions: Flow<List<String>>

	/**
	 * A collection of novels and their statistics from the users library.
	 */
	abstract val novels: Flow<List<AnalyticsNovelUI>>
}