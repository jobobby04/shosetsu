package app.shosetsu.android.ui.main

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import app.shosetsu.android.R
import kotlinx.serialization.Serializable

interface ShosetsuDestination
interface Root : ShosetsuDestination {
	@get:DrawableRes val icon: Int
	@get:StringRes val name: Int
	val viewOrigin: ShosetsuDestination get() = this
}

object Destination {
	@Serializable object More : Root {
		override val icon: Int = R.drawable.ic_baseline_more_horiz_24
		override val name: Int = R.string.more
		override val viewOrigin: ShosetsuDestination get() = View

		@Serializable object View : ShosetsuDestination
		@Serializable object About : ShosetsuDestination
		@Serializable object Categories : ShosetsuDestination
		@Serializable object Downloads : ShosetsuDestination
		@Serializable data class AddShare(val url: String?) : ShosetsuDestination
		@Serializable object Repositories : ShosetsuDestination
		@Serializable object Backup : ShosetsuDestination
		@Serializable object History : ShosetsuDestination
		@Serializable object Analytics : ShosetsuDestination
		@Serializable object Settings : ShosetsuDestination {
			@Serializable object Overview : ShosetsuDestination
			@Serializable object View : ShosetsuDestination
			@Serializable object Update : ShosetsuDestination
			@Serializable object Advanced : ShosetsuDestination
			@Serializable object Download : ShosetsuDestination
			@Serializable object Reader : ShosetsuDestination
		}
		@Serializable data class TextReader(val assetId: Int) : ShosetsuDestination
	}
	@Serializable object Updates : Root {
		override val icon: Int = R.drawable.update
		override val name: Int = R.string.updates
	}
	@Serializable data class Novel(val novelId: Int) : ShosetsuDestination
	@Serializable data class Search(val query: String?) : ShosetsuDestination
	@Serializable data class Migration(val novelId: List<Int>) : ShosetsuDestination
	@Serializable object Browse : Root {
		override val icon: Int = R.drawable.navigation_arrow
		override val name: Int = R.string.browse
		override val viewOrigin: ShosetsuDestination get() = View

		@Serializable object View : ShosetsuDestination
		@Serializable data class Catalog(val extensionId: Int) : ShosetsuDestination
		@Serializable data class ConfigureExtension(val extensionId: Int) : ShosetsuDestination
	}
	@Serializable object Library : Root {
		override val icon: Int = R.drawable.library
		override val name: Int = R.string.library
	}
}

fun NavBackStackEntry.has(destination: ShosetsuDestination) = this.destination.hierarchy.any { it.hasRoute(route = destination::class) }
fun NavBackStackEntry.topIs(destination: ShosetsuDestination) = this.destination.hierarchy.first().hasRoute(route = destination::class)