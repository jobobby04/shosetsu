package app.shosetsu.android.viewmodel.impl

import app.shosetsu.android.domain.model.local.AnalyticsNovelEntity
import app.shosetsu.android.domain.repository.base.IExtensionsRepository
import app.shosetsu.android.domain.repository.base.INovelsRepository
import app.shosetsu.android.viewmodel.abstracted.AnalyticsViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.*
import java.util.concurrent.TimeUnit.MILLISECONDS

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
 * @since 11 / 04 / 2023
 * @author Doomsdayrs
 */
class AnalyticsViewModelImpl(
	private val extRepo: IExtensionsRepository,
	private val novelRepo: INovelsRepository
) : AnalyticsViewModel() {
	private val novels: Flow<List<AnalyticsNovelEntity>> =
		novelRepo.getAnalytics()

	val totalReadingTime: Flow<Long> =
		novels.map { list -> list.sumOf { it.totalReadingTime } }

	override val days: Flow<Int> = totalReadingTime.map { MILLISECONDS.toDays(it).toInt() }


	override val hours: Flow<Int> =
		totalReadingTime.combine(days) { total, days -> (MILLISECONDS.toHours(total) - days * 24).toInt() }

	override val minutes: Flow<Int> =
		totalReadingTime.map { total -> (MILLISECONDS.toMinutes(total) - MILLISECONDS.toHours(total) * 60).toInt() }

	override val totalLibraryNovelCount: Flow<Int> = novels.map { it.size }

	override val totalUnreadNovelCount: Flow<Int> =
		novels.map { it -> it.count { it.unreadChapterCount > 0 } }

	override val totalReadingNovelCount: Flow<Int> =
		novels.map { it -> it.count { it.readingChapterCount > 0 } }

	override val totalReadNovelCount: Flow<Int> =
		novels.map { it -> it.count { it.readChapterCount > 0 } }

	override val totalChapterCount: Flow<Int> =
		novels.map { it -> it.sumOf { it.chapterCount } }

	override val totalUnreadChapterCount: Flow<Int> =
		novels.map { it -> it.sumOf { it.unreadChapterCount } }

	override val totalReadingChapterCount: Flow<Int> =
		novels.map { it -> it.sumOf { it.readingChapterCount } }

	override val totalReadChapterCount: Flow<Int> =
		novels.map { it -> it.sumOf { it.readChapterCount } }

	override val topGenres: Flow<List<String>> =
		novels.map { list ->
			val genreCount = HashMap<String, Int>()
			list.forEach { novel ->
				novel.genres.filter { it.isNotBlank() }
					.map { it.lowercase() }
					.forEach { genre ->
						genreCount[genre] = genreCount.getOrElse(genre) { 0 } + 1
					}
			}
			genreCount.entries.sortedByDescending { it.value }
				.take(3)
				.map { (key) ->
					key.replaceFirstChar {
						if (it.isLowerCase()) it.titlecase(
							Locale.getDefault()
						) else it.toString()
					}
				}
		}

	override val topExtensions: Flow<List<String>> =
		novels.map { list ->
			val extensionCount = HashMap<Int, Int>()
			list.forEach { novel ->
				val extensionId = novel.extensionId
				extensionCount[extensionId] = extensionCount.getOrElse(extensionId) { 0 } + 1
			}
			extensionCount.entries.sortedByDescending { it.value }
				.take(3)
				.map { it.key }
				.map { extRepo.getInstalledExtension(it)?.name ?: "Unknown Extension" }
		}
}