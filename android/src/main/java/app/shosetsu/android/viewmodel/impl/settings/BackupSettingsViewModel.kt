package app.shosetsu.android.viewmodel.impl.settings

import android.content.Context
import android.net.Uri
import androidx.core.provider.DocumentsContractCompat
import app.shosetsu.android.backend.workers.onetime.NovelUpdateWorker
import app.shosetsu.android.common.FilePermissionException
import app.shosetsu.android.common.NullContentResolverException
import app.shosetsu.android.common.SettingKey
import app.shosetsu.android.common.enums.ExternalFileDir.APP
import app.shosetsu.android.common.ext.launchIO
import app.shosetsu.android.common.ext.logV
import app.shosetsu.android.domain.repository.base.ISettingsRepository
import app.shosetsu.android.domain.usecases.start.StartBackupWorkerUseCase
import app.shosetsu.android.domain.usecases.start.StartRestoreWorkerUseCase
import app.shosetsu.android.providers.file.base.IFileSystemProvider
import app.shosetsu.android.viewmodel.abstracted.settings.ABackupSettingsViewModel
import java.io.FileOutputStream

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
 * 31 / 08 / 2020
 */
class BackupSettingsViewModel(
	iSettingsRepository: ISettingsRepository,
	private val manager: NovelUpdateWorker.Manager,
	private val startBackupWorkerUseCase: StartBackupWorkerUseCase,
	private val startRestoreWorker: StartRestoreWorkerUseCase,
	private val iFileSystemProvider: IFileSystemProvider,
) : ABackupSettingsViewModel(iSettingsRepository) {

	override fun startBackup() {
		launchIO {
			if (manager.isRunning()) manager.stop()
			startBackupWorkerUseCase()
		}
	}

	override fun restore(uri: Uri) {
		logV("Restoring: $uri")
		startRestoreWorker(uri)
	}

	override suspend fun setBackupStorageLocation(context: Context, uri: Uri) {
		val contentResolver = context.applicationContext.contentResolver
			?: throw NullContentResolverException()
		for (file in iFileSystemProvider.listFiles(APP, BACKUP_DIRECTORY)) {
			val data = iFileSystemProvider.readFile(APP, "$BACKUP_DIRECTORY/$file")
			val docId = DocumentsContractCompat.getTreeDocumentId(uri) ?: continue
			val parentDocumentUri = DocumentsContractCompat.buildDocumentUriUsingTree(uri, docId) ?: continue
			val uri = DocumentsContractCompat.createDocument(
				contentResolver,
				parentDocumentUri,
				"application/octet-stream",
				file
			) ?: continue
			contentResolver.openFileDescriptor(uri, "w")?.use { descriptor ->
				FileOutputStream(descriptor.fileDescriptor).use {
					it.write(data)
				}
			} ?: throw FilePermissionException(
				uri.path ?: "",
				FilePermissionException.PermissionType.WRITE
			)
			iFileSystemProvider.deleteFile(APP, "$BACKUP_DIRECTORY/$file")
		}
		settingsRepo.setString(SettingKey.BackupStorageLocation, uri.toString())
	}

	companion object {
		private const val BACKUP_DIRECTORY = "Backups"
	}
}