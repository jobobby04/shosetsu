package app.shosetsu.android.datasource.local.memory

import app.shosetsu.android.datasource.local.memory.base.IMemChaptersDataSource
import app.shosetsu.android.datasource.local.memory.base.IMemExtLibDataSource
import app.shosetsu.android.datasource.local.memory.base.IMemExtensionsDataSource
import app.shosetsu.android.datasource.local.memory.impl.GuavaMemChaptersDataSource
import app.shosetsu.android.datasource.local.memory.impl.GuavaMemExtLibDataSource
import app.shosetsu.android.datasource.local.memory.impl.GuavaMemExtensionDataSource
import org.kodein.di.DI
import org.kodein.di.bind
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
 * 04 / 05 / 2020
 * These modules handle cached data that is in memory
 */
val memoryDataSourceModule: DI.Module = DI.Module("cache_data_source") {
	bind<IMemChaptersDataSource>() with singleton {
		GuavaMemChaptersDataSource()
	}

	bind<IMemExtensionsDataSource>() with singleton {
		GuavaMemExtensionDataSource()
	}

	bind<IMemExtLibDataSource>() with singleton {
		GuavaMemExtLibDataSource()
	}
}