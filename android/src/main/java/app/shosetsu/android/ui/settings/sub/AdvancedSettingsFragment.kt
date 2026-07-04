package app.shosetsu.android.ui.settings.sub

import android.webkit.CookieManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.shosetsu.android.R
import app.shosetsu.android.common.SettingKey.ACRAEnabled
import app.shosetsu.android.common.SettingKey.AutoBookmarkFromQR
import app.shosetsu.android.common.SettingKey.ExposeTrueChapterDelete
import app.shosetsu.android.common.SettingKey.LogToFile
import app.shosetsu.android.common.SettingKey.ProxyHost
import app.shosetsu.android.common.SettingKey.RequireDoubleBackToExit
import app.shosetsu.android.common.SettingKey.SiteProtectionDelay
import app.shosetsu.android.common.SettingKey.UseProxy
import app.shosetsu.android.common.SettingKey.UseShosetsuAgent
import app.shosetsu.android.common.SettingKey.UserAgent
import app.shosetsu.android.common.SettingKey.VerifyCheckSum
import app.shosetsu.android.common.ext.logE
import app.shosetsu.android.common.ext.logV
import app.shosetsu.android.common.ext.viewModelDi
import app.shosetsu.android.common.utils.webview.WebViewUtil
import app.shosetsu.android.view.compose.NavigateBackButton
import app.shosetsu.android.view.compose.SimpleIconButton
import app.shosetsu.android.view.compose.setting.ProxySettingsContent
import app.shosetsu.android.view.compose.setting.SliderSettingContent
import app.shosetsu.android.view.compose.setting.StringSettingContent
import app.shosetsu.android.view.compose.setting.SwitchSettingContent
import app.shosetsu.android.view.compose.setting.widget.TextPreferenceWidget
import app.shosetsu.android.view.uimodels.StableHolder
import app.shosetsu.android.viewmodel.abstracted.settings.AAdvancedSettingsViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

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

