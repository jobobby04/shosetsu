package app.shosetsu.android.domain.repository.base

import kotlinx.coroutines.flow.StateFlow

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
 * 17 / 01 / 2021
 *
 * Planned backup repository, handles saving and loading backups
 */
interface IBackupRepository {
	enum class BackupProgress {
		IN_PROGRESS,
		NOT_STARTED,
		COMPLETE,
		FAILURE
	}

	val backupProgress: StateFlow<BackupProgress>


	/**
	 * Update the progress of backup
	 *
	 * Will cause emission of [backupProgress]
	 */
	fun updateProgress(result: BackupProgress)
}