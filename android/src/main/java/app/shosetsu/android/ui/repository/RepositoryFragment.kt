package app.shosetsu.android.ui.repository

import android.os.Bundle
import android.view.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.view.MenuProvider
import app.shosetsu.android.R
import app.shosetsu.android.common.consts.REPOSITORY_HELP_URL
import app.shosetsu.android.common.ext.*
import app.shosetsu.android.view.compose.ErrorAction
import app.shosetsu.android.view.compose.ErrorContent
import app.shosetsu.android.view.compose.ShosetsuCompose
import app.shosetsu.android.view.compose.rememberFakePullRefreshState
import app.shosetsu.android.view.controller.ShosetsuFragment
import app.shosetsu.android.view.controller.base.ExtendedFABController
import app.shosetsu.android.view.controller.base.ExtendedFABController.EFabMaintainer
import app.shosetsu.android.view.controller.base.syncFABWithCompose
import app.shosetsu.android.view.uimodels.model.RepositoryUI
import app.shosetsu.android.viewmodel.abstracted.ARepositoryViewModel
import app.shosetsu.android.viewmodel.abstracted.ARepositoryViewModel.AddRepoState
import app.shosetsu.android.viewmodel.abstracted.ARepositoryViewModel.RemoveRepoState
import app.shosetsu.android.viewmodel.abstracted.ARepositoryViewModel.ToggleRepoIsEnabledState
import app.shosetsu.android.viewmodel.abstracted.ARepositoryViewModel.UndoRepoRemoveState
import com.google.android.material.snackbar.BaseTransientBottomBar.BaseCallback
import com.google.android.material.snackbar.Snackbar
import kotlinx.collections.immutable.ImmutableList
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
 */
class RepositoryFragment : ShosetsuFragment(),
	ExtendedFABController, MenuProvider {
	override val viewTitleRes: Int = R.string.repositories

	private val viewModel: ARepositoryViewModel by viewModel()

	private lateinit var fab: EFabMaintainer

	/***/
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedViewState: Bundle?
	): View {
		activity?.addMenuProvider(this, viewLifecycleOwner)
		setViewTitle()
		return ComposeView {
			RepositoriesView(
				viewModel,
				::displayOfflineSnackBar,
				makeSnackBar = {
					makeSnackBar(it)
				},
				makeSnackBarT = { a, b ->
					makeSnackBar(a, b)
				},
				fab
			)
		}
	}

	override fun manipulateFAB(fab: EFabMaintainer) {
		this.fab = fab.apply {
			setIconResource(R.drawable.add_circle_outline)
			setText(R.string.fragment_repositories_action_add)
			// When the FAB is clicked, open a alert dialog to input a new repository
			setOnClickListener { viewModel.showAddDialog() }
		}
	}

	/***/
	override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
		menuInflater.inflate(R.menu.repositories, menu)
	}

	/***/
	override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
		when (menuItem.itemId) {
			R.id.help -> {
				activity?.openInWebView(REPOSITORY_HELP_URL)
				true
			}

			else -> false
		}
}

