package app.shosetsu.android.viewmodel.abstracted

import app.shosetsu.android.common.enums.AppThemes
import app.shosetsu.android.common.enums.NavigationStyle
import app.shosetsu.android.domain.model.local.AppUpdateEntity
import app.shosetsu.android.domain.repository.base.IBackupRepository
import app.shosetsu.android.viewmodel.base.IsOnlineCheckViewModel
import app.shosetsu.android.viewmodel.base.ShosetsuViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
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
abstract class AMainViewModel : ShosetsuViewModel(), IsOnlineCheckViewModel {
	abstract fun startAppUpdateCheck(): Flow<AppUpdateEntity?>

	/**
	 * If 0, Bottom
	 * If 1, Drawer
	 */
	abstract val navigationStyle: StateFlow<NavigationStyle>

	abstract val appThemeLiveData: SharedFlow<AppThemes>

	abstract val requireDoubleBackToExit: StateFlow<Boolean>

	/**
	 * The user requests to update the app
	 *
	 * If preview, will use in-app update for preview
	 * If stable-git, will use in-app update for stable
	 * If stable-goo, will open up google play store
	 * If stable-utd, will open up up-to-down
	 * If stable-fdr, will open up f-droid
	 */
	abstract fun handleAppUpdate(): Flow<AppUpdateAction?>

	sealed class AppUpdateAction {

		/**
		 * Shosetsu is downloading the update itself
		 */
		object SelfUpdate : AppUpdateAction()

		/**
		 * The user has to handle the update
		 *
		 * @param pkg preferred application to open with
		 */
		data class UserUpdate(
			val updateURL: String,
			val pkg: String?
		) : AppUpdateAction()

	}

	abstract val backupProgressState: StateFlow<IBackupRepository.BackupProgress>

	/** If the application should show the show splash screen */
	abstract suspend fun showIntro(): Boolean

	/** Toggle the state if show intro or not*/
	abstract fun toggleShowIntro()
}