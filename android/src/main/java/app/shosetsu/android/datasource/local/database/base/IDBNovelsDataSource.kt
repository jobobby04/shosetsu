package app.shosetsu.android.datasource.local.database.base

import android.database.sqlite.SQLiteException
import androidx.paging.PagingSource
import app.shosetsu.android.domain.model.local.*
import kotlinx.coroutines.flow.Flow

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
interface IDBNovelsDataSource {
	/** Loads a [List] of [NovelEntity]s present */
	@Throws(SQLiteException::class)
	fun loadNovels(): List<NovelEntity>

	/** Load a [List] of [NovelEntity]s that are bookmarked */
	@Throws(SQLiteException::class)
	suspend fun loadBookmarkedNovels(): List<NovelEntity>

	/** Loads a [List] okf [LibraryNovelEntity] */
	fun loadBookmarkedNovelsFlow(): Flow<List<LibraryNovelEntity>>

	/** Loads a [NovelEntity] by its [novelID] */
	@Throws(SQLiteException::class)
	suspend fun getNovel(novelID: Int): NovelEntity?

	/** Loads a [Flow] of a [NovelEntity] by its [novelID] */
	suspend fun getNovelFlow(novelID: Int): Flow<NovelEntity?>

	/** Updates a [NovelEntity] */
	@Throws(SQLiteException::class)
	suspend fun update(novelEntity: NovelEntity)

	/** Updates a list of [LibraryNovelEntity] */
	@Throws(SQLiteException::class)
	suspend fun update(list: List<LibraryNovelEntity>)

	/** Inserts a [NovelEntity] then returns its [StrippedNovelEntity] */
	@Throws(SQLiteException::class)
	suspend fun insertReturnStripped(novelEntity: NovelEntity): StrippedNovelEntity?

	/** Inserts a [NovelEntity] */
	@Throws(SQLiteException::class)
	suspend fun insert(novelEntity: NovelEntity): Long

	/**
	 * Clears out novels that have not been bookmarked
	 */
	@Throws(SQLiteException::class)
	suspend fun clearUnBookmarkedNovels()

	fun searchBookmarked(query: String): PagingSource<Int, StrippedBookmarkedNovelEntity>

	/**
	 * Get analytics
	 */
	fun getAnalytics(): Flow<List<AnalyticsNovelEntity>>
}