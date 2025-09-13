package app.shosetsu.android.viewmodel.abstracted

import app.shosetsu.android.common.enums.NavigationStyle
import app.shosetsu.android.domain.repository.base.IBackupRepository
import app.shosetsu.android.viewmodel.base.IsOnlineCheckViewModel
import app.shosetsu.android.viewmodel.base.ShosetsuRootViewModel
import kotlinx.coroutines.flow.Flow
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
abstract class AMainViewModel : ShosetsuRootViewModel(), IsOnlineCheckViewModel {

	/**
	 * If 0, Bottom
	 * If 1, Drawer
	 */
	abstract val navigationStyle: StateFlow<NavigationStyle>

	/**
	 * The app needs two presses to exit
	 */
	abstract val requireDoubleBackToExit: StateFlow<Boolean>

	/**
	 * Action to take for an update
	 */
	abstract val openUpdate: Flow<UserUpdate>

    /**
	 * An action the user is prompted with to handle an update
	 * The user has to handle the update
	 *
	 * @param pkg preferred application to open with
	 * @param updateURL url to open with
	 */
	data class UserUpdate(
		val updateURL: String,
		val pkg: String?
	)

	/**
	 *
	 */
	abstract val backupProgressState: StateFlow<IBackupRepository.BackupProgress>

	/** If the application should show the show splash screen */
	abstract val showIntro: StateFlow<Boolean>
}