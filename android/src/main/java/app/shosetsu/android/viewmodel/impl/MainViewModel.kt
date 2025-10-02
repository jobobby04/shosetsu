package app.shosetsu.android.viewmodel.impl

import app.shosetsu.android.common.SettingKey
import app.shosetsu.android.common.enums.ProductFlavors
import app.shosetsu.android.common.ext.launchIO
import app.shosetsu.android.common.utils.archURL
import app.shosetsu.android.common.utils.flavor
import app.shosetsu.android.domain.model.local.AppUpdateEntity
import app.shosetsu.android.domain.repository.base.IAppUpdatesRepository
import app.shosetsu.android.domain.repository.base.ISettingsRepository
import app.shosetsu.android.domain.usecases.IsOnlineUseCase
import app.shosetsu.android.domain.usecases.load.LoadLiveAppThemeUseCase
import app.shosetsu.android.domain.usecases.start.StartAppUpdateInstallWorkerUseCase
import app.shosetsu.android.viewmodel.abstracted.AMainViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first

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
 * 20 / 06 / 2020
 */
class MainViewModel(
	private val appUpdateRepo: IAppUpdatesRepository,
	private val isOnlineUseCase: IsOnlineUseCase,
	override val loadLiveAppThemeUseCase: LoadLiveAppThemeUseCase,
	private val startInstallWorker: StartAppUpdateInstallWorkerUseCase,
	private val settingsRepository: ISettingsRepository,
) : AMainViewModel() {

	override val openUpdate: MutableSharedFlow<UserUpdate> = MutableSharedFlow()

	override val appUpdate: MutableStateFlow<AppUpdateEntity?> = MutableStateFlow(null)


	override fun isOnline(): Boolean = isOnlineUseCase()

	override fun update() {
		launchIO {
			if (appUpdateRepo.canSelfUpdate) {
				startInstallWorker()
			} else {
				val update = appUpdateRepo.appUpdate.first()

				if (update != null) {
					openUpdate.emit(
						UserUpdate(
							update.archURL(),
							when (flavor()) {
								ProductFlavors.PLAY_STORE -> "com.android.vending"
								ProductFlavors.F_DROID -> "org.fdroid.fdroid"
								else -> null
							}
						)
					)
				}
			}
		}
	}

	override val showIntro: StateFlow<Boolean> by lazy {
		settingsRepository.getBooleanFlow(SettingKey.FirstTime)
	}
	override val showVerificationWarning: StateFlow<Boolean> by lazy {
		settingsRepository.getBooleanFlow(SettingKey.ShowVerificationWarning)
	}

	override fun dismissUpdateDialog() {
		appUpdate.value = null
	}

	override fun dismissVerificationWarning() {
		launchIO {
			settingsRepository.setBoolean(SettingKey.ShowVerificationWarning, false)
		}
	}

	init {
		launchIO {
			// Pass updates to UI
			appUpdateRepo.appUpdate.collect { it ->
				appUpdate.emit(it)
			}
		}
	}
}