/**
 * View of repositories
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepositoriesView(
	viewModel: ARepositoryViewModel = viewModelDi(),
	displayOfflineSnackBar: (Int) -> Unit,
	makeSnackBar: (Int) -> Snackbar?,
	makeSnackBarT: (Int, Int) -> Snackbar?,
	fab: EFabMaintainer
) {
	ShosetsuCompose {

		fun onRefresh() {
			if (viewModel.isOnline()) {
				viewModel.updateRepositories()
			} else displayOfflineSnackBar(R.string.fragment_repositories_snackbar_offline_no_update)
		}

		/**
		 * Warn the user that they need to refresh their extension list
		 */
		fun showWarning() {
			makeSnackBarT(
				R.string.fragment_repositories_snackbar_repo_changed,
				Snackbar.LENGTH_LONG
			)
				// Ask the user if they want to refresh
				?.setAction(R.string.fragment_repositories_action_repo_update) {
					onRefresh()
				}?.show()
		}

		val items by viewModel.liveData.collectAsState()
		var itemToRemove: RepositoryUI? by remember { mutableStateOf(null) }
		val isAddDialogVisible by viewModel.isAddDialogVisible.collectAsState()

		val removeState by viewModel.removeState.collectAsState()
		val addRepoState by viewModel.addState.collectAsState()
		val undoRemoveState by viewModel.undoRemoveState.collectAsState()
		val toggleIsEnabledState by viewModel.toggleIsEnabledState.collectAsState()

		LaunchedEffect(removeState) {
			when (removeState) {
				is RemoveRepoState.Failure -> {
					val (exception, repo) = removeState as RemoveRepoState.Failure

					logE(
						"Failed to remove repository $repo",
						exception
					)
					makeSnackBar(R.string.toast_repository_remove_fail)
						?.setAction(R.string.generic_question_retry) {
							viewModel.remove(repo)
						}?.show()
				}

				is RemoveRepoState.Success -> {
					val (repo) = removeState as RemoveRepoState.Success

					// Inform user of the repository being removed
					makeSnackBar(
						R.string.fragment_repositories_snackbar_repo_removed,
					)
						// Ask the user if they want to undo
						?.setAction(R.string.generic_undo) {
							viewModel.undoRemove(repo)
						}
						// If they don't, ask to refresh
						?.setOnDismissedNotByAction { _, _ ->
							showWarning()
						}?.show()
				}

				RemoveRepoState.Unknown -> {}
			}
		}

		LaunchedEffect(addRepoState) {
			when (addRepoState) {
				is AddRepoState.Failure -> {
					val (_, name, url) = addRepoState as AddRepoState.Failure
					// Inform the user the repository couldn't be added
					makeSnackBar(R.string.toast_repository_add_fail)
						// Ask the user if they want to retry
						?.setAction(R.string.generic_question_retry) {
							viewModel.addRepository(name, url)
						}?.show()
				}

				AddRepoState.Success -> {
					// Inform the user that the repository was added
					makeSnackBar(R.string.toast_repository_added)
						// Ask if the user wants to refresh the UI
						?.setOnDismissed { _, _ ->
							showWarning()
						}?.show()
				}

				AddRepoState.Unknown -> {
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
					makeSnackBar(R.string.fragment_repositories_snackbar_fail_undo_repo_removal)
						// Ask if the user wants to retry
						?.setAction(R.string.generic_question_retry) {
							viewModel.undoRemove(repo)
						}
						// If the user doesn't want to retry, ask to refresh
						?.setOnDismissedNotByAction { _, _ ->
							showWarning()
						}
						?.show()
				}

				UndoRepoRemoveState.Success -> {
					// Success, ask to refresh
					showWarning()
				}

				UndoRepoRemoveState.Unknown -> {
				}
			}
		}

		LaunchedEffect(toggleIsEnabledState) {
			when (toggleIsEnabledState) {
				is ToggleRepoIsEnabledState.Failure -> {
					val (repo, _) = toggleIsEnabledState as ToggleRepoIsEnabledState.Failure

					// Inform the user of an error
					makeSnackBar(R.string.toast_error_repository_toggle_enabled_failed)
						// Ask the user if they want to retry
						?.setAction(R.string.generic_question_retry) {
							viewModel.toggleIsEnabled(repo)
						}?.show()
				}

				is ToggleRepoIsEnabledState.Success -> {
					val (_, newState) = toggleIsEnabledState as ToggleRepoIsEnabledState.Success

					// Inform the user of the new state
					makeSnackBar(
						if (newState)
							R.string.toast_success_repository_toggled_enabled
						else
							R.string.toast_success_repository_toggled_disabled
					)
						// After, ask the user if they want to refresh
						?.setOnDismissed { _, event ->
							if (event != BaseCallback.DISMISS_EVENT_CONSECUTIVE)
								showWarning()
						}?.show()
				}

				ToggleRepoIsEnabledState.Unknown -> {
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
			onRefresh = ::onRefresh,
			fab
		)

		if (isAddDialogVisible) {
			RepositoriesAddDialog(viewModel)
		}

		if (itemToRemove != null) {
			RepositoriesRemoveDialog(
				repo = itemToRemove ?: return@ShosetsuCompose,
				remove = viewModel::remove,
				dismiss = {
					itemToRemove = null
				}
			)
		}
	}
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepositoriesAddDialog(
	viewModel: ARepositoryViewModel
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
			viewModel.hideAddDialog()
		},
		confirmButton = {
			TextButton(
				onClick = {
					viewModel.addRepository(name, url)
					viewModel.hideAddDialog()
				},
				enabled = !isError
			) {
				Text(stringResource(android.R.string.ok))
			}
		},
		dismissButton = {
			TextButton(
				onClick = {
					viewModel.hideAddDialog()
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
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RepositoriesContent(
	items: ImmutableList<RepositoryUI>,
	toggleEnabled: (RepositoryUI) -> Unit,
	onRemove: (RepositoryUI) -> Unit,
	addRepository: () -> Unit,
	onRefresh: () -> Unit,
	fab: EFabMaintainer
) {
	if (items.isNotEmpty()) {
		val (isRefreshing, pullRefreshState) = rememberFakePullRefreshState(onRefresh)
		Box(Modifier.pullRefresh(pullRefreshState)) {
			val state = rememberLazyListState()
			syncFABWithCompose(state, fab)
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
						}
					)
				}
			}

			PullRefreshIndicator(isRefreshing, pullRefreshState, Modifier.align(Alignment.TopCenter))
		}
	} else {
		ErrorContent(
			stringResource(R.string.empty_repositories_message),
			ErrorAction(R.string.empty_repositories_action) { addRepository() }
		)
	}
}

/**
 * Content of a repository item
 */
@Composable
fun RepositoryContent(
	item: RepositoryUI,
	onCheckedChange: () -> Unit,
	onRemove: () -> Unit
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
				IconButton(onClick = onRemove) {
					Icon(
						painter = painterResource(R.drawable.close),
						contentDescription =
						stringResource(R.string.fragment_repositories_action_remove)
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
