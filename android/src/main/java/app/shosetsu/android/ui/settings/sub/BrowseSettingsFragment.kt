package app.shosetsu.android.ui.settings.sub

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.shosetsu.android.R
import app.shosetsu.android.common.SettingKey
import app.shosetsu.android.common.ext.viewModelDi
import app.shosetsu.android.view.compose.NavigateBackButton
import app.shosetsu.android.view.compose.setting.SwitchSettingContent
import app.shosetsu.android.view.compose.setting.widget.TextPreferenceWidget
import app.shosetsu.android.viewmodel.abstracted.settings.ABrowseSettingsViewModel

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
 * shosetsu
 * 20 / 06 / 2020
 */

@Composable
fun BrowseSettingsView(
	onBack: () -> Unit,
	onNavToRepositories: () -> Unit,
) {
	val viewModel: ABrowseSettingsViewModel = viewModelDi()

	BrowseSettingsContent(
		viewModel = viewModel,
		onNavToRepositories = onNavToRepositories,
		onBack = onBack
	)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseSettingsContent(
	viewModel: ABrowseSettingsViewModel,
	onNavToRepositories: () -> Unit,
	onBack: () -> Unit
) {
	Scaffold(
		topBar = {
			TopAppBar(
				title = {
					Text(stringResource(R.string.browse))
				},
				navigationIcon = {
					NavigateBackButton(onBack)
				}
			)
		}
	) { paddingValues ->
		LazyColumn(
			contentPadding = PaddingValues(bottom = 64.dp, top = 16.dp),
			modifier = Modifier.padding(paddingValues)
		) {
			item {
				val reposCount by viewModel.repoCount.collectAsState()

				TextPreferenceWidget(
					title = stringResource(R.string.settings_browse_repositories_title),
					subtitle = stringResource(R.string.settings_browse_repositories_desc, reposCount),
					onPreferenceClick = onNavToRepositories
				)
			}

			item {
				SwitchSettingContent(
					title = stringResource(R.string.settings_update_repo_on_metered_title),
					description = stringResource(R.string.settings_update_repo_on_metered_desc),
					repo = viewModel.settingsRepo,
					key = SettingKey.RepoUpdateOnMeteredConnection,
					modifier = Modifier
						.fillMaxWidth()
				)
			}
			item {
				SwitchSettingContent(
					stringResource(R.string.settings_update_repo_on_low_bat_title),
					stringResource(R.string.settings_update_repo_on_low_bat_desc),
					viewModel.settingsRepo,
					SettingKey.RepoUpdateOnLowBattery,
					modifier = Modifier
						.fillMaxWidth()
				)
			}
			item {
				SwitchSettingContent(
					stringResource(R.string.settings_update_repo_on_low_sto_title),
					stringResource(R.string.settings_update_repo_on_low_sto_desc),
					viewModel.settingsRepo,
					SettingKey.RepoUpdateOnLowStorage,
					modifier = Modifier
						.fillMaxWidth()
				)
			}
			item {
				SwitchSettingContent(
					stringResource(R.string.settings_update_repo_disable_on_fail_title),
					stringResource(R.string.settings_update_repo_disable_on_fail_desc),
					viewModel.settingsRepo,
					SettingKey.RepoUpdateDisableOnFail, modifier = Modifier.fillMaxWidth()
				)
			}
		}
	}
}
