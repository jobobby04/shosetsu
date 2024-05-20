package app.shosetsu.android.domain.usecases.get

import android.database.sqlite.SQLiteException
import androidx.paging.PagingSource
import androidx.paging.PagingState
import app.shosetsu.android.common.ext.convertTo
import app.shosetsu.android.common.ext.logE
import app.shosetsu.android.domain.repository.base.INovelsRepository
import app.shosetsu.android.view.uimodels.model.catlog.ACatalogNovelUI
import app.shosetsu.lib.IExtension
import app.shosetsu.lib.LISTING_INDEX
import app.shosetsu.lib.PAGE_INDEX
import app.shosetsu.lib.QUERY_INDEX
import app.shosetsu.lib.exceptions.HTTPException
import coil.network.HttpException
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.luaj.vm2.LuaError
import java.io.IOException
import javax.net.ssl.SSLException

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
 * 15 / 05 / 2020
 */
class GetCatalogueListingDataUseCase(
	private val novelsRepository: INovelsRepository,
) {
	inner class MyPagingSource(
		private val iExtension: IExtension,
		val data: Map<Int, Any>,
		private val listing: IExtension.Listing.Item?,
	) : PagingSource<Int, ACatalogNovelUI>() {
		override fun getRefreshKey(state: PagingState<Int, ACatalogNovelUI>): Int? {
			return state.anchorPosition?.let {
				state.closestPageToPosition(it)?.prevKey?.plus(1)
					?: state.closestPageToPosition(it)?.nextKey?.minus(1)
			}
		}

		private var lastPage: List<ACatalogNovelUI> = emptyList()

		override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ACatalogNovelUI> {
			return withContext(Dispatchers.IO) {
				try {
					// Key may be null during a refresh, if no explicit key is passed into Pager
					// construction. Use 0 as default, because our API is indexed started at index 0
					val pageNumber = params.key ?: iExtension.startIndex

					// Suspending network load via Retrofit. This doesn't need to be wrapped in a
					// withContext(Dispatcher.IO) { ... } block since Retrofit's Coroutine
					// CallAdapter dispatches on a worker thread.
					val response =
						search(
							iExtension,
							HashMap(data).also {
								it[PAGE_INDEX] = pageNumber
								it[QUERY_INDEX] = ""
								it[LISTING_INDEX] = listing?.link
							},
							listing // todo remove
						)

					// Since 0 is the lowest page number, return null to signify no more pages should
					// be loaded before it.
					val prevKey = if (pageNumber > iExtension.startIndex) pageNumber - 1 else null

					// This API defines that it's out of data when a page returns empty. When out of
					// data, we return `null` to signify no more pages should be loaded
					val nextKey = if (response.isNotEmpty() && lastPage != response) {
						lastPage = response
						pageNumber + 1
					} else {
						null
					}
					LoadResult.Page(
						data = response,
						prevKey = prevKey,
						nextKey = nextKey
					)
				} catch (e: IOException) {
					LoadResult.Error(e)
				} catch (e: HTTPException) {
					LoadResult.Error(e)
				} catch (e: HttpException) {
					LoadResult.Error(e)
				} catch (e: LuaError) {
					LoadResult.Error(e)
				} catch (e: IllegalArgumentException) {
					LoadResult.Error(e)
				}
			}
		}
	}

	@Throws(SSLException::class, LuaError::class)
	operator fun invoke(
		iExtension: IExtension,
		data: Map<Int, Any>,
		listing: IExtension.Listing.Item?,
	) = MyPagingSource(iExtension, data, listing)

	@Throws(SSLException::class, LuaError::class)
	suspend fun search(
		iExtension: IExtension,
		data: Map<Int, Any>,
		listing: IExtension.Listing.Item?,
	): List<ACatalogNovelUI> =
		novelsRepository.getCatalogueData(
			iExtension,
			listing,
			data
		).let { list ->
			list.mapNotNull { novelListing ->
				val ne = novelListing.convertTo(iExtension)
				// For each, insert and return a stripped card
				// This operation is to pre-cache URL and ID so loading occurs smoothly
				try {
					novelsRepository.insertReturnStripped(ne)
						?.let { (id, title, imageURL, bookmarked) ->
							ACatalogNovelUI(
								id = id,
								title = title,
								imageURL = imageURL,
								bookmarked = bookmarked,
								language = novelListing.language,
								description = novelListing.description,
								status = novelListing.status,
								tags = novelListing.tags.asList().toImmutableList(),
								genres = novelListing.genres.asList().toImmutableList(),
								authors = novelListing.authors.asList().toImmutableList(),
								artists = novelListing.artists.asList().toImmutableList(),
								chapters = novelListing.chapters.asList().toImmutableList(),
								chapterCount = novelListing.chapterCount,
								wordCount = novelListing.wordCount,
								commentCount = novelListing.commentCount,
								viewCount = novelListing.viewCount,
								favoriteCount = novelListing.favoriteCount
							)
						}
				} catch (e: SQLiteException) {
					logE("Failed to load parse novel", e)
					null
				}
			}
		}

}