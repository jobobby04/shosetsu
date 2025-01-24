package app.shosetsu.android.ui.main.graph

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import app.shosetsu.android.common.enums.TextAsset
import app.shosetsu.android.common.ext.openChapter
import app.shosetsu.android.ui.about.AboutView
import app.shosetsu.android.ui.add.AddShareView
import app.shosetsu.android.ui.analytics.AnalyticsView
import app.shosetsu.android.ui.backup.BackupView
import app.shosetsu.android.ui.categories.CategoriesView
import app.shosetsu.android.ui.downloads.DownloadsView
import app.shosetsu.android.ui.history.HistoryView
import app.shosetsu.android.ui.main.Destination.More
import app.shosetsu.android.ui.main.Destination.More.About
import app.shosetsu.android.ui.main.Destination.More.AddShare
import app.shosetsu.android.ui.main.Destination.More.Analytics
import app.shosetsu.android.ui.main.Destination.More.Backup
import app.shosetsu.android.ui.main.Destination.More.Categories
import app.shosetsu.android.ui.main.Destination.More.Downloads
import app.shosetsu.android.ui.main.Destination.More.History
import app.shosetsu.android.ui.main.Destination.More.Repositories
import app.shosetsu.android.ui.main.Destination.More.Settings
import app.shosetsu.android.ui.main.Destination.More.TextReader
import app.shosetsu.android.ui.main.Destination.More.View
import app.shosetsu.android.ui.main.Destination.Novel
import app.shosetsu.android.ui.more.MoreView
import app.shosetsu.android.ui.repository.RepositoriesView

fun NavGraphBuilder.moreGraph(
	navController: NavHostController,
	drawerIcon: @Composable () -> Unit
) {
	navigation<More>(View) {
		composable<View> {
			MoreView(
				onNavToAbout = {
					navController.navigate(About)
				},
				onNavToDownloads = {
					navController.navigate(Downloads)
				},
				onNavToBackup = {
					navController.navigate(Backup)
				},
				onNavToRepositories = {
					navController.navigate(Repositories)
				},
				onNavToCategories = {
					navController.navigate(Categories)
				},
				onNavToStyles = {
				},
				onNavToAddShare = {
					navController.navigate(AddShare(null))
				},
				onNavToAnalytics = {
					navController.navigate(Analytics)
				},
				onNavToHistory = {
					navController.navigate(History)
				},
				onNavToSettings = {
					navController.navigate(Settings)
				},
				drawerIcon = drawerIcon
			)
		}

		assetReader(navController)

		composable<About> {
			AboutView(
				onOpenLicense = {
					navController.navigate(TextReader(TextAsset.LICENSE.ordinal))
				},
				onBack = navController::popBackStack
			)
		}
		composable<Categories> {
			CategoriesView(
				onBack = navController::popBackStack
			)
		}
		composable<Downloads> {
			DownloadsView(
				onBack = navController::popBackStack
			)
		}

		composable<AddShare> { entry ->
			AddShareView(
				entry.toRoute<AddShare>().url,
				onBackPressed = navController::popBackStack,
				openNovel = {
					if (it != null)
						navController.navigate(Novel(it.id!!))
				}
			)
		}
		composable<Repositories> {
			RepositoriesView(
				onBack = navController::popBackStack
			)
		}
		composable<Backup> {
			BackupView(
				onBack = navController::popBackStack
			)
		}

		composable<History> {
			val context = LocalContext.current
			HistoryView(
				openNovel = {
					navController.navigate(Novel(it))
				},
				openChapter = { nId, cId ->
					context.openChapter(nId, cId)
				},
				onBack = navController::popBackStack
			)
		}
		composable<Analytics> {
			AnalyticsView(navController::popBackStack)
		}

		settingsGraph(navController)
	}
}