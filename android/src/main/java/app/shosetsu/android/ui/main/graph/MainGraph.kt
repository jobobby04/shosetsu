package app.shosetsu.android.ui.main.graph

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.toRoute
import app.shosetsu.android.common.ext.openChapter
import app.shosetsu.android.common.ext.openInWebView
import app.shosetsu.android.ui.main.Destination.Migration
import app.shosetsu.android.ui.main.Destination.Novel
import app.shosetsu.android.ui.main.Destination.PrimaryWrapper
import app.shosetsu.android.ui.main.HomeView
import app.shosetsu.android.ui.migration.MigrationView
import app.shosetsu.android.ui.novel.NovelInfoView

/**
 * Shosetsu
 *
 * @since 22 / 12 / 2023
 * @author Doomsdayrs
 */

fun NavGraphBuilder.mainGraph(
	navController: ShosetsuNavController,
	sizeClass: WindowSizeClass
) {
	composableSub<PrimaryWrapper> {
		HomeView(
			navController,
			sizeClass = sizeClass
		)
	}
	browseGraph(navController)
	moreGraph(navController)
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
			onBack = navController::popBackStack
		)
	}

	composableMain<Migration> {
		MigrationView(emptyList())
	}
}