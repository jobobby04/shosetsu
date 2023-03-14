package app.shosetsu.android.datasource.local.file.base

import app.shosetsu.android.common.FileNotFoundException
import app.shosetsu.android.common.FilePermissionException
import app.shosetsu.android.domain.model.local.ChapterEntity
import app.shosetsu.lib.Novel
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
 * 12 / 05 / 2020
 */
interface IFileChapterDataSource {
	/**
	 * Save the chapter passage to storage
	 */
	@Throws(FilePermissionException::class, IOException::class)
	suspend fun save(
		chapterEntity: ChapterEntity,
		chapterType: Novel.ChapterType,
		passage: ByteArray
	)

	/**
	 * Gets chapter passage via it's ID
	 */
	@Throws(FilePermissionException::class, FileNotFoundException::class)
	suspend fun load(
		chapterEntity: ChapterEntity,
		chapterType: Novel.ChapterType
	): ByteArray

	/** Deletes a chapter from the filesystem */
	@Throws(FilePermissionException::class)
	suspend fun delete(
		chapterEntity: ChapterEntity,
		chapterType: Novel.ChapterType
	)

	/** Deletes chapters from the filesystem */
	@Throws(FilePermissionException::class)
	suspend fun delete(
		chapterEntities: List<ChapterEntity>,
		chapterType: Novel.ChapterType
	)
}