@Composable
fun AdvancedSettingsView(
	onBack: () -> Unit
) {
	val viewModel: AAdvancedSettingsViewModel = viewModelDi()

	val purgeState by viewModel.purgeState.collectAsState(null)
	val workerState by viewModel.workerState.collectAsState(null)

	val hostState = remember { SnackbarHostState() }
	val scope = rememberCoroutineScope()
	val context = LocalContext.current

	LaunchedEffect(purgeState) {
		when (purgeState) {
			null -> {}
			is AAdvancedSettingsViewModel.PurgeState.Failure -> {
				scope.launch {
					logE("Failed to purge")
					val result = hostState.showSnackbar(
						context.getString(R.string.fragment_settings_advanced_snackbar_purge_failure),
						duration = SnackbarDuration.Long,
						actionLabel = context.getString(R.string.retry),
						withDismissAction = true
					)
					if (result == SnackbarResult.ActionPerformed) {
						viewModel.purgeUselessData()
					}
				}
			}

			AAdvancedSettingsViewModel.PurgeState.Success -> {
				scope.launch {
					hostState.showSnackbar(
						context.getString(R.string.fragment_settings_advanced_snackbar_purge_success)
					)
				}
			}

		}
	}

	LaunchedEffect(workerState) {
		when (workerState) {
			AAdvancedSettingsViewModel.RestartResult.RESTARTED -> {
				val result = hostState.showSnackbar(
					context.getString(R.string.settings_advanced_snackbar_cycle_kill_success),
					duration = SnackbarDuration.Long,
					actionLabel = context.getString(R.string.restart)
				)

				if (result == SnackbarResult.ActionPerformed) {
					viewModel.startCycleWorkers()
				}
			}

			null -> {}

			AAdvancedSettingsViewModel.RestartResult.KILLED -> {
				scope.launch {
					hostState.showSnackbar(
						context.getString(R.string.settings_advanced_cycle_start_success)
					)
				}
			}
		}
	}

	AdvancedSettingsContent(
		viewModel,
		onPurgeNovelCache = viewModel::purgeUselessData,
		onKillCycleWorkers = viewModel::killCycleWorkers,
		onClearCookies = {
			viewModel.logV("Clearing cookies")
			CookieManager.getInstance().removeAllCookies {
				viewModel.logV("Cookies cleared")
				scope.launch {
					hostState.showSnackbar(
						context.getString(
							if (it) {
								R.string.settings_advanced_clear_cookies_complete
							} else {
								R.string.settings_advanced_clear_cookies_nada
							}
						)
					)
				}
			}
		},
		onForceRepoSync = {
			viewModel.forceRepoSync()
		},
		onBack = onBack,
		hostState = hostState
	)
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedSettingsContent(
	viewModel: AAdvancedSettingsViewModel,
	onPurgeNovelCache: () -> Unit,
	onKillCycleWorkers: () -> Unit,
	onForceRepoSync: () -> Unit,
	onClearCookies: () -> Unit,
	onBack: () -> Unit,
	hostState: SnackbarHostState
) {
	val useShosetsuAgent by viewModel.settingsRepo.getBooleanFlow(UseShosetsuAgent)
		.collectAsState()
	Scaffold(
		topBar = {
			TopAppBar(
				title = {
					Text(stringResource(R.string.advanced))
				},
				navigationIcon = {
					NavigateBackButton(onBack)
				}
			)
		},
		snackbarHost = {
			SnackbarHost(hostState)
		}
	) { paddingValues ->
		LazyColumn(
			contentPadding = PaddingValues(
				top = 16.dp,
				bottom = 64.dp
			),
			modifier = Modifier.padding(paddingValues)
		) {

			item {
				TextPreferenceWidget(
					title = stringResource(R.string.remove_novel_cache),
					subtitle = stringResource(R.string.settings_advanced_purge_novel_cache),
					onPreferenceClick = onPurgeNovelCache,
					iconDescription = null
				)
			}

			item {
				SwitchSettingContent(
					title = stringResource(R.string.settings_advanced_verify_checksum_title),
					description = stringResource(R.string.settings_advanced_verify_checksum_desc),
					repo = viewModel.settingsRepo,
					key = VerifyCheckSum
				)
			}

			item {
				SwitchSettingContent(
					title = stringResource(R.string.settings_advanced_require_double_back_title),
					description = stringResource(R.string.settings_advanced_require_double_back_desc),
					repo = viewModel.settingsRepo,
					key = RequireDoubleBackToExit
				)
			}

			item {
				TextPreferenceWidget(
					title = stringResource(R.string.settings_advanced_kill_cycle_workers_title),
					subtitle = stringResource(R.string.settings_advanced_kill_cycle_workers_desc),
					onPreferenceClick = onKillCycleWorkers,
					iconDescription = null
				)
			}

			item {
				TextPreferenceWidget(
					title = stringResource(R.string.settings_advanced_force_repo_update_title),
					subtitle = stringResource(R.string.settings_advanced_force_repo_update_desc),
					onPreferenceClick = onForceRepoSync,
					iconDescription = null
				)
			}

			item {
				TextPreferenceWidget(
					title = stringResource(R.string.settings_advanced_clear_cookies_title),
					subtitle = stringResource(R.string.settings_advanced_clear_cookies_desc),
					onPreferenceClick = onClearCookies,
					iconDescription = null
				)
			}

			item {
				SwitchSettingContent(
					title = stringResource(R.string.settings_advanced_true_chapter_delete_title),
					description = stringResource(R.string.settings_advanced_true_chapter_delete_desc),
					modifier = Modifier
						.fillMaxWidth(),
					repo = viewModel.settingsRepo,
					key = ExposeTrueChapterDelete
				)
			}

			item {
				SwitchSettingContent(
					title = stringResource(R.string.settings_advanced_log_title),
					description = stringResource(R.string.settings_advanced_log_desc),
					modifier = Modifier
						.fillMaxWidth(),
					repo = viewModel.settingsRepo,
					key = LogToFile
				)
			}

			item {
				SwitchSettingContent(
					title = stringResource(R.string.settings_advanced_auto_bookmark_title),
					description = stringResource(R.string.settings_advanced_auto_bookmark_desc),
					modifier = Modifier
						.fillMaxWidth(),
					repo = viewModel.settingsRepo,
					key = AutoBookmarkFromQR
				)
			}

			item {
				SwitchSettingContent(
					title = stringResource(R.string.intro_acra),
					description = stringResource(R.string.settings_advanced_enable_acra),
					modifier = Modifier
						.fillMaxWidth(),
					repo = viewModel.settingsRepo,
					key = ACRAEnabled
				)
			}

			item {
				SliderSettingContent(
					title = stringResource(R.string.settings_advanced_site_protection_title),
					description = stringResource(R.string.settings_advanced_site_protection_desc),
					valueRange = remember { StableHolder(300..5000) },
					parseValue = {
						"$it ms"
					},
					repo = viewModel.settingsRepo,
					key = SiteProtectionDelay,
					haveSteps = false,
				)
			}

			item {
				SwitchSettingContent(
					title = stringResource(R.string.settings_advanced_sua_title),
					description = stringResource(R.string.settings_advanced_sua_desc),
					repo = viewModel.settingsRepo,
					modifier = Modifier
						.fillMaxWidth(),
					key = UseShosetsuAgent
				)
			}

			item {
				Column(
					modifier = Modifier
						.alpha(if (useShosetsuAgent) .5f else 1f),
					horizontalAlignment = Alignment.CenterHorizontally
				) {
					StringSettingContent(
						title = stringResource(R.string.settings_advanced_ua_title),
						description = stringResource(R.string.settings_advanced_ua_desc),
						repo = viewModel.settingsRepo,
						modifier = Modifier
							.fillMaxWidth(),
						key = UserAgent,
						enabled = !useShosetsuAgent
					)
					val context = LocalContext.current
					SimpleIconButton(
						Icons.Default.Refresh, stringResource(R.string.reset),
						onClick = {
							runBlocking {
								viewModel.settingsRepo.setString(UserAgent, WebViewUtil.getInferredUserAgent(context))
							}
						},
						enabled = !useShosetsuAgent
					)
				}
			}

			item {
				Row(modifier = Modifier.fillMaxWidth()) {
					ProxySettingsContent(
						title = "Use SOCKS5 Proxy",
						description = "Use proxy for all internal communications. Changes applied after restart",
						repo = viewModel.settingsRepo,
						usedKey = UseProxy,
						settingKey = ProxyHost,
					)
				}
			}
		}
	}
}
