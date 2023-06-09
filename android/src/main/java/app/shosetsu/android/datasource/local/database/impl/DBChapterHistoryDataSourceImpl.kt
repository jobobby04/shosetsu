package app.shosetsu.android.datasource.local.database.impl

import android.database.sqlite.SQLiteException
import androidx.paging.PagingSource
import app.shosetsu.android.datasource.local.database.base.DBChapterHistoryDataSource
import app.shosetsu.android.domain.model.database.DBChapterHistoryEntity
import app.shosetsu.android.domain.model.local.ChapterEntity
import app.shosetsu.android.domain.model.local.ChapterHistoryEntity
import app.shosetsu.android.domain.model.local.backup.BackupChapterEntity
import app.shosetsu.android.providers.database.dao.ChapterHistoryDao

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
class DBChapterHistoryDataSourceImpl(
	private val dao: ChapterHistoryDao
) : DBChapterHistoryDataSource {
	@Throws(SQLiteException::class)
	override fun getHistory(): PagingSource<Int, DBChapterHistoryEntity> =
		dao.getHistory()

	private fun ChapterHistoryEntity.toDB(): DBChapterHistoryEntity =
		DBChapterHistoryEntity(id, novelId, chapterId, startedReadingAt, endedReadingAt)

	private fun DBChapterHistoryEntity.toEntity(): ChapterHistoryEntity =
		ChapterHistoryEntity(id!!, novelId, chapterId, startedReadingAt, endedReadingAt)

	@Throws(SQLiteException::class)
	override suspend fun get(chapterId: Int): ChapterHistoryEntity? =
		dao.get(chapterId)?.toEntity()

	@Throws(SQLiteException::class)
	override suspend fun update(chapterHistoryEntity: ChapterHistoryEntity) {
		dao.update(chapterHistoryEntity.toDB())
	}

	@Throws(SQLiteException::class)
	override suspend fun insert(
		novelId: Int,
		chapterId: Int,
		startedReadingAt: Long,
		endedReadingAt: Long?
	) {
		dao.insert(
			novelId,
			chapterId,
			startedReadingAt,
			endedReadingAt
		)
	}

	@Throws(SQLiteException::class)
	override suspend fun getLastRead(novelId: Int): ChapterHistoryEntity? =
		dao.getLastRead(novelId)?.toEntity()

	@Throws(SQLiteException::class)
	override suspend fun clearAll() {
		dao.clearAll()
	}

	@Throws(SQLiteException::class)
	override suspend fun clearBefore(date: Long) {
		dao.clearBefore(date)
	}

	override suspend fun restoreBackup(chapterMap: Map<BackupChapterEntity, ChapterEntity>) {
		dao.restoreBackup(chapterMap)
	}

	override suspend fun markChapterAsRead(chapter: ChapterEntity, time: Long) {
		dao.markChapterAsRead(chapter.id!!, chapter.novelID, time)
	}

	override suspend fun markChapterAsReading(chapter: ChapterEntity, time: Long) {
		dao.markChapterAsReading(chapter.id!!, chapter.novelID, time)
	}
}