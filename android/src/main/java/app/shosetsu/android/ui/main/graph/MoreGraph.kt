package app.shosetsu.android.ui.main.graph

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import app.shosetsu.android.common.consts.BundleKeys
import app.shosetsu.android.ui.about.AboutView
import app.shosetsu.android.ui.add.AddShareView
import app.shosetsu.android.ui.analytics.AnalyticsView
import app.shosetsu.android.ui.categories.CategoriesView
import app.shosetsu.android.ui.downloads.DownloadsView
import app.shosetsu.android.ui.main.Destination
import app.shosetsu.android.ui.more.MoreView

fun NavGraphBuilder.moreGraph(navController: NavHostController) {
	navigation(startDestination = "main", Destination.MORE.route) {
		composable("main") {
			MoreView(
				onNavToAbout = {
					navController.navigate(Destination.ABOUT.route)
				},
				onNavToDownloads = {
					navController.navigate(Destination.DOWNLOADS.route)
				},
				onNavToBackup = {
					navController.navigate(Destination.SETTINGS_BACKUP.route)
				},
				onNavToRepositories = {
					navController.navigate(Destination.REPOSITORIES.route)
				},
				onNavToCategories = {
					navController.navigate(Destination.CATEGORIES.route)
				},
				onNavToStyles = {
				},
				onNavToAddShare = {
					navController.navigate(Destination.ADD_SHARE.route)
				},
				onNavToAnalytics = {
					navController.navigate(Destination.ANALYTICS.route)
				},
				onNavToHistory = {
					navController.navigate(Destination.HISTORY.route)
				},
				onNavToSettings = {
					navController.navigate(Destination.SETTINGS.route)
				}
			)
		}
		composable(Destination.ABOUT.route) {
			AboutView(
				onOpenLicense = {
					TODO("Bind")
				},
				onBack = navController::popBackStack
			)
		}
		composable(Destination.CATEGORIES.route) {
			CategoriesView(
				onBack = navController::popBackStack
			)
		}
		composable(Destination.DOWNLOADS.route) {
			DownloadsView(
				onBack = navController::popBackStack
			)
		}

		composable(Destination.ADD_SHARE.route) { entry ->
			val shareURL = entry.arguments!!.getString(BundleKeys.BUNDLE_URL)
			AddShareView(
				shareURL,
				onBackPressed = navController::popBackStack,
				openNovel = {
					if (it != null)
						navController.navigate(
							Destination.NOVEL.routeWith(
								it.id ?: return@AddShareView
							)
						)
				}
			)
		}
		composable(Destination.REPOSITORIES.route) {
		}

		composable(Destination.HISTORY.route) {
		}
		composable(Destination.ANALYTICS.route) {
			AnalyticsView(navController::popBackStack)
		}

		settingsGraph()
	}
}