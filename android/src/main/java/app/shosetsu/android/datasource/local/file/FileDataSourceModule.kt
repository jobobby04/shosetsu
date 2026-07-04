package app.shosetsu.android.datasource.local.file

import app.shosetsu.android.datasource.local.file.base.IFileCachedAppUpdateDataSource
import app.shosetsu.android.datasource.local.file.base.IFileCachedChapterDataSource
import app.shosetsu.android.datasource.local.file.base.IFileChapterDataSource
import app.shosetsu.android.datasource.local.file.base.IFileCrashDataSource
import app.shosetsu.android.datasource.local.file.base.IFileExtLibDataSource
import app.shosetsu.android.datasource.local.file.base.IFileExtensionDataSource
import app.shosetsu.android.datasource.local.file.base.IFileSettingsDataSource
import app.shosetsu.android.datasource.local.file.impl.FileAppUpdateDataSource
import app.shosetsu.android.datasource.local.file.impl.FileCachedChapterDataSource
import app.shosetsu.android.datasource.local.file.impl.FileChapterDataSource
import app.shosetsu.android.datasource.local.file.impl.FileCrashDataSource
import app.shosetsu.android.datasource.local.file.impl.FileExtLibDataSource
import app.shosetsu.android.datasource.local.file.impl.FileExtensionDataSource
import app.shosetsu.android.datasource.local.file.impl.FileSharedPreferencesSettingsDataSource
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
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
 * 12 / 05 / 2020
 */
val fileDataSourceModule: DI.Module = DI.Module("file_data_source") {
	bind<IFileExtensionDataSource>() with singleton {
		FileExtensionDataSource(instance())
	}

	bind<IFileChapterDataSource>() with singleton {
		FileChapterDataSource(instance())
	}

	bind<IFileExtLibDataSource>() with singleton {
		FileExtLibDataSource(instance())
	}

	bind<IFileCachedChapterDataSource>() with singleton {
		FileCachedChapterDataSource(instance())
//		QueuedFileCacheChapterDataSource(instance())
	}

	bind<IFileCachedAppUpdateDataSource>() with singleton {
		FileAppUpdateDataSource(instance())
	}

	bind<IFileSettingsDataSource>() with singleton {
		FileSharedPreferencesSettingsDataSource(
			instance()
		)
	}
	bind<IFileCrashDataSource>() with singleton { FileCrashDataSource(instance()) }
}