package app.shosetsu.android.ui.main

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
 * @since 31 / 12 / 2023
 * @author Doomsdayrs
 */
@Composable
fun <T> NavigationRail(
	destinations: List<T>,
	currentDestination: NavBackStackEntry?,
	onNavigate: (Destination) -> Unit
) where T : Destination, T : Root {
	NavigationRail {
		destinations.forEach { destination ->
			NavigationRailItem(
				selected =
				currentDestination?.destination?.route == destination.route,
				icon = {
					Icon(
						painterResource(destination.icon),
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