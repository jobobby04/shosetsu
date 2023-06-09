package app.shosetsu.android.domain.repository.base

import android.database.sqlite.SQLiteException
import androidx.paging.PagingSource
import app.shosetsu.android.domain.model.database.DBChapterHistoryEntity
import app.shosetsu.android.domain.model.local.ChapterEntity
import app.shosetsu.android.domain.model.local.ChapterHistoryEntity
import app.shosetsu.android.domain.model.local.backup.BackupChapterEntity

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
 * Contain a history of chapters.
 *
 * @since 11 / 11 / 2021
 * @author Doomsdayrs
 */
interface ChapterHistoryRepository {

	/**
	 * Mark a chapter as having been read
	 */
	@Throws(SQLiteException::class)
	suspend fun markChapterAsRead(chapter: ChapterEntity, time: Long = System.currentTimeMillis())

	/**
	 * Mark a chapter as being read
	 */
	@Throws(SQLiteException::class)
	suspend fun markChapterAsReading(
		chapter: ChapterEntity,
		time: Long = System.currentTimeMillis()
	)

	/**
	 * Get the last read chapter for a novel
	 *
	 * @return a chapter is found, empty if nothing is there
	 */
	@Throws(SQLiteException::class)
	suspend fun getLastRead(novelId: Int): ChapterHistoryEntity?

	/**
	 * Live view of the history
	 */
	fun getHistory(): PagingSource<Int, DBChapterHistoryEntity>

	/**
	 * Clear all history
	 */
	@Throws(SQLiteException::class)
	suspend fun clearAll()

	/**
	 * Clear all history before date provided
	 */
	@Throws(SQLiteException::class)
	suspend fun clearBefore(date: Long)

	/**
	 * Get [ChapterHistoryEntity] for the specific chapter
	 */
	@Throws(SQLiteException::class)
	suspend fun get(chapterId: Int): ChapterHistoryEntity?

	/**
	 * Restore a backup.
	 *
	 * Bulk action.
	 */
	suspend fun restoreBackup(chapterMap: Map<BackupChapterEntity, ChapterEntity>)
}