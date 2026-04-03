package app.shosetsu.android.ui.main.graph

import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.toRoute
import app.shosetsu.android.common.enums.TextAsset
import app.shosetsu.android.common.ext.openChapter
import app.shosetsu.android.ui.about.AboutView
import app.shosetsu.android.ui.add.AddShareView
import app.shosetsu.android.ui.analytics.AnalyticsView
import app.shosetsu.android.ui.categories.CategoriesView
import app.shosetsu.android.ui.downloads.DownloadsView
import app.shosetsu.android.ui.history.HistoryView
import app.shosetsu.android.ui.main.Destination.More.About
import app.shosetsu.android.ui.main.Destination.More.AddShare
import app.shosetsu.android.ui.main.Destination.More.Analytics
import app.shosetsu.android.ui.main.Destination.More.Categories
import app.shosetsu.android.ui.main.Destination.More.Downloads
import app.shosetsu.android.ui.main.Destination.More.History
import app.shosetsu.android.ui.main.Destination.More.Repositories
import app.shosetsu.android.ui.main.Destination.More.TextReader
import app.shosetsu.android.ui.main.Destination.Novel
import app.shosetsu.android.ui.repository.RepositoriesView

fun NavGraphBuilder.moreGraph(
	navController: ShosetsuNavController,
) {
	assetReader(navController)

	composableSub<About> {
		AboutView(
			onOpenLicense = {
				navController.navigate(TextReader(TextAsset.LICENSE.ordinal))
			},
			onBack = navController::popBackStack
		)
	}
	composableSub<Categories> {
		CategoriesView(
			onBack = navController::popBackStack
		)
	}
	composableSub<Downloads> {
		DownloadsView(
			onBack = navController::popBackStack
		)
	}

	composableSub<AddShare> { entry ->
		AddShareView(
			entry.toRoute<AddShare>().url,
			onBackPressed = navController::popBackStack,
			openNovel = {
				if (it != null)
					navController.navigate(Novel(it.id!!))
			}
		)
	}
	composableSub<Repositories> {
		RepositoriesView(
			onBack = navController::popBackStack
		)
	}

	composableSub<History> {
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
	composableSub<Analytics> {
		AnalyticsView(navController::popBackStack)
	}

	settingsGraph(navController)
}
