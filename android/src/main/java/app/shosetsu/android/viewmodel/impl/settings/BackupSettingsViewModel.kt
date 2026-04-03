package app.shosetsu.android.viewmodel.impl.settings

import android.net.Uri
import app.shosetsu.android.backend.workers.onetime.NovelUpdateWorker
import app.shosetsu.android.common.SettingKey
import app.shosetsu.android.common.ext.launchIO
import app.shosetsu.android.common.ext.logV
import app.shosetsu.android.domain.repository.base.ISettingsRepository
import app.shosetsu.android.domain.usecases.start.StartBackupMigrationWorkerUseCase
import app.shosetsu.android.domain.usecases.start.StartBackupWorkerUseCase
import app.shosetsu.android.domain.usecases.start.StartRestoreWorkerUseCase
import app.shosetsu.android.viewmodel.abstracted.settings.ABackupSettingsViewModel
import kotlinx.coroutines.flow.MutableStateFlow

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
	private val startBackupMigrationWorker: StartBackupMigrationWorkerUseCase,
) : ABackupSettingsViewModel(iSettingsRepository) {
	override val promptMigration = MutableStateFlow(false)

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

	override fun setBackupStorageLocation(uri: Uri) {
		launchIO {
			// Get the current uri for next behaviors
			val currentUri = settingsRepo.getString(SettingKey.BackupStorageLocation)

			// set the new backup directory
			settingsRepo.setString(SettingKey.BackupStorageLocation, uri.toString())

			// If the current URI is empty, we can assume that this is the users first time setting their backup directory
			if (currentUri.isEmpty())
				promptMigration.emit(true)
		}
	}

	override fun dismissMigration() {
		promptMigration.tryEmit(false)
	}

	override fun startMigration() {
		launchIO {
			startBackupMigrationWorker()
			// dismiss *after* the worker starts.
			promptMigration.emit(false)
		}
	}
}