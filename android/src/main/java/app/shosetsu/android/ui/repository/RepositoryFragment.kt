package app.shosetsu.android.ui.repository

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.shosetsu.android.R
import app.shosetsu.android.common.OfflineException
import app.shosetsu.android.common.consts.REPOSITORY_HELP_URL
import app.shosetsu.android.common.ext.logE
import app.shosetsu.android.common.ext.viewModelDi
import app.shosetsu.android.view.QRCodeShareDialog
import app.shosetsu.android.view.compose.ErrorAction
import app.shosetsu.android.view.compose.ErrorContent
import app.shosetsu.android.view.compose.HelpButton
import app.shosetsu.android.view.compose.NavigateBackButton
import app.shosetsu.android.view.compose.rememberFakePullRefreshState
import app.shosetsu.android.view.uimodels.model.RepositoryUI
import app.shosetsu.android.viewmodel.abstracted.ARepositoryViewModel
import app.shosetsu.android.viewmodel.abstracted.ARepositoryViewModel.AddRepoState
import app.shosetsu.android.viewmodel.abstracted.ARepositoryViewModel.RemoveRepoState
import app.shosetsu.android.viewmodel.abstracted.ARepositoryViewModel.ToggleRepoIsEnabledState
import app.shosetsu.android.viewmodel.abstracted.ARepositoryViewModel.UndoRepoRemoveState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.launch
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.acra.ACRA

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
 * shosetsu
 * 16 / 09 / 2020

 * View of repositories
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepositoriesView(
	onBack: () -> Unit
) {
	val viewModel: ARepositoryViewModel = viewModelDi()

	val error by viewModel.error.collectAsState(null)

	val context = LocalContext.current
	val hostState = remember { SnackbarHostState() }
	val scope = rememberCoroutineScope()

	LaunchedEffect(error) {
		if (error != null) {
			when (error) {
				is OfflineException -> {
					scope.launch {
						val result = hostState.showSnackbar(
							context.getString((error as OfflineException).messageRes),
							duration = SnackbarDuration.Long,
							actionLabel = context.getString(R.string.generic_wifi_settings)
						)
						if (result == SnackbarResult.ActionPerformed) {
							context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
						}
					}
				}

				else -> {
					scope.launch {
						hostState.showSnackbar(
							error?.message ?: context.getString(R.string.error)
						)
					}
				}
			}
		}
	}

	/**
	 * Warn the user that they need to refresh their extension list
	 */
	/**
	 * Warn the user that they need to refresh their extension list
	 */
	fun showWarning() {
		scope.launch {
			// Ask the user if they want to refresh
			val result = hostState.showSnackbar(
				context.getString(R.string.fragment_repositories_snackbar_repo_changed),
				duration = SnackbarDuration.Long,
				actionLabel = context.getString(R.string.fragment_repositories_action_repo_update)
			)
			if (result == SnackbarResult.ActionPerformed)
				viewModel.updateRepositories()
		}
	}

	val items by viewModel.liveData.collectAsState()
	var itemToRemove: RepositoryUI? by remember { mutableStateOf(null) }
	val isAddDialogVisible by viewModel.isAddDialogVisible.collectAsState()

	val removeState by viewModel.removeState.collectAsState(null)
	val addRepoState by viewModel.addState.collectAsState(null)
	val undoRemoveState by viewModel.undoRemoveState.collectAsState(null)
	val toggleIsEnabledState by viewModel.toggleIsEnabledState.collectAsState(null)
	val currentShare by viewModel.currentShare.collectAsState()

	LaunchedEffect(removeState) {
		when (removeState) {
			is RemoveRepoState.Failure -> {
				val (exception, repo) = removeState as RemoveRepoState.Failure

				logE(
					"Failed to remove repository $repo",
					exception
				)

				scope.launch {
					val result = hostState.showSnackbar(
						context.getString(R.string.toast_repository_remove_fail),
						actionLabel = context.getString(R.string.generic_question_retry)
					)

					if (result == SnackbarResult.ActionPerformed) {
						viewModel.remove(repo)
					}
				}
			}

			is RemoveRepoState.Success -> {
				val (repo) = removeState as RemoveRepoState.Success

				// Inform user of the repository being removed
				scope.launch {
					val result = hostState.showSnackbar(
						context.getString(
							R.string.fragment_repositories_snackbar_repo_removed,
						),
						// Ask the user if they want to undo
						actionLabel = context.getString(R.string.generic_undo)
					)
					when (result) {
						SnackbarResult.ActionPerformed -> viewModel.undoRemove(repo)
						// If they don't, ask to refresh
						SnackbarResult.Dismissed -> showWarning()
					}
				}
			}

			null -> {}
		}
	}

	LaunchedEffect(addRepoState) {
		when (addRepoState) {
			is AddRepoState.Failure -> {
				val (_, name, url) = addRepoState as AddRepoState.Failure
				// Inform the user the repository couldn't be added
				scope.launch {
					// Ask the user if they want to retry
					val result =
						hostState.showSnackbar(
							context.getString(R.string.toast_repository_add_fail),
							actionLabel = context.getString(R.string.generic_question_retry)
						)

					if (result == SnackbarResult.ActionPerformed) viewModel.addRepository(name, url)
				}
			}

			AddRepoState.Success -> {
				// Inform the user that the repository was added
				scope.launch {
					// Ask if the user wants to refresh the UI
					val result = hostState.showSnackbar(
						context.getString(R.string.toast_repository_added)
					)

					if (result == SnackbarResult.Dismissed) showWarning()
				}
			}

			null -> {
			}
		}
	}

	LaunchedEffect(undoRemoveState) {
		when (undoRemoveState) {
			is UndoRepoRemoveState.Failure -> {
				val (repo, exception) =
					undoRemoveState as UndoRepoRemoveState.Failure

				exception.printStackTrace()
				ACRA.errorReporter.handleSilentException(exception)

				// Warn the user that there was an error
				scope.launch {
					val result =
						hostState.showSnackbar(
							context.getString(R.string.fragment_repositories_snackbar_fail_undo_repo_removal),
							// Ask if the user wants to retry
							actionLabel = context.getString(R.string.generic_question_retry)
						)

					when (result) {
						SnackbarResult.ActionPerformed -> viewModel.undoRemove(repo)
						// If the user doesn't want to retry, ask to refresh
						SnackbarResult.Dismissed -> showWarning()
					}
				}
			}

			UndoRepoRemoveState.Success -> {
				// Success, ask to refresh
				showWarning()
			}

			null -> {
			}
		}
	}

	LaunchedEffect(toggleIsEnabledState) {
		when (toggleIsEnabledState) {
			is ToggleRepoIsEnabledState.Failure -> {
				val (repo, _) = toggleIsEnabledState as ToggleRepoIsEnabledState.Failure

				// Inform the user of an error
				scope.launch {
					// Ask the user if they want to retry
					val result =
						hostState.showSnackbar(
							context.getString(R.string.toast_error_repository_toggle_enabled_failed)
						)
					if (result == SnackbarResult.ActionPerformed) viewModel.toggleIsEnabled(repo)
				}
			}

			is ToggleRepoIsEnabledState.Success -> {
				val (_, newState) = toggleIsEnabledState as ToggleRepoIsEnabledState.Success

				// Inform the user of the new state
				scope.launch {
					val result = hostState.showSnackbar(
						context.getString(
							if (newState)
								R.string.toast_success_repository_toggled_enabled
							else
								R.string.toast_success_repository_toggled_disabled
						)
					)
					// After, ask the user if they want to refresh
					if (result == SnackbarResult.Dismissed) showWarning()
				}
			}

			null -> {
			}
		}
	}


	RepositoriesContent(
		items = items,
		toggleEnabled = {
			viewModel.toggleIsEnabled(it)
		},
		onRemove = {
			itemToRemove = it
		},
		addRepository = {
			viewModel.showAddDialog()
		},
		onRefresh = viewModel::updateRepositories,
		onShowShare = viewModel::showShare,
		onBack = onBack,
		hostState = hostState
	)

	if (isAddDialogVisible) {
		RepositoriesAddDialog(viewModel::addRepository, viewModel::hideAddDialog)
	}

	if (itemToRemove != null) {
		RepositoriesRemoveDialog(
			repo = itemToRemove ?: return,
			remove = viewModel::remove,
			dismiss = {
				itemToRemove = null
			}
		)
	}

	if (currentShare != null) {
		val map by viewModel.qrCode.collectAsState(null)

		QRCodeShareDialog(
			map,
			hide = viewModel::hideShare,
			currentShare!!.name
		)
	}
}

