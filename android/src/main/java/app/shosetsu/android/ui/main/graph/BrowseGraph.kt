package app.shosetsu.android.ui.main.graph

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import app.shosetsu.android.common.consts.BundleKeys
import app.shosetsu.android.ui.browse.BrowseView
import app.shosetsu.android.ui.catalogue.CatalogueView
import app.shosetsu.android.ui.extensionsConfigure.ConfigureExtensionView
import app.shosetsu.android.ui.main.Destination
import app.shosetsu.android.ui.search.SearchView

fun NavGraphBuilder.browseGraph(navController: NavHostController) {
	navigation("main", Destination.BROWSE.route) {
		composable("main") {
			BrowseView(
				openCatalogue = {
					navController.navigate(Destination.CATALOG.routeWith(it))
				},
				openSettings = {
					navController.navigate(Destination.CONFIGURE_EXTENSION.routeWith(it))
				},
				openRepositories = {
					navController.navigate(Destination.REPOSITORIES.route)
				},
				openSearch = {
					navController.navigate(Destination.SEARCH.route)
				}
			)
		}

		composable(Destination.CATALOG.route, Destination.CATALOG.arguments) { entry ->
			val extensionId = entry.arguments!!.getInt(BundleKeys.BUNDLE_EXTENSION)
			CatalogueView(
				extensionId,
				onOpenNovel = {
					navController.navigate(Destination.NOVEL.routeWith(it))
				},
				onBack = navController::popBackStack
			)
		}

		composable(
			Destination.CONFIGURE_EXTENSION.route,
			Destination.CONFIGURE_EXTENSION.arguments
		) { entry ->
			val extensionId = entry.arguments!!.getInt(BundleKeys.BUNDLE_EXTENSION)
			ConfigureExtensionView(
				extensionId,
				onExit = navController::popBackStack
			)
		}

		composable(Destination.SEARCH.route) { entry ->
			val query = entry.arguments?.getString(BundleKeys.BUNDLE_QUERY)
			SearchView(
				query = query,
				openNovel = {
					navController.navigate(Destination.NOVEL.routeWith(it))
				}
			)
		}
	}
}