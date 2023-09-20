package app.shosetsu.android.providers.database.dao

import android.database.sqlite.SQLiteException
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import app.shosetsu.android.common.enums.ReadingStatus
import app.shosetsu.android.common.ext.entity
import app.shosetsu.android.common.ext.toDB
import app.shosetsu.android.common.utils.ensureSQLSizeCompliant
import app.shosetsu.android.domain.model.database.DBChapterEntity
import app.shosetsu.android.domain.model.local.ChapterEntity
import app.shosetsu.android.domain.model.local.ReaderChapterEntity
import app.shosetsu.android.domain.model.local.backup.BackupChapterEntity
import app.shosetsu.android.providers.database.dao.base.BaseDao
import app.shosetsu.lib.Novel
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
 * 23 / 04 / 2020
 *
 * @author github.com/doomsdayrs
 */
@Dao
interface ChaptersDao : BaseDao<DBChapterEntity> {

	//# Queries

	/**
	 * Gets a flow of the chapters corresponding to the novel
	 */
	@Throws(SQLiteException::class)
	@Query("SELECT * FROM chapters WHERE novelID = :novelID")
	fun getChaptersFlow(novelID: Int): Flow<List<DBChapterEntity>>

	/**
	 * Get the current chapters of a novel
	 */
	@Throws(SQLiteException::class)
	@Query("SELECT * FROM chapters WHERE novelID = :novelID")
	suspend fun getChapters(novelID: Int): List<DBChapterEntity>

	/**
	 * Gets a flow of chapters as [ReaderChapterEntity]
	 */
	@Throws(SQLiteException::class)
	@Query("SELECT id, title FROM chapters WHERE novelID = :novelID ORDER BY `order`")
	fun getReaderChaptersFlow(novelID: Int): Flow<List<ReaderChapterEntity>>


	//## Single result queries

	/**
	 * Get a chapter by its id
	 */
	@Throws(SQLiteException::class)
	@Query("SELECT * FROM chapters WHERE id = :chapterID LIMIT 1")
	suspend fun getChapter(chapterID: Int): DBChapterEntity?

	/**
	 * Get a chapter by its rowId
	 */
	@Throws(SQLiteException::class)
	@Query("SELECT * FROM chapters WHERE _rowid_ = :rowId LIMIT 1")
	suspend fun getChapter(rowId: Long): DBChapterEntity?

	//# Transactions

	private suspend inline fun <R> internalHandleNewData(
		novelId: Int,
		newData: List<Novel.Chapter>,
		inserter: (novelChapter: Novel.Chapter) -> R,
		withResult: (R) -> Unit = {}
	) {
		val dbChapters: List<DBChapterEntity> = getChapters(novelId)

		newData.forEach { newChapter ->
			// Try to find a matching chapter
			// This can be done via the chapter order or url
			val matchingChapter = dbChapters.find { it.url == newChapter.link }
				?: dbChapters.find { it.order == newChapter.order }

			if (matchingChapter != null) {
				update(
					chapterEntity = matchingChapter,
					newData = newChapter
				)
			} else {
				withResult(inserter(newChapter))
			}
		}

		deleteMissing(getChapters(novelId), newData)
	}

	/**
	 * Delete [dbChapters] that are not in [newData]
	 */
	private suspend inline fun deleteMissing(
		dbChapters: List<DBChapterEntity>,
		newData: List<Novel.Chapter>
	) {
		// Remove deleted chapters
		val removedChapters = dbChapters
			// Filter out any dbChapters that have a url matching a new chapters link
			// What remains will be chapters that are not present on the site anymore
			.filterNot { dbChapter ->
				newData.any { newChapter -> newChapter.link == dbChapter.url }
			}
			// Filter out any dbChapters that have been saved or bookmarked
			// User choice!
			.filterNot { it.isSaved || it.bookmarked }

		// Delete the given chapters
		delete(removedChapters)
	}

	/**
	 * Handle new data. Update's chapters that already exist, and insert any new chapters.
	 *
	 * @param novelId Id of the novel to work on
	 * @param extensionId Id of the extension to work with
	 * @param newData List of new data to work on
	 */
	@Transaction
	@Throws(SQLiteException::class)
	suspend fun handleNewData(
		novelId: Int,
		extensionId: Int,
		newData: List<Novel.Chapter>
	) {
		internalHandleNewData(
			novelId = novelId,
			newData = newData,
			inserter = { insertAbort(it, novelId, extensionId) }
		)
	}

	/**
	 * Handle new data. Update's chapters that already exist, and insert any new chapters.
	 *
	 * @param novelId Id of the novel to work on
	 * @param extensionId Id of the extension to work with
	 * @param newData List of new data to work on
	 *
	 * @return list of chapters that were newly inserted
	 */
	@Throws(SQLiteException::class, IndexOutOfBoundsException::class)
	@Transaction
	suspend fun handleNewDataReturn(
		novelId: Int,
		extensionId: Int,
		newData: List<Novel.Chapter>,
	): List<DBChapterEntity> {
		val newChapters = ArrayList<DBChapterEntity>()

		internalHandleNewData(
			novelId = novelId,
			newData = newData,
			inserter = { insertReturn(novelId, extensionId, it) },
			withResult = { if (it != null) newChapters.add(it) }
		)

		return newChapters
	}

