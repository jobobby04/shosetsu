/*
 * This file is part of Shosetsu.
 *
 * Shosetsu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Shosetsu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Shosetsu.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package app.shosetsu.android.providers.database.migrations

import android.database.SQLException
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Shosetsu
 *
 * @since 08 / 08 / 2022
 */
object Migration11to12 : Migration(11, 12) {

	@Throws(SQLException::class)
	override fun migrate(db: SupportSQLiteDatabase) {
		// Cleanup rows that might cause the migration to fail
		db.execSQL(
			"DELETE FROM `chapters` WHERE `id` NOT IN " +
				"(SELECT MIN(`id`) FROM `chapters` GROUP BY `url`, `novelID`)",
		)
		db.execSQL("DROP INDEX IF EXISTS `index_chapters_url_formatterID`")
		db.execSQL(
			"CREATE UNIQUE INDEX IF NOT EXISTS `index_chapters_url_novelID` ON `chapters` (`url`, `novelID`)",
		)
	}
}
