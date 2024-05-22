package app.shosetsu.android.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import app.shosetsu.android.R
import app.shosetsu.android.common.ext.viewModelDi
import app.shosetsu.android.view.compose.ErrorContent
import app.shosetsu.android.view.compose.ImageLoadingError
import app.shosetsu.android.view.compose.NavigateBackButton
import app.shosetsu.android.view.compose.coverRatio
import app.shosetsu.android.view.compose.placeholder
import app.shosetsu.android.view.uimodels.model.ChapterHistoryUI
import app.shosetsu.android.viewmodel.abstracted.HistoryViewModel
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest

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
 * Shosetsu
 *
 * @since 09 / 10 / 2021
 * @author Doomsdayrs
 */

@Composable
fun HistoryView(
	openNovel: (novelId: Int) -> Unit,
	openChapter: (novelId: Int, chapterId: Int) -> Unit,
	onBack: () -> Unit
) {
	val viewModel: HistoryViewModel = viewModelDi()
	val items = viewModel.items.collectAsLazyPagingItems()
	val isClearBeforeDialogVisible by viewModel.isClearBeforeDialogShown.collectAsState()

	HistoryContent(
		items,
		openNovel = {
			openNovel(it.novelId)
		},
		openChapter = {
			openChapter(it.novelId, it.chapterId)
		},
		onBack = onBack,
		onClearAll = viewModel::clearAll,
		onOpenClearBefore = viewModel::showClearBeforeDialog
	)

	if (isClearBeforeDialogVisible) {
		HistoryDatePickerDialog(
			onDismiss = viewModel::hideClearBeforeDialog,
			onClearBefore = viewModel::clearBefore
		)
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryDatePickerDialog(
	onDismiss: () -> Unit,
	onClearBefore: (Long) -> Unit
) {
	val state = rememberDatePickerState()

	DatePickerDialog(
		onDismissRequest = onDismiss,
		confirmButton = {
			TextButton(
				onClick = {
					if (state.selectedDateMillis != null) {
						onClearBefore(state.selectedDateMillis!!)
						onDismiss()
					}
				}
			) {
				Text(stringResource(android.R.string.ok))
			}
		},
		dismissButton = {
			TextButton(onClick = onDismiss) {
				Text(stringResource(android.R.string.cancel))
			}
		}
	) {
		DatePicker(
			state,
			title = {
				Text(
					stringResource(R.string.fragment_history_picker_date),
					modifier = Modifier.padding(
						// Taken from DatePickerTitle
						PaddingValues(
							start = 24.dp,
							end = 12.dp,
							top = 16.dp
						)
					)
				)
			},
		)
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryContent(
	items: LazyPagingItems<ChapterHistoryUI>,
	openNovel: (ChapterHistoryUI) -> Unit,
	openChapter: (ChapterHistoryUI) -> Unit,
	onBack: () -> Unit,
	onClearAll: () -> Unit,
	onOpenClearBefore: () -> Unit
) {
	Scaffold(
		topBar = {
			TopAppBar(
				title = {
					Text(stringResource(R.string.fragment_history))
				},
				navigationIcon = {
					NavigateBackButton(onBack)
				},
				actions = {
					HistoryMoreOption(
						onClearAll,
						onOpenClearBefore,
					)
				},
				colors = TopAppBarDefaults.topAppBarColors(
					containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
					titleContentColor = MaterialTheme.colorScheme.onSurface,
				)
			)
		}
	) { paddingValues ->
		if (items.itemCount == 0) {
			ErrorContent(
				R.string.fragment_history_error_empty,
				modifier = Modifier.padding(paddingValues)
			)
		} else {
			LazyColumn(
				contentPadding = PaddingValues(top = 8.dp, bottom = 112.dp),
				verticalArrangement = Arrangement.spacedBy(4.dp),
				modifier = Modifier.padding(paddingValues)
			) {
				items(items.itemCount) {
					val historyItem = items[it]
					if (historyItem != null) {
						HistoryItemContent(
							updateUI = historyItem,
							openNovel = {
								openNovel(historyItem)
							},
							onClick = {
								openChapter(historyItem)
							}
						)
					}
				}
			}
		}
	}
}


@Composable
fun HistoryMoreOption(
	onClearAll: () -> Unit,
	onOpenClearBefore: () -> Unit
) {
	var showDropDown by remember { mutableStateOf(false) }

	Box {
		IconButton(
			onClick = {
				showDropDown = true
			}
		) {
			Icon(Icons.Default.Delete, stringResource(R.string.clear))
		}

		DropdownMenu(
			showDropDown,
			onDismissRequest = {
				showDropDown = false
			},
		) {
			DropdownMenuItem(
				text = {
					Text(stringResource(R.string.all))
				},
				onClick = onClearAll
			)

			DropdownMenuItem(
				text = {
					Text(stringResource(R.string.before))
				},
				onClick = onOpenClearBefore
			)

		}
	}
}

@ExperimentalMaterial3Api
@Preview
@Composable
fun PreviewHistoryItemContent() {
	HistoryItemContent(
		ChapterHistoryUI(
			1, 1, "", "", 1, "", System.currentTimeMillis(), null
		),
		{},
		{}
	)
}


@Composable
fun HistoryItemContent(
	updateUI: ChapterHistoryUI?,
	openNovel: () -> Unit,
	onClick: () -> Unit
) {
	Row(
		Modifier
			.fillMaxWidth()
			.height(72.dp)
			.clickable(onClick = onClick)
			.padding(start = 8.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically
	) {
		if (updateUI?.novelImageURL?.isNotEmpty() == true) {
			SubcomposeAsyncImage(
				ImageRequest.Builder(LocalContext.current)
					.data(updateUI.novelImageURL).crossfade(true).build(),
				contentDescription = null,
				contentScale = ContentScale.Crop,
				modifier = Modifier
					.clip(MaterialTheme.shapes.medium)
					.aspectRatio(coverRatio)
					.clickable(onClick = openNovel),
				error = {
					ImageLoadingError()
				},
				loading = {
					Box(Modifier.placeholder(true))
				})
		} else {
			ImageLoadingError(
				Modifier
					.aspectRatio(coverRatio)
					.placeholder(updateUI == null)
			)
		}
		Column(
			verticalArrangement = Arrangement.Center,
			modifier = Modifier
				.fillMaxWidth()
				.padding(4.dp),
		) {
			Text(
				updateUI?.chapterTitle ?: "", maxLines = 1, overflow = TextOverflow.Ellipsis,
				modifier = Modifier.placeholder(updateUI == null)
			)
			Text(
				updateUI?.novelTitle ?: "",
				fontSize = 14.sp,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
				modifier = Modifier
					.alpha(.75f)
					.placeholder(updateUI == null)
			)
			Text(
				updateUI?.endedTime ?: updateUI?.startedTime ?: "",
				fontSize = 12.sp,
				maxLines = 1,
				modifier = Modifier
					.alpha(.5f)
					.placeholder(updateUI == null)
			)
		}
	}
}