package app.shosetsu.android.datasource.local.database.impl

import android.database.sqlite.SQLiteException
import app.shosetsu.android.common.ext.toDB
import app.shosetsu.android.datasource.local.database.base.IDBUpdatesDataSource
import app.shosetsu.android.domain.model.local.UpdateCompleteEntity
import app.shosetsu.android.domain.model.local.UpdateEntity
import app.shosetsu.android.dto.convertList
import app.shosetsu.android.providers.database.dao.UpdatesDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

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
 * shosetsu
 * 12 / 05 / 2020
 */
class DBUpdatesDataSource(
	private val updatesDao: UpdatesDao,
) : IDBUpdatesDataSource {
	@Throws(SQLiteException::class)
	override fun getUpdates(): Flow<List<UpdateEntity>> =
		updatesDao.loadUpdates().map { it.convertList() }

	@Throws(SQLiteException::class)
	override suspend fun insertUpdates(list: List<UpdateEntity>): Array<Long> =
		(updatesDao.insertAllReplace(list.toDB()))

	@Throws(SQLiteException::class)
	override fun getCompleteUpdates(
	): Flow<List<UpdateCompleteEntity>> =
		updatesDao.loadCompleteUpdates()

	@Throws(SQLiteException::class)
	override suspend fun clearAll() {
		updatesDao.clearAll()
	}

	@Throws(SQLiteException::class)
	override suspend fun clearBefore(date: Long) {
		updatesDao.clearBefore(date)
	}
}