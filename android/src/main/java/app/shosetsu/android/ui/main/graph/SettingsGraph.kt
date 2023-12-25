package app.shosetsu.android.ui.main.graph

import android.app.Activity
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import app.shosetsu.android.ui.css.CSSEditorActivity
import app.shosetsu.android.ui.main.Destination.SETTINGS
import app.shosetsu.android.ui.main.Destination.SETTINGS_ADVANCED
import app.shosetsu.android.ui.main.Destination.SETTINGS_DOWNLOAD
import app.shosetsu.android.ui.main.Destination.SETTINGS_READER
import app.shosetsu.android.ui.main.Destination.SETTINGS_UPDATE
import app.shosetsu.android.ui.main.Destination.SETTINGS_VIEW
import app.shosetsu.android.ui.settings.SettingsView
import app.shosetsu.android.ui.settings.sub.AdvancedSettingsView
import app.shosetsu.android.ui.settings.sub.DownloadSettingsView
import app.shosetsu.android.ui.settings.sub.ReaderSettingsView
import app.shosetsu.android.ui.settings.sub.UpdateSettingsView
import app.shosetsu.android.ui.settings.sub.ViewSettingsView

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
			val activity = LocalContext.current as Activity
			ViewSettingsView(
				onExit = activity::finish,
				onBack = navController::popBackStack
			)
		}
		composable(SETTINGS_UPDATE.route) {
			UpdateSettingsView(
				onBack = navController::popBackStack
			)
		}
		composable(SETTINGS_ADVANCED.route) {
			AdvancedSettingsView(
				onBack = navController::popBackStack
			)
		}
		composable(SETTINGS_DOWNLOAD.route) {
			DownloadSettingsView(
				onBack = navController::popBackStack
			)
		}
		composable(SETTINGS_READER.route) {
			val context = LocalContext.current
			ReaderSettingsView(
				onBack = navController::popBackStack,
				openCSS = {
					ContextCompat.startActivity(
						context,
						Intent(context, CSSEditorActivity::class.java).apply {
							putExtra(CSSEditorActivity.CSS_ID, -1)
						},
						null
					)
				}
			)
		}
	}
}