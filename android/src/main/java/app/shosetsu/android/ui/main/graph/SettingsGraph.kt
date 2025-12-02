package app.shosetsu.android.ui.main.graph

import android.content.Intent
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import androidx.navigation.toRoute
import app.shosetsu.android.ui.css.CSSEditorActivity
import app.shosetsu.android.ui.main.Destination.More
import app.shosetsu.android.ui.main.Destination.More.Settings
import app.shosetsu.android.ui.main.Destination.More.Settings.Overview
import app.shosetsu.android.ui.settings.SettingsView
import app.shosetsu.android.ui.settings.sub.AdvancedSettingsView
import app.shosetsu.android.ui.settings.sub.AppearanceSettingsView
import app.shosetsu.android.ui.settings.sub.BackupView
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

fun NavGraphBuilder.settingsGraph(navController: ShosetsuNavController) {
	navigation<Settings>(Overview) {
		composableSub<Overview> {
			SettingsView(
				onBack = navController::popBackStack,
				navToAppearance = {
					navController.navigate(Settings.Appearance)
				},
				navToLibrary = {
					navController.navigate(Settings.Library)
				},
				navToDownloads = {
					navController.navigate(Settings.Downloads)
				},
				navToReader = {
					navController.navigate(Settings.Reader)
				},
				navToBrowse = {
					navController.navigate(Settings.Browse)
				},
				navToBackup = {
					navController.navigate(Settings.Backup())
				},
				navToAdvanced = {
					navController.navigate(Settings.Advanced)
				},
				navToAbout = {
					navController.navigate(More.About)
				}
			)
		}

		composableSub<Settings.Appearance> {
			AppearanceSettingsView(
				onBack = navController::popBackStack
			)
		}
		composableSub<Settings.Library> {
			LibrarySettingsView(
				onBack = navController::popBackStack,
				onNavToCategories = {
					navController.navigate(More.Categories)
				}
			)
		}
		composableSub<Settings.Reader> {
			val context = LocalContext.current
			val hostState = remember { SnackbarHostState() }

			ReaderSettingsView(
				hostState = hostState,
				onBack = navController::popBackStack,
				openCSS = {
					context.startActivity(
						Intent(context, CSSEditorActivity::class.java).apply {
							putExtra(CSSEditorActivity.CSS_ID, -1)
						},
						null
					)
				}
			)
		}
		composableSub<Settings.Downloads> {
			DownloadsSettingsView(
				onBack = navController::popBackStack
			)
		}
		composableSub<Settings.Browse> {
			BrowseSettingsView(
				onBack = navController::popBackStack,
				onNavToRepositories = {
					navController.navigate(More.Repositories)
				}
			)
		}
		composableSub<Settings.Backup> { entry ->
			BackupView(
				highlightBackupFolder = entry.toRoute<Settings.Backup>().highlightBackupFolder,
				onBack = navController::popBackStack
			)
		}
		composableSub<Settings.Advanced> {
			AdvancedSettingsView(
				onBack = navController::popBackStack
			)
		}
	}
}