package app.shosetsu.android.backend.workers.onetime

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DriveFileMove
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import androidx.core.provider.DocumentsContractCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.Operation
import androidx.work.WorkInfo
import androidx.work.WorkerParameters
import app.shosetsu.android.R
import app.shosetsu.android.backend.workers.CoroutineWorkerManager
import app.shosetsu.android.backend.workers.NotificationCapable
import app.shosetsu.android.common.FilePermissionException
import app.shosetsu.android.common.NullContentResolverException
import app.shosetsu.android.common.SettingKey
import app.shosetsu.android.common.consts.LogConstants
import app.shosetsu.android.common.consts.Notifications
import app.shosetsu.android.common.consts.Notifications.ID_BACKUP_MIGRATE
import app.shosetsu.android.common.consts.WorkerTags.BACKUP_MIGRATE_WORK_ID
import app.shosetsu.android.common.enums.ExternalFileDir.APP
import app.shosetsu.android.common.ext.getString
import app.shosetsu.android.common.ext.launchIO
import app.shosetsu.android.common.ext.logE
import app.shosetsu.android.common.ext.logI
import app.shosetsu.android.common.ext.notificationBuilder
import app.shosetsu.android.common.ext.notificationManager
import app.shosetsu.android.common.ext.removeProgress
import app.shosetsu.android.common.ext.setSmallIcon
import app.shosetsu.android.common.utils.await
import app.shosetsu.android.domain.repository.base.ISettingsRepository
import app.shosetsu.android.providers.file.base.IFileSystemProvider
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.instance
import java.io.FileOutputStream

/*
 * This file is part of Shosetsu.
 *
 * Shosetsu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Shosetsu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Shosetsu.  If not, see <https://www.gnu.org/licenses/>.
 */

/**
 * 21 / 01 / 2021
 */
class MigrateBackupWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(
	appContext,
	params
), DIAware, NotificationCapable {

	companion object {
		@Deprecated("After a few versions, drop this.")
		private const val BACKUP_DIRECTORY = "Backups"
	}

	override val di: DI by closestDI(applicationContext)

	override val baseNotificationBuilder: NotificationCompat.Builder
		get() = notificationBuilder(applicationContext, Notifications.CHANNEL_BACKUP)
			.setSubText(getString(R.string.migrate_backup_subtitle))
			.setSmallIcon(Icons.AutoMirrored.Outlined.DriveFileMove)
			.setOnlyAlertOnce(true)
			.setOngoing(true)

	override val notificationManager: NotificationManagerCompat by notificationManager()
	override val notifyContext: Context = appContext
	override val defaultNotificationID: Int = ID_BACKUP_MIGRATE


	private val iFileSystemProvider: IFileSystemProvider by instance()
	private val settingsRepository: ISettingsRepository by instance()

	override suspend fun doWork(): Result {
		// The new directory to transfer to
		val newDirectory = settingsRepository.getString(SettingKey.BackupStorageLocation).toUri()

		// Content resolver to access remote directory
		val contentResolver = applicationContext.contentResolver
			?: throw NullContentResolverException()

		notify(R.string.notification_starting_with_trailer) {
			setOngoing(true)
		}

		val oldBackupFiles = iFileSystemProvider.listFiles(APP, BACKUP_DIRECTORY)

		// Loop over files in OLD internal app directories to move out.
		for ((index, file) in oldBackupFiles.withIndex()) {
			notify(applicationContext.getString(R.string.notification_migration_migrating, file)) {
				setProgress(oldBackupFiles.size, index, false)
			}

			// Get the document id of the new directory
			val docId = DocumentsContractCompat.getTreeDocumentId(newDirectory) ?: continue

			// create a proper documentUri
			val parentDocumentUri =
				DocumentsContractCompat.buildDocumentUriUsingTree(newDirectory, docId) ?: continue

			// Begin the process of creating a new external backup file using the new document uri.
			val uri = DocumentsContractCompat.createDocument(
				contentResolver,
				parentDocumentUri,
				"application/octet-stream",
				file
			) ?: continue

			// Open the remote file, and try to write to it
			val remoteFile = contentResolver.openFileDescriptor(uri, "w")

			// Ensure we have something to work with
			if (remoteFile != null) {
				remoteFile.use { descriptor ->
					FileOutputStream(descriptor.fileDescriptor).use { outputStream ->
						iFileSystemProvider.copyFileTo(APP, "$BACKUP_DIRECTORY/$file", outputStream)
					}
				}
			} else {
				// We do not have a remote file! Failure!
				val error = FilePermissionException(
					uri.path ?: "",
					FilePermissionException.PermissionType.WRITE
				)

				notify(
					notificationId = defaultNotificationID + index,
					contentText = applicationContext.getString(
						R.string.notification_migration_failure,
						file,
						error.message
					)
				) {
					setOngoing(false)
					removeProgress()
				}

				logE("Failed to move file: ${error.message}", error)

				return Result.failure()
			}

			// Delete the original backup file to save app space.
			iFileSystemProvider.deleteFile(APP, "$BACKUP_DIRECTORY/$file")
		}

		return Result.success()
	}

	/**
	 * Manager of [BackupWorker]
	 */
	class Manager(context: Context) : CoroutineWorkerManager(context) {

		override suspend fun getCount(): Int =
			getWorkerInfoList().size

		/**
		 * Returns the status of the service.
		 *
		 * @return true if the service is running, false otherwise.
		 */
		override suspend fun isRunning(): Boolean = try {
			// Is this running
			val a = (getWorkerState() == WorkInfo.State.RUNNING)

			// Don't run if update is being installed
			val b = !AppUpdateInstallWorker.Manager(context).isRunning()
			a && b
		} catch (e: Exception) {
			false
		}

		override suspend fun getWorkerState(index: Int) =
			getWorkerInfoList().getOrNull(index)?.state

		override suspend fun getWorkerInfoList(): List<WorkInfo> =
			workerManager.getWorkInfosForUniqueWork(BACKUP_MIGRATE_WORK_ID).await()

		/**
		 * Starts the service. It will be started only if there isn't another instance already
		 * running.
		 */
		override fun start(data: Data) {
			launchIO {
				logI(LogConstants.SERVICE_NEW)
				workerManager.enqueueUniqueWork(
					BACKUP_MIGRATE_WORK_ID,
					ExistingWorkPolicy.REPLACE,
					OneTimeWorkRequestBuilder<MigrateBackupWorker>(
					).setInputData(data).build()
				)
				logI(
					"Worker State ${
						workerManager.getWorkInfosForUniqueWork(BACKUP_MIGRATE_WORK_ID)
							.await()[0].state
					}"
				)
			}
		}

		/**
		 * Stops the service.
		 */
		override fun stop(): Operation =
			workerManager.cancelUniqueWork(BACKUP_MIGRATE_WORK_ID)
	}
}