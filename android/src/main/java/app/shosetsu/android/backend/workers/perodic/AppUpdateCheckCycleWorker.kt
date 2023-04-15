package app.shosetsu.android.backend.workers.perodic

import android.content.Context
import android.os.Build
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType.CONNECTED
import androidx.work.NetworkType.UNMETERED
import androidx.work.Operation
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkerParameters
import androidx.work.await
import app.shosetsu.android.backend.workers.CoroutineWorkerManager
import app.shosetsu.android.backend.workers.onetime.AppUpdateCheckWorker
import app.shosetsu.android.common.SettingKey.AppUpdateCycle
import app.shosetsu.android.common.SettingKey.AppUpdateOnMeteredConnection
import app.shosetsu.android.common.SettingKey.AppUpdateOnlyWhenIdle
import app.shosetsu.android.common.consts.LogConstants
import app.shosetsu.android.common.consts.WorkerTags.APP_UPDATE_CYCLE_WORK_ID
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
class AppUpdateCheckCycleWorker(
	appContext: Context,
	params: WorkerParameters
) : CoroutineWorker(appContext, params) {

	override suspend fun doWork(): Result {
		logI(LogConstants.SERVICE_EXECUTE)
		val manager = AppUpdateCheckWorker.Manager(applicationContext)
		when (manager.getWorkerState()) {
			WorkInfo.State.ENQUEUED -> {
				logI("AppUpdaterCheck is waiting to check, ignoring")
			}
			WorkInfo.State.RUNNING -> {
				logI("AppUpdaterCheck is running, ignoring")
			}
			WorkInfo.State.SUCCEEDED -> {
				logI("AppUpdaterCheck has completed, starting again")
				manager.start()
			}
			WorkInfo.State.FAILED -> {
				logI("Previous AppUpdaterCheck has failed, starting again")
				manager.start()
			}
			WorkInfo.State.BLOCKED -> {
				logI("Previous AppUpdaterCheck is blocked, ignoring")
			}
			WorkInfo.State.CANCELLED -> {
				logI("Previous AppUpdaterCheck was cancelled, starting again")
				manager.start()
			}
		}
		return Result.success()
	}


	/**
	 * Manager of [AppUpdateCheckCycleWorker]
	 */
	class Manager(context: Context) : CoroutineWorkerManager(context) {
		private val iSettingsRepository: ISettingsRepository by instance()

		private suspend fun appUpdateCycle(): Long =
			iSettingsRepository.getInt(AppUpdateCycle).toLong()

		private suspend fun appUpdateOnMetered(): Boolean =
			iSettingsRepository.getBoolean(AppUpdateOnMeteredConnection)

		private suspend fun appUpdateOnlyIdle(): Boolean =
			iSettingsRepository.getBoolean(AppUpdateOnlyWhenIdle)

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
			workerManager.getWorkInfosForUniqueWork(APP_UPDATE_CYCLE_WORK_ID).await()

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
					APP_UPDATE_CYCLE_WORK_ID,
					ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
					PeriodicWorkRequestBuilder<AppUpdateCheckCycleWorker>(
						appUpdateCycle(),
						TimeUnit.HOURS
					).setConstraints(
						Constraints.Builder().apply {
							setRequiredNetworkType(
								if (appUpdateOnMetered()) CONNECTED else UNMETERED
							)
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
								setRequiresDeviceIdle(appUpdateOnlyIdle())
						}.build()
					).build()
				)
				logI(
					"Worker State ${
						workerManager.getWorkInfosForUniqueWork(
							APP_UPDATE_CYCLE_WORK_ID
						).await()[0].state
					}"
				)
			}
		}

		/**
		 * Stops the service.
		 */
		override fun stop(): Operation = workerManager.cancelUniqueWork(APP_UPDATE_CYCLE_WORK_ID)
	}

}