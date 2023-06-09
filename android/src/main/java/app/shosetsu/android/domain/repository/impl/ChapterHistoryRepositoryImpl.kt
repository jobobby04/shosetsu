package app.shosetsu.android.domain.repository.impl

import android.database.sqlite.SQLiteException
import androidx.paging.PagingSource
import app.shosetsu.android.common.ext.onIO
import app.shosetsu.android.datasource.local.database.base.DBChapterHistoryDataSource
import app.shosetsu.android.domain.model.database.DBChapterHistoryEntity
import app.shosetsu.android.domain.model.local.ChapterEntity
import app.shosetsu.android.domain.model.local.ChapterHistoryEntity
import app.shosetsu.android.domain.model.local.backup.BackupChapterEntity
import app.shosetsu.android.domain.repository.base.ChapterHistoryRepository

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
 */
class ChapterHistoryRepositoryImpl(
	private val chapterHistoryDatabase: DBChapterHistoryDataSource
) : ChapterHistoryRepository {
	@Throws(SQLiteException::class)
	override suspend fun markChapterAsRead(chapter: ChapterEntity, time: Long) {
		chapterHistoryDatabase.markChapterAsRead(chapter, time)
	}

	@Throws(SQLiteException::class)
	override suspend fun markChapterAsReading(chapter: ChapterEntity, time: Long) {
		chapterHistoryDatabase.markChapterAsReading(chapter, time)
	}

	@Throws(SQLiteException::class)
	override suspend fun getLastRead(novelId: Int): ChapterHistoryEntity? = onIO {
		chapterHistoryDatabase.getLastRead(novelId)
	}

	@Throws(SQLiteException::class)
	override fun getHistory(): PagingSource<Int, DBChapterHistoryEntity> =
		chapterHistoryDatabase.getHistory()

	override suspend fun clearAll() {
		onIO {
			chapterHistoryDatabase.clearAll()
		}
	}

	override suspend fun clearBefore(date: Long) {
		onIO {
			chapterHistoryDatabase.clearBefore(date)
		}
	}

	@Throws(SQLiteException::class)
	override suspend fun get(chapterId: Int): ChapterHistoryEntity? = onIO {
		chapterHistoryDatabase.get(chapterId)
	}

	override suspend fun restoreBackup(chapterMap: Map<BackupChapterEntity, ChapterEntity>) = onIO {
		chapterHistoryDatabase.restoreBackup(chapterMap)
	}
}