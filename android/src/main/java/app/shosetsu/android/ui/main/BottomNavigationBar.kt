package app.shosetsu.android.ui.main

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavBackStackEntry

/*
 * This file is part of shosetsu.
 *
 * shosetsu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * shosetsu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with shosetsu.  If not, see <https://www.gnu.org/licenses/>.
 */

/**
 * Shosetsu
 *
 * @since 09 / 01 / 2024
 * @author Doomsdayrs
 */
@Composable
fun <T> BottomNavigationBar(
	destinations: List<T>,
	currentDestination: NavBackStackEntry?,
	onNavigate: (Destination) -> Unit
) where T : Destination, T : Root {
	var isVisible by remember { mutableStateOf(true) }

	isVisible = destinations.any { destination ->
		currentDestination?.destination?.route == destination.route ||
				currentDestination?.destination?.route == "main"
	}

	if (isVisible) {
		NavigationBar {
			destinations.forEach { destination ->
				NavigationBarItem(
					selected =
					currentDestination?.destination?.route == destination.route,
					icon = {
						Icon(
							painterResource(
								destination.icon
							),
							destination.route
						)
					},
					label = {
						Text(destination.route)
					},
					onClick = {
						onNavigate(destination)
					}
				)
			}
		}
	}
}
