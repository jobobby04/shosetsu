package app.shosetsu.android.providers.database.dao

import android.database.sqlite.SQLiteException
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import app.shosetsu.android.domain.model.database.DBExtLibEntity
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
 * ====================================================================
 */

/**
 * shosetsu
 * 22 / 04 / 2020
 *
 * @author github.com/doomsdayrs
 */
@Dao
interface ExtensionLibraryDao : BaseDao<DBExtLibEntity> {
	@Throws(SQLiteException::class)
	@Insert(onConflict = OnConflictStrategy.IGNORE, entity = DBExtLibEntity::class)
	suspend fun insertScriptLib(extLibEntityEntity: DBExtLibEntity)

	@Throws(SQLiteException::class)
	@Query("SELECT * FROM libs WHERE repoID = :repositoryID")
	suspend fun loadLibByRepoID(repositoryID: Int): List<DBExtLibEntity>


	@Throws(SQLiteException::class)
	@Query("SELECT COUNT(*) FROM libs WHERE scriptName = :name AND repoID = :repoId")
	suspend fun scriptLibCountFromName(name: String, repoId: Int): Int

	@Throws(SQLiteException::class)
	@Transaction
	suspend fun insertOrUpdateScriptLib(extLibEntityEntity: DBExtLibEntity) {
		if (scriptLibCountFromName(extLibEntityEntity.scriptName, extLibEntityEntity.repoID) > 0) {
			blockingUpdate(extLibEntityEntity)
		} else insertScriptLib(extLibEntityEntity)
	}

	@Query("SELECT * FROM libs WHERE scriptName = :name ORDER BY repoID DESC")
	suspend fun getExtLibsMatchingName(name: String): List<DBExtLibEntity>


	@Query("SELECT * FROM libs")
	suspend fun loadAll(): List<DBExtLibEntity>
}
