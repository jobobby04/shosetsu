package app.shosetsu.android.datasource.local.database.base

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
 * @since 16 / 01 / 2023
 * @author Doomsdayrs
 *
 * Denotes the read history,
 *  containing a map of the chapter id to the time the user finished reading the chatper.
 */
interface DBChapterHistoryDataSource {
	@Throws(SQLiteException::class)
	fun getHistory(): PagingSource<Int, DBChapterHistoryEntity>

	@Throws(SQLiteException::class)
	suspend fun get(chapterId: Int): ChapterHistoryEntity?

	@Throws(SQLiteException::class)
	suspend fun update(chapterHistoryEntity: ChapterHistoryEntity)

	@Throws(SQLiteException::class)
	suspend fun insert(
		novelId: Int,
		chapterId: Int,
		startedReadingAt: Long,
		endedReadingAt: Long?
	)

	@Throws(SQLiteException::class)
	suspend fun getLastRead(novelId: Int): ChapterHistoryEntity?

	@Throws(SQLiteException::class)
	suspend fun clearAll()

	@Throws(SQLiteException::class)
	suspend fun clearBefore(date: Long)
	suspend fun restoreBackup(chapterMap: Map<BackupChapterEntity, ChapterEntity>)
	suspend fun markChapterAsRead(chapter: ChapterEntity, time: Long)
	suspend fun markChapterAsReading(chapter: ChapterEntity, time: Long)
}