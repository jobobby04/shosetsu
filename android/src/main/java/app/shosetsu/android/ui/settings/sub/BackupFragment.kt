package app.shosetsu.android.ui.settings.sub

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.shosetsu.android.R
import app.shosetsu.android.common.SettingKey
import app.shosetsu.android.common.ext.logE
import app.shosetsu.android.common.ext.toast
import app.shosetsu.android.common.ext.viewModelDi
import app.shosetsu.android.view.compose.NavigateBackButton
import app.shosetsu.android.view.compose.setting.RestrictionSelectPreferenceWidget
import app.shosetsu.android.view.compose.setting.SliderSettingContent
import app.shosetsu.android.view.compose.setting.SwitchSettingContent
import app.shosetsu.android.view.compose.setting.widget.BasePreferenceWidget
import app.shosetsu.android.view.compose.setting.widget.HighlightPreference
import app.shosetsu.android.view.compose.setting.widget.PreferenceGroupHeader
import app.shosetsu.android.view.compose.setting.widget.PrefsHorizontalPadding
import app.shosetsu.android.view.compose.setting.widget.TextPreferenceWidget
import app.shosetsu.android.view.uimodels.StableHolder
import app.shosetsu.android.viewmodel.abstracted.settings.ABackupSettingsViewModel
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

@Composable
fun BackupView(
	highlightBackupFolder: Boolean,
	onBack: () -> Unit
) {
	val viewModel: ABackupSettingsViewModel = viewModelDi()
	val promptMigration by viewModel.promptMigration.collectAsState()

	val context = LocalContext.current
	val scope = rememberCoroutineScope()
	val hostState = remember { SnackbarHostState() }

	val selectBackupToRestoreLauncher = rememberLauncherForActivityResult(
		ActivityResultContracts.OpenDocument()
	) { uri ->
		if (uri == null) {
			viewModel.logE("Cancelled")
			return@rememberLauncherForActivityResult
		}

		// TODO Possibly add popup verification to make sure that an invalid file ext is oki

		viewModel.restore(uri)

		scope.launch {
			hostState.showSnackbar(context.getString(R.string.view_backup_restore_start))
		}
	}

	val selectBackupStorageLocationLauncher = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.OpenDocumentTree()
	) { uri ->
		if (uri != null) {
			val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
				Intent.FLAG_GRANT_WRITE_URI_PERMISSION
			try {
				context.contentResolver.takePersistableUriPermission(uri, flags)
			} catch (e: SecurityException) {
				viewModel.logE("File picker failed", e)
				context.toast(R.string.file_picker_uri_permission_unsupported)
				return@rememberLauncherForActivityResult
			}

			viewModel.setBackupStorageLocation(uri)
		}
	}

	BackupSettingsContent(
		viewModel,
		// Stops novel updates while backup is taking place
		// Starts backing up data
		backupNow = viewModel::startBackup,
		performFileSelection = {
			selectBackupToRestoreLauncher.launch(arrayOf("application/octet-stream"))
		},
		performBackupStorageLocationSelection = {
			try {
				selectBackupStorageLocationLauncher.launch(null)
			} catch (e: ActivityNotFoundException) {
				context.toast(R.string.file_picker_error)
			}
		},
		highlightBackupFolder = highlightBackupFolder,
		onBack = onBack
	)

	// If we need to show a dialog, show it.
	if (promptMigration) {
		BackupMigrationDialog(viewModel)
	}
}

/**
 * Migration dialog to get the user to migrate their backups.
 */
