package app.shosetsu.android.providers.database.dao

import android.database.sqlite.SQLiteException
import androidx.room.Dao
import androidx.room.Query
import app.shosetsu.android.domain.model.database.DBChapterHistoryEntity
import app.shosetsu.android.providers.database.dao.base.BaseDao
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
 * @since 12 / 02 / 2023
 * @author Doomsdayrs
 */
@Dao
interface ChapterHistoryDao : BaseDao<DBChapterHistoryEntity> {

	@Throws(SQLiteException::class)
	@Query("SELECT * FROM chapter_history")
	fun getHistory(): Flow<List<DBChapterHistoryEntity>>

	@Throws(SQLiteException::class)
	@Query("SELECT * FROM chapter_history WHERE novelId = :novelId AND chapterId = :chapterId")
	suspend fun get(novelId: Int, chapterId: Int): DBChapterHistoryEntity?

	@Throws(SQLiteException::class)
	@Query("SELECT * FROM chapter_history WHERE novelId = :novelId ORDER BY endedReadingAt LIMIT 1")
	suspend fun getLastRead(novelId: Int): DBChapterHistoryEntity?


	@Throws(SQLiteException::class)
	@Query("DELETE FROM chapter_history")
	suspend fun clearAll()

	@Throws(SQLiteException::class)
	@Query("DELETE FROM chapter_history WHERE IFNULL(endedReadingAt, startedReadingAt) < :date")
	suspend fun clearBefore(date: Long)
}