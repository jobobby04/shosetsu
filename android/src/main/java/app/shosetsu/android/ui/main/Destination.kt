package app.shosetsu.android.ui.main

import androidx.annotation.StringRes
import androidx.compose.animation.Crossfade
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Update
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import app.shosetsu.android.R
import kotlinx.serialization.Serializable
import androidx.compose.material3.Icon as MIcon

sealed interface ShosetsuDestination {
	interface Primary : ShosetsuDestination {
		val icon: ImageVector

		@get:StringRes
		val name: Int

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
	@Serializable
	object PrimaryWrapper : ShosetsuDestination.Root

	@Serializable
	object More : ShosetsuDestination.Primary {
		override val icon: ImageVector = Icons.Default.MoreHoriz
		override val name: Int = R.string.more

		@Serializable
		object About : ShosetsuDestination.Root

		@Serializable
		object Categories : ShosetsuDestination.Root

		@Serializable
		object Downloads : ShosetsuDestination.Root

		@Serializable
		data class AddShare(val url: String?) : ShosetsuDestination.Root

		@Serializable
		object Repositories : ShosetsuDestination.Root

		@Serializable
		object History : ShosetsuDestination.Root

		@Serializable
		object Analytics : ShosetsuDestination.Root

		@Serializable
		object Settings : ShosetsuDestination.Root {
			@Serializable
			object Overview : ShosetsuDestination.Root

			@Serializable
			object Appearance : ShosetsuDestination.Root

			@Serializable
			object Library : ShosetsuDestination.Root

			@Serializable
			object Reader : ShosetsuDestination.Root

			@Serializable
			object Downloads : ShosetsuDestination.Root

			@Serializable
			object Browse : ShosetsuDestination.Root

			@Serializable
			data class Backup(val highlightBackupFolder: Boolean = false) : ShosetsuDestination.Root

			@Serializable
			object Advanced : ShosetsuDestination.Root
		}

		@Serializable
		data class TextReader(val assetId: Int) : ShosetsuDestination.Root
	}

	@Serializable
	object Updates : ShosetsuDestination.Primary {
		override val icon: ImageVector = Icons.Outlined.Update
		override val name: Int = R.string.updates
	}

	@Serializable
	data class Novel(val novelId: Int) : ShosetsuDestination.Root

	@Serializable
	data class Search(val query: String?) : ShosetsuDestination.Root

	@Serializable
	data class Migration(val novelId: List<Int>) : ShosetsuDestination.Root

	@Serializable
	object Browse : ShosetsuDestination.Primary {
		override val icon: ImageVector = Icons.Outlined.Explore
		override val name: Int = R.string.browse

		@Serializable
		data class Catalog(val extensionId: Int) : ShosetsuDestination.Root

		@Serializable
		data class ConfigureExtension(val extensionId: Int) : ShosetsuDestination.Root
	}

	@Serializable
	object Library : ShosetsuDestination.Primary {
		override val icon: ImageVector = Icons.Outlined.CollectionsBookmark
		override val name: Int = R.string.library
	}
}

@Composable
fun DestinationIcon(destination: ShosetsuDestination.Primary, isSelected: Boolean) =
	when (destination) {
		is Destination.Browse -> {
			Crossfade(isSelected) {
				MIcon(
					rememberVectorPainter(
						if (it) Icons.Filled.Explore
						else Icons.Outlined.Explore
					),
					stringResource(destination.name)
				)
			}
		}

		is Destination.Library -> {
			Crossfade(isSelected) {
				MIcon(
					rememberVectorPainter(
						if (it) Icons.Filled.CollectionsBookmark
						else Icons.Outlined.CollectionsBookmark
					),
					stringResource(destination.name)
				)
			}
		}

		else -> {
			MIcon(
				rememberVectorPainter(destination.icon),
				stringResource(destination.name)
			)
		}
	}

fun NavBackStackEntry.has(destination: ShosetsuDestination) =
	this.destination.hierarchy.any { it.hasRoute(route = destination::class) }

fun NavBackStackEntry.topIs(destination: ShosetsuDestination) =
	this.destination.hierarchy.first().hasRoute(route = destination::class)

inline fun <reified T : ShosetsuDestination> NavBackStackEntry.has() =
	this.destination.hierarchy.any { it.hasRoute(route = T::class) }

inline fun <reified T : ShosetsuDestination> NavBackStackEntry.topIs() =
	this.destination.hierarchy.first().hasRoute(route = T::class)