package app.shosetsu.android.datasource.local.database.base

import android.database.sqlite.SQLiteException
import app.shosetsu.android.domain.model.local.NovelPinEntity

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
interface IDBNovelPinsDataSource {

	/**
	 * Toggle the pin state of the following novel ids
	 */
	@Throws(SQLiteException::class)
	suspend fun togglePin(ids: List<Int>)

	@Throws(SQLiteException::class)
	suspend fun isPinned(id: Int): Boolean

	@Throws(SQLiteException::class)
	suspend fun updateOrInsert(pinEntity: NovelPinEntity)
}