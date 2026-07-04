package app.shosetsu.android.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import app.shosetsu.android.R
import app.shosetsu.android.ui.theme.Primary
import app.shosetsu.android.view.compose.minimumTouchTargetSize

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

/*
 * Shosetsu
 *
 * @since 25 / 12 / 2023
 * @author Doomsdayrs
 */
@Preview
@Composable
fun PreviewNavigationDrawerContent() {
	NavigationDrawerContent(
		null,
		{},
		drawerState = rememberDrawerState(DrawerValue.Open)
	)
}

@Composable
fun NavigationDrawerContent(
	currentDestination: NavBackStackEntry?,
	onNavigate: (ShosetsuDestination.Primary) -> Unit,
	drawerState: DrawerState
) {
	ModalDrawerSheet(
		drawerState
	) {
		Row(
			verticalAlignment = Alignment.Bottom,
			horizontalArrangement = Arrangement.spacedBy(8.dp),
			modifier = Modifier.padding(8.dp)
		) {
			Image(
				painterResource(R.drawable.shou_icon),
				stringResource(R.string.logo_desc),
				modifier = Modifier
					.minimumTouchTargetSize()
					.fillMaxWidth(.15f)
					.aspectRatio(1f)
					.clip(CircleShape)
					.background(Primary)
			)
			Column(
				verticalArrangement = Arrangement.Bottom,
				modifier = Modifier.fillMaxWidth()
			) {
				Text(
					stringResource(R.string.app_name),
					Modifier.padding(top = 32.dp),
					style = MaterialTheme.typography.titleLarge
				)
				Text(stringResource(R.string.header_text), style = MaterialTheme.typography.labelLarge)
			}
		}

		HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))

		ShosetsuDestination.Primary.all.forEach { destination ->
			val isSelected = currentDestination?.has(destination) == true
			NavigationDrawerItem(
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