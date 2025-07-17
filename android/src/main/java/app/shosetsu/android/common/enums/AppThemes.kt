package app.shosetsu.android.common.enums

import androidx.appcompat.app.AppCompatDelegate

/*
 * This file is part of Shosetsu.
 *
 * Shosetsu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Shosetsu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Shosetsu.  If not, see <https://www.gnu.org/licenses/>.
 */

/**
 * shosetsu
 * 22 / 11 / 2020
 */
enum class AppThemes(val key: Int, private val appCompatId: Int) {
	FOLLOW_SYSTEM(0, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM),
	LIGHT(1, AppCompatDelegate.MODE_NIGHT_NO),
	DARK(2, AppCompatDelegate.MODE_NIGHT_YES);

	companion object {
		fun fromKey(key: Int): AppThemes = entries.find { it.key == key } ?: FOLLOW_SYSTEM
	}

	fun setAppCompatDelegateThemeMode() = AppCompatDelegate.setDefaultNightMode(appCompatId)
}