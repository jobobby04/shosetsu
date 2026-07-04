package app.shosetsu.android.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.shosetsu.android.R

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
 * @since 06 / 03 / 2022
 * @author Doomsdayrs
 */

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun NovelShareMenu(
	shareBasicURL: () -> Unit,
	shareQRCode: () -> Unit,
	dismiss: () -> Unit
) {
	BottomSheetDialog(dismiss) {
		Column(
			modifier = Modifier
		) {
			Box(
				modifier = Modifier
					.height(56.dp)
					.padding(start = 16.dp),
				contentAlignment = Alignment.CenterStart
			) {
				Text(
					stringResource(R.string.share),
					style = MaterialTheme.typography.bodyLarge,
					modifier = Modifier.alpha(0.8f)
				)
			}

			Card(
				onClick = {
					shareBasicURL()
					dismiss()
				},
				modifier = Modifier
					.fillMaxWidth(),
				shape = RectangleShape,
				colors = CardDefaults.cardColors(containerColor = Color.Transparent),
			) {
				Row(
					verticalAlignment = Alignment.CenterVertically,
					modifier = Modifier
						.height(56.dp)
						.padding(start = 16.dp)
				) {
					Icon(
						Icons.Outlined.Link,
						"",
						modifier = Modifier.padding(end = 8.dp)
					)
					Text(
						stringResource(R.string.menu_share_url),
						style = MaterialTheme.typography.bodyLarge
					)
				}
			}
			Card(
				onClick = {
					shareQRCode()
					dismiss()
				},
				modifier = Modifier
					.fillMaxWidth(),
				shape = RectangleShape,
				colors = CardDefaults.cardColors(containerColor = Color.Transparent),
			) {
				Row(
					verticalAlignment = Alignment.CenterVertically,
					modifier = Modifier
						.height(56.dp)
						.padding(start = 16.dp)
				) {
					Icon(
						Icons.Outlined.QrCode,
						"",
						modifier = Modifier.padding(end = 8.dp)
					)
					Text(
						stringResource(R.string.menu_share_qr),
						style = MaterialTheme.typography.bodyLarge
					)
				}
			}
		}
	}
}