package app.shosetsu.android.viewmodel.impl.settings

import androidx.work.WorkInfo
import app.shosetsu.android.backend.workers.onetime.DownloadWorker
import app.shosetsu.android.common.SettingKey.DownloadOnLowBattery
import app.shosetsu.android.common.SettingKey.DownloadOnLowStorage
import app.shosetsu.android.common.SettingKey.DownloadOnMeteredConnection
import app.shosetsu.android.common.SettingKey.DownloadOnlyWhenIdle
import app.shosetsu.android.common.ext.launchIO
import app.shosetsu.android.domain.repository.base.ISettingsRepository
import app.shosetsu.android.viewmodel.abstracted.settings.ADownloadSettingsViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

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
class DownloadSettingsViewModel(
	iSettingsRepository: ISettingsRepository,
	private val manager: DownloadWorker.Manager
) : ADownloadSettingsViewModel(iSettingsRepository) {


	override val notifyRestartWorker = MutableStateFlow<Boolean>(false)

	init {
		launchIO {
			settingsRepo.getBooleanFlow(DownloadOnlyWhenIdle)
				.combine(settingsRepo.getBooleanFlow(DownloadOnLowStorage)) { a, b -> a to b }
				.combine(settingsRepo.getBooleanFlow(DownloadOnLowBattery)) { a, b -> a to b }
				.combine(settingsRepo.getBooleanFlow(DownloadOnMeteredConnection)) { a, b -> a to b }
				.collect {
					if (manager.getCount() != 0 && manager.getWorkerState() == WorkInfo.State.ENQUEUED)
						notifyRestartWorker.value = true
				}
		}
	}

	override fun restartDownloadWorker() {
		manager.stop()
		manager.start()
		notifyRestartWorker.value = false
	}

	override fun dismissNotifyRestartWorker() {
		notifyRestartWorker.value = false
	}
}