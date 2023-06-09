package app.shosetsu.android.providers.database.dao

import android.database.sqlite.SQLiteException
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import app.shosetsu.android.common.ext.onIO
import app.shosetsu.android.domain.model.database.DBChapterHistoryEntity
import app.shosetsu.android.domain.model.local.ChapterEntity
import app.shosetsu.android.domain.model.local.backup.BackupChapterEntity
import app.shosetsu.android.providers.database.dao.base.BaseDao

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
 * @since 12 / 02 / 2023
 * @author Doomsdayrs
 */
@Dao
interface ChapterHistoryDao : BaseDao<DBChapterHistoryEntity> {

	@Throws(SQLiteException::class)
	@Query("SELECT * FROM chapter_history ORDER BY IFNULL(endedReadingAt, startedReadingAt) DESC")
	fun getHistory(): PagingSource<Int, DBChapterHistoryEntity>

	@Throws(SQLiteException::class)
	@Query("SELECT * FROM chapter_history WHERE chapterId = :chapterId")
	suspend fun get(chapterId: Int): DBChapterHistoryEntity?

	@Throws(SQLiteException::class)
	@Query("SELECT * FROM chapter_history WHERE novelId = :novelId ORDER BY endedReadingAt LIMIT 1")
	suspend fun getLastRead(novelId: Int): DBChapterHistoryEntity?


	@Throws(SQLiteException::class)
	@Query("DELETE FROM chapter_history")
	suspend fun clearAll()

	@Throws(SQLiteException::class)
	@Query("DELETE FROM chapter_history WHERE IFNULL(endedReadingAt, startedReadingAt) < :date")
	suspend fun clearBefore(date: Long)

	@Throws(SQLiteException::class)
	suspend fun markChapterAsRead(chapterId: Int, novelId: Int, time: Long) {
		onIO {
			val history = get(chapterId)
			if (history != null) {
				update(
					history.copy(
						endedReadingAt = time
					)
				)
			} else {
				val time = System.currentTimeMillis()

				insert(
					novelId,
					chapterId,
					time - 1000,
					time
				)
			}
		}
	}

	@Throws(SQLiteException::class)
	suspend fun insert(
		novelId: Int,
		chapterId: Int,
		startedReadingAt: Long,
		endedReadingAt: Long?
	) {
		insertAbort(
			DBChapterHistoryEntity(
				null,
				novelId,
				chapterId,
				startedReadingAt,
				endedReadingAt
			)
		)
	}

	@Throws(SQLiteException::class)
	suspend fun markChapterAsReading(chapterId: Int, novelId: Int, time: Long) {
		onIO {
			val history = get(chapterId)
			if (history != null) {
				update(
					history.copy(
						startedReadingAt = time
					)
				)
			} else {
				insert(
					novelId,
					chapterId,
					System.currentTimeMillis(),
					null
				)
			}
		}
	}

	@Transaction
	@Throws(SQLiteException::class)
	suspend fun restoreBackup(chapterMap: Map<BackupChapterEntity, ChapterEntity>) {
		for ((bChapter, rChapter) in chapterMap.entries) {
			val startedAt = bChapter.startedReadingAt
			val endedAt = bChapter.endedReadingAt

			if (startedAt != null) {
				markChapterAsReading(rChapter.id ?: continue, rChapter.novelID, startedAt)

				if (endedAt != null)
					markChapterAsRead(rChapter.id ?: continue, rChapter.novelID, endedAt)
			}
		}
	}
}