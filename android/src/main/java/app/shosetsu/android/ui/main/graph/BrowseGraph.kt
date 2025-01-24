package app.shosetsu.android.ui.main.graph

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import app.shosetsu.android.common.utils.ListingSerializer
import app.shosetsu.android.ui.browse.BrowseView
import app.shosetsu.android.ui.catalogue.CatalogueView
import app.shosetsu.android.ui.extensionsConfigure.ConfigureExtensionView
import app.shosetsu.android.ui.main.Destination.Browse
import app.shosetsu.android.ui.main.Destination.Browse.Catalog
import app.shosetsu.android.ui.main.Destination.Browse.ConfigureExtension
import app.shosetsu.android.ui.main.Destination.More.Repositories
import app.shosetsu.android.ui.main.Destination.Novel
import app.shosetsu.android.ui.main.Destination.Search
import app.shosetsu.android.ui.search.SearchView
import kotlin.reflect.typeOf

fun NavGraphBuilder.browseGraph(
	navController: NavHostController,
	drawerIcon: @Composable () -> Unit
) {
	navigation<Browse>(Browse.View) {
		composable<Browse.View> {
			BrowseView(
				openCatalogue = {
					navController.navigate(Catalog(it, ListingSerializer.SerializableListing(null)))
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

		composable<Catalog>(typeMap = mapOf(typeOf<ListingSerializer.SerializableListing>() to ListingSerializer.NavParameter)) { entry ->
			val route = entry.toRoute<Catalog>()
			CatalogueView(
				extensionId = route.extensionId,
				onOpenNovel = {
					navController.navigate(Novel(it))
				},
				onBack = navController::popBackStack,
				onSelectListing = {
					navController.navigate(Catalog(route.extensionId, ListingSerializer.SerializableListing(it)))
				},
				listing = route.listing.listing
			)
		}

		composable<ConfigureExtension> { entry ->
			val extensionId = entry.toRoute<ConfigureExtension>().extensionId
			ConfigureExtensionView(
				extensionId,
				onExit = navController::popBackStack
			)
		}

		composable<Search> { entry ->
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
}