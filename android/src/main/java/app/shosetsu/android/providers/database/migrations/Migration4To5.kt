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
 * @since 15 / 03 / 2022
 * @author Doomsdayrs
 */
object Migration4To5 : Migration(4, 5) {
	@Throws(SQLException::class)
	override fun migrate(db: SupportSQLiteDatabase) {
		// Download migrate
		run {
			db.execSQL("DROP INDEX IF EXISTS `index_downloads_chapterURL`")
			db.execSQL("ALTER TABLE `downloads` RENAME TO `downloads_old`")
			db.execSQL("CREATE TABLE IF NOT EXISTS `downloads` (`chapterID` INTEGER NOT NULL, `novelID` INTEGER NOT NULL, `chapterURL` TEXT NOT NULL, `chapterName` TEXT NOT NULL, `novelName` TEXT NOT NULL, `formatterID` INTEGER NOT NULL, `status` INTEGER NOT NULL, PRIMARY KEY(`chapterID`), FOREIGN KEY(`chapterID`) REFERENCES `chapters`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`novelID`) REFERENCES `novels`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
			db.execSQL("INSERT INTO `downloads` SELECT * FROM `downloads_old`")
			db.execSQL("DROP TABLE IF EXISTS `downloads_old`")
			db.execSQL("CREATE INDEX IF NOT EXISTS `index_downloads_chapterID` ON `downloads` (`chapterID`)")
			db.execSQL("CREATE INDEX IF NOT EXISTS `index_downloads_novelID` ON `downloads` (`novelID`)")
		}

		// Chapter migrate
		run {
			db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_chapters_url_formatterID` ON `chapters` (`url`, `formatterID`)")
			db.execSQL("DROP INDEX IF EXISTS `index_chapters_url`")
		}

		// Novels migrate
		run {
			db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_novels_url_formatterID` ON `novels` (`url`, `formatterID`)")
		}
	}

}