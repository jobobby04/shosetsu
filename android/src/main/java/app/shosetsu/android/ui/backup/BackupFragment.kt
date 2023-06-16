package app.shosetsu.android.ui.backup

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import app.shosetsu.android.R
import app.shosetsu.android.common.SettingKey
import app.shosetsu.android.common.consts.BACKUP_FILE_EXTENSION
import app.shosetsu.android.common.ext.ComposeView
import app.shosetsu.android.common.ext.logE
import app.shosetsu.android.common.ext.toast
import app.shosetsu.android.common.ext.viewModelDi
import app.shosetsu.android.view.compose.ShosetsuCompose
import app.shosetsu.android.view.compose.setting.ButtonSettingContent
import app.shosetsu.android.view.compose.setting.SliderSettingContent
import app.shosetsu.android.view.compose.setting.SwitchSettingContent
import app.shosetsu.android.view.controller.ShosetsuFragment
import app.shosetsu.android.view.uimodels.StableHolder
import app.shosetsu.android.viewmodel.abstracted.settings.ABackupSettingsViewModel

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
class BackupFragment : ShosetsuFragment() {

	override val viewTitleRes: Int = R.string.fragment_backup_title

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedViewState: Bundle?
	): View {
		setViewTitle()

		return ComposeView {
			BackupView(
				toast = {
					requireContext().toast(it)
				}
			)
		}
	}
}

@Composable
fun BackupView(
	toast: (String) -> Unit,
	viewModel: ABackupSettingsViewModel = viewModelDi(),
) {
	val selectBackupToRestoreLauncher = rememberLauncherForActivityResult(
		ActivityResultContracts.OpenDocument()
	) { uri ->
		if (uri == null) {
			viewModel.logE("Cancelled")
			return@rememberLauncherForActivityResult
		}

		// TODO Possibly add popup verification to make sure that an invalid file ext is oki

		toast("Restoring now...")
		viewModel.restore(uri)
	}

	val selectLocationToExportLauncher = rememberLauncherForActivityResult(
		CreateDocument("application/octet-stream")
	) { uri: Uri? ->
		if (uri == null) {
			viewModel.logE("Cancelled")
			viewModel.clearExport()
			return@rememberLauncherForActivityResult
		}

		toast("Exporting now")
		viewModel.exportBackup(uri)
	}

	var hasNoBackupSelectedForExport by remember { mutableStateOf(false) }

	fun performExportSelection() {
		val backupFileName = viewModel.getBackupToExport()

		if (backupFileName == null) {
			hasNoBackupSelectedForExport = true
			return
		}

		selectLocationToExportLauncher.launch(backupFileName)
	}

	ShosetsuCompose {
		BackupSettingsContent(
			viewModel,
			// Stops novel updates while backup is taking place
			// Starts backing up data
			backupNow = viewModel::startBackup,
			restore = viewModel::restore,
			export = {
				viewModel.holdBackupToExport(it)
				performExportSelection()
			},
			performFileSelection = {
				selectBackupToRestoreLauncher.launch(arrayOf("application/octet-stream"))
			},
			hasNoBackupSelectedForExport = hasNoBackupSelectedForExport
		)
	}
}

