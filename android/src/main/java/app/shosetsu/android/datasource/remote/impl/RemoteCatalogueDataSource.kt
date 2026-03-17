package app.shosetsu.android.datasource.remote.impl

import app.shosetsu.android.datasource.remote.base.IRemoteCatalogueDataSource
import app.shosetsu.lib.IExtension
import app.shosetsu.lib.Novel
import org.luaj.vm2.LuaError

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
 * shosetsu
 * 10 / May / 2020
 */
class RemoteCatalogueDataSource : IRemoteCatalogueDataSource {
	override suspend fun loadListing(
		ext: IExtension,
		listing: IExtension.Listing.Item,
		data: Map<Int, Any>,
		page: Int,
	): List<Novel.Info> {
		return if (!listing.isIncrementing && page > ext.startIndex) {
			emptyList()
		} else try {
            listing.getListing(data, page).toList()
		} catch (e: LuaError) {
			throw e.cause ?: e
		}
	}

	override suspend fun search(
		ext: IExtension,
		search: IExtension.Listing.Search,
		query: String?,
		filters: Map<Int, Any>,
		page: Int,
	): List<Novel.Info> {
		val query = query ?: ""
		return if (!search.isIncrementing && page > ext.startIndex) {
			emptyList()
		} else try {
			search.getListing(query, filters, page)?.toList() ?: emptyList()
		} catch (e: LuaError) {
			throw e.cause ?: e
		}
	}
}
