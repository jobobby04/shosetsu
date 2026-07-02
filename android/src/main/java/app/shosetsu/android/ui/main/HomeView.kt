package app.shosetsu.android.ui.main

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.util.lerp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import app.shosetsu.android.R
import app.shosetsu.android.common.enums.NavigationStyle
import app.shosetsu.android.common.ext.viewModelDi
import app.shosetsu.android.domain.repository.base.IBackupRepository
import app.shosetsu.android.ui.main.graph.DefaultMotionDuration
import app.shosetsu.android.ui.main.graph.PredictiveBack
import app.shosetsu.android.ui.main.graph.ShosetsuNavController
import app.shosetsu.android.ui.main.graph.homeGraph
import app.shosetsu.android.ui.main.graph.materialFadeThroughIn
import app.shosetsu.android.ui.main.graph.materialFadeThroughOut
import app.shosetsu.android.view.compose.SimpleIconButton
import app.shosetsu.android.viewmodel.abstracted.AHomeViewModel
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

private const val TabFadeDuration = 200

private enum class NavigationMode {
	BOTTOM, DRAWER, RAIL
}

/**
 * Allows the caller to change the UI focus to the main view, assuming ofc they have the nav controller.
 *
 * @param shosetsuNavController The applications nav controller
 * @param route The primary destination to go to
 */
fun navigateToMainView(shosetsuNavController: ShosetsuNavController, route: ShosetsuDestination.Primary) {
	shosetsuNavController.home.navigate(route) {
		// Pop up to the start destination of the graph to
		// avoid building up a large stack of destinations
		// on the back stack as users select items
		popUpTo(shosetsuNavController.home.graph.findStartDestination().id) {
			saveState = true
		}
		// Avoid multiple copies of the same destination when
		// reselecting the same item
		launchSingleTop = true
		// Restore state when reselecting a previously selected item
		restoreState = true
	}
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeView(
	shosetsuNavController: ShosetsuNavController,
	sizeClass: WindowSizeClass,
) {
	val viewModel: AHomeViewModel = viewModelDi()
	val scope = rememberCoroutineScope()

	val navStyle by viewModel.navigationStyle.collectAsState()
	val navigationMode = when (navStyle) {
		NavigationStyle.MATERIAL -> if (sizeClass.widthSizeClass == WindowWidthSizeClass.Compact) NavigationMode.BOTTOM else NavigationMode.RAIL
		NavigationStyle.LEGACY -> NavigationMode.DRAWER
	}

	val backupProgressState by viewModel.backupProgressState.collectAsState()

	val navBackStackEntry by shosetsuNavController.home.currentBackStackEntryAsState()
	val drawerState = rememberDrawerState(DrawerValue.Closed)

	val requireDoubleBackToExit by viewModel.requireDoubleBackToExit.collectAsState()

	ShosetsuBackHandler(
		requireDoubleBackToExit = requireDoubleBackToExit,
		isDrawerOpen = drawerState.isOpen,
		onCloseDrawer = drawerState::close
	)



	@Composable
	fun Content() = Scaffold(
		bottomBar = {
			if (navigationMode == NavigationMode.BOTTOM) {
				BottomNavigationBar(
					navBackStackEntry,
					{ navigateToMainView(shosetsuNavController, it) }
				)
			}
		},
		topBar = {
			AnimatedVisibility(
				backupProgressState == IBackupRepository.BackupProgress.IN_PROGRESS,
				enter = slideInVertically(),
				exit = slideOutVertically()
			) {
				Box(
					modifier = Modifier.windowInsetsPadding(TopAppBarDefaults.windowInsets)
				) {
					BackupProgressIndicator()
				}
			}
		},
		contentWindowInsets = WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal)
	) { paddingValues ->
		var scale by remember { mutableFloatStateOf(1f) }

		Box(
			Modifier
				.padding(paddingValues)
				.consumeWindowInsets(paddingValues)
				.graphicsLayer {
					scaleX = scale
					scaleY = scale
				}
		) {
			NavHost(
				shosetsuNavController.home,
				startDestination = Destination.Library,
				enterTransition = { materialFadeThroughIn(initialScale = 1f, durationMillis = TabFadeDuration) },
				exitTransition = { materialFadeThroughOut(durationMillis = TabFadeDuration) }
			) {
				homeGraph(
					shosetsuNavController,
					drawerIcon = {
						if (navigationMode == NavigationMode.DRAWER) {
							SimpleIconButton(
								Icons.Default.Menu,
								stringResource(R.string.navigation_drawer_open),
								onClick = {
									scope.launch {
										drawerState.open()
									}
								}
							)
						}
					},
					windowSize = sizeClass
				)
			}
		}

		var handlingBack by remember { mutableStateOf(false) }
		PredictiveBackHandler(
			enabled = handlingBack || navBackStackEntry?.topIs<Destination.Library>() != true,
		) { progress ->
			handlingBack = true
			val currentTab = ShosetsuDestination.Primary.all.firstOrNull { navBackStackEntry!!.topIs(it) }
			if (currentTab == null) {
				// This should never happen, but just in case
				handlingBack = false
				return@PredictiveBackHandler
			}
			try {
				progress.collect { backEvent ->
					scale = lerp(1f, 0.92f, PredictiveBack.transform(backEvent.progress))
					navigateToMainView(
						shosetsuNavController,
						if (backEvent.progress > 0.25f) Destination.Library else currentTab
					)
				}
				navigateToMainView(shosetsuNavController, Destination.Library)
			} catch (_: CancellationException) {
				navigateToMainView(shosetsuNavController, currentTab)
			} finally {
				animate(
					initialValue = scale,
					targetValue = 1f,
					animationSpec = tween(durationMillis = DefaultMotionDuration),
				) { value, _ ->
					scale = value
				}
				handlingBack = false
			}
		}
	}

	if (navigationMode == NavigationMode.BOTTOM) {
		Content()
	} else {
		ModalNavigationDrawer(
			drawerContent = {
				NavigationDrawerContent(
					navBackStackEntry,
					onNavigate = {
						navigateToMainView(shosetsuNavController, it)
						scope.launch {
							drawerState.close()
						}
					}
				)
			},
			drawerState = drawerState,
			gesturesEnabled = navigationMode == NavigationMode.DRAWER
		) {
			Row(Modifier.fillMaxSize()) {
				if (navigationMode == NavigationMode.RAIL) {
					NavigationRail(
						navBackStackEntry,
						onNavigate = {
							navigateToMainView(shosetsuNavController, it)
						}
					)
				}

				Content()
			}
		}
	}
}
