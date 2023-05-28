package app.shosetsu.android.domain.repository.impl

import android.database.sqlite.SQLiteException
import app.shosetsu.android.common.ext.onIO
import app.shosetsu.android.datasource.local.database.base.IDBNovelPinsDataSource
import app.shosetsu.android.domain.model.local.NovelPinEntity
import app.shosetsu.android.domain.repository.base.INovelPinsRepository

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
 * @since 01 / 11 / 2022
 * @author Doomsdayrs
 */
class NovelPinsRepository(
	private val db: IDBNovelPinsDataSource
) : INovelPinsRepository {
	override suspend fun togglePin(ids: List<Int>) {
		try {
			onIO {
				db.togglePin(ids)
			}
		} catch (ignored: SQLiteException) {
			ignored.printStackTrace()
		}
	}

	@Throws(SQLiteException::class)
	override suspend fun updateOrInsert(pinEntity: NovelPinEntity) {
		db.updateOrInsert(pinEntity)
	}

	override suspend fun isPinned(id: Int): Boolean =
		try {
			db.isPinned(id)
		} catch (e: SQLiteException) {
			false
		}

}