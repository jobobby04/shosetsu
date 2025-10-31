package app.shosetsu.android.viewmodel.impl

import app.shosetsu.android.common.SettingKey
import app.shosetsu.android.domain.repository.base.ISettingsRepository
import app.shosetsu.android.domain.usecases.IsOnlineUseCase
import app.shosetsu.android.domain.usecases.load.LoadLiveAppThemeUseCase
import app.shosetsu.android.viewmodel.abstracted.AMainViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
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
 * 20 / 06 / 2020
 */
class MainViewModel(
	private val isOnlineUseCase: IsOnlineUseCase,
	override val loadLiveAppThemeUseCase: LoadLiveAppThemeUseCase,
	private val settingsRepository: ISettingsRepository,
) : AMainViewModel() {

	override val openUpdate: MutableSharedFlow<UserUpdate> = MutableSharedFlow()

	override fun isOnline(): Boolean = isOnlineUseCase()

	override val showIntro: StateFlow<Boolean> by lazy {
		settingsRepository.getBooleanFlow(SettingKey.FirstTime)
	}
}