package app.shosetsu.android.providers.database.dao

import android.database.sqlite.SQLiteException
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import app.shosetsu.android.domain.model.database.DBNovelPinEntity
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
 * @since 30 / 10 / 2022
 * @author Doomsdayrs
 */
@Dao
interface NovelPinsDao : BaseDao<DBNovelPinEntity> {

	@Throws(SQLiteException::class)
	@Query("SELECT * FROM novel_pins WHERE novelId = :id")
	suspend fun get(id: Int): DBNovelPinEntity?

	/**
	 * Toggle the pin of the following novel ids in one go via transaction.
	 */
	@Throws(SQLiteException::class)
	@Transaction
	suspend fun togglePin(ids: List<Int>) {
		ids.forEach { id ->
			val item = get(id)
			if (item != null)
				update(item.copy(pinned = !item.pinned))
			else {
				// Insert as true, as we are assuming null = false
				insertIgnore(DBNovelPinEntity(id, true))
			}
		}
	}

	@Throws(SQLiteException::class)
	@Query("SELECT pinned FROM novel_pins WHERE novelId = :id")
	suspend fun isPinned(id: Int): Boolean

	@Throws(SQLiteException::class)
	@Transaction
	suspend fun updateOrInsert(entity: DBNovelPinEntity) {
		insertReplace(entity)
	}
}