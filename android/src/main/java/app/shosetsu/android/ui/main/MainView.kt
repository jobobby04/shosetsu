package app.shosetsu.android.ui.main

import android.app.Activity
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import app.shosetsu.android.R
import app.shosetsu.android.common.consts.BundleKeys
import app.shosetsu.android.common.ext.getNovelID
import app.shosetsu.android.common.ext.openChapter
import app.shosetsu.android.ui.about.AboutView
import app.shosetsu.android.ui.browse.BrowseView
import app.shosetsu.android.ui.catalogue.CatalogueView
import app.shosetsu.android.ui.library.LibraryView
import app.shosetsu.android.ui.main.Destination.ABOUT
import app.shosetsu.android.ui.main.Destination.ADD_SHARE
import app.shosetsu.android.ui.main.Destination.ANALYTICS
import app.shosetsu.android.ui.main.Destination.BROWSE
import app.shosetsu.android.ui.main.Destination.CATALOG
import app.shosetsu.android.ui.main.Destination.CATEGORIES
import app.shosetsu.android.ui.main.Destination.CONFIGURE_EXTENSION
import app.shosetsu.android.ui.main.Destination.DOWNLOADS
import app.shosetsu.android.ui.main.Destination.HISTORY
import app.shosetsu.android.ui.main.Destination.LIBRARY
import app.shosetsu.android.ui.main.Destination.MIGRATION
import app.shosetsu.android.ui.main.Destination.MORE
import app.shosetsu.android.ui.main.Destination.NOVEL
import app.shosetsu.android.ui.main.Destination.REPOSITORY
import app.shosetsu.android.ui.main.Destination.SEARCH
import app.shosetsu.android.ui.main.Destination.SETTINGS
import app.shosetsu.android.ui.main.Destination.SETTINGS_ADVANCED
import app.shosetsu.android.ui.main.Destination.SETTINGS_BACKUP
import app.shosetsu.android.ui.main.Destination.SETTINGS_DOWNLOAD
import app.shosetsu.android.ui.main.Destination.SETTINGS_READER
import app.shosetsu.android.ui.main.Destination.SETTINGS_UPDATE
import app.shosetsu.android.ui.main.Destination.SETTINGS_VIEW
import app.shosetsu.android.ui.main.Destination.TEXT_READER
import app.shosetsu.android.ui.main.Destination.UPDATES
import app.shosetsu.android.ui.migration.MigrationView
import app.shosetsu.android.ui.more.MoreView
import app.shosetsu.android.ui.novel.NovelInfoView
import app.shosetsu.android.ui.settings.SettingsView
import app.shosetsu.android.ui.settings.sub.TextAssetReaderView
import app.shosetsu.android.ui.theme.ShosetsuTheme
import app.shosetsu.android.ui.updates.UpdatesView

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
	val context = LocalContext.current
	val navController = rememberNavController()
	val currentDestination = navController.currentDestination
	val navBackStackEntry by navController.currentBackStackEntryAsState()

	val destinations = listOf(
		LIBRARY,
		UPDATES,
		BROWSE,
		MORE
	)

	val sizeClass = calculateWindowSizeClass(context as Activity)

	ShosetsuTheme {
		Scaffold(
			bottomBar = {
				BottomNavigationBar(destinations, currentDestination, navController)
			},
		) { paddingValues ->
			NavHost(
				navController,
				startDestination = LIBRARY.route,
				modifier = Modifier.padding(paddingValues)
			) {
				composable(LIBRARY.route) {
					LibraryView(
						onOpenNovel = { novelId ->
							navController.navigate(NOVEL.routeWith(novelId))
						},
						onMigrate = {
						}
					)
				}
				browseGraph()
				moreGraph(navController)
				composable(TEXT_READER.route) {
					TextAssetReaderView(
						0,
						setViewTitle = {},
					)
				}
				composable(UPDATES.route) {
					UpdatesView(
						openNovel = { novelId ->
							navController.navigate(NOVEL.routeWith(novelId))
						},
						openChapter = { novelId, chapterId ->
							context.openChapter(novelId, chapterId)
						},
					)
				}
				composable(
					NOVEL.route, arguments = NOVEL.arguments
				) { entry ->
					val novelId = entry.arguments!!.getNovelID();

					NovelInfoView(
						resume = null,
						invalidateOptionsMenu = {},
						displayOfflineSnackBar = {},
						makeSnackBar = { null },
						refresh = {},
						windowSize = sizeClass
					)
				}
				composable(MIGRATION.route) {
					MigrationView(intArrayOf())
				}
			}
		}
	}
}

