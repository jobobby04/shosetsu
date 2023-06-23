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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import app.shosetsu.android.R
import app.shosetsu.android.common.ext.ComposeView
import app.shosetsu.android.common.ext.logE
import app.shosetsu.android.common.ext.makeSnackBar
import app.shosetsu.android.common.ext.viewModel
import app.shosetsu.android.view.compose.ErrorContent
import app.shosetsu.android.view.compose.ShosetsuCompose
import app.shosetsu.android.view.controller.ShosetsuFragment
import app.shosetsu.android.view.controller.base.ExtendedFABController
import app.shosetsu.android.view.controller.base.syncFABWithCompose
import app.shosetsu.android.view.uimodels.model.CategoryUI
import app.shosetsu.android.viewmodel.abstracted.ACategoriesViewModel
import app.shosetsu.android.viewmodel.abstracted.ACategoriesViewModel.CategoryChangeState
import com.google.android.material.snackbar.Snackbar
import kotlinx.collections.immutable.ImmutableList

class CategoriesFragment : ShosetsuFragment(), ExtendedFABController {

	private lateinit var fab: ExtendedFABController.EFabMaintainer

	private val viewModel: ACategoriesViewModel by viewModel()

	override val viewTitleRes: Int = R.string.categories

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedViewState: Bundle?
	): View {
		setViewTitle()
		return ComposeView {
			CategoriesView(
				viewModel,
				fab,
				makeSnackBar = { makeSnackBar(it) }
			)
		}
	}

	override fun manipulateFAB(fab: ExtendedFABController.EFabMaintainer) {
		this.fab = fab
		fab.setIconResource(R.drawable.add_circle_outline)
		fab.setText(R.string.fragment_categories_action_add)

		// When the FAB is clicked, open a alert dialog to input a new category
		fab.setOnClickListener {
			viewModel.showAddDialog()
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesView(
	viewModel: ACategoriesViewModel,
	fab: ExtendedFABController.EFabMaintainer,
	makeSnackBar: (
		stringRes: Int,
	) -> Snackbar?
) {
	ShosetsuCompose {
		val items by viewModel.liveData.collectAsState()

		val addCategoryState by viewModel.addCategoryState.collectAsState()
		LaunchedEffect(addCategoryState) {
			when (addCategoryState) {
				CategoryChangeState.Finished ->
					makeSnackBar(R.string.toast_categories_added)?.show()

				is CategoryChangeState.Failure ->
					makeSnackBar(R.string.toast_categories_add_fail)?.show()

				CategoryChangeState.Unknown -> {}
			}
		}

		val removeCategoryState by viewModel.removeCategoryState.collectAsState()
		LaunchedEffect(removeCategoryState) {
			when (addCategoryState) {
				CategoryChangeState.Finished ->
					makeSnackBar(R.string.fragment_categories_snackbar_repo_removed)?.show()

				is CategoryChangeState.Failure -> {
					val state = removeCategoryState as CategoryChangeState.Failure
					logE("Failed to remove category ${state.category}", state.exception)
					makeSnackBar(R.string.toast_categories_remove_fail)
						?.setAction(R.string.generic_question_retry) {
							viewModel.remove(state.category)
						}?.show()
				}

				CategoryChangeState.Unknown -> {}
			}
		}

		val moveUpCategoryState by viewModel.moveUpCategoryState.collectAsState()
		LaunchedEffect(moveUpCategoryState) {
			when (addCategoryState) {
				CategoryChangeState.Finished -> {}
				is CategoryChangeState.Failure ->
					makeSnackBar(R.string.toast_categories_move_fail)?.show()

				CategoryChangeState.Unknown -> {}
			}
		}

		val moveDownCategoryState by viewModel.moveDownCategoryState.collectAsState()
		LaunchedEffect(moveDownCategoryState) {
			when (addCategoryState) {
				CategoryChangeState.Finished -> {}
				is CategoryChangeState.Failure ->
					makeSnackBar(R.string.toast_categories_move_fail)?.show()

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
			fab = fab
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
			var text by remember { mutableStateOf("") }
			AlertDialog(
				onDismissRequest = {
					viewModel.hideAddDialog()
				},
				confirmButton = {
					TextButton(
						onClick = {
							viewModel.addCategory(text)
							viewModel.hideAddDialog()
						},
						enabled = text.isNotBlank()
					) {
						Text(stringResource(android.R.string.ok))
					}
				},
				dismissButton = {
					TextButton(onClick = { viewModel.hideAddDialog() }) {
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
	}
}

@Composable
fun CategoriesContent(
	items: ImmutableList<CategoryUI>,
	onRemove: (CategoryUI) -> Unit,
	onMoveUp: (CategoryUI) -> Unit,
	onMoveDown: (CategoryUI) -> Unit,
	fab: ExtendedFABController.EFabMaintainer
) {
	val state = rememberLazyListState()
	syncFABWithCompose(state, fab)

	if (items.isNotEmpty())
		LazyColumn(
			Modifier.fillMaxSize(),
			state,
			contentPadding = PaddingValues(bottom = 64.dp, top = 16.dp, start = 8.dp, end = 8.dp),
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
								Icon(painterResource(R.drawable.trash), contentDescription = null)
							}
						}
					}
				}
			}
		}
	else {
		ErrorContent(
			R.string.categories_empty
		)
	}
}