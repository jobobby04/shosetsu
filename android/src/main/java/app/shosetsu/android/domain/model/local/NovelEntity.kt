package app.shosetsu.android.domain.model.local

import app.shosetsu.lib.Novel

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
 * ====================================================================
 */

/**
 * shosetsu
 * 23 / 04 / 2020
 *
 * @author github.com/doomsdayrs
 */

data class NovelEntity(
	/** ID of this novel */
	val id: Int = 0,

	/** URL of the novel */
	val url: String,

	/** Source this novel is from */
	val extensionID: Int,

	/** If this novel is in the user's library */
	val bookmarked: Boolean = false,

	/** Says if the data is loaded or now, if it is not it needs to be loaded */
	val loaded: Boolean = false,

	/** The title of the novel */
	val title: String,

	/** Image URL of the novel */
	val imageURL: String = "",

	/** Description */
	val description: String = "",

	/** Language of the novel */
	val language: String = "",

	/** Genres this novel matches too */
	val genres: List<String> = emptyList(),

	/** Authors of this novel */
	val authors: List<String> = emptyList(),

	/** Artists who helped with the novel illustration */
	val artists: List<String> = emptyList(),

	/** Tags this novel matches, in case genres were not enough*/
	val tags: List<String> = emptyList(),

	/** The publishing status of this novel */
	val status: Novel.Status = Novel.Status.UNKNOWN,
)