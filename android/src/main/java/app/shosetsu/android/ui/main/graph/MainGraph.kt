package app.shosetsu.android.ui.main.graph

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.toRoute
import app.shosetsu.android.common.ext.openChapter
import app.shosetsu.android.common.ext.openInWebView
import app.shosetsu.android.ui.library.LibraryView
import app.shosetsu.android.ui.main.Destination.Library
import app.shosetsu.android.ui.main.Destination.Migration
import app.shosetsu.android.ui.main.Destination.Novel
import app.shosetsu.android.ui.main.Destination.Updates
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
    composableMain<Library> {
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
	browseGraph(
		navController,
		drawerIcon = drawerIcon
	)
	moreGraph(
		navController,
		drawerIcon = drawerIcon
	)
    composableMain<Updates> {
		val context = LocalContext.current
		UpdatesView(
			openNovel = { novelId ->
				navController.navigate(Novel(novelId))
			},
			openChapter = context::openChapter,
			drawerIcon = drawerIcon
		)
	}
    composableMain<Novel> { entry ->
		val novelId = entry.toRoute<Novel>().novelId
		val context = LocalContext.current

		NovelInfoView(
			novelId,
			windowSize = sizeClass,
			onMigrate = {
				navController.navigate(Migration(listOf(it)))
			},
			openInWebView = context::openInWebView,
			openChapter = context::openChapter,
			onBack = navController::popBackStack,
			drawerIcon = drawerIcon
		)
	}

    composableMain<Migration> {
		MigrationView(emptyList())
	}
}