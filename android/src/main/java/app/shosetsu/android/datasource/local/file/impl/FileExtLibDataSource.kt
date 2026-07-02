package app.shosetsu.android.datasource.local.file.impl

import app.shosetsu.android.common.FileNotFoundException
import app.shosetsu.android.common.FilePermissionException
import app.shosetsu.android.common.consts.FILE_LIBRARY_DIR
import app.shosetsu.android.common.enums.InternalFileDir.FILES
import app.shosetsu.android.common.ext.logE
import app.shosetsu.android.common.ext.logV
import app.shosetsu.android.datasource.local.file.base.IFileExtLibDataSource
import app.shosetsu.android.domain.model.local.ExtLibEntity
import app.shosetsu.android.providers.file.base.IFileSystemProvider
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
class FileExtLibDataSource(
	private val iFileSystemProvider: IFileSystemProvider,
) : IFileExtLibDataSource {
	init {
		logV("Creating required directories")
		try {
			iFileSystemProvider.createDirectory(FILES, FILE_LIBRARY_DIR)
			logV("Created required directories")
		} catch (e: Exception) {
			logE("Error on creation of directories", e)
		}
	}

	private fun makeOldLibraryFile(entity: ExtLibEntity): String =
		"$FILE_LIBRARY_DIR/${entity.scriptName}.lua"

	private fun makeRepoLibraryFile(entity: ExtLibEntity): String =
		"$FILE_LIBRARY_DIR/${entity.repoID}/${entity.scriptName}.lua"


	@Throws(FilePermissionException::class, IOException::class, CharacterCodingException::class)
	override suspend fun writeExtLib(entity: ExtLibEntity, data: String) {
		// Create the repo directory if it does not exist
		iFileSystemProvider.createDirectory(FILES, "$FILE_LIBRARY_DIR/${entity.repoID}/")

		// Write the new file
		iFileSystemProvider.writeFile(
			FILES,
			makeRepoLibraryFile(entity),
			data.encodeToByteArray()
		)
	}

	@Throws(FileNotFoundException::class, FilePermissionException::class)
	override suspend fun loadExtLib(entity: ExtLibEntity): String {
		// Check if the repo file is present
		return if (iFileSystemProvider.doesFileExist(FILES, makeRepoLibraryFile(entity))) {
			// Read from the repo specific file
			iFileSystemProvider.readFile(FILES, makeRepoLibraryFile(entity)).decodeToString()
		} else {
			// Read from the
			iFileSystemProvider.readFile(
				FILES,
				makeOldLibraryFile(entity)
			).decodeToString()
		}
	}

	@Throws(FileNotFoundException::class, FilePermissionException::class)
	override suspend fun deleteExtLib(entity: ExtLibEntity) {
		iFileSystemProvider.deleteFile(FILES, makeOldLibraryFile(entity))
		iFileSystemProvider.deleteFile(FILES, makeRepoLibraryFile(entity))
	}
}