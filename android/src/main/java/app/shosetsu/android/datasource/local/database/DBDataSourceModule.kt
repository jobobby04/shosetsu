package app.shosetsu.android.datasource.local.database

import app.shosetsu.android.datasource.local.database.base.DBChapterHistoryDataSource
import app.shosetsu.android.datasource.local.database.base.IDBCategoriesDataSource
import app.shosetsu.android.datasource.local.database.base.IDBChaptersDataSource
import app.shosetsu.android.datasource.local.database.base.IDBDownloadsDataSource
import app.shosetsu.android.datasource.local.database.base.IDBExtLibDataSource
import app.shosetsu.android.datasource.local.database.base.IDBExtRepoDataSource
import app.shosetsu.android.datasource.local.database.base.IDBInstalledExtensionsDataSource
import app.shosetsu.android.datasource.local.database.base.IDBNovelCategoriesDataSource
import app.shosetsu.android.datasource.local.database.base.IDBNovelPinsDataSource
import app.shosetsu.android.datasource.local.database.base.IDBNovelReaderSettingsDataSource
import app.shosetsu.android.datasource.local.database.base.IDBNovelSettingsDataSource
import app.shosetsu.android.datasource.local.database.base.IDBNovelsDataSource
import app.shosetsu.android.datasource.local.database.base.IDBRepositoryExtensionsDataSource
import app.shosetsu.android.datasource.local.database.base.IDBUpdatesDataSource
import app.shosetsu.android.datasource.local.database.impl.DBCategoriesDataSource
import app.shosetsu.android.datasource.local.database.impl.DBChapterHistoryDataSourceImpl
import app.shosetsu.android.datasource.local.database.impl.DBChaptersDataSource
import app.shosetsu.android.datasource.local.database.impl.DBDownloadsDataSource
import app.shosetsu.android.datasource.local.database.impl.DBExtLibDataSource
import app.shosetsu.android.datasource.local.database.impl.DBExtRepoDataSource
import app.shosetsu.android.datasource.local.database.impl.DBInstalledExtensionsDataSource
import app.shosetsu.android.datasource.local.database.impl.DBNovelCategoriesDataSource
import app.shosetsu.android.datasource.local.database.impl.DBNovelPinsDataSource
import app.shosetsu.android.datasource.local.database.impl.DBNovelReaderSettingsDataSource
import app.shosetsu.android.datasource.local.database.impl.DBNovelSettingsDataSource
import app.shosetsu.android.datasource.local.database.impl.DBNovelsDataSource
import app.shosetsu.android.datasource.local.database.impl.DBRepositoryExtensionsDataSource
import app.shosetsu.android.datasource.local.database.impl.DBUpdatesDataSource
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.new
import org.kodein.di.singleton

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
 */

/**
 * 01 / 01 / 2021
 */
val dbDataSourceModule = DI.Module("database_data_source") {
	bind<IDBCategoriesDataSource>() with singleton { new(::DBCategoriesDataSource) }
	bind<IDBChaptersDataSource>() with singleton { new(::DBChaptersDataSource) }
	bind<DBChapterHistoryDataSource>() with singleton { new(::DBChapterHistoryDataSourceImpl) }
	bind<IDBDownloadsDataSource>() with singleton { new(::DBDownloadsDataSource) }
	bind<IDBInstalledExtensionsDataSource>() with singleton { new(::DBInstalledExtensionsDataSource) }
	bind<IDBRepositoryExtensionsDataSource>() with
		singleton { new(::DBRepositoryExtensionsDataSource) }
	bind<IDBExtLibDataSource>() with singleton { new(::DBExtLibDataSource) }
	bind<IDBNovelCategoriesDataSource>() with singleton { new(::DBNovelCategoriesDataSource) }
	bind<IDBNovelsDataSource>() with singleton { new(::DBNovelsDataSource) }
	bind<IDBNovelPinsDataSource>() with singleton { new(::DBNovelPinsDataSource) }
	bind<IDBExtRepoDataSource>() with singleton { new(::DBExtRepoDataSource) }
	bind<IDBUpdatesDataSource>() with singleton { new(::DBUpdatesDataSource) }
	bind<IDBNovelSettingsDataSource>() with singleton { new(::DBNovelSettingsDataSource) }
	bind<IDBNovelReaderSettingsDataSource>() with singleton { new(::DBNovelReaderSettingsDataSource) }
}
