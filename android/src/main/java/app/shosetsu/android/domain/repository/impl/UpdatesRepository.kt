package app.shosetsu.android.domain.repository.impl

import android.database.sqlite.SQLiteException
import app.shosetsu.android.common.ext.onIO
import app.shosetsu.android.datasource.local.database.base.IDBUpdatesDataSource
import app.shosetsu.android.domain.model.local.UpdateCompleteEntity
import app.shosetsu.android.domain.model.local.UpdateEntity
import app.shosetsu.android.domain.repository.base.IUpdatesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

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
 * ====================================================================
 */

/**
 * shosetsu
 * 24 / 04 / 2020
 *
 * @author github.com/doomsdayrs
 */
class UpdatesRepository(
	private val database: IDBUpdatesDataSource,
) : IUpdatesRepository {

	@Throws(SQLiteException::class)
	override suspend fun addUpdates(list: List<UpdateEntity>): Array<Long> =
		onIO { database.insertUpdates(list) }

	override fun getUpdatesFlow(): Flow<List<UpdateEntity>> =
		database.getUpdates().onIO()

	override fun getCompleteUpdatesFlow(): Flow<List<UpdateCompleteEntity>> =
		database.getCompleteUpdates().distinctUntilChanged().onIO()

	@Throws(SQLiteException::class)
	override suspend fun clearAll() {
		onIO {
			database.clearAll()
		}
	}

	@Throws(SQLiteException::class)
	override suspend fun clearBefore(date: Long) {
		onIO {
			database.clearBefore(date)
		}
	}
}