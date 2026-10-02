package app.shosetsu.android.di

import app.shosetsu.android.providers.database.ShosetsuDatabase
import app.shosetsu.android.providers.database.ShosetsuDatabase.Companion.getRoomDatabase
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
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.new
import org.kodein.di.singleton

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
 * 23 / 04 / 2020
 *
 * @author github.com/doomsdayrs
 */

val databaseModule: DI.Module = DI.Module("database_module") {
	bind<ShosetsuDatabase>() with singleton { new(::getRoomDatabase) }

	bind<CategoriesDao>() with singleton { instance<ShosetsuDatabase>().categoriesDao }
	bind<ChaptersDao>() with singleton { instance<ShosetsuDatabase>().chaptersDao }
	bind<ChapterHistoryDao>() with singleton { instance<ShosetsuDatabase>().chapterHistoryDao }
	bind<DownloadsDao>() with singleton { instance<ShosetsuDatabase>().downloadsDao }
	bind<ExtensionLibraryDao>() with singleton { instance<ShosetsuDatabase>().extensionLibraryDao }
	bind<InstalledExtensionsDao>() with
		singleton { instance<ShosetsuDatabase>().installedExtensionsDao }
	bind<RepositoryExtensionsDao>() with
		singleton { instance<ShosetsuDatabase>().repositoryExtensionDao }
	bind<NovelCategoriesDao>() with singleton { instance<ShosetsuDatabase>().novelCategoriesDao }
	bind<NovelReaderSettingsDao>() with
		singleton { instance<ShosetsuDatabase>().novelReaderSettingsDao }
	bind<NovelsDao>() with singleton { instance<ShosetsuDatabase>().novelsDao }
	bind<NovelPinsDao>() with singleton { instance<ShosetsuDatabase>().novelPinsDao }
	bind<NovelSettingsDao>() with singleton { instance<ShosetsuDatabase>().novelSettingsDao }
	bind<RepositoryDao>() with singleton { instance<ShosetsuDatabase>().repositoryDao }
	bind<UpdatesDao>() with singleton { instance<ShosetsuDatabase>().updatesDao }
}
