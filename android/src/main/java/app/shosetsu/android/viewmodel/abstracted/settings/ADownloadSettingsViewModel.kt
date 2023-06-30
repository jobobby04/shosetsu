package app.shosetsu.android.viewmodel.abstracted.settings

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
abstract class ADownloadSettingsViewModel(iSettingsRepository: ISettingsRepository) :
	ASubSettingsViewModel(iSettingsRepository) {
	abstract val notifyRestartWorker: StateFlow<Boolean>
	abstract fun restartDownloadWorker()
	abstract fun dismissNotifyRestartWorker()
}