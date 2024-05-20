package app.shosetsu.android.ui.main

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument
import app.shosetsu.android.R
import app.shosetsu.android.common.consts.BundleKeys

interface Root {
	@get:DrawableRes
	val icon: Int
	@get:StringRes
	val name: Int
}

sealed class Destination {
	open val route: String
		get() = this::class.simpleName!!

	open val arguments: List<NamedNavArgument> = emptyList()

	data object SETTINGS : Destination()
	data object SETTINGS_VIEW : Destination()
	data object SETTINGS_UPDATE : Destination()
	data object SETTINGS_ADVANCED : Destination()
	data object SETTINGS_DOWNLOAD : Destination()
	data object BACKUP : Destination()
	data object SETTINGS_READER : Destination()
	data object CATEGORIES : Destination()
	data object MORE : Destination(), Root {
		override val icon: Int = R.drawable.ic_baseline_more_horiz_24
		override val name: Int = R.string.more
	}

	data object ABOUT : Destination()
	data object TEXT_READER : Destination() {
		override val route: String = "text/{${BundleKeys.BUNDLE_ID}}"

		override val arguments: List<NamedNavArgument> =
			listOf(
				navArgument(BundleKeys.BUNDLE_ID) { type = NavType.IntType }
			)

		fun routeWith(assetId: Int): String =
			"text/$assetId"
	}

	data object DOWNLOADS : Destination()
	data object ANALYTICS : Destination()
	data object ADD_SHARE : Destination() {
		override val route: String = "add_share/{${BundleKeys.BUNDLE_URL}}"
		override val arguments: List<NamedNavArgument> =
			listOf(
				navArgument(BundleKeys.BUNDLE_URL) { type = NavType.StringType }
			)

		fun routeWith(url: String): String =
			"add_share/$url"
	}

	data object HISTORY : Destination()
	data object UPDATES : Destination(), Root {
		override val icon: Int = R.drawable.update
		override val name: Int = R.string.updates
	}

	data object NOVEL : Destination() {
		override val route: String = "novel/{${BundleKeys.BUNDLE_NOVEL_ID}}"
		override val arguments: List<NamedNavArgument> =
			listOf(
				navArgument(BundleKeys.BUNDLE_NOVEL_ID) { type = NavType.IntType }
			)

		fun routeWith(novelId: Int): String =
			"novel/$novelId"
	}

	data object SEARCH : Destination() {
		override val route: String = "search/{${BundleKeys.BUNDLE_QUERY}}"
		override val arguments: List<NamedNavArgument> =
			listOf(
				navArgument(BundleKeys.BUNDLE_QUERY) { type = NavType.StringType }
			)

		fun routeWith(query: String?): String =
			"search/$query"
	}

	data object REPOSITORIES : Destination()
	data object MIGRATION : Destination() {
		override val route: String = "migrate/{${BundleKeys.BUNDLE_NOVEL_ID}}"

		override val arguments: List<NamedNavArgument> =
			listOf(
				navArgument(BundleKeys.BUNDLE_NOVEL_ID) { type = NavType.IntArrayType }
			)

		fun routeWith(novelId: List<Int>): String =
			"migrate/$novelId"
	}

	data object CATALOG : Destination() {
		override val route: String = "catalog/{${BundleKeys.BUNDLE_EXTENSION}}"

		override val arguments: List<NamedNavArgument> =
			listOf(
				navArgument(BundleKeys.BUNDLE_EXTENSION) { type = NavType.IntType }
			)

		fun routeWith(extensionId: Int): String =
			"catalog/$extensionId"
	}

	data object BROWSE : Destination(), Root {
		override val icon: Int = R.drawable.navigation_arrow
		override val name: Int = R.string.browse
	}

	data object CONFIGURE_EXTENSION : Destination() {
		override val route: String = "configure/{${BundleKeys.BUNDLE_EXTENSION}}"
		override val arguments: List<NamedNavArgument> =
			listOf(
				navArgument(BundleKeys.BUNDLE_EXTENSION) { type = NavType.IntType }
			)

		fun routeWith(extensionId: Int): String =
			"configure/$extensionId"
	}

	data object LIBRARY : Destination(), Root {
		override val icon: Int = R.drawable.library
		override val name: Int = R.string.library
	}
}