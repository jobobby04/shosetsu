package app.shosetsu.android.ui.main.graph

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import app.shosetsu.android.common.consts.BundleKeys
import app.shosetsu.android.ui.browse.BrowseView
import app.shosetsu.android.ui.catalogue.CatalogueView
import app.shosetsu.android.ui.extensionsConfigure.ConfigureExtensionView
import app.shosetsu.android.ui.main.Destination.BROWSE
import app.shosetsu.android.ui.main.Destination.CATALOG
import app.shosetsu.android.ui.main.Destination.CONFIGURE_EXTENSION
import app.shosetsu.android.ui.main.Destination.NOVEL
import app.shosetsu.android.ui.main.Destination.REPOSITORIES
import app.shosetsu.android.ui.main.Destination.SEARCH
import app.shosetsu.android.ui.search.SearchView

fun NavGraphBuilder.browseGraph(
	navController: NavHostController,
	drawerIcon: @Composable () -> Unit
) {
	navigation("main", BROWSE.route) {
		composable("main") {
			BrowseView(
				openCatalogue = {
					navController.navigate(CATALOG.routeWith(it))
				},
				openSettings = {
					navController.navigate(CONFIGURE_EXTENSION.routeWith(it))
				},
				openRepositories = {
					navController.navigate(REPOSITORIES.route)
				},
				openSearch = {
					navController.navigate(SEARCH.route)
				},
				drawerIcon = drawerIcon
			)
		}

		composable(CATALOG.route, CATALOG.arguments) { entry ->
			val extensionId = entry.arguments!!.getInt(BundleKeys.BUNDLE_EXTENSION)
			CatalogueView(
				extensionId,
				onOpenNovel = {
					navController.navigate(NOVEL.routeWith(it))
				},
				onBack = navController::popBackStack
			)
		}

		composable(
			CONFIGURE_EXTENSION.route,
			CONFIGURE_EXTENSION.arguments
		) { entry ->
			val extensionId = entry.arguments!!.getInt(BundleKeys.BUNDLE_EXTENSION)
			ConfigureExtensionView(
				extensionId,
				onExit = navController::popBackStack
			)
		}

		composable(SEARCH.route, SEARCH.arguments) { entry ->
			// TODO fix crash here
			val query = entry.arguments?.getString(BundleKeys.BUNDLE_QUERY)
			SearchView(
				initalQuery = query,
				openNovel = {
					navController.navigate(NOVEL.routeWith(it))
				},
				onBack = navController::popBackStack
			)
		}
	}
}