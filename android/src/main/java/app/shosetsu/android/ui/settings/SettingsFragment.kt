package app.shosetsu.android.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ChromeReaderMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import app.shosetsu.android.BuildConfig
import app.shosetsu.android.R
import app.shosetsu.android.view.compose.NavigateBackButton
import app.shosetsu.android.view.compose.setting.widget.TextPreferenceWidget

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
	navToAppearance: () -> Unit,
	navToLibrary: () -> Unit,
	navToReader: () -> Unit,
	navToDownloads: () -> Unit,
	navToBrowse: () -> Unit,
	navToBackup: () -> Unit,
	navToAdvanced: () -> Unit,
	navToAbout: () -> Unit,
	onBack: () -> Unit
) {
	SettingsContent(
		navToAppearance = navToAppearance,
		navToLibrary = navToLibrary,
		navToReader = navToReader,
		navToDownloads = navToDownloads,
		navToBrowse = navToBrowse,
		navToBackup = navToBackup,
		navToAdvanced = navToAdvanced,
		navToAbout = navToAbout,
		onBack = onBack
	)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
	navToAppearance: () -> Unit,
	navToLibrary: () -> Unit,
	navToReader: () -> Unit,
	navToDownloads: () -> Unit,
	navToBrowse: () -> Unit,
	navToBackup: () -> Unit,
	navToAdvanced: () -> Unit,
	navToAbout: () -> Unit,
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
				}
			)
		}
	) { paddingValues ->
		Column(
			Modifier.padding(paddingValues)
		) {
			TextPreferenceWidget(
				title = stringResource(R.string.appearance),
				subtitle = stringResource(R.string.appearance_summary),
				icon = Icons.Outlined.Palette,
				onPreferenceClick = navToAppearance
			)

			TextPreferenceWidget(
				title = stringResource(R.string.library),
				subtitle = stringResource(R.string.library_summary),
				icon = Icons.Outlined.CollectionsBookmark,
				onPreferenceClick = navToLibrary
			)

			TextPreferenceWidget(
				title = stringResource(R.string.reader),
				subtitle = stringResource(R.string.reader_summary),
				icon = Icons.AutoMirrored.Outlined.ChromeReaderMode,
				onPreferenceClick = navToReader
			)

			TextPreferenceWidget(
				title = stringResource(R.string.downloads),
				subtitle = stringResource(R.string.downloads_summary),
				icon = Icons.Filled.Download,
				onPreferenceClick = navToDownloads
			)

			TextPreferenceWidget(
				title = stringResource(R.string.browse),
				subtitle = stringResource(R.string.browse_summary),
				icon = Icons.Outlined.Explore,
				onPreferenceClick = navToBrowse
			)

			TextPreferenceWidget(
				title = stringResource(R.string.backup),
				subtitle = stringResource(R.string.backup_summary),
				icon = Icons.Outlined.Restore,
				onPreferenceClick = navToBackup
			)

			TextPreferenceWidget(
				title = stringResource(R.string.advanced),
				subtitle = stringResource(R.string.advanced_summary),
				icon = Icons.Outlined.Code,
				onPreferenceClick = navToAdvanced
			)

			TextPreferenceWidget(
				title = stringResource(R.string.about),
				subtitle = stringResource(R.string.about_summary, BuildConfig.VERSION_NAME),
				icon = Icons.Outlined.Info,
				onPreferenceClick = navToAbout
			)
		}
	}
}

@PreviewLightDark
@Composable
fun SettingsContentPreview() {
	SettingsContent({}, {}, {}, {}, {}, {}, {}, {}, {})
}
