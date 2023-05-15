package app.shosetsu.android.viewmodel.abstracted

import app.shosetsu.android.viewmodel.base.ShosetsuViewModel
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
 * Shosetsu
 *
 * @since 18 / 09 / 2022
 * @author Doomsdayrs
 */
abstract class AIntroViewModel : ShosetsuViewModel() {

	abstract val shouldSupportShowNext: StateFlow<Boolean>

	abstract fun supportShowNext()

	/**
	 * Is license read, the user must read it
	 */
	abstract val isLicenseRead: StateFlow<Boolean>

	/**
	 * Set license as read
	 */
	abstract fun setLicenseRead()

	/**
	 * Is ACRA enabled
	 */
	abstract val isACRAEnabled: StateFlow<Boolean>

	/**
	 * Set ACRA reporting enabled
	 */
	abstract fun setACRAEnabled(boolean: Boolean)


	/**
	 * If the user finished the entire introduction or not
	 */
	abstract val isFinished: Boolean

	/**
	 * The user has been introduced, continuing
	 */
	abstract fun setFinished()
}