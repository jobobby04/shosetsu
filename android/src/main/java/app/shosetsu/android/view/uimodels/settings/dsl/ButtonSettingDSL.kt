package app.shosetsu.android.view.uimodels.settings.dsl

import android.view.View
import app.shosetsu.android.view.uimodels.settings.ButtonSettingData
import app.shosetsu.android.view.uimodels.settings.base.SettingsItemData
import app.shosetsu.android.view.uimodels.settings.base.SettingsListBuilder

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
 * 25 / 06 / 2020
 */

@SettingsItemDSL
inline fun buttonSettingData(
	id: Int,
	action: ButtonSettingData.() -> Unit,
): SettingsItemData = ButtonSettingData(id).also(action)

@SettingsItemDSL
inline fun SettingsListBuilder.buttonSettingData(
	id: Int,
	action: ButtonSettingData.() -> Unit,
): Unit = this.let { list.add(ButtonSettingData(id).also(action)) }

@SettingsItemDSL
inline fun ButtonSettingData.onButtonClicked(
	crossinline action: ButtonSettingData.(
		@ParameterName("view") View,
	) -> Unit,
) {
	buttonOnClickListener = { action(it) }
}
