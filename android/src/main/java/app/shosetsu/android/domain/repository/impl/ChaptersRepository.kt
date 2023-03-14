package app.shosetsu.android.domain.repository.impl

import android.database.sqlite.SQLiteException
import app.shosetsu.android.common.FileNotFoundException
import app.shosetsu.android.common.FilePermissionException
import app.shosetsu.android.common.enums.ReadingStatus
import app.shosetsu.android.common.ext.onIO
import app.shosetsu.android.datasource.local.database.base.IDBChaptersDataSource
import app.shosetsu.android.datasource.local.file.base.IFileCachedChapterDataSource
import app.shosetsu.android.datasource.local.file.base.IFileChapterDataSource
import app.shosetsu.android.datasource.local.memory.base.IMemChaptersDataSource
import app.shosetsu.android.datasource.remote.base.IRemoteChaptersDataSource
import app.shosetsu.android.domain.model.local.ChapterEntity
import app.shosetsu.android.domain.model.local.ReaderChapterEntity
import app.shosetsu.android.domain.repository.base.IChaptersRepository
import app.shosetsu.lib.IExtension
import app.shosetsu.lib.Novel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
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
 * 02 / 05 / 2020
 * @param memorySource Source from memory
 * @param dbSource Source from db
 * @param remoteSource Source from online
 * @param fileSource Source from storage
 */
class ChaptersRepository(
	private val memorySource: IMemChaptersDataSource,
	private val cacheSource: IFileCachedChapterDataSource,
	private val dbSource: IDBChaptersDataSource,
	private val fileSource: IFileChapterDataSource,
	private val remoteSource: IRemoteChaptersDataSource,
) : IChaptersRepository {

	private suspend inline fun placeIntoCache(
		entity: ChapterEntity,
		chapterType: Novel.ChapterType,
		result: ByteArray
	) = saveChapterPassageToMemory(entity, chapterType, result)

	@Throws(FilePermissionException::class, FileNotFoundException::class, LuaError::class)
	override suspend fun getChapterPassage(
		formatter: IExtension,
		entity: ChapterEntity,
	): ByteArray = onIO {
		return@onIO try {
			memorySource.loadChapterFromCache(entity.id!!)!!
		} catch (e: Exception) {
			try {
				cacheSource.loadChapterPassage(entity.id!!, formatter.chapterType).also { result ->
					memorySource.saveChapterInCache(entity.id!!, result)
				}
			} catch (e: Exception) {
				try {
					fileSource.load(entity, formatter.chapterType)
						.also { placeIntoCache(entity, formatter.chapterType, it) }
				} catch (e: Exception) {
					remoteSource.loadChapterPassage(
						formatter,
						entity.url
					).also { placeIntoCache(entity, formatter.chapterType, it) }
				}
			}
		}
	}

	suspend fun saveChapterPassageToMemory(
		chapterEntity: ChapterEntity,
		chapterType: Novel.ChapterType,
		passage: ByteArray,
	) {
		onIO {
			memorySource.saveChapterInCache(chapterEntity.id!!, passage)
			cacheSource.saveChapterInCache(chapterEntity.id!!, chapterType, passage)
		}
	}


	/**
	 *
	 * 1. save to memory
	 * 2. save to filesystem
	 * 3. if filesystem save was a success, then update the chapter
	 * 4. finally evaluate with and between 1&&(2||3)
	 */

	/**
	 * We want to ensure that the [passage] is saved either to [memorySource] or [fileSource] first off
	 * After that, then we can update the [entity] to say [passage] was properly saved
	 */
	@Throws(FilePermissionException::class, IOException::class, SQLiteException::class)
	override suspend fun saveChapterPassageToStorage(
		entity: ChapterEntity,
		chapterType: Novel.ChapterType,
		passage: ByteArray,
	) {
		onIO {
			saveChapterPassageToMemory(entity, chapterType, passage)

			fileSource.save(entity, chapterType, passage)

			dbSource.updateChapter(entity.copy(isSaved = true))
		}
	}

	@Throws(SQLiteException::class)
	override suspend fun handleChapters(
		novelID: Int,
		extensionID: Int, list: List<Novel.Chapter>,
	) = onIO { dbSource.handleChapters(novelID, extensionID, list) }

	@Throws(IndexOutOfBoundsException::class, SQLiteException::class)
	override suspend fun handleChaptersReturn(
		novelID: Int,
		extensionID: Int, list: List<Novel.Chapter>,
	): List<ChapterEntity> = onIO {
		dbSource.handleChapterReturn(novelID, extensionID, list)
	}

	override suspend fun getChaptersLive(novelID: Int): Flow<List<ChapterEntity>> =
		dbSource.getChaptersFlow(novelID).distinctUntilChanged().onIO()

	@Throws(SQLiteException::class)
	override suspend fun getChapters(novelID: Int): List<ChapterEntity> =
		onIO { dbSource.getChapters(novelID) }

	@Throws(SQLiteException::class)
	override suspend fun getChaptersByExtension(extensionId: Int): List<ChapterEntity> =
		onIO { dbSource.getChaptersByExtension(extensionId) }

	@Throws(SQLiteException::class)
	override suspend fun updateChapter(chapterEntity: ChapterEntity): Unit =
		onIO { dbSource.updateChapter(chapterEntity) }

	@Throws(SQLiteException::class)
	override suspend fun getChapter(chapterID: Int): ChapterEntity? =
		onIO { dbSource.getChapter(chapterID) }

	override fun getReaderChaptersFlow(
		novelID: Int,
	): Flow<List<ReaderChapterEntity>> =
		dbSource.getReaderChapters(novelID).distinctUntilChanged().onIO()

	@Throws(SQLiteException::class, FilePermissionException::class)
	override suspend fun deleteChapterPassage(
		chapterEntity: ChapterEntity,
		chapterType: Novel.ChapterType
	) = onIO {
		dbSource.updateChapter(
			chapterEntity.copy(
				isSaved = false
			)
		)
		fileSource.delete(chapterEntity, chapterType)
	}

	override suspend fun deleteChapterPassage(
		chapters: List<ChapterEntity>,
		chapterType: Novel.ChapterType
	) {
		onIO {
			try {
				fileSource.delete(chapters, chapterType)
			} finally {
				dbSource.markChaptersDeleted(chapters.map { it.id!! })
			}
		}
	}

	@Throws(SQLiteException::class)
	override suspend fun delete(entity: ChapterEntity) =
		onIO { dbSource.delete(entity) }

	override suspend fun delete(entity: List<ChapterEntity>) {
		onIO { dbSource.delete(entity) }
	}

	override fun getChapterProgress(chapter: ReaderChapterEntity): Flow<Double> =
		dbSource.getChapterProgress(chapter.id).distinctUntilChanged().onIO()

	override fun getChapterBookmarkedFlow(id: Int): Flow<Boolean?> =
		dbSource.getChapterBookmarkedFlow(id).distinctUntilChanged().onIO()

	override suspend fun updateChapterReadingStatus(
		chapterIds: List<Int>,
		readingStatus: ReadingStatus
	) = onIO {
		dbSource.updateChapterReadingStatus(chapterIds, readingStatus)
	}

	override suspend fun updateChapterBookmark(
		chapterIds: List<Int>,
		bookmarked: Boolean
	) = onIO {
		dbSource.updateChapterBookmark(chapterIds, bookmarked)
	}

	override fun getChapterFlow(chapterId: Int): Flow<ChapterEntity?> =
		dbSource.getChapterFlow(chapterId).onIO()

}