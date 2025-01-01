package app.shosetsu.android.ui.settings.sub

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.shosetsu.android.R
import app.shosetsu.android.common.SettingKey.AppTheme
import app.shosetsu.android.common.SettingKey.ChapterColumnsInLandscape
import app.shosetsu.android.common.SettingKey.ChapterColumnsInPortait
import app.shosetsu.android.common.SettingKey.NavStyle
import app.shosetsu.android.common.SettingKey.NovelBadgeToast
import app.shosetsu.android.common.SettingKey.SelectedNovelCardType
import app.shosetsu.android.common.ext.launchIO
import app.shosetsu.android.common.ext.viewModelDi
import app.shosetsu.android.ui.settings.widget.AppThemeModePreferenceWidget
import app.shosetsu.android.ui.settings.widget.ListPreferenceWidget
import app.shosetsu.android.ui.settings.widget.PreferenceGroupHeader
import app.shosetsu.android.view.compose.NavigateBackButton
import app.shosetsu.android.view.compose.setting.DropdownSettingContent
import app.shosetsu.android.view.compose.setting.ListPreferenceSettingContent
import app.shosetsu.android.view.compose.setting.NumberPickerSettingContent
import app.shosetsu.android.view.compose.setting.SwitchSettingContent
import app.shosetsu.android.view.uimodels.StableHolder
import app.shosetsu.android.viewmodel.abstracted.settings.AViewSettingsViewModel
import kotlinx.collections.immutable.toImmutableList

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

@Composable
fun AppearanceSettingsView(
	onBack: () -> Unit
) {
	val viewModel: AViewSettingsViewModel = viewModelDi()

	AppearanceSettingsContent(
		viewModel,
		onBack = onBack
	)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettingsContent(
	viewModel: AViewSettingsViewModel,
	onBack: () -> Unit
) {
	Scaffold(
		topBar = {
			TopAppBar(
				title = {
					Text(stringResource(R.string.appearance))
				},
				navigationIcon = {
					NavigateBackButton(onBack)
				}
			)
		}
	) { paddingValues ->
		LazyColumn(
			contentPadding = PaddingValues(
				top = 16.dp,
				bottom = 64.dp
			),
			verticalArrangement = Arrangement.spacedBy(8.dp),
			modifier = Modifier.padding(paddingValues)
		) {
			item {
				PreferenceGroupHeader(stringResource(R.string.theme))
			}

			item {
				val choice by viewModel.appTheme.collectAsState()

				AppThemeModePreferenceWidget(
					value = choice,
					onItemClick = {
						launchIO {
							viewModel.settingsRepo.setInt(AppTheme, it.key)
						}
						it.setAppCompatDelegateThemeMode()
					}
				)
			}

			item {
				PreferenceGroupHeader(stringResource(R.string.display))
			}

			item {
				NumberPickerSettingContent(
					title = stringResource(R.string.columns_of_novel_listing_p),
					description = stringResource(R.string.columns_zero_automatic),
					range = remember { StableHolder(0..10) },
					repo = viewModel.settingsRepo,
					key = ChapterColumnsInPortait,
					modifier = Modifier
						.fillMaxWidth()
				)
			}

			item { Spacer(modifier = Modifier.height(12.dp)) }

			item {
				NumberPickerSettingContent(
					title = stringResource(R.string.columns_of_novel_listing_h),
					description = stringResource(R.string.columns_zero_automatic),
					range = remember { StableHolder(0..10) },
					repo = viewModel.settingsRepo,
					key = ChapterColumnsInLandscape,
					modifier = Modifier
						.fillMaxWidth()
				)
			}

			item { Spacer(modifier = Modifier.height(12.dp)) }

			item {
				ListPreferenceSettingContent(
					title = stringResource(R.string.novel_card_type_selector_title),
					choices = stringArrayResource(R.array.novel_card_types).toList(),
					repo = viewModel.settingsRepo,
					key = SelectedNovelCardType
				)
			}

			item { Spacer(modifier = Modifier.height(12.dp)) }

			item {
				SwitchSettingContent(
					title = stringResource(R.string.novel_badge_toast_title),
					description = stringResource(R.string.novel_badge_toast_desc),
					repo = viewModel.settingsRepo,
					key = NovelBadgeToast,
					modifier = Modifier.fillMaxWidth()
				)
			}

			item { Spacer(modifier = Modifier.height(12.dp)) }

			item {
				SwitchSettingContent(
					title = stringResource(R.string.settings_view_legacy_nav_title),
					description = stringResource(R.string.settings_view_legacy_nav_desc),
					modifier = Modifier.fillMaxWidth(),
					repo = viewModel.settingsRepo,
					key = NavStyle
				)
			}
		}
	}
}
