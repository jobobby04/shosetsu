package app.shosetsu.android.ui.main.graph

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import app.shosetsu.android.common.ext.getNovelID
import app.shosetsu.android.common.ext.openChapter
import app.shosetsu.android.common.ext.openInWebView
import app.shosetsu.android.ui.library.LibraryView
import app.shosetsu.android.ui.main.Destination
import app.shosetsu.android.ui.migration.MigrationView
import app.shosetsu.android.ui.novel.NovelInfoView
import app.shosetsu.android.ui.settings.sub.TextAssetReaderView
import app.shosetsu.android.ui.updates.UpdatesView

/**
 * Shosetsu
 *
 * @since 22 / 12 / 2023
 * @author Doomsdayrs
 */

fun NavGraphBuilder.mainGraph(
	navController: NavHostController,
	sizeClass: WindowSizeClass
) {
	composable(Destination.LIBRARY.route) {
		LibraryView(
			onOpenNovel = { novelId ->
				navController.navigate(Destination.NOVEL.routeWith(novelId))
			},
			onMigrate = {
			}
		)
	}
	browseGraph(navController)
	moreGraph(navController)
	composable(Destination.TEXT_READER.route) {
		TextAssetReaderView(
			0,
			setViewTitle = {},
		)
	}
	composable(Destination.UPDATES.route) {
		val context = LocalContext.current
		UpdatesView(
			openNovel = { novelId ->
				navController.navigate(Destination.NOVEL.routeWith(novelId))
			},
			openChapter = context::openChapter,
		)
	}
	composable(
		Destination.NOVEL.route, arguments = Destination.NOVEL.arguments
	) { entry ->
		val novelId = entry.arguments!!.getNovelID();
		val context = LocalContext.current

		NovelInfoView(
			novelId,
			windowSize = sizeClass,
			onMigrate = {
				TODO("Bind")
			},
			openInWebView = context::openInWebView,
			openChapter = context::openChapter,
			onBack = navController::popBackStack
		)
	}
	composable(Destination.MIGRATION.route) {
		MigrationView(emptyList())
	}
}