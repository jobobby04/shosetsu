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
 *
 */

package app.shosetsu.android.ui.categories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.shosetsu.android.R
import app.shosetsu.android.common.ext.logE
import app.shosetsu.android.common.ext.viewModelDi
import app.shosetsu.android.ui.theme.ShosetsuTheme
import app.shosetsu.android.view.compose.ErrorContent
import app.shosetsu.android.view.compose.NavigateBackButton
import app.shosetsu.android.view.uimodels.model.CategoryUI
import app.shosetsu.android.viewmodel.abstracted.ACategoriesViewModel
import app.shosetsu.android.viewmodel.abstracted.ACategoriesViewModel.CategoryChangeState
import kotlinx.collections.immutable.ImmutableList

/**
 * Allow user to configure categories
 */
@Composable
fun CategoriesView(
	onBack: () -> Unit,
) {
	ShosetsuTheme {
		val viewModel: ACategoriesViewModel = viewModelDi()

		val items by viewModel.liveData.collectAsState()

		val addCategoryState by viewModel.addCategoryState.collectAsState()

		val hostState = remember { SnackbarHostState() }
		val context = LocalContext.current

		LaunchedEffect(addCategoryState) {
			when (addCategoryState) {
				CategoryChangeState.Finished ->
					hostState.showSnackbar(context.getString(R.string.toast_categories_added))

				is CategoryChangeState.Failure ->
					hostState.showSnackbar(context.getString(R.string.toast_categories_add_fail))

				CategoryChangeState.Unknown -> {}
			}
		}

		val removeCategoryState by viewModel.removeCategoryState.collectAsState()
		LaunchedEffect(removeCategoryState) {
			when (addCategoryState) {
				CategoryChangeState.Finished ->
					hostState.showSnackbar(context.getString(R.string.fragment_categories_snackbar_repo_removed))

				is CategoryChangeState.Failure -> {
					val state = removeCategoryState as CategoryChangeState.Failure
					logE("Failed to remove category ${state.category}", state.exception)
					val result =
						hostState.showSnackbar(
							context.getString(R.string.toast_categories_remove_fail),
							actionLabel = context.getString(R.string.retry)
						)

					if (result == SnackbarResult.ActionPerformed)
						viewModel.remove(state.category)
				}

				CategoryChangeState.Unknown -> {}
			}
		}

		val moveUpCategoryState by viewModel.moveUpCategoryState.collectAsState()
		LaunchedEffect(moveUpCategoryState) {
			when (addCategoryState) {
				CategoryChangeState.Finished -> {}
				is CategoryChangeState.Failure ->
					hostState.showSnackbar(context.getString(R.string.toast_categories_move_fail))

				CategoryChangeState.Unknown -> {}
			}
		}

		val moveDownCategoryState by viewModel.moveDownCategoryState.collectAsState()
		LaunchedEffect(moveDownCategoryState) {
			when (addCategoryState) {
				CategoryChangeState.Finished -> {}
				is CategoryChangeState.Failure ->
					hostState.showSnackbar(context.getString(R.string.toast_categories_move_fail))

				CategoryChangeState.Unknown -> {}
			}
		}

		var itemToRemove: CategoryUI? by remember { mutableStateOf(null) }

		CategoriesContent(
			items = items,
			onRemove = {
				itemToRemove = it
			},
			onMoveUp = {
				viewModel.moveUp(it)
			},
			onMoveDown = {
				viewModel.moveDown(it)
			},
			showAddDialog = viewModel::showAddDialog,
			onBack = onBack
		)

		if (itemToRemove != null) {
			AlertDialog(
				onDismissRequest = {
					itemToRemove = null
				},
				confirmButton = {
					TextButton(
						onClick = {
							viewModel.remove(itemToRemove!!)
							itemToRemove = null
						}
					) {
						Text(stringResource(android.R.string.ok))
					}
				},
				dismissButton = {
					TextButton(onClick = { itemToRemove = null }) {
						Text(stringResource(android.R.string.cancel))
					}
				},
				title = {
					Text(stringResource(R.string.alert_dialog_title_warn_categories_removal))
				},
				text = {
					Text(stringResource(R.string.alert_dialog_message_warn_categories_removal))
				}
			)
		}

		val isAddDialogVisible by viewModel.isAddDialogVisible.collectAsState()

		if (isAddDialogVisible) {
			CategoriesAddDialog(
				viewModel::hideAddDialog,
				viewModel::addCategory
			)
		}
	}
}

