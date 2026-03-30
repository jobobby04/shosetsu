package app.shosetsu.android.ui.main.graph

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import app.shosetsu.android.common.ext.openChapter
import app.shosetsu.android.ui.browse.BrowseView
import app.shosetsu.android.ui.library.LibraryView
import app.shosetsu.android.ui.main.Destination.Browse
import app.shosetsu.android.ui.main.Destination.Browse.Catalog
import app.shosetsu.android.ui.main.Destination.Browse.ConfigureExtension
import app.shosetsu.android.ui.main.Destination.Library
import app.shosetsu.android.ui.main.Destination.Migration
import app.shosetsu.android.ui.main.Destination.More
import app.shosetsu.android.ui.main.Destination.More.About
import app.shosetsu.android.ui.main.Destination.More.AddShare
import app.shosetsu.android.ui.main.Destination.More.Analytics
import app.shosetsu.android.ui.main.Destination.More.Categories
import app.shosetsu.android.ui.main.Destination.More.Downloads
import app.shosetsu.android.ui.main.Destination.More.History
import app.shosetsu.android.ui.main.Destination.More.Repositories
import app.shosetsu.android.ui.main.Destination.More.Settings
import app.shosetsu.android.ui.main.Destination.Novel
import app.shosetsu.android.ui.main.Destination.Search
import app.shosetsu.android.ui.main.Destination.Updates
import app.shosetsu.android.ui.more.MoreView
import app.shosetsu.android.ui.updates.UpdatesView

fun NavGraphBuilder.homeGraph(
	navController: ShosetsuNavController,
	drawerIcon: @Composable () -> Unit
) {
	composable<Library> {
		LibraryView(
			onOpenNovel = { novelId ->
				navController.navigate(Novel(novelId))
			},
			onMigrate = {
				navController.navigate(Migration(it))
			},
			drawerIcon = drawerIcon
		)
	}

	composable<Browse> {
		BrowseView(
			openCatalogue = {
				navController.navigate(Catalog(it, null))
			},
			openSettings = {
				navController.navigate(ConfigureExtension(it))
			},
			openRepositories = {
				navController.navigate(Repositories)
			},
			openSearch = {
				navController.navigate(Search(null))
			},
			drawerIcon = drawerIcon
		)
	}

	composable<Updates> {
		val context = LocalContext.current
		UpdatesView(
			openNovel = { novelId ->
				navController.navigate(Novel(novelId))
			},
			openChapter = context::openChapter,
			drawerIcon = drawerIcon
		)
	}
	composable<More> {
		MoreView(
			onNavToAbout = {
				navController.navigate(About)
			},
			onNavToDownloads = {
				navController.navigate(Downloads)
			},
			onNavToBackup = {
				navController.navigate(Settings.Backup())
			},
			onNavToCategories = {
				navController.navigate(Categories)
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
}
