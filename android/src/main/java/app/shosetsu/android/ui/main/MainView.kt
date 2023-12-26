package app.shosetsu.android.ui.main

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.shosetsu.android.R
import app.shosetsu.android.common.enums.AppThemes
import app.shosetsu.android.common.enums.NavigationStyle
import app.shosetsu.android.common.ext.viewModelDi
import app.shosetsu.android.domain.repository.base.IBackupRepository.BackupProgress
import app.shosetsu.android.ui.main.Destination.BROWSE
import app.shosetsu.android.ui.main.Destination.LIBRARY
import app.shosetsu.android.ui.main.Destination.MORE
import app.shosetsu.android.ui.main.Destination.UPDATES
import app.shosetsu.android.ui.main.graph.mainGraph
import app.shosetsu.android.ui.theme.ShosetsuTheme
import app.shosetsu.android.viewmodel.abstracted.AMainViewModel
import kotlinx.coroutines.launch

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
@Composable
fun MainView() {
	val viewModel = viewModelDi<AMainViewModel>()
	val backupProgressState by viewModel.backupProgressState.collectAsState()
	val theme by viewModel.appTheme.collectAsState()
	val navStyle by viewModel.navigationStyle.collectAsState()
	val isMaterial = navStyle == NavigationStyle.MATERIAL
	val isLegacy = navStyle == NavigationStyle.LEGACY

	val context = LocalContext.current
	val navController = rememberNavController()
	val navBackStackEntry by navController.currentBackStackEntryAsState()
	val drawerState = rememberDrawerState(DrawerValue.Closed)
	val scope = rememberCoroutineScope()

	val destinations = listOf(
		LIBRARY,
		UPDATES,
		BROWSE,
		MORE
	)

	val sizeClass = calculateWindowSizeClass(context as Activity)

	BackHandler(drawerState.isOpen) {
		scope.launch {
			drawerState.close()
		}
	}

	fun navigate(destination: Destination) {
		navController.navigate(destination.route) {
			// Pop up to the start destination of the graph to
			// avoid building up a large stack of destinations
			// on the back stack as users select items
			popUpTo(navController.graph.findStartDestination().id) {
				saveState = true
			}
			// Avoid multiple copies of the same destination when
			// reselecting the same item
			launchSingleTop = true
			// Restore state when reselecting a previously selected item
			restoreState = true
		}
	}

	ShosetsuTheme(
		darkTheme = when (theme) {
			AppThemes.FOLLOW_SYSTEM -> isSystemInDarkTheme()
			AppThemes.LIGHT -> false
			AppThemes.DARK -> true
		}
	) {
		ModalNavigationDrawer(
			drawerContent = {
				NavigationDrawerContent(
					destinations,
					navBackStackEntry,
					onNavigate = {
						navigate(it)
						scope.launch {
							drawerState.close()
						}
					}
				)
			},
			drawerState = drawerState,
			gesturesEnabled = isLegacy
		) {
			Scaffold(
				bottomBar = {
					AnimatedVisibility(isMaterial) {
						BottomNavigationBar(
							destinations,
							navBackStackEntry,
							::navigate
						)
					}
				},
				topBar = {
					AnimatedVisibility(
						backupProgressState == BackupProgress.IN_PROGRESS,
						enter = slideInVertically(),
						exit = slideOutVertically()
					) {
						BackupProgressIndicator()
					}
				}
			) { paddingValues ->
				NavHost(
					navController,
					startDestination = LIBRARY.route,
					modifier = Modifier.padding(paddingValues)
				) {
					mainGraph(
						navController,
						sizeClass,
						drawerIcon = {
							AnimatedVisibility(isLegacy) {
								IconButton(
									onClick = {
										scope.launch {
											drawerState.open()
										}
									}
								) {
									Icon(
										Icons.Default.Menu,
										stringResource(R.string.navigation_drawer_open)
									)
								}
							}
						}
					)
				}
			}
		}
	}
}


@Composable
fun <T> BottomNavigationBar(
	destinations: List<T>,
	currentDestination: NavBackStackEntry?,
	onNavigate: (Destination) -> Unit
) where T : Destination, T : Root {
	var isVisible by remember { mutableStateOf(true) }

	isVisible = destinations.any { destination ->
		currentDestination?.destination?.route == destination.route ||
				currentDestination?.destination?.route == "main"
	}

	if (isVisible) {
		NavigationBar {
			destinations.forEach { destination ->
				NavigationBarItem(
					selected =
					currentDestination?.destination?.route == destination.route,
					icon = {
						Icon(
							painterResource(
								destination.icon
							),
							destination.route
						)
					},
					label = {
						Text(destination.route)
					},
					onClick = {
						onNavigate(destination)
					}
				)
			}
		}
	}
}
