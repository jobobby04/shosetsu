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
object Migration8to9 : Migration(8, 9) {

	@Throws(SQLException::class)
	override fun migrate(database: SupportSQLiteDatabase) {
		database.execSQL("CREATE TABLE IF NOT EXISTS `chapter_history` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `novelId` INTEGER NOT NULL, `chapterId` INTEGER NOT NULL, `startedReadingAt` INTEGER NOT NULL, `endedReadingAt` INTEGER, FOREIGN KEY(`novelId`) REFERENCES `novels`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`chapterId`) REFERENCES `chapters`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
		database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_chapter_history_novelId_chapterId` ON `chapter_history` (`novelId`, `chapterId`)")
		database.execSQL("CREATE INDEX IF NOT EXISTS `index_chapter_history_chapterId` ON `chapter_history` (`chapterId`)")
	}
}