@Composable
fun <T> BottomNavigationBar(
	destinations: List<T>,
	currentDestination: NavDestination?,
	navController: NavController
) where T : Destination, T : Root {
	NavigationBar {
		destinations.forEach { destination ->
			NavigationBarItem(
				selected =
				currentDestination?.hierarchy?.any {
					it.route == destination.route
				} == true,
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
			)
		}
	}
}

fun NavGraphBuilder.browseGraph() {
	navigation("main", BROWSE.route) {
		composable("main") {
			BrowseView(
				onRefresh = {},
				installExtension = { _, _ -> },
				openCatalogue = {},
				openSettings = {},
				fab = null,
				openRepositories = {}
			)
		}
		composable(CATALOG.route) { entry ->
			val novelId = entry.arguments!!.getInt(BundleKeys.BUNDLE_EXTENSION)
			CatalogueView(
				onOpenNovel = {},
				errorMessage = { _, _ -> },
				openInWebView = {},
				makeSnackBar = { _, _ -> null }
			)
		}
		composable(CONFIGURE_EXTENSION.route) {
		}

		composable(SEARCH.route) {
		}
	}
}

fun NavGraphBuilder.moreGraph(navController: NavHostController) {
	navigation(startDestination = "main", MORE.route) {
		composable("main") {
			MoreView(
				makeSnackBar = {},
				navigateSafely = { _, _ -> }
			)
		}
		composable(ABOUT.route) {
			AboutView(
				onOpenLicense = {
					TODO("Bind")
				},
				onBack = {
					navController.popBackStack()
				}
			)
		}
		composable(CATEGORIES.route) {
		}
		composable(DOWNLOADS.route) {
		}

		composable(ADD_SHARE.route) {
		}
		composable(REPOSITORY.route) {
		}

		composable(HISTORY.route) {
		}
		composable(ANALYTICS.route) {
		}

		settingsGraph()
	}
}

fun NavGraphBuilder.settingsGraph() {
	navigation(startDestination = "main", SETTINGS.route) {
		composable("main") {
			SettingsView {
			}
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

interface Root {
	@get:DrawableRes
	val icon: Int
}

sealed class Destination {
	open val route: String
		get() = this::class.simpleName!!

	open val arguments: List<NamedNavArgument> = emptyList()

	data object SETTINGS : Destination()
	data object SETTINGS_VIEW : Destination()
	data object SETTINGS_UPDATE : Destination()
	data object SETTINGS_ADVANCED : Destination()
	data object SETTINGS_DOWNLOAD : Destination()
	data object SETTINGS_BACKUP : Destination()
	data object SETTINGS_READER : Destination()
	data object CATEGORIES : Destination()
	data object MORE : Destination(), Root {
		override val icon: Int = R.drawable.ic_baseline_more_horiz_24
	}

	data object ABOUT : Destination()
	data object TEXT_READER : Destination()
	data object DOWNLOADS : Destination()
	data object ANALYTICS : Destination()
	data object ADD_SHARE : Destination()
	data object HISTORY : Destination()
	data object UPDATES : Destination(), Root {
		override val icon: Int = R.drawable.update
	}

	data object NOVEL : Destination() {
		override val route: String = "novel/{novelId}"
		override val arguments: List<NamedNavArgument> =
			listOf(
				navArgument("novelId") { type = NavType.IntType }
			)

		fun routeWith(novelId: Int): String =
			"novel/$novelId"
	}

	data object SEARCH : Destination()
	data object REPOSITORY : Destination()
	data object MIGRATION : Destination()
	data object CATALOG : Destination() {
		override val route: String = "catalog/{extId}"
		override val arguments: List<NamedNavArgument> =
			listOf(
				navArgument("extId") { type = NavType.IntType }
			)
	}

	data object BROWSE : Destination(), Root {
		override val icon: Int = R.drawable.navigation_arrow
	}

	object CONFIGURE_EXTENSION : Destination()
	object LIBRARY : Destination(), Root {
		override val icon: Int = R.drawable.library
	}
}