package app.shosetsu.android.ui.main.graph

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import app.shosetsu.android.common.ext.getNovelID
import app.shosetsu.android.common.ext.openChapter
import app.shosetsu.android.common.ext.openInWebView
import app.shosetsu.android.ui.library.LibraryView
import app.shosetsu.android.ui.main.Destination.LIBRARY
import app.shosetsu.android.ui.main.Destination.MIGRATION
import app.shosetsu.android.ui.main.Destination.NOVEL
import app.shosetsu.android.ui.main.Destination.UPDATES
import app.shosetsu.android.ui.migration.MigrationView
import app.shosetsu.android.ui.novel.NovelInfoView
import app.shosetsu.android.ui.updates.UpdatesView

/**
 * Shosetsu
 *
 * @since 22 / 12 / 2023
 * @author Doomsdayrs
 */

fun NavGraphBuilder.mainGraph(
	navController: NavHostController,
	sizeClass: WindowSizeClass,
	drawerIcon: @Composable () -> Unit
) {
	composable(LIBRARY.route) {
		LibraryView(
			onOpenNovel = { novelId ->
				navController.navigate(NOVEL.routeWith(novelId))
			},
			onMigrate = {
				navController.navigate(MIGRATION.routeWith(it))
			},
			drawerIcon = drawerIcon
		)
	}
	browseGraph(
		navController,
		drawerIcon = drawerIcon
	)
	moreGraph(
		navController,
		drawerIcon = drawerIcon
	)
	composable(UPDATES.route) {
		val context = LocalContext.current
		UpdatesView(
			openNovel = { novelId ->
				navController.navigate(NOVEL.routeWith(novelId))
			},
			openChapter = context::openChapter,
			drawerIcon = drawerIcon
		)
	}
	composable(
		NOVEL.route, arguments = NOVEL.arguments
	) { entry ->
		val novelId = entry.arguments!!.getNovelID();
		val context = LocalContext.current

		NovelInfoView(
			novelId,
			windowSize = sizeClass,
			onMigrate = {
				navController.navigate(MIGRATION.routeWith(listOf(it)))
			},
			openInWebView = context::openInWebView,
			openChapter = context::openChapter,
			onBack = navController::popBackStack,
			drawerIcon = drawerIcon
		)
	}

	composable(MIGRATION.route) {
		MigrationView(emptyList())
	}
}