@Composable
private fun createPreviewUI(id: Int = 1, enabled: Boolean = true) =
	remember {
		RepositoryUI(
			id,
			"shosetsu.app/$id",
			"Example $id",
			enabled
		)
	}

@Preview
@Composable
fun PreviewRepositoriesRemoveDialog() {
	RepositoriesRemoveDialog(
		createPreviewUI(1),
		remove = { },
		dismiss = {}
	)
}

@Composable
fun RepositoriesRemoveDialog(
	repo: RepositoryUI,
	remove: (RepositoryUI) -> Unit,
	dismiss: () -> Unit
) {
	AlertDialog(
		title = {
			Text(stringResource(R.string.alert_dialog_title_warn_repo_removal))
		},
		text = {
			Text(stringResource(R.string.alert_dialog_message_warn_repo_removal))
		},
		onDismissRequest = dismiss,
		confirmButton = {
			TextButton(
				onClick = {
					remove(repo)
					dismiss()
				}
			) {
				Text(stringResource(android.R.string.ok))
			}
		},
		dismissButton = {
			TextButton(
				onClick = dismiss
			) {
				Text(stringResource(android.R.string.cancel))
			}
		}
	)
}

@Preview
@Composable
fun PreviewRepositoriesAddDialog() {
	RepositoriesAddDialog(
		addRepository = { _, _ -> },
		hideAddDialog = {}
	)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepositoriesAddDialog(
	addRepository: (name: String, url: String) -> Unit,
	hideAddDialog: () -> Unit,
) {
	var name by remember { mutableStateOf("") }
	var url by remember { mutableStateOf("") }
	val isError by remember { derivedStateOf { url.toHttpUrlOrNull() == null } }

	AlertDialog(
		title = {
			Text(stringResource(R.string.repository_add_title))
		},
		text = {
			Column(
				horizontalAlignment = Alignment.CenterHorizontally,
				verticalArrangement = Arrangement.spacedBy(4.dp)
			) {
				OutlinedTextField(
					name,
					onValueChange = {
						name = it
					},
					placeholder = {
						Text(stringResource(R.string.repository_add_name_hint))
					}
				)
				OutlinedTextField(
					url,
					onValueChange = {
						url = it
					},
					isError = isError,
					placeholder = {
						Text(stringResource(R.string.repository_add_url_hint))
					},
					keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Uri)
				)
			}
		},
		onDismissRequest = {
			hideAddDialog()
		},
		confirmButton = {
			TextButton(
				onClick = {
					addRepository(name, url)
					hideAddDialog()
				},
				enabled = !isError
			) {
				Text(stringResource(android.R.string.ok))
			}
		},
		dismissButton = {
			TextButton(
				onClick = {
					hideAddDialog()
				},
			) {
				Text(stringResource(android.R.string.cancel))
			}
		}
	)
}