@Composable
fun BackupSelectionDialog(
	viewModel: ABackupSettingsViewModel,
	dismiss: () -> Unit,
	optionSelected: (String) -> Unit,
) {
	val options by viewModel.loadInternalOptions().collectAsState(emptyList())
	Dialog(
		onDismissRequest = {
			dismiss()
		},
	) {
		Card {
			Column(
				modifier = Modifier
					.padding(8.dp),
			) {
				Text(
					stringResource(R.string.settings_backup_alert_select_backup_title),
					style = MaterialTheme.typography.titleLarge,
					modifier = Modifier.padding(
						bottom = 16.dp,
						top = 8.dp,
						start = 24.dp,
						end = 24.dp
					)
				)

				LazyColumn(
					modifier = Modifier
						.padding(bottom = 8.dp, start = 24.dp, end = 24.dp)
						.height(200.dp)
						.fillMaxWidth()
				) {
					items(options) { option ->
						TextButton(onClick = {
							optionSelected(option)
							dismiss()
						}) {
							Text(
								remember(option) {
									option.removePrefix("shosetsu-backup-")
										.removeSuffix(".$BACKUP_FILE_EXTENSION")
								}
							)
						}
					}
				}
			}
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupSettingsContent(
	viewModel: ABackupSettingsViewModel,
	backupNow: () -> Unit,
	performFileSelection: () -> Unit,
	restore: (String) -> Unit,
	export: (String) -> Unit,
	hasNoBackupSelectedForExport: Boolean,
) {
	val snackbarHostState = remember { SnackbarHostState() }
	val message = stringResource(R.string.fragment_backup_error_unselected)

	LaunchedEffect(hasNoBackupSelectedForExport) {
		if (hasNoBackupSelectedForExport)
			snackbarHostState.showSnackbar(message)
	}

	Scaffold(
		snackbarHost = {
			SnackbarHost(snackbarHostState)
		}
	) {
		LazyColumn(
			contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 64.dp),
			verticalArrangement = Arrangement.spacedBy(8.dp),
			modifier = Modifier
				.fillMaxSize()
				.padding(it)
		) {

			item {
				ButtonSettingContent(
					stringResource(R.string.backup_now),
					"",
					stringResource(R.string.backup_now),
					onClick = backupNow, modifier = Modifier
						.fillMaxWidth()
				)
			}

			item {
				var isDialogShowing: Boolean by remember { mutableStateOf(false) }
				var isRestoreDialogShowing: Boolean by remember { mutableStateOf(false) }

				ButtonSettingContent(
					stringResource(R.string.restore_now),
					"",
					stringResource(R.string.restore_now),
					modifier = Modifier
						.fillMaxWidth()
				) {
					isDialogShowing = true
				}
				if (isRestoreDialogShowing)
					BackupSelectionDialog(viewModel, { isRestoreDialogShowing = false }, restore)

				if (isDialogShowing)
					AlertDialog(
						onDismissRequest = {
							isDialogShowing = false
						},
						confirmButton = {
							TextButton(onClick = {
								// Open file selector
								performFileSelection()
								isDialogShowing = false
							}) {
								Text(stringResource(R.string.settings_backup_alert_location_external))
							}
							TextButton(onClick = {
								isDialogShowing = false
								isRestoreDialogShowing = true
							}) {
								Text(stringResource(R.string.settings_backup_alert_location_internal))
							}
						},
						title = {
							Text(
								stringResource(R.string.settings_backup_alert_select_location_title),
								style = MaterialTheme.typography.titleLarge,
								modifier = Modifier.padding(
									bottom = 16.dp,
									top = 8.dp,
									start = 24.dp,
									end = 24.dp
								)
							)
						},
						modifier = Modifier.padding(8.dp)
					)
			}

			item {
				var isExportShowing: Boolean by remember { mutableStateOf(false) }

				if (isExportShowing) {
					BackupSelectionDialog(viewModel, { isExportShowing = false }, export)
				}

				ButtonSettingContent(
					stringResource(R.string.settings_backup_export),
					"",
					stringResource(R.string.settings_backup_export),
					onClick = {
						isExportShowing = true
					},
					modifier = Modifier
						.fillMaxWidth()
				)
			}

			item {
				Row(
					verticalAlignment = Alignment.Bottom,
					modifier = Modifier.padding(top = 8.dp)
				) {
					Text(
						stringResource(R.string.fragment_backup_settings_label),
						modifier = Modifier.padding(end = 8.dp),
						style = MaterialTheme.typography.titleLarge
					)
					Divider()
				}
			}

			item {
				SliderSettingContent(
					title = stringResource(R.string.settings_backup_cycle_title),
					description = stringResource(R.string.settings_backup_cycle_desc),
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
					key = SettingKey.BackupCycle,
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
					stringResource(R.string.backup_chapters_option),
					stringResource(R.string.backup_chapters_option_description),
					viewModel.settingsRepo,
					SettingKey.ShouldBackupChapters,
					modifier = Modifier
						.fillMaxWidth()
				)
			}

			item {
				SwitchSettingContent(
					stringResource(R.string.backup_settings_option),
					stringResource(R.string.backup_settings_option_desc),
					viewModel.settingsRepo,
					SettingKey.ShouldBackupSettings,
					modifier = Modifier
						.fillMaxWidth()
				)
			}

			item {
				SwitchSettingContent(
					stringResource(R.string.backup_only_modified_title),
					stringResource(R.string.backup_only_modified_desc),
					viewModel.settingsRepo,
					SettingKey.BackupOnlyModifiedChapters,
					modifier = Modifier
						.fillMaxWidth()
				)
			}

			item {
				SwitchSettingContent(
					stringResource(R.string.backup_restore_low_storage),
					stringResource(R.string.backup_restore_low_storage_desc),
					viewModel.settingsRepo,
					SettingKey.BackupOnLowStorage,
					modifier = Modifier
						.fillMaxWidth()
				)
			}

			item {
				SwitchSettingContent(
					stringResource(R.string.backup_restore_low_battery),
					stringResource(R.string.backup_restore_low_battery_desc),
					viewModel.settingsRepo,
					SettingKey.BackupOnLowBattery,
					modifier = Modifier
						.fillMaxWidth()
				)
			}

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
				item {
					SwitchSettingContent(
						stringResource(R.string.backup_restore_only_idle),
						stringResource(R.string.backup_restore_only_idle_desc),
						viewModel.settingsRepo,
						SettingKey.BackupOnlyWhenIdle,
						modifier = Modifier
							.fillMaxWidth()
					)
				}

		}
	}
}