@Preview
@Composable
fun PreviewCategoriesAddDialog() {
	CategoriesAddDialog(
		hideAddDialog = {},
		addCategory = {}
	)
}

@Composable
fun CategoriesAddDialog(
	hideAddDialog: () -> Unit,
	addCategory: (String) -> Unit
) {
	var text by remember { mutableStateOf("") }
	AlertDialog(
		onDismissRequest = {
			hideAddDialog()
		},
		confirmButton = {
			TextButton(
				onClick = {
					addCategory(text)
					hideAddDialog()
				},
				enabled = text.isNotBlank()
			) {
				Text(stringResource(android.R.string.ok))
			}
		},
		dismissButton = {
			TextButton(onClick = { hideAddDialog() }) {
				Text(stringResource(android.R.string.cancel))
			}
		},
		title = {
			Text(stringResource(R.string.categories_add_title))
		},
		text = {
			OutlinedTextField(
				text,
				onValueChange = {
					if (!it.contains('\n'))
						text = it
				},
				label = {
					Text(stringResource(R.string.categories_add_name_hint))
				},
				isError = text.isBlank(),
				singleLine = true
			)
		}
	)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesContent(
	items: ImmutableList<CategoryUI>,
	onRemove: (CategoryUI) -> Unit,
	onMoveUp: (CategoryUI) -> Unit,
	onMoveDown: (CategoryUI) -> Unit,
	showAddDialog: () -> Unit,
	onBack: () -> Unit
) {
	val state = rememberLazyListState()

	Scaffold(
		topBar = {
			TopAppBar(
				title = {
					Text(stringResource(R.string.categories))
				},
				navigationIcon = {
					NavigateBackButton(onBack)
				}
			)
		},
		floatingActionButton = {
			ExtendedFloatingActionButton(
				text = {
					Text(stringResource(R.string.fragment_categories_action_add))
				},
				icon = {
					Icon(
						Icons.Default.AddCircle,
						stringResource(R.string.fragment_categories_action_add)
					)
				},
				onClick = showAddDialog
			)
		}
	) { padding ->
		if (items.isNotEmpty())
			LazyColumn(
				Modifier
					.padding(padding)
					.fillMaxSize(),
				state,
				contentPadding = PaddingValues(
					bottom = 64.dp,
					top = 16.dp,
					start = 8.dp,
					end = 8.dp
				),
				verticalArrangement = Arrangement.spacedBy(4.dp)
			) {
				val isNotSingluar by derivedStateOf { items.size > 1 }

				itemsIndexed(items) { index, item ->
					Card {
						Row(
							Modifier
								.padding(horizontal = 8.dp, vertical = 4.dp)
								.fillMaxWidth(),
							verticalAlignment = Alignment.CenterVertically,
							horizontalArrangement = Arrangement.SpaceBetween
						) {
							Text(item.name, style = MaterialTheme.typography.titleLarge)
							Row(
								verticalAlignment = Alignment.CenterVertically,
								horizontalArrangement = Arrangement.SpaceBetween
							) {
								if (isNotSingluar) {
									if (index != 0)
										IconButton(onClick = { onMoveDown(item) }) {
											Icon(
												painterResource(R.drawable.expand_less),
												contentDescription = null
											)
										}

									if (index != items.lastIndex)
										IconButton(onClick = { onMoveUp(item) }) {
											Icon(
												painterResource(R.drawable.expand_more),
												contentDescription = null
											)
										}
								}
								IconButton(onClick = { onRemove(item) }) {
									Icon(
										painterResource(R.drawable.trash),
										contentDescription = null
									)
								}
							}
						}
					}
				}
			}
		else {
			ErrorContent(
				R.string.categories_empty,
				modifier = Modifier.padding(padding)
			)
		}
	}
}