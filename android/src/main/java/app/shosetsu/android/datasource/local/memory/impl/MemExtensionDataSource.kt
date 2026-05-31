package app.shosetsu.android.datasource.local.memory.impl

import app.shosetsu.android.common.consts.MEMORY_EXPIRE_EXT_LIB_TIME
import app.shosetsu.android.common.consts.MEMORY_MAX_EXTENSIONS
import app.shosetsu.android.datasource.local.memory.base.IMemExtensionsDataSource
import app.shosetsu.android.datasource.local.memory.base.ICache
import app.shosetsu.lib.IExtension
import kotlin.time.Duration.Companion.minutes

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
 */
class MemExtensionDataSource(factory: ICache.Factory) : IMemExtensionsDataSource {
	/** Map of Formatter ID to Formatter */
	private val extensionsCache: ICache<Int, IExtension> =
		factory.create(MEMORY_EXPIRE_EXT_LIB_TIME.minutes, MEMORY_MAX_EXTENSIONS)

	override fun loadExtensionFromMemory(extensionID: Int): IExtension? {
		//	logV("Loading formatter $extensionID from memory")
		return extensionsCache[extensionID]
	}

	override fun putExtensionInMemory(iExtension: IExtension) {
		//	logV("Putting formatter ${iExtension.formatterID} into memory")
		extensionsCache[iExtension.formatterID] = iExtension
	}

	override fun removeExtensionFromMemory(extensionID: Int): Boolean {
		//	logV("Removing formatter $extensionID from memory")
		return extensionsCache.remove(extensionID)
	}
}
