package app.shosetsu.android.ui.settings

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.shosetsu.android.R
import app.shosetsu.android.view.compose.NavigateBackButton

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
 * @since 06 / 10 / 2021
 * @author Doomsdayrs
 */
@Composable
fun SettingsView(
	navToView: () -> Unit,
	navToReader: () -> Unit,
	navToDownload: () -> Unit,
	navToUpdate: () -> Unit,
	navToAdvanced: () -> Unit,
	onBack: () -> Unit
) {
	SettingsContent(
		navToView,
		navToReader,
		navToDownload,
		navToUpdate,
		navToAdvanced,
		onBack
	)
}

@Composable
fun SettingMenuItem(@StringRes title: Int, @DrawableRes drawableRes: Int, onClick: () -> Unit) {
	Box(
		modifier = Modifier
			.clickable(onClick = onClick)
			.fillMaxWidth(),
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically
		) {
			Icon(
				painterResource(drawableRes),
				null,
				modifier = Modifier
					.padding(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 24.dp)
					.size(24.dp),
				tint = MaterialTheme.colorScheme.primary
			)
			Text(stringResource(title))
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
	navToView: () -> Unit,
	navToReader: () -> Unit,
	navToDownload: () -> Unit,
	navToUpdate: () -> Unit,
	navToAdvanced: () -> Unit,
	onBack: () -> Unit
) {
	Scaffold(
		topBar = {
			TopAppBar(
				title = {
					Text(stringResource(R.string.settings))
				},
				navigationIcon = {
					NavigateBackButton(onBack)
				},
				colors = TopAppBarDefaults.topAppBarColors(
					containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
					titleContentColor = MaterialTheme.colorScheme.onSurface,
				)
			)
		}
	) { paddingValues ->
		Column(
			Modifier.padding(paddingValues)
		) {
			SettingMenuItem(R.string.view, R.drawable.view_module, navToView)

			SettingMenuItem(R.string.reader, R.drawable.book, navToReader)

			SettingMenuItem(R.string.download, R.drawable.download, navToDownload)

			SettingMenuItem(R.string.update, R.drawable.update, navToUpdate)

			SettingMenuItem(R.string.advanced, R.drawable.settings, navToAdvanced)
		}
	}
}
