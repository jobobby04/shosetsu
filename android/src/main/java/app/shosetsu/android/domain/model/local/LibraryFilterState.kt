package app.shosetsu.android.domain.model.local

import app.shosetsu.android.common.enums.InclusionState
import app.shosetsu.android.common.enums.NovelSortType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

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
 * 03 / 01 / 2021
 */
@Serializable
data class LibraryFilterState(
	val sortType: NovelSortType = NovelSortType.BY_TITLE,
	val reversedSort: Boolean = false,
	val unreadInclusion: InclusionState? = null,

	val genreFilter: Map<String, InclusionState> = emptyMap(),
	val authorFilter: Map<String, InclusionState> = emptyMap(),
	val artistFilter: Map<String, InclusionState> = emptyMap(),
	val tagFilter: Map<String, InclusionState> = emptyMap(),
	val arePinsOnTop: Boolean = true,
	val downloadedOnly: InclusionState? = null
) {
	companion object {
		val libraryFilterStateJson = Json {
			encodeDefaults = true
			ignoreUnknownKeys = true
		}
	}
}