@Composable
fun BackupMigrationDialog(viewModel: ABackupSettingsViewModel) {
	AlertDialog(
		onDismissRequest = {},
		title = {
			Text(stringResource(R.string.settings_backup_dialog_migration_title))
		},
		text = {
			Text(stringResource(R.string.settings_backup_dialog_migration_desc))
		},
		dismissButton = {
			Button(onClick = viewModel::dismissMigration) {
				Text(stringResource(R.string.settings_backup_migration_dismiss))
			}
		},
		confirmButton = {
			Button(onClick = viewModel::startMigration) {
				Text(stringResource(R.string.settings_backup_migration_start))
			}
		},
	)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupSettingsContent(
	viewModel: ABackupSettingsViewModel,
	highlightBackupFolder: Boolean,
	backupNow: () -> Unit,
	performFileSelection: () -> Unit,
	performBackupStorageLocationSelection: () -> Unit,
	onBack: () -> Unit
) {
	val snackbarHostState = remember { SnackbarHostState() }

	Scaffold(
		snackbarHost = {
			SnackbarHost(snackbarHostState)
		},
		topBar = {
			TopAppBar(
				title = {
					Text(stringResource(R.string.backup))
				},
				navigationIcon = {
					NavigateBackButton(onBack)
				}
			)
		}
	) {
		LazyColumn(
			contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 64.dp),
			modifier = Modifier
				.fillMaxSize()
				.padding(it)
		) {
			item {
				val subtitle by viewModel.settingsRepo
					.getStringFlow(SettingKey.BackupStorageLocation)
					.collectAsState("")

				HighlightPreference(highlightBackupFolder) {
					TextPreferenceWidget(
						title = stringResource(R.string.settings_backup_location),
						subtitle = subtitle,
						iconDescription = null
					) {
						performBackupStorageLocationSelection()
					}
				}
			}

			item {
				BasePreferenceWidget(
					subcomponent = {
						MultiChoiceSegmentedButtonRow(
							modifier = Modifier
								.fillMaxWidth()
								.height(intrinsicSize = IntrinsicSize.Min)
								.padding(horizontal = PrefsHorizontalPadding),
						) {
							SegmentedButton(
								modifier = Modifier.fillMaxHeight(),
								checked = false,
								onCheckedChange = { backupNow() },
								shape = SegmentedButtonDefaults.itemShape(0, 2)
							) {
								Text(stringResource(R.string.backup_now))
							}

							SegmentedButton(
								modifier = Modifier.fillMaxHeight(),
								checked = false,
								onCheckedChange = { performFileSelection() },
								shape = SegmentedButtonDefaults.itemShape(1, 2),
							) {
								Text(stringResource(R.string.restore_now))
							}
						}
					}
				)
			}

			item {
				PreferenceGroupHeader(stringResource(R.string.fragment_backup_settings_label))
			}

			item {
				SliderSettingContent(
					title = stringResource(R.string.settings_backup_cycle_title),
					description = stringResource(R.string.settings_backup_cycle_desc),
					valueRange = remember { StableHolder(0..168) },
					parseValue = { value ->
						when (value) {
							12 -> stringResource(R.string.cycle_bidaily)
							24 -> stringResource(R.string.cycle_daily)
							48 -> stringResource(R.string.cycle_2_days)
							72 -> stringResource(R.string.cycle_3_days)
							96 -> stringResource(R.string.cycle_4_days)
							120 -> stringResource(R.string.cycle_5_days)
							144 -> stringResource(R.string.cycle_6_days)
							168 -> stringResource(R.string.cycle_weekly)
							else -> if (value == 0) {
								stringResource(R.string.cycle_disabled)
							} else {
								pluralStringResource(R.plurals.cycle_hourly, value, value)
							}
						}
					},
					repo = viewModel.settingsRepo,
					key = SettingKey.BackupCycle,
					haveSteps = false,
					manipulateUpdate = { value ->
						when (value) {
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
							else -> value
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
				RestrictionSelectPreferenceWidget(
					title = stringResource(R.string.backup_restrictions_title),
					subtitle = R.string.backup_restrictions_desc,
					restrictions = mapOf(
						R.string.backup_restore_low_storage to SettingKey.BackupOnLowStorage,
						R.string.backup_restore_low_battery to SettingKey.BackupOnLowBattery,
					) + if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
						mapOf(R.string.backup_restore_only_idle to SettingKey.BackupOnlyWhenIdle)
					else
						emptyMap(),
					repo = viewModel.settingsRepo,
				)
			}

			item {
				TextPreferenceWidget(
					title = stringResource(R.string.settings_backup_start_migration_title),
					subtitle = stringResource(R.string.settings_backup_start_migration_desc),
					onPreferenceClick = viewModel::startMigration,
					iconDescription = null
				)
			}
		}
	}
}
