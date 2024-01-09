package app.shosetsu.android.viewmodel.impl

import app.shosetsu.android.common.SettingKey
import app.shosetsu.android.common.enums.AppThemes
import app.shosetsu.android.common.enums.NavigationStyle
import app.shosetsu.android.common.enums.ProductFlavors
import app.shosetsu.android.common.ext.launchIO
import app.shosetsu.android.common.utils.archURL
import app.shosetsu.android.common.utils.flavor
import app.shosetsu.android.domain.model.local.AppUpdateEntity
import app.shosetsu.android.domain.repository.base.IAppUpdatesRepository
import app.shosetsu.android.domain.repository.base.IBackupRepository
import app.shosetsu.android.domain.repository.base.ISettingsRepository
import app.shosetsu.android.domain.usecases.IsOnlineUseCase
import app.shosetsu.android.domain.usecases.load.LoadLiveAppThemeUseCase
import app.shosetsu.android.domain.usecases.settings.LoadNavigationStyleUseCase
import app.shosetsu.android.domain.usecases.settings.LoadRequireDoubleBackUseCase
import app.shosetsu.android.domain.usecases.start.StartAppUpdateInstallWorkerUseCase
import app.shosetsu.android.viewmodel.abstracted.AMainViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
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
 * shosetsu
 * 20 / 06 / 2020
 */
class MainViewModel(
	private val appUpdateRepo: IAppUpdatesRepository,
	private val isOnlineUseCase: IsOnlineUseCase,
	loadNavigationStyleUseCase: LoadNavigationStyleUseCase,
	private val loadRequireDoubleBackUseCase: LoadRequireDoubleBackUseCase,
	loadLiveAppThemeUseCase: LoadLiveAppThemeUseCase,
	private val startInstallWorker: StartAppUpdateInstallWorkerUseCase,
	backupRepo: IBackupRepository,
	private val settingsRepository: ISettingsRepository
) : AMainViewModel() {

	override val requireDoubleBackToExit: StateFlow<Boolean> by lazy {
		loadRequireDoubleBackUseCase()
	}

	override val openUpdate: MutableSharedFlow<UserUpdate> = MutableSharedFlow()

	override val appUpdate: MutableStateFlow<AppUpdateEntity?> = MutableStateFlow(null)

	override val navigationStyle: StateFlow<NavigationStyle> =
		loadNavigationStyleUseCase().map {
			if (it) {
				NavigationStyle.LEGACY
			} else {
				NavigationStyle.MATERIAL
			}
		}
			.stateIn(viewModelScopeIO, SharingStarted.Eagerly, NavigationStyle.MATERIAL)


	override fun isOnline(): Boolean = isOnlineUseCase()

	override val appTheme: StateFlow<AppThemes> =
		loadLiveAppThemeUseCase()
			.stateIn(viewModelScopeIO, SharingStarted.Lazily, AppThemes.FOLLOW_SYSTEM)

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

	override val backupProgressState: StateFlow<IBackupRepository.BackupProgress> =
		backupRepo.backupProgress

	override val showIntro: StateFlow<Boolean> by lazy {
		settingsRepository.getBooleanFlow(SettingKey.FirstTime)
	}

	override fun dismissUpdateDialog() {
		appUpdate.value = null
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