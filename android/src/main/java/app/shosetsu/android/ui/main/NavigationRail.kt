package app.shosetsu.android.ui.main

import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
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
fun NavigationRail(
	currentDestination: NavBackStackEntry?,
	onNavigate: (ShosetsuDestination.Primary) -> Unit
) {
	NavigationRail {
		ShosetsuDestination.Primary.all.forEach { destination ->
			val isSelected = currentDestination?.has(destination) == true
			NavigationRailItem(
				selected = isSelected,
				icon = { DestinationIcon(destination, isSelected) },
				label = { Text(stringResource(destination.name)) },
				onClick = {
					onNavigate(destination)
				}
			)
		}
	}
}
