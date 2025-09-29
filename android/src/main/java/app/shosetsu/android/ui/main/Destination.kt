package app.shosetsu.android.ui.main

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import app.shosetsu.android.R
import kotlinx.serialization.Serializable

sealed interface ShosetsuDestination {
	interface Primary : ShosetsuDestination {
		@get:DrawableRes val icon: Int
		@get:StringRes val name: Int

		companion object {
			val all = listOf(
				Destination.Library,
				Destination.Updates,
				Destination.Browse,
				Destination.More
			)
		}
	}

	interface Root : ShosetsuDestination
}

object Destination {
	@Serializable object PrimaryWrapper : ShosetsuDestination.Root

	@Serializable object More : ShosetsuDestination.Primary {
		override val icon: Int = R.drawable.ic_baseline_more_horiz_24
		override val name: Int = R.string.more

		@Serializable object About : ShosetsuDestination.Root
		@Serializable object Categories : ShosetsuDestination.Root
		@Serializable object Downloads : ShosetsuDestination.Root
		@Serializable data class AddShare(val url: String?) : ShosetsuDestination.Root
		@Serializable object Repositories : ShosetsuDestination.Root
		@Serializable object History : ShosetsuDestination.Root
		@Serializable object Analytics : ShosetsuDestination.Root
		@Serializable object Settings : ShosetsuDestination.Root {
			@Serializable object Overview : ShosetsuDestination.Root
			@Serializable object Appearance : ShosetsuDestination.Root
			@Serializable object Library : ShosetsuDestination.Root
			@Serializable object Reader : ShosetsuDestination.Root
			@Serializable object Downloads : ShosetsuDestination.Root
			@Serializable object Browse : ShosetsuDestination.Root
			@Serializable object Backup : ShosetsuDestination.Root
			@Serializable object Advanced : ShosetsuDestination.Root
		}
		@Serializable data class TextReader(val assetId: Int) : ShosetsuDestination.Root
	}
	@Serializable object Updates : ShosetsuDestination.Primary {
		override val icon: Int = R.drawable.update
		override val name: Int = R.string.updates
	}
	@Serializable data class Novel(val novelId: Int) : ShosetsuDestination.Root
	@Serializable data class Search(val query: String?) : ShosetsuDestination.Root
	@Serializable data class Migration(val novelId: List<Int>) : ShosetsuDestination.Root
	@Serializable object Browse : ShosetsuDestination.Primary {
		override val icon: Int = R.drawable.navigation_arrow
		override val name: Int = R.string.browse

		@Serializable data class Catalog(val extensionId: Int) : ShosetsuDestination.Root
		@Serializable data class ConfigureExtension(val extensionId: Int) : ShosetsuDestination.Root
	}
	@Serializable object Library : ShosetsuDestination.Primary {
		override val icon: Int = R.drawable.library
		override val name: Int = R.string.library
	}
}

fun NavBackStackEntry.has(destination: ShosetsuDestination) = this.destination.hierarchy.any { it.hasRoute(route = destination::class) }
fun NavBackStackEntry.topIs(destination: ShosetsuDestination) = this.destination.hierarchy.first().hasRoute(route = destination::class)
inline fun <reified T : ShosetsuDestination> NavBackStackEntry.has() = this.destination.hierarchy.any { it.hasRoute(route = T::class) }
inline fun <reified T : ShosetsuDestination> NavBackStackEntry.topIs() = this.destination.hierarchy.first().hasRoute(route = T::class)