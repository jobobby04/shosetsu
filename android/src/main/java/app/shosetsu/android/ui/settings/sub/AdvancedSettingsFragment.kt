package app.shosetsu.android.ui.settings.sub

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.shosetsu.android.R
import app.shosetsu.android.common.SettingKey.ACRAEnabled
import app.shosetsu.android.common.SettingKey.AppTheme
import app.shosetsu.android.common.SettingKey.AutoBookmarkFromQR
import app.shosetsu.android.common.SettingKey.ConcurrentMemoryExperiment
import app.shosetsu.android.common.SettingKey.ExposeTrueChapterDelete
import app.shosetsu.android.common.SettingKey.LogToFile
import app.shosetsu.android.common.SettingKey.ProxyHost
import app.shosetsu.android.common.SettingKey.RequireDoubleBackToExit
import app.shosetsu.android.common.SettingKey.SiteProtectionPeriod
import app.shosetsu.android.common.SettingKey.SiteProtectionPermits
import app.shosetsu.android.common.SettingKey.UseProxy
import app.shosetsu.android.common.SettingKey.UseShosetsuAgent
import app.shosetsu.android.common.SettingKey.UserAgent
import app.shosetsu.android.common.SettingKey.VerifyCheckSum
import app.shosetsu.android.common.consts.DEFAULT_USER_AGENT
import app.shosetsu.android.common.ext.ComposeView
import app.shosetsu.android.common.ext.launchIO
import app.shosetsu.android.common.ext.logE
import app.shosetsu.android.common.ext.logV
import app.shosetsu.android.common.ext.viewModelDi
import app.shosetsu.android.view.compose.NavigateBackButton
import app.shosetsu.android.view.compose.setting.ButtonSettingContent
import app.shosetsu.android.view.compose.setting.DropdownSettingContent
import app.shosetsu.android.view.compose.setting.ProxySettingsContent
import app.shosetsu.android.view.compose.setting.SliderSettingContent
import app.shosetsu.android.view.compose.setting.StringSettingContent
import app.shosetsu.android.view.compose.setting.SwitchSettingContent
import app.shosetsu.android.view.controller.ShosetsuFragment
import app.shosetsu.android.view.uimodels.StableHolder
import app.shosetsu.android.viewmodel.abstracted.settings.AAdvancedSettingsViewModel
import kotlinx.collections.immutable.toImmutableList
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

/**
 * Shosetsu
 * 13 / 07 / 2019
 */
@Deprecated("Composed")
class AdvancedSettingsFragment : ShosetsuFragment() {
	override val viewTitleRes: Int = R.string.settings_advanced

