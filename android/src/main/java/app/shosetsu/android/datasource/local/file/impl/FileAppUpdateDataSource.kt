package app.shosetsu.android.datasource.local.file.impl

import app.shosetsu.android.common.FileNotFoundException
import app.shosetsu.android.common.FilePermissionException
import app.shosetsu.android.common.consts.APP_UPDATE_CACHE_FILE
import app.shosetsu.android.common.enums.InternalFileDir.CACHE
import app.shosetsu.android.common.ext.logE
import app.shosetsu.android.common.ext.logV
import app.shosetsu.android.datasource.local.file.base.IFileCachedAppUpdateDataSource
import app.shosetsu.android.domain.model.local.AppUpdateEntity
import app.shosetsu.android.domain.model.remote.AppUpdateDTO
import app.shosetsu.android.providers.file.base.IFileSystemProvider
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException
import java.io.InputStream

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
 * 07 / 09 / 2020
 */
class FileAppUpdateDataSource(
	private val iFileSystemProvider: IFileSystemProvider
) : IFileCachedAppUpdateDataSource {

	init {
		try {
			iFileSystemProvider.createDirectory(CACHE, updatesPath)
			logV("Created directory: `$updatesPath`")
		} catch (e: Exception) {
			logE("Failed to create directory", e)
		}
	}

	@Throws(FilePermissionException::class, IOException::class)
	private fun write(debugAppUpdate: AppUpdateDTO) =
		iFileSystemProvider.writeFile(
			CACHE,
			APP_UPDATE_CACHE_FILE,
			Json.encodeToString(debugAppUpdate).encodeToByteArray()
		)

	@Throws(FileNotFoundException::class, FilePermissionException::class)
	override suspend fun load(): AppUpdateEntity =
		Json.decodeFromString<AppUpdateDTO>(
			iFileSystemProvider.readFile(CACHE, APP_UPDATE_CACHE_FILE).decodeToString()
		).convertTo()


	@Throws(FilePermissionException::class, IOException::class)
	override suspend fun save(
		appUpdate: AppUpdateEntity
	) {
		write(AppUpdateDTO.fromEntity(appUpdate))
	}

	@Throws(IOException::class, FilePermissionException::class, FileNotFoundException::class)
	override fun writeAPK(
		appUpdate: AppUpdateEntity,
		bytes: InputStream
	): String {
		// Ensure no previous file exists
		if (iFileSystemProvider.doesFileExist(CACHE, updatesCPath))
			iFileSystemProvider.deleteFile(CACHE, updatesCPath)

		// Create the new file
		iFileSystemProvider.createFile(CACHE, updatesCPath)

		// Write to the new file
		iFileSystemProvider.writeFile(CACHE, updatesCPath, bytes)

		// Return the file path
		return iFileSystemProvider.retrievePath(CACHE, updatesCPath)
	}

	companion object {
		const val updatesPath = "/updates/"
		const val updatesFile = "/update.apk"
		const val updatesCPath = "$updatesPath$updatesFile"
	}


}