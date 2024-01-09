package app.shosetsu.android.viewmodel.abstracted.settings

import androidx.lifecycle.LiveData
import app.shosetsu.android.domain.repository.base.ISettingsRepository
import kotlinx.coroutines.flow.Flow

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
abstract class AAdvancedSettingsViewModel(iSettingsRepository: ISettingsRepository) :
	ASubSettingsViewModel(iSettingsRepository) {

	sealed interface PurgeState {
		data object Success : PurgeState
		data class Failure(val exception: Exception) : PurgeState
	}

	abstract val purgeState: Flow<PurgeState>

	sealed interface RestartResult {
		data object RESTARTED : RestartResult
		data object KILLED : RestartResult
	}

	abstract val workerState: Flow<RestartResult>

	/**
	 * Executes a purge async, provides a [LiveData] for result
	 */
	abstract fun purgeUselessData()

	abstract fun killCycleWorkers()
	abstract fun startCycleWorkers()
	abstract fun forceRepoSync()
}