	/**
	 * Insert a new [DBChapterEntity] using [novelChapter] as the data
	 *
	 * @return the new [DBChapterEntity]
	 */
	@Transaction
	@Throws(
		IndexOutOfBoundsException::class,
		SQLiteException::class,
		IndexOutOfBoundsException::class
	)
	suspend fun insertReturn(
		novelID: Int,
		extensionID: Int,
		novelChapter: Novel.Chapter,
	): DBChapterEntity? {
		val rowId = insertAbort(
			novelChapter = novelChapter,
			novelID = novelID,
			extensionID = extensionID
		)

		if (rowId < 0) throw IndexOutOfBoundsException("Invalid rowId")

		return getChapter(rowId)
	}

	/**
	 * Inserts a new [DBChapterEntity] using [novelChapter] as the data
	 *
	 * @return the rowId of the new row
	 */
	@Throws(SQLiteException::class)
	private suspend fun insertAbort(
		novelChapter: Novel.Chapter,
		novelID: Int,
		extensionID: Int
	): Long =
		insertAbort(
			novelChapter.entity(
				novelID = novelID,
				extensionID = extensionID
			).toDB()
		)

	/**
	 * Update's [chapterEntity] with [newData]
	 */
	@Throws(SQLiteException::class)
	private suspend fun update(
		chapterEntity: DBChapterEntity,
		newData: Novel.Chapter
	) {
		update(
			chapterEntity.copy(
				url = newData.link,
				title = newData.title,
				releaseDate = newData.release,
				order = newData.order
			)
		)
	}

	@Query("SELECT * FROM chapters WHERE formatterID = :extensionId")
	fun getChaptersByExtension(extensionId: Int): List<DBChapterEntity>

	@Query("SELECT readingPosition FROM chapters WHERE id = :chapterId")
	fun getChapterProgress(chapterId: Int): Flow<Double>

	@Query("SELECT bookmarked FROM chapters WHERE id = :id LIMIT 1")
	fun getChapterBookmarkedFlow(id: Int): Flow<Boolean?>

	@Query(
		"""
		UPDATE chapters
		SET readingStatus = :readingStatus
		WHERE id in (:chapterIds)
		"""
	)
	suspend fun updateChapterReadingStatus(chapterIds: List<Int>, readingStatus: ReadingStatus)

	/**
	 * Bulk transaction to ensure that [chapterIds] < 999
	 */
	@Transaction
	suspend fun updateChapterReadingStatusBulk(
		chapterIds: List<Int>,
		readingStatus: ReadingStatus
	) {
		ensureSQLSizeCompliant(chapterIds) {
			updateChapterReadingStatus(it, readingStatus)
		}
	}

	@Query(
		"""
		UPDATE chapters
		SET bookmarked = :bookmarked
		WHERE id in (:chapterIds)
		"""
	)
	suspend fun updateChapterBookmark(chapterIds: List<Int>, bookmarked: Boolean)

	/**
	 * Bulk transaction to ensure that [chapterIds] < 999
	 */
	@Transaction
	suspend fun updateChapterBookmarkBulk(chapterIds: List<Int>, bookmarked: Boolean) {
		ensureSQLSizeCompliant(chapterIds) {
			updateChapterBookmark(it, bookmarked)
		}
	}

	@Query(
		"""
		UPDATE chapters
		SET isSaved = 0
		WHERE id in (:chapterIds)
		"""
	)
	suspend fun markChaptersDeleted(chapterIds: List<Int>)

	/**
	 * Bulk transaction to ensure that [chapterIds] < 999
	 */
	@Transaction
	suspend fun markChaptersDeletedBulk(chapterIds: List<Int>) {
		ensureSQLSizeCompliant(chapterIds) {
			markChaptersDeleted(it)
		}
	}

	/**
	 * Observe a chapter
	 */
	@Throws(SQLiteException::class)
	@Query("SELECT * FROM chapters WHERE id = :chapterId LIMIT 1")
	fun getChapterFlow(chapterId: Int): Flow<DBChapterEntity?>

	@Transaction
	@Throws(SQLiteException::class)
	suspend fun restoreBackup(chapterMap: Map<BackupChapterEntity, ChapterEntity>) {
		chapterMap.onEach { (bChapter, rChapter) ->
			update(
				rChapter.copy(
					title = bChapter.name,
					bookmarked = bChapter.bookmarked,
				).let {
					when (it.readingStatus) {
						ReadingStatus.READING -> it
						ReadingStatus.READ -> it
						else -> it.copy(
							readingStatus = bChapter.rS,
							readingPosition = bChapter.rP
						)
					}
				}.toDB()
			)
		}
	}
}