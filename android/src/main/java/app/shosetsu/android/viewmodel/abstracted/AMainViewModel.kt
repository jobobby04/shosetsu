package app.shosetsu.android.viewmodel.abstracted

import app.shosetsu.android.domain.model.local.AppUpdateEntity
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
	 * App update, if any
	 */
	abstract val appUpdate: StateFlow<AppUpdateEntity?>

	/**
	 * Action to take for an update
	 */
	abstract val openUpdate: Flow<UserUpdate>

	/**
	 * The user requests to update the app
	 *
	 * If preview, will use in-app update for preview
	 * If stable-git, will use in-app update for stable
	 * If stable-goo, will open up google play store
	 * If stable-utd, will open up up-to-down
	 * If stable-fdr, will open up f-droid
	 */
	abstract fun update()

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

	/** If the application should show the show splash screen */
	abstract val showIntro: StateFlow<Boolean>

	/**
	 * Warning in regards to Google locking down the Android ecosystem.
	 */
	abstract val showVerificationWarning: StateFlow<Boolean>

	/**
	 * Dismiss the update dialog
	 */
	abstract fun dismissUpdateDialog()

	/**
	 * Dismiss the verification warning dialog.
	 */
	abstract fun dismissVerificationWarning()
}