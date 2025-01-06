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
import kotlin.reflect.KType

fun fadeInX() = fadeIn(animationSpec = tween(700))
fun fadeOutX() = fadeOut(animationSpec = tween(700))

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
