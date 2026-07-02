package app.shosetsu.android.datasource.local.file.impl

import app.shosetsu.android.common.FileNotFoundException
import app.shosetsu.android.common.FilePermissionException
import app.shosetsu.android.common.consts.FILE_SCRIPT_DIR
import app.shosetsu.android.common.enums.InternalFileDir.FILES
import app.shosetsu.android.common.ext.logV
import app.shosetsu.android.common.ext.logW
import app.shosetsu.android.common.utils.asIEntity
import app.shosetsu.android.common.utils.fileExtension
import app.shosetsu.android.datasource.local.file.base.IFileExtensionDataSource
import app.shosetsu.android.domain.model.local.GenericExtensionEntity
import app.shosetsu.android.providers.file.base.IFileSystemProvider
import app.shosetsu.lib.IExtension
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
class FileExtensionDataSource(
	private val iFileSystemProvider: IFileSystemProvider
) : IFileExtensionDataSource {
	init {
		logV("Creating required directories")
		try {
			iFileSystemProvider.createDirectory(FILES, FILE_SCRIPT_DIR)
			logV("Created required directories")
		} catch (e: Exception) {
			logV("Error on creation of directories `$FILE_SCRIPT_DIR`", e)
		}
	}

	/**
	 * The old directory style, kept for sanity
	 */
	private fun makeOldExtensionFilePath(entity: GenericExtensionEntity): String =
		"$FILE_SCRIPT_DIR${entity.fileName}.${entity.type.fileExtension}"

	/**
	 * The new directory style, using the repo id to prevent file collisions
	 */
	private fun makeRepoExtensionFilePath(entity: GenericExtensionEntity): String =
		"$FILE_SCRIPT_DIR/${entity.repoID}/${entity.fileName}.${entity.type.fileExtension}"

	@Throws(FileNotFoundException::class, FilePermissionException::class, IOException::class)
	override suspend fun loadExtension(entity: GenericExtensionEntity): IExtension {
		// Create new repo path
		val repoPath = makeRepoExtensionFilePath(entity)

		// Try to read the extension
		if (iFileSystemProvider.doesFileExist(FILES, repoPath)) {
			return entity.asIEntity(iFileSystemProvider.readFile(FILES, repoPath))
		} else {
			// the repo file does not exist, lets do some work here...
			logW("Extension not found via repo sub directory, checking old dir...")

			// Create the old path
			val oldPath = makeOldExtensionFilePath(entity)

			// Try it again! Throwing if we do not find it
			return entity.asIEntity(iFileSystemProvider.readFile(FILES, oldPath))
		}
	}

	@Throws(FilePermissionException::class, IOException::class)
	override suspend fun writeExtension(entity: GenericExtensionEntity, data: ByteArray) {
		// Create the repository subdirectory
		iFileSystemProvider.createDirectory(FILES, FILE_SCRIPT_DIR + "/${entity.repoID}/")

		iFileSystemProvider.writeFile(
			FILES,
			makeRepoExtensionFilePath(entity),
			data
		)
	}


	@Throws(FilePermissionException::class)
	override suspend fun deleteExtension(entity: GenericExtensionEntity) {
		iFileSystemProvider.deleteFile(FILES, makeRepoExtensionFilePath(entity))
	}
}