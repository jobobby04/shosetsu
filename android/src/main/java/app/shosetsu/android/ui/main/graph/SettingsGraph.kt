package app.shosetsu.android.ui.main.graph

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import app.shosetsu.android.ui.main.Destination
import app.shosetsu.android.ui.settings.SettingsView

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

fun NavGraphBuilder.settingsGraph() {
	navigation(startDestination = "main", Destination.SETTINGS.route) {
		composable("main") {
			SettingsView {
			}
		}
		composable(Destination.SETTINGS_VIEW.route) {
		}
		composable(Destination.SETTINGS_UPDATE.route) {
		}
		composable(Destination.SETTINGS_ADVANCED.route) {
		}
		composable(Destination.SETTINGS_DOWNLOAD.route) {
		}
		composable(Destination.SETTINGS_BACKUP.route) {
		}
		composable(Destination.SETTINGS_READER.route) {
		}
	}
}