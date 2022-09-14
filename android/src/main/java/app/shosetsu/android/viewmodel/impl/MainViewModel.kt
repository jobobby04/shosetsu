package app.shosetsu.android.viewmodel.impl

import app.shosetsu.android.common.SettingKey
import app.shosetsu.android.common.enums.AppThemes
import app.shosetsu.android.common.enums.NavigationStyle
import app.shosetsu.android.common.ext.launchIO
import app.shosetsu.android.common.utils.archURL
import app.shosetsu.android.domain.model.local.AppUpdateEntity
import app.shosetsu.android.domain.repository.base.ISettingsRepository
import app.shosetsu.android.domain.usecases.CanAppSelfUpdateUseCase
import app.shosetsu.android.domain.usecases.IsOnlineUseCase
import app.shosetsu.android.domain.usecases.load.LoadAppUpdateFlowLiveUseCase
import app.shosetsu.android.domain.usecases.load.LoadAppUpdateUseCase
import app.shosetsu.android.domain.usecases.load.LoadBackupProgressFlowUseCase
import app.shosetsu.android.domain.usecases.load.LoadLiveAppThemeUseCase
import app.shosetsu.android.domain.usecases.settings.LoadNavigationStyleUseCase
import app.shosetsu.android.domain.usecases.settings.LoadRequireDoubleBackUseCase
import app.shosetsu.android.domain.usecases.start.StartAppUpdateInstallWorkerUseCase
import app.shosetsu.android.viewmodel.abstracted.AMainViewModel
import kotlinx.coroutines.flow.*

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
	private val loadAppUpdateFlowLiveUseCase: LoadAppUpdateFlowLiveUseCase,
	private val isOnlineUseCase: IsOnlineUseCase,
	private val loadNavigationStyleUseCase: LoadNavigationStyleUseCase,
	private val loadRequireDoubleBackUseCase: LoadRequireDoubleBackUseCase,
	private var loadLiveAppThemeUseCase: LoadLiveAppThemeUseCase,
	private val startInstallWorker: StartAppUpdateInstallWorkerUseCase,
	private val canAppSelfUpdateUseCase: CanAppSelfUpdateUseCase,
	private val loadAppUpdateUseCase: LoadAppUpdateUseCase,
	private val loadBackupProgress: LoadBackupProgressFlowUseCase,
	private val settingsRepository: ISettingsRepository
) : AMainViewModel() {

	override val requireDoubleBackToExit by lazy {
		loadRequireDoubleBackUseCase()
	}

	override fun startAppUpdateCheck(): Flow<AppUpdateEntity?> =
		loadAppUpdateFlowLiveUseCase()

	override val navigationStyle by lazy {
		loadNavigationStyleUseCase().map { NavigationStyle.values()[it] }
			.onIO()
			.stateIn(viewModelScopeIO, SharingStarted.Lazily, NavigationStyle.MATERIAL)
	}

	override fun isOnline(): Boolean = isOnlineUseCase()

	override val appThemeLiveData: SharedFlow<AppThemes> by lazy {
		loadLiveAppThemeUseCase()
			.onIO()
			.shareIn(viewModelScopeIO, SharingStarted.Lazily, replay = 1)
	}

	override fun handleAppUpdate(): Flow<AppUpdateAction?> =
		flow {
			emit(
				canAppSelfUpdateUseCase().let { canSelfUpdate ->
					if (canSelfUpdate) {
						startInstallWorker()
						AppUpdateAction.SelfUpdate
					} else {
						loadAppUpdateUseCase().let {
							AppUpdateAction.UserUpdate(
								it.version,
								it.archURL()
							)
						}
					}
				}
			)
		}.onIO()

	override val backupProgressState = loadBackupProgress()

	private val showIntro by lazy {
		settingsRepository.getBooleanFlow(SettingKey.FirstTime)
	}

	override suspend fun showIntro(): Boolean =
		settingsRepository.getBoolean(SettingKey.FirstTime)

	override fun toggleShowIntro() {
		launchIO {
			settingsRepository.setBoolean(SettingKey.FirstTime, !showIntro.value)
		}
	}
}