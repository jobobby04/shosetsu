package app.shosetsu.android.ui.main

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
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
 * @since 09 / 01 / 2024
 * @author Doomsdayrs
 */
@Composable
fun BottomNavigationBar(
	currentDestination: NavBackStackEntry?,
	onNavigate: (ShosetsuDestination.Primary) -> Unit
) {
	NavigationBar {
		ShosetsuDestination.Primary.all.forEach { destination ->
			NavigationBarItem(
				selected =
					currentDestination?.has(destination) == true,
				icon = {
					Icon(
						painterResource(
							destination.icon
						),
						destination::class.simpleName
					)
				},
				label = {
					Text(stringResource(destination.name))
				},
				onClick = {
					onNavigate(destination)
				}
			)
		}
	}
}
