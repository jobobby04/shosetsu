package app.shosetsu.android.ui.main.graph

import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import app.shosetsu.android.ui.css.CSSEditorActivity
import app.shosetsu.android.ui.main.Destination.More.Settings
import app.shosetsu.android.ui.main.Destination.More.Settings.Overview
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
	navigation<Settings>(Overview) {
		composableSub<Overview> {
			SettingsView(
				onBack = navController::popBackStack,
				navToAdvanced = {
					navController.navigate(Settings.Advanced)
				},
				navToView = {
					navController.navigate(Settings.View)
				},
				navToDownload = {
					navController.navigate(Settings.Download)
				},
				navToReader = {
					navController.navigate(Settings.Reader)
				},
				navToUpdate = {
					navController.navigate(Settings.Update)
				}
			)
		}

		composableSub<Settings.View> {
			ViewSettingsView(
				onBack = navController::popBackStack
			)
		}
		composableSub<Settings.Update> {
			UpdateSettingsView(
				onBack = navController::popBackStack
			)
		}
		composableSub<Settings.Advanced> {
			AdvancedSettingsView(
				onBack = navController::popBackStack
			)
		}
		composableSub<Settings.Download> {
			DownloadSettingsView(
				onBack = navController::popBackStack
			)
		}
		composableSub<Settings.Reader> {
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