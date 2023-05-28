package app.shosetsu.android.backend.workers.perodic

import android.content.Context
import android.os.Build
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.Operation
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkerParameters
import androidx.work.await
import app.shosetsu.android.backend.workers.CoroutineWorkerManager
import app.shosetsu.android.backend.workers.onetime.BackupWorker
import app.shosetsu.android.common.SettingKey.BackupCycle
import app.shosetsu.android.common.SettingKey.BackupOnLowBattery
import app.shosetsu.android.common.SettingKey.BackupOnLowStorage
import app.shosetsu.android.common.SettingKey.BackupOnlyWhenIdle
import app.shosetsu.android.common.consts.LogConstants
import app.shosetsu.android.common.consts.WorkerTags.BACKUP_CYCLE_WORK_ID
import app.shosetsu.android.common.ext.launchIO
import app.shosetsu.android.common.ext.logI
import app.shosetsu.android.domain.repository.base.ISettingsRepository
import org.kodein.di.instance
import java.util.concurrent.TimeUnit

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
 * 06 / 09 / 2020
 */
class BackupCycleWorker(
	appContext: Context,
	params: WorkerParameters
) : CoroutineWorker(appContext, params) {

	override suspend fun doWork(): Result {
		logI(LogConstants.SERVICE_EXECUTE)
		val manager = BackupWorker.Manager(applicationContext)
		when (manager.getWorkerState()) {
			WorkInfo.State.ENQUEUED -> {
				logI("BackupWorker is waiting to backup, ignoring")
			}
			WorkInfo.State.RUNNING -> {
				logI("BackupWorker is running, ignoring")
			}
			WorkInfo.State.SUCCEEDED -> {
				logI("BackupWorker has completed, starting again")
				manager.start()
			}
			WorkInfo.State.FAILED -> {
				logI("Previous BackupWorker has failed, starting again")
				manager.start()
			}
			WorkInfo.State.BLOCKED -> {
				logI("Previous BackupWorker is blocked, ignoring")
			}
			WorkInfo.State.CANCELLED -> {
				logI("Previous BackupWorker was cancelled, starting again")
				manager.start()
			}
		}
		return Result.success()
	}

	/**
	 * Manager of [BackupCycleWorker]
	 */
	class Manager(context: Context) : CoroutineWorkerManager(context) {
		private val iSettingsRepository: ISettingsRepository by instance()

		private suspend fun backupCycle(): Long =
			iSettingsRepository.getInt(BackupCycle).toLong()

		private suspend fun requiresBackupOnIdle(): Boolean =
			iSettingsRepository.getBoolean(BackupOnlyWhenIdle)

		private suspend fun allowsBackupOnLowStorage(): Boolean =
			iSettingsRepository.getBoolean(BackupOnLowStorage)

		private suspend fun allowsBackupOnLowBattery(): Boolean =
			iSettingsRepository.getBoolean(BackupOnLowBattery)

		/**
		 * Returns the status of the service.
		 *
		 * @return true if the service is running, false otherwise.
		 */
		override suspend fun isRunning(): Boolean = try {
			getWorkerState() == WorkInfo.State.RUNNING
		} catch (e: Exception) {
			false
		}

		override suspend fun getWorkerState(index: Int): WorkInfo.State =
			getWorkerInfoList()[index].state

		override suspend fun getWorkerInfoList(): List<WorkInfo> =
			workerManager.getWorkInfosForUniqueWork(BACKUP_CYCLE_WORK_ID).await()

		override suspend fun getCount(): Int =
			getWorkerInfoList().size

		/**
		 * Starts the service. It will be started only if there isn't another instance already
		 * running.
		 */
		override fun start(data: Data) {
			launchIO {
				logI(LogConstants.SERVICE_NEW)
				workerManager.enqueueUniquePeriodicWork(
					BACKUP_CYCLE_WORK_ID,
					ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
					PeriodicWorkRequestBuilder<BackupCycleWorker>(
						backupCycle(),
						TimeUnit.HOURS
					).setConstraints(
						Constraints.Builder().apply {
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
								setRequiresDeviceIdle(requiresBackupOnIdle())
							setRequiresBatteryNotLow(!allowsBackupOnLowBattery())
							setRequiresStorageNotLow(!allowsBackupOnLowStorage())
						}.build()
					).build()
				)
				logI(
					"Worker State ${
						workerManager.getWorkInfosForUniqueWork(
							BACKUP_CYCLE_WORK_ID
						).await()[0].state
					}"
				)
			}
		}

		/**
		 * Stops the service.
		 */
		override fun stop(): Operation = workerManager.cancelUniqueWork(BACKUP_CYCLE_WORK_ID)
	}

}