/**
 * Repositories content
 */
@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RepositoriesContent(
	items: ImmutableList<RepositoryUI>,
	toggleEnabled: (RepositoryUI) -> Unit,
	onRemove: (RepositoryUI) -> Unit,
	addRepository: () -> Unit,
	onRefresh: () -> Unit,
	onShowShare: (RepositoryUI) -> Unit,
	onBack: () -> Unit,
	hostState: SnackbarHostState
) {
	Scaffold(
		topBar = {
			TopAppBar(
				title = {
					Text(stringResource(R.string.repositories))
				},
				navigationIcon = {
					NavigateBackButton(onBack)
				},
				actions = {
					HelpButton(REPOSITORY_HELP_URL)
				}
			)
		},
		snackbarHost = {
			SnackbarHost(hostState)
		},
		floatingActionButton = {
			ExtendedFloatingActionButton(
				text = {
					Text(stringResource(R.string.fragment_repositories_action_add))
				},
				icon = {
					Icon(
						Icons.Default.Add,
						stringResource(R.string.fragment_repositories_action_add)
					)
				},
				onClick = addRepository
			)
		}
	) { paddingValues ->
		if (items.isNotEmpty()) {
			val (isRefreshing, pullRefreshState) = rememberFakePullRefreshState(onRefresh)
			Box(
				Modifier
					.pullRefresh(pullRefreshState)
					.padding(paddingValues)
			) {
				val state = rememberLazyListState()
				LazyColumn(
					contentPadding = PaddingValues(
						start = 8.dp,
						top = 8.dp,
						end = 8.dp,
						bottom = 64.dp
					),
					state = state
				) {
					items(items, key = { it.id }) { item ->
						RepositoryContent(
							item,
							onCheckedChange = {
								toggleEnabled(item)
							},
							onRemove = {
								onRemove(item)
							},
							onShowShare = {
								onShowShare(item)
							}
						)
					}
				}

				PullRefreshIndicator(
					isRefreshing,
					pullRefreshState,
					Modifier.align(Alignment.TopCenter)
				)
			}
		} else {
			ErrorContent(
				stringResource(R.string.empty_repositories_message),
				ErrorAction(R.string.empty_repositories_action) { addRepository() },
				modifier = Modifier.padding(paddingValues)
			)
		}
	}
}

