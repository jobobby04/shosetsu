package app.shosetsu.android.view.uimodels.model.catlog

import androidx.compose.runtime.Immutable
import app.shosetsu.android.domain.model.local.StrippedBookmarkedNovelEntity
import app.shosetsu.android.domain.model.local.StrippedNovelEntity
import app.shosetsu.lib.Novel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

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
 * 23 / 08 / 2020
 *
 * This class represents novels listed by an extension in its catalogue
 */
@Immutable
data class ACatalogNovelUI(
	val id: Int,
	val title: String,
	val imageURL: String,
	val bookmarked: Boolean,
	val language: String,
	val description: String,
	val status: Novel.Status,
	val tags: ImmutableList<String>,
	val genres: ImmutableList<String>,
	val authors: ImmutableList<String>,
	val artists: ImmutableList<String>,
	val chapters: ImmutableList<Novel.Chapter>,
	val chapterCount: Int?,
	val wordCount: Int?,
	val commentCount: Int?,
	val viewCount: Int?,
	val favoriteCount: Int?,
) {
	companion object {
		operator fun invoke(entity: StrippedBookmarkedNovelEntity): ACatalogNovelUI {
			val (id, title, imageUrl) = entity
			return ACatalogNovelUI(
				id = id,
				title = title,
				imageURL = imageUrl,
				bookmarked = false,
				language = "",
				description = "",
				status = Novel.Status.UNKNOWN,
				tags = persistentListOf(),
				genres = persistentListOf(),
				authors = persistentListOf(),
				artists = persistentListOf(),
				chapters = persistentListOf(),
				chapterCount = null,
				wordCount = null,
				commentCount = null,
				viewCount = null,
				favoriteCount = null
			)
		}

		operator fun invoke(entity: StrippedNovelEntity, info: Novel.Info): ACatalogNovelUI {
			val (id, title, imageUrl, bookmarked) = entity
			return ACatalogNovelUI(
				id = id,
				title = title,
				imageURL = imageUrl,
				bookmarked = bookmarked,
				language = info.language,
				description = info.description,
				status = info.status,
				tags = info.tags.asList().toImmutableList(),
				genres = info.genres.asList().toImmutableList(),
				authors = info.authors.asList().toImmutableList(),
				artists = info.artists.asList().toImmutableList(),
				chapters = info.chapters.asList().toImmutableList(),
				chapterCount = info.chapterCount,
				wordCount = info.wordCount,
				commentCount = info.commentCount,
				viewCount = info.viewCount,
				favoriteCount = info.favoriteCount
			)
		}
	}
}