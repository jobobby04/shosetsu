package app.shosetsu.android.providers.database

import android.content.Context
import android.database.sqlite.SQLiteException
import androidx.room.Database
import androidx.room.Fts4
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import app.shosetsu.android.domain.model.database.DBCategoryEntity
import app.shosetsu.android.domain.model.database.DBChapterEntity
import app.shosetsu.android.domain.model.database.DBChapterHistoryEntity
import app.shosetsu.android.domain.model.database.DBDownloadEntity
import app.shosetsu.android.domain.model.database.DBExtLibEntity
import app.shosetsu.android.domain.model.database.DBInstalledExtensionEntity
import app.shosetsu.android.domain.model.database.DBNovelCategoryEntity
import app.shosetsu.android.domain.model.database.DBNovelEntity
import app.shosetsu.android.domain.model.database.DBNovelPinEntity
import app.shosetsu.android.domain.model.database.DBNovelReaderSettingEntity
import app.shosetsu.android.domain.model.database.DBNovelSettingsEntity
import app.shosetsu.android.domain.model.database.DBRepositoryEntity
import app.shosetsu.android.domain.model.database.DBRepositoryExtensionEntity
import app.shosetsu.android.domain.model.database.DBUpdate
import app.shosetsu.android.providers.database.converters.ChapterSortTypeConverter
import app.shosetsu.android.providers.database.converters.ChapterTypeConverter
import app.shosetsu.android.providers.database.converters.DownloadStatusConverter
import app.shosetsu.android.providers.database.converters.ExtensionTypeConverter
import app.shosetsu.android.providers.database.converters.ListConverter
import app.shosetsu.android.providers.database.converters.NovelStatusConverter
import app.shosetsu.android.providers.database.converters.ReadingStatusConverter
import app.shosetsu.android.providers.database.converters.StringArrayConverters
import app.shosetsu.android.providers.database.converters.VersionConverter
import app.shosetsu.android.providers.database.dao.CategoriesDao
import app.shosetsu.android.providers.database.dao.ChapterHistoryDao
import app.shosetsu.android.providers.database.dao.ChaptersDao
import app.shosetsu.android.providers.database.dao.DownloadsDao
import app.shosetsu.android.providers.database.dao.ExtensionLibraryDao
import app.shosetsu.android.providers.database.dao.InstalledExtensionsDao
import app.shosetsu.android.providers.database.dao.NovelCategoriesDao
import app.shosetsu.android.providers.database.dao.NovelPinsDao
import app.shosetsu.android.providers.database.dao.NovelReaderSettingsDao
import app.shosetsu.android.providers.database.dao.NovelSettingsDao
import app.shosetsu.android.providers.database.dao.NovelsDao
import app.shosetsu.android.providers.database.dao.RepositoryDao
import app.shosetsu.android.providers.database.dao.RepositoryExtensionsDao
import app.shosetsu.android.providers.database.dao.UpdatesDao
import app.shosetsu.android.providers.database.migrations.Migration10to11
import app.shosetsu.android.providers.database.migrations.Migration1To2
import app.shosetsu.android.providers.database.migrations.Migration2To3
import app.shosetsu.android.providers.database.migrations.Migration3To4
import app.shosetsu.android.providers.database.migrations.Migration4To5
import app.shosetsu.android.providers.database.migrations.Migration5To6
import app.shosetsu.android.providers.database.migrations.Migration6To7
import app.shosetsu.android.providers.database.migrations.Migration7to8
import app.shosetsu.android.providers.database.migrations.Migration8to9
import app.shosetsu.android.providers.database.migrations.Migration9to10
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

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
 * 17 / 04 / 2020
 *
 * @author github.com/doomsdayrs
 */
@Fts4
@Database(
	entities = [
		DBCategoryEntity::class,
		DBChapterEntity::class,
		DBChapterHistoryEntity::class,
		DBDownloadEntity::class,
		DBInstalledExtensionEntity::class,
		DBRepositoryExtensionEntity::class,
		DBExtLibEntity::class,
		DBNovelCategoryEntity::class,
		DBNovelReaderSettingEntity::class,
		DBNovelEntity::class,
		DBNovelPinEntity::class,
		DBNovelSettingsEntity::class,
		DBRepositoryEntity::class,
		DBUpdate::class,
	],
	version = 11
)
@TypeConverters(
	ChapterSortTypeConverter::class,
	DownloadStatusConverter::class,
	ListConverter::class,
	NovelStatusConverter::class,
	ChapterTypeConverter::class,
	ReadingStatusConverter::class,
	StringArrayConverters::class,
	VersionConverter::class,
	ExtensionTypeConverter::class
)
abstract class ShosetsuDatabase : RoomDatabase() {

	abstract val categoriesDao: CategoriesDao
	abstract val chaptersDao: ChaptersDao
	abstract val chapterHistoryDao: ChapterHistoryDao
	abstract val downloadsDao: DownloadsDao
	abstract val extensionLibraryDao: ExtensionLibraryDao
	abstract val installedExtensionsDao: InstalledExtensionsDao
	abstract val repositoryExtensionDao: RepositoryExtensionsDao
	abstract val novelCategoriesDao: NovelCategoriesDao
	abstract val novelReaderSettingsDao: NovelReaderSettingsDao
	abstract val novelsDao: NovelsDao
	abstract val novelPinsDao: NovelPinsDao
	abstract val novelSettingsDao: NovelSettingsDao
	abstract val repositoryDao: RepositoryDao
	abstract val updatesDao: UpdatesDao

	companion object {
		@Volatile
		private lateinit var databaseShosetsu: ShosetsuDatabase

		@OptIn(DelicateCoroutinesApi::class)
		@Synchronized
		fun getRoomDatabase(context: Context): ShosetsuDatabase {
			if (!Companion::databaseShosetsu.isInitialized)
				databaseShosetsu = Room.databaseBuilder(
					context.applicationContext,
					ShosetsuDatabase::class.java,
					"room_database"
				).addMigrations(
					Migration1To2,
					Migration2To3,
					Migration3To4,
					Migration4To5,
					Migration5To6,
					Migration6To7,
					Migration7to8,
					Migration8to9,
					Migration9to10,
					Migration10to11,
				).build()

			GlobalScope.launch {
				try {
					databaseShosetsu.repositoryDao.initializeData()
				} catch (e: SQLiteException) {
					e.printStackTrace()
				}
			}
			return databaseShosetsu
		}
	}
}