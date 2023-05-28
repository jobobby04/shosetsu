package app.shosetsu.android.datasource.local.memory

import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.M
import app.shosetsu.android.common.FLAG_CONCURRENT_MEMORY
import app.shosetsu.android.datasource.local.memory.base.IMemChaptersDataSource
import app.shosetsu.android.datasource.local.memory.base.IMemExtLibDataSource
import app.shosetsu.android.datasource.local.memory.base.IMemExtensionsDataSource
import app.shosetsu.android.datasource.local.memory.impl.*
import app.shosetsu.android.datasource.local.memory.impl.concurrent.ConcurrentMemChaptersDataSource
import app.shosetsu.android.datasource.local.memory.impl.concurrent.ConcurrentMemExtLibDataSource
import app.shosetsu.android.datasource.local.memory.impl.concurrent.ConcurrentMemExtensionDataSource
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
		if (SDK_INT <= M) {
			if (FLAG_CONCURRENT_MEMORY) {
				ConcurrentMemChaptersDataSource()
			} else {
				GenericMemChaptersDataSource()
			}
		} else {
			GuavaMemChaptersDataSource()
		}
	}

	bind<IMemExtensionsDataSource>() with singleton {
		if (SDK_INT <= M) {
			if (FLAG_CONCURRENT_MEMORY) {
				ConcurrentMemExtensionDataSource()
			} else {
				GenericMemExtensionDataSource()
			}
		} else {
			GuavaMemExtensionDataSource()
		}
	}

	bind<IMemExtLibDataSource>() with singleton {
		if (SDK_INT <= M) {
			if (FLAG_CONCURRENT_MEMORY) {
				ConcurrentMemExtLibDataSource()
			} else {
				GenericMemExtLibDataSource()
			}
		} else {
			GuavaMemExtLibDataSource()
		}
	}

}