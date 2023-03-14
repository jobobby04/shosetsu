package app.shosetsu.android.domain.repository.base

import android.database.sqlite.SQLiteException
import app.shosetsu.android.common.FileNotFoundException
import app.shosetsu.android.common.FilePermissionException
import app.shosetsu.android.common.enums.ReadingStatus
import app.shosetsu.android.domain.model.local.ChapterEntity
import app.shosetsu.android.domain.model.local.ReaderChapterEntity
import app.shosetsu.lib.IExtension
import app.shosetsu.lib.Novel
import kotlinx.coroutines.flow.Flow
import org.luaj.vm2.LuaError
import java.io.IOException

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
 * 30 / 04 / 2020
 *
 * @author github.com/doomsdayrs
 */
interface IChaptersRepository {
	/**
	 * Loads a [ChapterEntity]s text
	 * First checks memory
	 * Then checks storage
	 * Then checks network
	 *
	 * Saves successful passages into caches
	 *
	 */
	@Throws(FilePermissionException::class, FileNotFoundException::class, LuaError::class)
	suspend fun getChapterPassage(
		formatter: IExtension,
		entity: ChapterEntity,
	): ByteArray

	/**
	 * Save the [ChapterEntity] [passage] to storage
	 *
	 * Will not save into any caches, as is assuming content retrieved from [getChapterPassage]
	 *
	 */
	@Throws(FilePermissionException::class, IOException::class, SQLiteException::class)
	suspend fun saveChapterPassageToStorage(
		entity: ChapterEntity,
		chapterType: Novel.ChapterType,
		passage: ByteArray
	)

	/**
	 * Handles chapters for ze novel
	 */
	@Throws(SQLiteException::class)
	suspend fun handleChapters(
		novelID: Int,
		extensionID: Int,
		list: List<Novel.Chapter>
	)

	/**
	 * Handles chapters return, but returns the chapters that are new
	 */
	@Throws(IndexOutOfBoundsException::class, SQLiteException::class)
	suspend fun handleChaptersReturn(
		novelID: Int,
		extensionID: Int,
		list: List<Novel.Chapter>,
	): List<ChapterEntity>

	/**
	 * Loads [ChapterEntity]s matching [novelID] in a [Flow]
	 */
	suspend fun getChaptersLive(novelID: Int): Flow<List<ChapterEntity>>

	@Throws(SQLiteException::class)
	suspend fun getChapters(novelID: Int): List<ChapterEntity>

	@Throws(SQLiteException::class)
	suspend fun getChaptersByExtension(extensionId: Int): List<ChapterEntity>

	/**
	 * Loads a [ChapterEntity] by its [chapterID]
	 */
	@Throws(SQLiteException::class)
	suspend fun getChapter(chapterID: Int): ChapterEntity?

	/**
	 * Update [chapterEntity] in database
	 */
	@Throws(SQLiteException::class)
	suspend fun updateChapter(chapterEntity: ChapterEntity)

	/**
	 * Loads [ReaderChapterEntity]s by it's [novelID]
	 */
	fun getReaderChaptersFlow(novelID: Int): Flow<List<ReaderChapterEntity>>

	/**
	 * Delete the chapter passage from storage
	 *
	 * Also deletes from memory and cache
	 */
	@Throws(SQLiteException::class, FilePermissionException::class)
	suspend fun deleteChapterPassage(
		chapterEntity: ChapterEntity,
		chapterType: Novel.ChapterType
	)

	/**
	 * Delete the chapter passages from storage
	 *
	 * Also deletes from memory and cache
	 */
	@Throws(SQLiteException::class, FilePermissionException::class)
	suspend fun deleteChapterPassage(
		chapters: Array<ChapterEntity>,
		chapterType: Novel.ChapterType
	)

	@Throws(SQLiteException::class)
	suspend fun delete(entity: ChapterEntity)

	@Throws(SQLiteException::class)
	suspend fun delete(entity: List<ChapterEntity>)

	fun getChapterProgress(chapter: ReaderChapterEntity): Flow<Double>

	fun getChapterBookmarkedFlow(id: Int): Flow<Boolean?>
	suspend fun updateChapterReadingStatus(chapterIds: List<Int>, readingStatus: ReadingStatus)
	suspend fun updateChapterBookmark(chapterIds: List<Int>, bookmarked: Boolean)

	/**
	 * Observe a chapter and it's changes
	 */
	fun getChapterFlow(chapterId: Int): Flow<ChapterEntity?>
}