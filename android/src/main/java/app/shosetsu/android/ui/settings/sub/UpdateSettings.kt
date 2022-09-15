package app.shosetsu.android.ui.settings.sub

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.shosetsu.android.BuildConfig
import app.shosetsu.android.R
import app.shosetsu.android.common.SettingKey
import app.shosetsu.android.common.ext.viewModel
import app.shosetsu.android.view.compose.ShosetsuCompose
import app.shosetsu.android.view.compose.setting.HeaderSettingContent
import app.shosetsu.android.view.compose.setting.SliderSettingContent
import app.shosetsu.android.view.compose.setting.SwitchSettingContent
import app.shosetsu.android.view.controller.ShosetsuController
import app.shosetsu.android.view.uimodels.StableHolder
import app.shosetsu.android.viewmodel.abstracted.settings.AUpdateSettingsViewModel

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
class UpdateSettings : ShosetsuController() {
	override val viewTitleRes: Int = R.string.settings_update
	val viewModel: AUpdateSettingsViewModel by viewModel()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedViewState: Bundle?
	): View = ComposeView(requireContext()).apply {
		setViewTitle()
		setContent {
			ShosetsuCompose {
				UpdateSettingsContent(
					viewModel,
				)
			}
		}
	}
}


@Composable
fun UpdateSettingsContent(viewModel: AUpdateSettingsViewModel) {
	LazyColumn(
		contentPadding = PaddingValues(bottom = 64.dp, top = 16.dp),
		verticalArrangement = Arrangement.spacedBy(8.dp)
	) {
		item {
			HeaderSettingContent(
				stringResource(R.string.settings_update_header_novel),
			)
		}

		item {
			SliderSettingContent(
				title = stringResource(R.string.settings_update_novel_frequency_title),
				description = stringResource(R.string.settings_update_novel_frequency_desc),
				valueRange = remember { StableHolder(1..168) },
				parseValue = {
					when (it) {
						12 -> "Bi Daily"
						24 -> "Daily"
						48 -> "2 Days"
						72 -> "3 Days"
						96 -> "4 Days"
						120 -> "5 Days"
						144 -> "6 Days"
						168 -> "Weekly"
						else -> "$it Hour(s)"
					}
				},
				repo = viewModel.settingsRepo,
				key = SettingKey.NovelUpdateCycle,
				haveSteps = false,
				manipulateUpdate = {
					when (it) {
						in 24..35 -> 24
						in 36..48 -> 48
						in 48..59 -> 48
						in 60..72 -> 72
						in 72..83 -> 72
						in 84..96 -> 96
						in 96..107 -> 96
						in 108..120 -> 120
						in 120..131 -> 120
						in 132..144 -> 144
						in 144..156 -> 144
						in 157..168 -> 168
						else -> it
					}
				},
				maxHeaderSize = 80.dp
			)
		}

		item {
			SwitchSettingContent(
				stringResource(R.string.settings_update_novel_on_update_title),
				stringResource(R.string.settings_update_novel_on_update_desc),
				viewModel.settingsRepo,
				SettingKey.DownloadNewNovelChapters,
				modifier = Modifier
					.fillMaxWidth()
			)
		}

		item {
			SwitchSettingContent(
				stringResource(R.string.settings_update_novel_only_ongoing_title),
				stringResource(R.string.settings_update_novel_only_ongoing_desc),
				viewModel.settingsRepo,
				SettingKey.OnlyUpdateOngoingNovels,
				modifier = Modifier
					.fillMaxWidth()
			)
		}
		item {
			SwitchSettingContent(
				stringResource(R.string.settings_update_novel_on_metered_title),
				stringResource(R.string.settings_update_novel_on_metered_desc),
				viewModel.settingsRepo,
				SettingKey.NovelUpdateOnMeteredConnection,
				modifier = Modifier
					.fillMaxWidth()
			)
		}
		item {
			SwitchSettingContent(
				stringResource(R.string.settings_update_novel_on_low_bat_title),
				stringResource(R.string.settings_update_novel_on_low_bat_desc),
				viewModel.settingsRepo,
				SettingKey.NovelUpdateOnLowBattery,
				modifier = Modifier
					.fillMaxWidth()
			)
		}
		item {
			SwitchSettingContent(
				stringResource(R.string.settings_update_novel_on_low_sto_title),
				stringResource(R.string.settings_update_novel_on_low_sto_desc),
				viewModel.settingsRepo,
				SettingKey.NovelUpdateOnLowStorage,
				modifier = Modifier
					.fillMaxWidth()
			)
		}
		if (BuildConfig.VERSION_CODE > Build.VERSION_CODES.M)
			item {
				SwitchSettingContent(
					stringResource(R.string.settings_update_novel_only_idle_title),
					stringResource(R.string.settings_update_novel_only_idle_desc),
					viewModel.settingsRepo,
					SettingKey.NovelUpdateOnlyWhenIdle,
					modifier = Modifier
						.fillMaxWidth()
				)
			}

		item {
			SwitchSettingContent(
				stringResource(R.string.settings_update_novel_notification_style_title),
				stringResource(R.string.settings_update_novel_notification_style_desc),
				viewModel.settingsRepo,
				SettingKey.UpdateNotificationStyle,
				modifier = Modifier
					.fillMaxWidth()
			)
		}

		item {
			SwitchSettingContent(
				stringResource(R.string.settings_update_novel_show_progress_title),
				stringResource(R.string.settings_update_novel_show_progress_desc),
				viewModel.settingsRepo,
				SettingKey.NovelUpdateShowProgress,
				modifier = Modifier
					.fillMaxWidth()
			)
		}

		item {
			SwitchSettingContent(
				stringResource(R.string.settings_update_novel_classic_notification_title),
				stringResource(R.string.settings_update_novel_classic_notification_desc),
				viewModel.settingsRepo,
				SettingKey.NovelUpdateClassicFinish,
				modifier = Modifier
					.fillMaxWidth()
			)
		}
		item {
			HeaderSettingContent(
				stringResource(R.string.settings_update_header_repositories)
			)
		}

		item {
			SwitchSettingContent(
				stringResource(R.string.settings_update_repo_on_metered_title),
				stringResource(R.string.settings_update_repo_on_metered_desc),
				viewModel.settingsRepo,
				SettingKey.RepoUpdateOnMeteredConnection,
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