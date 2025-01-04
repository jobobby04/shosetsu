package app.shosetsu.android.ui.main.graph

import android.content.Intent
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import app.shosetsu.android.ui.css.CSSEditorActivity
import app.shosetsu.android.ui.main.Destination.*
import app.shosetsu.android.ui.settings.SettingsView
import app.shosetsu.android.ui.settings.sub.AdvancedSettingsView
import app.shosetsu.android.ui.settings.sub.AppearanceSettingsView
import app.shosetsu.android.ui.settings.sub.BrowseSettingsView
import app.shosetsu.android.ui.settings.sub.DownloadsSettingsView
import app.shosetsu.android.ui.settings.sub.LibrarySettingsView
import app.shosetsu.android.ui.settings.sub.ReaderSettingsView

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
		composableSub("overview") {
			SettingsView(
				onBack = navController::popBackStack,
				navToAppearance = {
					navController.navigate(SETTINGS_APPEARANCE.route)
				},
				navToLibrary = {
					navController.navigate(SETTINGS_LIBRARY.route)
				},
				navToDownloads = {
					navController.navigate(SETTINGS_DOWNLOADS.route)
				},
				navToReader = {
					navController.navigate(SETTINGS_READER.route)
				},
				navToBrowse = {
					navController.navigate(SETTINGS_BROWSE.route)
				},
				navToBackup = {
					navController.navigate(BACKUP.route)
				},
				navToAdvanced = {
					navController.navigate(SETTINGS_ADVANCED.route)
				},
				navToAbout = {
					navController.navigate(ABOUT.route)
				}
			)
		}

		composableSub(SETTINGS_APPEARANCE.route) {
			AppearanceSettingsView(
				onBack = navController::popBackStack
			)
		}
		composableSub(SETTINGS_LIBRARY.route) {
			LibrarySettingsView(
				onBack = navController::popBackStack,
				onNavToCategories = {
					navController.navigate(CATEGORIES.route)
				}
			)
		}
		composableSub(SETTINGS_BROWSE.route) {
			BrowseSettingsView(
				onBack = navController::popBackStack,
				onNavToRepositories = {
					navController.navigate(REPOSITORIES.route)
				}
			)
		}
		composableSub(SETTINGS_ADVANCED.route) {
			AdvancedSettingsView(
				onBack = navController::popBackStack
			)
		}
		composableSub(SETTINGS_DOWNLOADS.route) {
			DownloadsSettingsView(
				onBack = navController::popBackStack
			)
		}
		composableSub(SETTINGS_READER.route) {
			val hostState = remember { SnackbarHostState() }
			val context = LocalContext.current

			ReaderSettingsView(
				hostState = hostState,
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