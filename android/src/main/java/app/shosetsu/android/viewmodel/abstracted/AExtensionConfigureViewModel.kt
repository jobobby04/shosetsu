package app.shosetsu.android.viewmodel.abstracted

import app.shosetsu.android.domain.model.local.FilterEntity
import app.shosetsu.android.view.uimodels.model.InstalledExtensionUI
import app.shosetsu.android.viewmodel.base.ShosetsuViewModel
import app.shosetsu.android.viewmodel.base.SubscribeViewModel
import kotlinx.collections.immutable.ImmutableList
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
 * 16 / 07 / 2020
 *
 * This file is mainly to configure settings of a formatter
 *
 * [liveData] is of the formatter object itself
 */
abstract class AExtensionConfigureViewModel
	: ShosetsuViewModel(), SubscribeViewModel<InstalledExtensionUI?> {

	abstract val extensionSettings: StateFlow<ImmutableList<FilterEntity>>


	/** Set the extension ID to use */
	abstract fun setExtensionID(id: Int)

	/**
	 * Uninstall this extension
	 */
	abstract fun uninstall(extension: InstalledExtensionUI)

	/**
	 * Destroy this controller
	 */
	abstract fun destroy()

	abstract fun saveSetting(id: Int, value: String)
	abstract fun saveSetting(id: Int, value: Boolean)
	abstract fun saveSetting(id: Int, value: Int)
}