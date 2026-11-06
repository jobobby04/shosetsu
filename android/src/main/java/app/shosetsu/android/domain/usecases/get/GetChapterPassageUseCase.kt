package app.shosetsu.android.domain.usecases.get

import android.database.sqlite.SQLiteException
import app.shosetsu.android.common.FileNotFoundException
import app.shosetsu.android.common.FilePermissionException
import app.shosetsu.android.domain.repository.base.IChaptersRepository
import app.shosetsu.android.view.uimodels.model.reader.ReaderUIItem.ReaderChapterUI
import app.shosetsu.lib.*
import org.luaj.vm2.*

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
 * 10 / 06 / 2020
 */
class GetChapterPassageUseCase(
	private val iChaptersRepository: IChaptersRepository,
	private val getExt: GetExtensionUseCase,
) {
	@Throws(
		SQLiteException::class,
		FilePermissionException::class,
		FileNotFoundException::class,
		LuaError::class,
		Exception::class,
	)
	suspend operator fun invoke(readerChapterUI: ReaderChapterUI): ByteArray? {
		// The chapter entity
		val chapterEntity = iChaptersRepository.getChapter(readerChapterUI.id) ?: return null

		// The extension, if found
		val ext = getExt(chapterEntity.extensionID)

		// Result holder
		var result: ByteArray? = null

		if (ext != null) {
			// We have the ext, so we can do a normal workflow
			result = iChaptersRepository.getChapterPassage(ext, ext.chapterType, chapterEntity)
		} else {
			// The extension is not installed, let us check every chapter type

			// Hold the last exception created by the following
			var lastException: Exception? = null

			// Loop over each chapter type to try and get a result
			// Reversed so we check HTML first
			for (chapterType in Novel.ChapterType.entries.reversed()) {
				try {
					// Get!
					result = iChaptersRepository.getChapterPassage(
						null,
						chapterType,
						chapterEntity,
					)
				} catch (e: Exception) {
					// Store the exception
					lastException = e
				}

				// Check if the result was found
				if (result != null) {
					// Found, break out of the loop
					break
				}
			}

			// Check if we didn't get a result and have an exception
			if (result == null && lastException != null) {
				// Throw the exception
				throw lastException
			}
		}

		// Return the result
		return result
	}
}
