package app.shosetsu.android.viewmodel.impl

import androidx.lifecycle.viewModelScope
import app.shosetsu.android.common.SettingKey
import app.shosetsu.android.common.ext.launchIO
import app.shosetsu.android.domain.repository.base.ISettingsRepository
import app.shosetsu.android.viewmodel.abstracted.AIntroViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

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
 * Shosetsu
 *
 * @since 18 / 09 / 2022
 * @author Doomsdayrs
 */
class IntroViewModel(
	private val settingsRepo: ISettingsRepository
) : AIntroViewModel() {
	override val isLicenseRead: MutableStateFlow<Boolean> = MutableStateFlow(false)

	override val shouldSupportShowNext: MutableStateFlow<Boolean> = MutableStateFlow(false)

	override fun supportShowNext() {
		shouldSupportShowNext.value = true
	}

	override fun setLicenseRead() {
		isLicenseRead.value = true
	}

	override val isACRAEnabled: StateFlow<Boolean> =
		settingsRepo.getBooleanFlow(SettingKey.ACRAEnabled)
			.stateIn(viewModelScope, SharingStarted.Lazily, false)

	override fun setACRAEnabled(boolean: Boolean) {
		launchIO {
			settingsRepo.setBoolean(SettingKey.ACRAEnabled, boolean)
		}
	}

	override var isFinished: Boolean = false

	override fun setFinished() {
		launchIO {
			isFinished = true
			settingsRepo.setBoolean(SettingKey.FirstTime, false)
		}
	}
}