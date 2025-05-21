package app.shosetsu.android.ui.main.graph

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import kotlin.reflect.KClass
import kotlin.reflect.KType

/**
 * Creates a fade-in animation with a common duration.
 *
 * @return A fade-in animation spec.
 */
fun fadeInX() = fadeIn(animationSpec = tween(250))

/**
 * Creates a fade-out animation with a common duration.
 *
 * @return A fade-out animation spec.
 */
fun fadeOutX() = fadeOut(animationSpec = tween(250))

/**
 * Add the [Composable] to the [NavGraphBuilder] with transitions appropriate for a
 * top-level screen accessible using the bottom navigation bar.
 *
 * @param T route from a [KClass] for the destination
 * @param typeMap map of destination arguments' kotlin type [KType] to its respective custom
 *   [NavType]. May be empty if [T] does not use custom NavTypes.
 * @param deepLinks list of deep links to associate with the destinations
 * @param content composable for the destination
 */
inline fun <reified T : Any> NavGraphBuilder.composableMain(
	typeMap: Map<KType, @JvmSuppressWildcards NavType<*>> = emptyMap(),
	deepLinks: List<NavDeepLink> = emptyList(),
	noinline content: @Composable (AnimatedContentScope.(NavBackStackEntry) -> Unit)
) = composable<T>(
	typeMap = typeMap,
	deepLinks = deepLinks,
	content = content,
	enterTransition = { fadeInX() },
	exitTransition = { fadeOutX() },
)

/**
 * Add the [Composable] to the [NavGraphBuilder] with transitions appropriate for a
 * sub-screen accessible not from the bottom navigation bar but from another screen.
 *
 * @param T route from a [KClass] for the destination
 * @param typeMap map of destination arguments' kotlin type [KType] to its respective custom
 *   [NavType]. May be empty if [T] does not use custom NavTypes.
 * @param deepLinks list of deep links to associate with the destinations
 * @param content composable for the destination
 */
inline fun <reified T : Any> NavGraphBuilder.composableSub(
	typeMap: Map<KType, @JvmSuppressWildcards NavType<*>> = emptyMap(),
	deepLinks: List<NavDeepLink> = emptyList(),
	noinline content: @Composable (AnimatedContentScope.(NavBackStackEntry) -> Unit)
) = composable<T>(
	typeMap = typeMap,
	deepLinks = deepLinks,
	content = content,
	enterTransition = { slideInHorizontally(animationSpec = tween(
		durationMillis = 300
	)) { it / 20 } + fadeIn(animationSpec = tween(
		durationMillis = 195,
		easing = LinearOutSlowInEasing
	)) },
	exitTransition = { slideOutHorizontally(animationSpec = tween(
		durationMillis = 300
	)) { it / 20 } + fadeOut(animationSpec = tween(
		durationMillis = 195,
		easing = FastOutLinearInEasing
	)) },
	popEnterTransition = { fadeInX() },
)
