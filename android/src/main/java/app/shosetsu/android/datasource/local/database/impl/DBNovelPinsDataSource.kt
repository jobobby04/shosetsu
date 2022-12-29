package app.shosetsu.android.datasource.local.database.impl

import android.database.sqlite.SQLiteException
import app.shosetsu.android.datasource.local.database.base.IDBNovelPinsDataSource
import app.shosetsu.android.domain.model.local.NovelPinEntity
import app.shosetsu.android.providers.database.dao.NovelPinsDao

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
class DBNovelPinsDataSource(
	private val dao: NovelPinsDao
) : IDBNovelPinsDataSource {
	@Throws(SQLiteException::class)
	override suspend fun togglePin(ids: List<Int>) {
		dao.togglePin(ids)
	}

	@Throws(SQLiteException::class)
	override suspend fun isPinned(id: Int): Boolean =
		dao.isPinned(id)

	@Throws(SQLiteException::class)
	override suspend fun updateOrInsert(pinEntity: NovelPinEntity) {
		dao.updateOrInsert(pinEntity.convertTo())
	}
}