@Preview
@Composable
fun PreviewRepositoryContent() {
	val enabled by remember { mutableStateOf(true) }
	RepositoryContent(
		createPreviewUI(enabled = enabled),
		onCheckedChange = { enabled != enabled },
		onRemove = {},
		onShowShare = {}
	)
}

/**
 * Content of a repository item
 */
@Composable
fun RepositoryContent(
	item: RepositoryUI,
	onCheckedChange: () -> Unit,
	onRemove: () -> Unit,
	onShowShare: () -> Unit
) {
	Card(Modifier.padding(bottom = 8.dp)) {
		Row(
			Modifier
				.padding(8.dp)
				.fillMaxWidth(),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.SpaceBetween
		) {
			Column(
				Modifier.fillMaxWidth(.7f)
			) {
				Text(text = item.name)

				Row(
					modifier = Modifier.padding(start = 16.dp)
				) {
					Row {
						Text(
							text = stringResource(id = R.string.id_label),
							style = MaterialTheme.typography.bodySmall
						)
						Text(text = "${item.id}", style = MaterialTheme.typography.bodySmall)
					}
					SelectionContainer {
						Text(
							text = item.url,
							style = MaterialTheme.typography.bodySmall,
							modifier = Modifier.padding(start = 8.dp)
						)
					}
				}
			}

			Row {
				var visible by remember { mutableStateOf(false) }

				DropdownMenu(
					visible,
					onDismissRequest = {
						visible = false
					}
				) {
					DropdownMenuItem(
						text = {
							Text(stringResource(R.string.remove))
						},
						onClick = {
							visible = false
							onRemove()
						}
					)

					DropdownMenuItem(
						text = {
							Text(stringResource(R.string.share))
						},
						onClick = {
							visible = false
							onShowShare()
						}
					)
				}

				IconButton(
					onClick = {
						visible = true
					}
				) {
					Icon(
						Icons.Default.MoreVert,
						contentDescription = stringResource(R.string.more)
					)
				}

				Switch(
					checked = item.isRepoEnabled,
					onCheckedChange = {
						onCheckedChange()
					}
				)
			}
		}
	}
}
