package app.shosetsu.android.ui.settings.sub

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.shosetsu.android.R
import app.shosetsu.android.common.SettingKey
import app.shosetsu.android.common.ext.ComposeView
import app.shosetsu.android.common.ext.viewModelDi
import app.shosetsu.android.view.compose.NavigateBackButton
import app.shosetsu.android.view.compose.setting.SliderSettingContent
import app.shosetsu.android.view.compose.setting.SwitchSettingContent
import app.shosetsu.android.view.controller.ShosetsuFragment
import app.shosetsu.android.view.uimodels.StableHolder
import app.shosetsu.android.viewmodel.abstracted.settings.ADownloadSettingsViewModel
import kotlinx.coroutines.launch

/*
 * This file is part of Shosetsu.
 *
 * Shosetsu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Shosetsu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Shosetsu.  If not, see <https://www.gnu.org/licenses/>.
 */

/**
 * Shosetsu
 * 13 / 07 / 2019
 */
@Deprecated("Composed")
class DownloadSettingsFragment : ShosetsuFragment() {
	override val viewTitleRes: Int = R.string.settings_download

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedViewState: Bundle?
	): View {
		return ComposeView {
		}
	}
}

@Composable
fun DownloadSettingsView(
	onBack: () -> Unit
) {
	val viewModel: ADownloadSettingsViewModel = viewModelDi()
	val notifyRestartWorker by viewModel.notifyRestartWorker.collectAsState(null)

	val hostState = remember { SnackbarHostState() }
	val context = LocalContext.current
	val scope = rememberCoroutineScope()

	LaunchedEffect(notifyRestartWorker) {
		if (notifyRestartWorker != null) {
			scope.launch {
				val result = hostState.showSnackbar(
					context.getString(
						R.string.fragment_settings_restart_worker,
						context.getString(R.string.worker_title_download)
					),
					duration = SnackbarDuration.Long
				)

				if (result == SnackbarResult.ActionPerformed) {
					viewModel.restartDownloadWorker()
				}
			}
		}
	}

	DownloadSettingsContent(
		viewModel,
		hostState,
		onBack
	)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadSettingsContent(
	viewModel: ADownloadSettingsViewModel,
	hostState: SnackbarHostState,
	onBack: () -> Unit,
) {
	Scaffold(
		topBar = {
			TopAppBar(
				title = {
					Text(stringResource(R.string.settings_download))
				},
				navigationIcon = {
					NavigateBackButton(onBack)
				},
				colors = TopAppBarDefaults.topAppBarColors(
					containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
					titleContentColor = MaterialTheme.colorScheme.onSurface,
				)
			)
		},
		snackbarHost = {
			SnackbarHost(hostState)
		}
	) { paddingValues ->
		LazyColumn(
			contentPadding = PaddingValues(top = 16.dp, bottom = 64.dp),
			verticalArrangement = Arrangement.spacedBy(8.dp),
			modifier = Modifier.padding(paddingValues)
		) {
			item {
				SliderSettingContent(
					"Download thread pool size",
					"How many simultaneous downloads occur at once",
					remember { StableHolder(1..6) },
					{ "$it" },
					viewModel.settingsRepo,
					SettingKey.DownloadThreadPool,
					modifier = Modifier
						.fillMaxWidth()
				)
			}

			item {
				SliderSettingContent(
					"Download threads per Extension",
					"How many simultaneous downloads per extension that can occur at once",
					remember { StableHolder(1..6) },
					{ "$it" },
					viewModel.settingsRepo,
					SettingKey.DownloadExtThreads,
					modifier = Modifier
						.fillMaxWidth()
				)
			}

			// TODO Figure out how to change download directory
			item {
				SwitchSettingContent(
					stringResource(R.string.download_chapter_updates),
					stringResource(R.string.download_chapter_updates_desc),
					viewModel.settingsRepo,
					SettingKey.DownloadNewNovelChapters,
					modifier = Modifier
						.fillMaxWidth()
				)
			}

			item {
				SwitchSettingContent(
					"Allow downloading on metered connection",
					"",//TODO Description
					viewModel.settingsRepo,
					SettingKey.DownloadOnMeteredConnection,
					modifier = Modifier
						.fillMaxWidth()
				)
			}
			item {
				SwitchSettingContent(
					"Download on low battery",
					"",//TODO Description
					viewModel.settingsRepo,
					SettingKey.DownloadOnLowBattery,
					modifier = Modifier
						.fillMaxWidth()
				)
			}
			item {
				SwitchSettingContent(
					"Download on low storage",
					"",//TODO Description
					viewModel.settingsRepo,
					SettingKey.DownloadOnLowStorage,
					modifier = Modifier
						.fillMaxWidth()
				)
			}

			item {
				SwitchSettingContent(
					"Download only when idle",
					"",//TODO Description
					viewModel.settingsRepo,
					SettingKey.DownloadOnlyWhenIdle,
					modifier = Modifier
						.fillMaxWidth()
				)
			}

			item {
				SwitchSettingContent(
					"Bookmarked novel on download",
					"If a novel is not bookmarked when a chapter is downloaded, this will",
					viewModel.settingsRepo,
					SettingKey.BookmarkOnDownload,
					modifier = Modifier
						.fillMaxWidth()
				)
			}
			item {
				SwitchSettingContent(
					stringResource(R.string.settings_download_notify_extension_install_title),
					stringResource(R.string.settings_download_notify_extension_install_desc),
					viewModel.settingsRepo,
					SettingKey.NotifyExtensionDownload,
					modifier = Modifier
						.fillMaxWidth()
				)
			}

			item {
				SliderSettingContent(
					stringResource(R.string.settings_download_delete_on_read_title),
					stringResource(R.string.settings_download_delete_on_read_desc),
					remember { StableHolder(-1..3) },
					{
						when (it) {
							-1 -> "Disabled"
							0 -> "Current"
							1 -> "Previous"
							2 -> "2nd to last"
							3 -> "3rd to last"
							else -> "Invalid"
						}
					},
					viewModel.settingsRepo,
					SettingKey.DeleteReadChapter,
					modifier = Modifier
						.fillMaxWidth()
				)
			}

			item {
				SwitchSettingContent(
					"Unique chapter notification",
					"Create a notification for each chapters status when downloading",
					viewModel.settingsRepo,
					SettingKey.DownloadNotifyChapters,
					modifier = Modifier
						.fillMaxWidth()
				)
			}
		}
	}
}