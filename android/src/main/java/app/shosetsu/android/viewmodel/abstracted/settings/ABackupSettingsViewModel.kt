package app.shosetsu.android.viewmodel.abstracted.settings

import android.net.Uri
import app.shosetsu.android.domain.repository.base.ISettingsRepository
import kotlinx.coroutines.flow.StateFlow

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
abstract class ABackupSettingsViewModel(iSettingsRepository: ISettingsRepository) :
	ASubSettingsViewModel(iSettingsRepository) {

	/**
	 * If the UI is expected to show the user a prompt to migrate their backups
	 */
	abstract val promptMigration: StateFlow<Boolean>

	/** Order the app to create a new backup now */
	abstract fun startBackup()

	/**
	 * Load backup via the uri
	 */
	abstract fun restore(uri: Uri)

	/**
	 * Set the backup storage location
	 */
	abstract fun setBackupStorageLocation(uri: Uri)

	/**
	 * Dismiss the migration pop-up
	 */
	abstract fun dismissMigration()

	/**
	 * Start the migration of backups, and dismiss the dialog.
	 */
	abstract fun startMigration()
}