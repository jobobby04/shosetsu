package app.shosetsu.android.providers.database.migrations

import android.database.SQLException
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

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
 * @since 08 / 08 / 2022
 */
object Migration10to11 : Migration(10, 11) {

	@Throws(SQLException::class)
	override fun migrate(db: SupportSQLiteDatabase) {
		db.execSQL("DROP TABLE IF EXISTS 'libs'")
		db.execSQL("CREATE TABLE IF NOT EXISTS `libs` (`scriptName` TEXT NOT NULL, `version` TEXT NOT NULL, `repoID` INTEGER NOT NULL, PRIMARY KEY(`scriptName`, `repoID`))")
	}
}