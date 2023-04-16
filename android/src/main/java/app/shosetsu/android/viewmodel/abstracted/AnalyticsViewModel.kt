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
	abstract val days: Flow<Int>
	abstract val hours: Flow<Int>
	abstract val minutes: Flow<Int>

	abstract val totalLibraryNovelCount: Flow<Int>
	abstract val totalUnreadNovelCount: Flow<Int>
	abstract val totalReadingNovelCount: Flow<Int>
	abstract val totalReadNovelCount: Flow<Int>

	abstract val totalChapterCount: Flow<Int>
	abstract val totalUnreadChapterCount: Flow<Int>
	abstract val totalReadingChapterCount: Flow<Int>
	abstract val totalReadChapterCount: Flow<Int>

	abstract val topGenres: Flow<List<String>>
	abstract val topExtensions: Flow<List<String>>

	abstract val novels: Flow<List<AnalyticsNovelUI>>
}