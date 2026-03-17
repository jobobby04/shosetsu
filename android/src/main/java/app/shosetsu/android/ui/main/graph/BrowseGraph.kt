package app.shosetsu.android.ui.main.graph

import androidx.navigation.NavGraphBuilder
import androidx.navigation.toRoute
import app.shosetsu.android.ui.catalogue.CatalogueView
import app.shosetsu.android.ui.extensionsConfigure.ConfigureExtensionView
import app.shosetsu.android.ui.main.Destination.Browse.Catalog
import app.shosetsu.android.ui.main.Destination.Browse.ConfigureExtension
import app.shosetsu.android.ui.main.Destination.Novel
import app.shosetsu.android.ui.main.Destination.Search
import app.shosetsu.android.ui.search.SearchView

fun NavGraphBuilder.browseGraph(navController: ShosetsuNavController) {
	composableSub<Catalog> { entry ->
		val route = entry.toRoute<Catalog>()
		CatalogueView(
			extensionId = route.extensionId,
			listing = route.listing,
			onOpenNovel = {
				navController.navigate(Novel(it))
			},
			onBack = navController::popBackStack,
			onSelectListing = {
				navController.navigate(Catalog(route.extensionId, it.link))
			}
		)
	}

	composableSub<ConfigureExtension> { entry ->
		val extensionId = entry.toRoute<ConfigureExtension>().extensionId
		ConfigureExtensionView(
			extensionId,
			onExit = navController::popBackStack
		)
	}

	composableSub<Search> { entry ->
		// TODO fix crash here
		val query = entry.toRoute<Search>().query
		SearchView(
			initalQuery = query,
			openNovel = {
				navController.navigate(Novel(it))
			},
			onBack = navController::popBackStack
		)
	}
}