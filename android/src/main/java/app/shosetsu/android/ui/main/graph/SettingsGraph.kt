package app.shosetsu.android.ui.main.graph

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import app.shosetsu.android.ui.main.Destination.SETTINGS
import app.shosetsu.android.ui.main.Destination.SETTINGS_ADVANCED
import app.shosetsu.android.ui.main.Destination.SETTINGS_BACKUP
import app.shosetsu.android.ui.main.Destination.SETTINGS_DOWNLOAD
import app.shosetsu.android.ui.main.Destination.SETTINGS_READER
import app.shosetsu.android.ui.main.Destination.SETTINGS_UPDATE
import app.shosetsu.android.ui.main.Destination.SETTINGS_VIEW
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

fun NavGraphBuilder.settingsGraph(navController: NavController) {
	navigation(startDestination = "overview", SETTINGS.route) {
		composable("overview") {
			SettingsView(
				onBack = navController::popBackStack,
				navToAdvanced = {
					navController.navigate(SETTINGS_ADVANCED.route)
				},
				navToView = {
					navController.navigate(SETTINGS_VIEW.route)
				},
				navToDownload = {
					navController.navigate(SETTINGS_DOWNLOAD.route)
				},
				navToReader = {
					navController.navigate(SETTINGS_READER.route)
				},
				navToUpdate = {
					navController.navigate(SETTINGS_UPDATE.route)
				}
			)
		}
		composable(SETTINGS_VIEW.route) {
		}
		composable(SETTINGS_UPDATE.route) {
		}
		composable(SETTINGS_ADVANCED.route) {
		}
		composable(SETTINGS_DOWNLOAD.route) {
		}
		composable(SETTINGS_BACKUP.route) {
		}
		composable(SETTINGS_READER.route) {
		}
	}
}