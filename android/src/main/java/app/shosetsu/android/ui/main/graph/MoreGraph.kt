package app.shosetsu.android.ui.main.graph

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import app.shosetsu.android.common.consts.BundleKeys
import app.shosetsu.android.common.enums.TextAsset
import app.shosetsu.android.common.ext.openChapter
import app.shosetsu.android.ui.about.AboutView
import app.shosetsu.android.ui.add.AddShareView
import app.shosetsu.android.ui.analytics.AnalyticsView
import app.shosetsu.android.ui.backup.BackupView
import app.shosetsu.android.ui.categories.CategoriesView
import app.shosetsu.android.ui.downloads.DownloadsView
import app.shosetsu.android.ui.history.HistoryView
import app.shosetsu.android.ui.main.Destination
import app.shosetsu.android.ui.more.MoreView
import app.shosetsu.android.ui.repository.RepositoriesView

fun NavGraphBuilder.moreGraph(
	navController: NavHostController,
	drawerIcon: @Composable () -> Unit
) {
	navigation(startDestination = "main", Destination.MORE.route) {
		composableMain("main") {
			MoreView(
				onNavToAbout = {
					navController.navigate(Destination.ABOUT.route)
				},
				onNavToDownloads = {
					navController.navigate(Destination.DOWNLOADS.route)
				},
				onNavToBackup = {
					navController.navigate(Destination.BACKUP.route)
				},
				onNavToCategories = {
					navController.navigate(Destination.CATEGORIES.route)
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
				},
				drawerIcon = drawerIcon
			)
		}

		assetReader(navController)

		composableSub(Destination.ABOUT.route) {
			AboutView(
				onOpenLicense = {
					navController.navigate(
						Destination.TEXT_READER.routeWith(TextAsset.LICENSE.ordinal)
					)
				},
				onBack = navController::popBackStack
			)
		}
		composableSub(Destination.CATEGORIES.route) {
			CategoriesView(
				onBack = navController::popBackStack
			)
		}
		composableSub(Destination.DOWNLOADS.route) {
			DownloadsView(
				onBack = navController::popBackStack
			)
		}

		composableSub(Destination.ADD_SHARE.route) { entry ->
			val shareURL = entry.arguments!!.getString(BundleKeys.BUNDLE_URL)
			AddShareView(
				shareURL,
				onBackPressed = navController::popBackStack,
				openNovel = {
					if (it != null)
						navController.navigate(
							Destination.NOVEL.routeWith(it.id!!)
						)
				}
			)
		}
		composableSub(Destination.REPOSITORIES.route) {
			RepositoriesView(
				onBack = navController::popBackStack
			)
		}
		composableSub(Destination.BACKUP.route) {
			BackupView(
				onBack = navController::popBackStack
			)
		}

		composableSub(Destination.HISTORY.route) {
			val context = LocalContext.current
			HistoryView(
				openNovel = {
					navController.navigate(Destination.NOVEL.routeWith(it))
				},
				openChapter = { nId, cId ->
					context.openChapter(nId, cId)
				},
				onBack = navController::popBackStack
			)
		}
		composableSub(Destination.ANALYTICS.route) {
			AnalyticsView(navController::popBackStack)
		}

		settingsGraph(navController)
	}
}