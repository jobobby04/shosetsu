package app.shosetsu.android.ui.main

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import app.shosetsu.android.common.ext.openInBrowser
import app.shosetsu.android.common.ext.viewModelDi
import app.shosetsu.android.ui.intro.IntroductionActivity
import app.shosetsu.android.ui.main.Destination.PrimaryWrapper
import app.shosetsu.android.ui.main.graph.ShosetsuNavController
import app.shosetsu.android.ui.main.graph.mainGraph
import app.shosetsu.android.ui.theme.ShosetsuTheme
import app.shosetsu.android.viewmodel.abstracted.AMainViewModel

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
 * @since 19 / 12 / 2023
 * @author Doomsdayrs
 */
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainView() {
	val viewModel: AMainViewModel = viewModelDi()
	val context = LocalContext.current

	val showIntro by viewModel.showIntro.collectAsState()
	val showVerificationWarning by viewModel.showVerificationWarning.collectAsState()

	// Has to happen as soon as possible
	LaunchedEffect(showIntro) {
		if (showIntro)
			context.startActivity(Intent(context, IntroductionActivity::class.java))
	}
	val theme by viewModel.appTheme.collectAsState()
	val updateToOpen by viewModel.openUpdate.collectAsState(null)
	val update by viewModel.appUpdate.collectAsState()

	val navController = ShosetsuNavController()

	val sizeClass = calculateWindowSizeClass(context as Activity)

	fun navigate(route: ShosetsuDestination) {
		navController.root.navigate(route) {
			// Pop up to the start destination of the graph to
			// avoid building up a large stack of destinations
			// on the back stack as users select items
			popUpTo(navController.root.graph.findStartDestination().id) {
				saveState = true
			}
			// Avoid multiple copies of the same destination when
			// reselecting the same item
			launchSingleTop = true
			// Restore state when reselecting a previously selected item
			restoreState = true
		}
	}

	IntentHandler(
		onNavigate = ::navigate,
		onUpdate = viewModel::update
	)

	LaunchedEffect(theme) {
		theme.setAppCompatDelegateThemeMode()
	}

	ShosetsuTheme(theme) {
		if (showVerificationWarning) {
			VerificationWarning(viewModel::dismissVerificationWarning)
		}

		NavHost(
			navController.root,
			startDestination = PrimaryWrapper
		) {
			mainGraph(
				navController,
				sizeClass,
			)
		}

		update?.let { concrete ->
			AppUpdateDialog(
				concrete,
				onDismissRequest = viewModel::dismissUpdateDialog,
				onUpdate = viewModel::update
			)
		}
	}

	LaunchedEffect(updateToOpen) {
		val userUpdate = updateToOpen ?: return@LaunchedEffect
		context.openInBrowser(userUpdate.updateURL, userUpdate.pkg)
	}
}
