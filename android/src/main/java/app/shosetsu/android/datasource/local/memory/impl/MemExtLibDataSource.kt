package app.shosetsu.android.datasource.local.memory.impl

import app.shosetsu.android.common.consts.MEMORY_EXPIRE_EXTENSION_TIME
import app.shosetsu.android.common.consts.MEMORY_MAX_EXT_LIBS
import app.shosetsu.android.datasource.local.memory.base.IMemExtLibDataSource
import app.shosetsu.android.datasource.local.memory.base.ICache
import kotlin.time.Duration.Companion.hours

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
 * 13 / 05 / 2020
 */
class MemExtLibDataSource(factory: ICache.Factory) : IMemExtLibDataSource {
	/** Library paring */
	private val libraries: ICache<String, String> =
		factory.create(MEMORY_EXPIRE_EXTENSION_TIME.hours, MEMORY_MAX_EXT_LIBS)

	override fun loadLibrary(name: String): String? {
		//logV("Loading $name from memory (success?: ${result != null})")
		return libraries[name]
	}

	override fun setLibrary(name: String, data: String) {
		//logV("Putting $name into memory")
		libraries[name] = data
	}

	override fun removeLibrary(name: String) {
		libraries.remove(name)
	}

	override fun clearExtLibs() {
		libraries.clear()
	}
}