	/***/
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedViewState: Bundle?
	): View {
		setViewTitle()
		return ComposeView {
		}
	}
}

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


	fun themeSelected(position: Int) {
		scope.launch {
			val result = hostState.showSnackbar(
				context.getString(R.string.fragment_settings_advanced_snackbar_ui_change),
				actionLabel = context.getString(R.string.apply),
				duration = SnackbarDuration.Indefinite,
			)

			if (result == SnackbarResult.ActionPerformed) {
				onBack()
				launchIO {
					viewModel.settingsRepo.setInt(AppTheme, position)
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
				hostState.showSnackbar(
					context.getString(R.string.settings_advanced_cycle_start_success)
				)
			}
		}
	}

	fun killCycleWorkers() {
		viewModel.killCycleWorkers()
	}

	AdvancedSettingsContent(
		viewModel,
		onThemeSelected = ::themeSelected,
		onPurgeNovelCache = viewModel::purgeUselessData,
		onKillCycleWorkers = ::killCycleWorkers,
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
	onThemeSelected: (Int) -> Unit,
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
					Text(stringResource(R.string.settings_advanced))
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
			contentPadding = PaddingValues(
				top = 16.dp,
				bottom = 64.dp
			),
			verticalArrangement = Arrangement.spacedBy(8.dp),
			modifier = Modifier.padding(paddingValues)
		) {
			item {
				val choice by viewModel.settingsRepo.getIntFlow(AppTheme)
					.collectAsState()

				DropdownSettingContent(
					title = stringResource(R.string.theme),
					description = stringResource(R.string.settings_advanced_theme_desc),
					choices = stringArrayResource(R.array.application_themes)
						.toList()
						.toImmutableList(),
					modifier = Modifier
						.fillMaxWidth(),
					selection = choice,
					onSelection = onThemeSelected
				)
			}

			item {
				ButtonSettingContent(
					title = stringResource(R.string.remove_novel_cache),
					description = stringResource(R.string.settings_advanced_purge_novel_cache),
					buttonText = stringResource(R.string.settings_advanced_purge_button),
					modifier = Modifier
						.fillMaxWidth(),
					onClick = onPurgeNovelCache
				)
			}

			item {
				SwitchSettingContent(
					title = stringResource(R.string.settings_advanced_verify_checksum_title),
					description = stringResource(R.string.settings_advanced_verify_checksum_desc),
					modifier = Modifier
						.fillMaxWidth(),
					repo = viewModel.settingsRepo,
					key = VerifyCheckSum
				)
			}

			item {
				SwitchSettingContent(
					title = stringResource(R.string.settings_advanced_require_double_back_title),
					description = stringResource(R.string.settings_advanced_require_double_back_desc),
					modifier = Modifier
						.fillMaxWidth(),
					repo = viewModel.settingsRepo,
					key = RequireDoubleBackToExit
				)
			}

			item {
				ButtonSettingContent(
					title = stringResource(R.string.settings_advanced_kill_cycle_workers_title),
					description = stringResource(R.string.settings_advanced_kill_cycle_workers_desc),
					buttonText = stringResource(R.string.settings_advanced_kill_cycle_workers_button),
					modifier = Modifier
						.fillMaxWidth(),
					onClick = onKillCycleWorkers
				)
			}

			item {
				ButtonSettingContent(
					title = stringResource(R.string.settings_advanced_force_repo_update_title),
					description = stringResource(R.string.settings_advanced_force_repo_update_desc),
					buttonText = stringResource(R.string.force),
					modifier = Modifier
						.fillMaxWidth(),
					onClick = onForceRepoSync
				)
			}

			item {
				ButtonSettingContent(
					title = stringResource(R.string.settings_advanced_clear_cookies_title),
					description = stringResource(R.string.settings_advanced_clear_cookies_desc),
					buttonText = stringResource(R.string.settings_advanced_clear_cookies_button),
					modifier = Modifier
						.fillMaxWidth(),
					onClick = onClearCookies
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
				title = stringResource(R.string.settings_advanced_site_protection_period),
				description = stringResource(R.string.settings_advanced_site_protection_period_desc),
				valueRange = remember { StableHolder(300..60000) },
				parseValue = {
					"$it ms"
				},
				repo = viewModel.settingsRepo,
				key = SiteProtectionPeriod,
				haveSteps = false,
			)
		}

		item {
			SliderSettingContent(
				title = stringResource(R.string.settings_advanced_site_protection_permits),
				description = stringResource(R.string.settings_advanced_site_protection_permits_desc),
				valueRange = remember { StableHolder(1..60) },
				parseValue = {
					"$it permits"
				},
				repo = viewModel.settingsRepo,
				key = SiteProtectionPermits,
				haveSteps = false,
			)
		}

			item {
				SwitchSettingContent(
					title = "Concurrent memory experiment",
					description =
					"""
					Enable if you experience random crashes during reading, this might help.
					Please tell developers you use this, as we are testing this.
					Requires restart.
				""".trimIndent(),
					modifier = Modifier
						.fillMaxWidth(),
					repo = viewModel.settingsRepo,
					key = ConcurrentMemoryExperiment
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
					IconButton(
						onClick = {
							runBlocking {
								viewModel.settingsRepo.setString(UserAgent, DEFAULT_USER_AGENT)
							}
						},
						enabled = !useShosetsuAgent
					) {
						Icon(Icons.Default.Refresh, stringResource(R.string.reset))
					}
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
