package app.shosetsu.android.ui.main.graph

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import app.shosetsu.android.ui.main.Destination
import app.shosetsu.android.ui.main.ShosetsuDestination

class ShosetsuNavController(
	val root: NavHostController,
	val home: NavHostController
) {
	fun navigate(route: ShosetsuDestination) {
		when (route) {
			is ShosetsuDestination.Root -> root.navigate(route)
			is ShosetsuDestination.Primary -> {
				root.popBackStack(Destination.PrimaryWrapper, inclusive = false)
				home.navigate(route)
			}
		}
	}

	fun popBackStack() {
		root.popBackStack()
	}

	companion object {
		@Composable
		operator fun invoke() = ShosetsuNavController(rememberNavController(), rememberNavController())
	}
}
