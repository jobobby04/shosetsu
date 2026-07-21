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
import org.kodein.di.instance
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
	bind<IDBCategoriesDataSource>() with singleton { DBCategoriesDataSource(instance()) }
	bind<IDBChaptersDataSource>() with singleton { DBChaptersDataSource(instance()) }
	bind<DBChapterHistoryDataSource>() with singleton { DBChapterHistoryDataSourceImpl(instance()) }
	bind<IDBDownloadsDataSource>() with singleton { DBDownloadsDataSource(instance()) }

	bind<IDBInstalledExtensionsDataSource>() with singleton {
		DBInstalledExtensionsDataSource(
			instance(),
		)
	}

	bind<IDBRepositoryExtensionsDataSource>() with singleton {
		DBRepositoryExtensionsDataSource(
			instance(),
		)
	}

	bind<IDBExtLibDataSource>() with singleton { DBExtLibDataSource(instance()) }

	bind<IDBNovelCategoriesDataSource>() with singleton { DBNovelCategoriesDataSource(instance()) }

	bind<IDBNovelsDataSource>() with singleton { DBNovelsDataSource(instance()) }
	bind<IDBNovelPinsDataSource>() with singleton { DBNovelPinsDataSource(instance()) }

	bind<IDBExtRepoDataSource>() with singleton { DBExtRepoDataSource(instance()) }

	bind<IDBUpdatesDataSource>() with singleton { DBUpdatesDataSource(instance()) }

	bind<IDBNovelSettingsDataSource>() with singleton { DBNovelSettingsDataSource(instance()) }
	bind<IDBNovelReaderSettingsDataSource>() with singleton {
		DBNovelReaderSettingsDataSource(
			instance(),
		)
	}
}
