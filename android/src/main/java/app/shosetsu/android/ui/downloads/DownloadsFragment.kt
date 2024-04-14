package app.shosetsu.android.ui.downloads

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

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.enterAlwaysScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.shosetsu.android.R
import app.shosetsu.android.common.OfflineException
import app.shosetsu.android.common.enums.DownloadStatus.DOWNLOADING
import app.shosetsu.android.common.enums.DownloadStatus.ERROR
import app.shosetsu.android.common.enums.DownloadStatus.PAUSED
import app.shosetsu.android.common.enums.DownloadStatus.PENDING
import app.shosetsu.android.common.enums.DownloadStatus.WAITING
import app.shosetsu.android.common.ext.viewModelDi
import app.shosetsu.android.ui.library.DeselectAllButton
import app.shosetsu.android.ui.library.InverseSelectionButton
import app.shosetsu.android.ui.library.SelectAllButton
import app.shosetsu.android.ui.library.SelectBetweenButton
import app.shosetsu.android.ui.theme.ShosetsuTheme
import app.shosetsu.android.view.compose.ErrorContent
import app.shosetsu.android.view.compose.LazyColumnScrollbar
import app.shosetsu.android.view.compose.NavigateBackButton
import app.shosetsu.android.view.compose.SelectableBox
import app.shosetsu.android.view.uimodels.model.DownloadUI
import app.shosetsu.android.viewmodel.abstracted.ADownloadsViewModel
import app.shosetsu.android.viewmodel.abstracted.ADownloadsViewModel.SelectedDownloadsState
import kotlinx.collections.immutable.ImmutableList

/**
 * Shosetsu
 * 9 / June / 2019
 *
 * @author github.com/doomsdayrs
 */
/**
 * View that displays downloads the app is working on
 */
@Composable
fun DownloadsView(
	onBack: () -> Unit
) {
	ShosetsuTheme {
		val viewModel: ADownloadsViewModel = viewModelDi()

		val items by viewModel.liveData.collectAsState()
		val selectedDownloadState by viewModel.selectedDownloadState.collectAsState()
		val hasSelected by viewModel.hasSelectedFlow.collectAsState()
		val isPaused by viewModel.isDownloadPaused.collectAsState()
		val error by viewModel.error.collectAsState(null)

		val context = LocalContext.current
		val hostState = remember { SnackbarHostState() }

		LaunchedEffect(error) {
			if (error != null) {
				when (error) {
					is OfflineException -> {
						val result = hostState.showSnackbar(
							context.getString((error as OfflineException).messageRes),
							duration = SnackbarDuration.Long,
							actionLabel = context.getString(R.string.generic_wifi_settings)
						)
						if (result == SnackbarResult.ActionPerformed) {
							context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
						}
					}

					else -> {
						hostState.showSnackbar(
							error?.message ?: context.getString(R.string.error)
						)
					}
				}
			}
		}

		DownloadsContent(
			items = items,
			selectedDownloadState = selectedDownloadState,
			hasSelected = hasSelected,
			pauseSelection = viewModel::pauseSelection,
			startSelection = viewModel::startSelection,
			startFailedSelection = viewModel::restartSelection,
			deleteSelected = viewModel::deleteSelected,
			toggleSelection = viewModel::toggleSelection,
			onInverseSelection = viewModel::invertSelection,
			onSetAllPending = viewModel::setAllPending,
			onDeleteAll = viewModel::deleteAll,
			onDeselectAll = viewModel::deselectAll,
			onSelectAll = viewModel::selectAll,
			onSelectBetween = viewModel::selectBetween,
			isPaused = isPaused,
			togglePause = viewModel::togglePause,
			onBack = onBack
		)
	}
}

/**
 * Content of [DownloadsView]
 */
@Composable
fun DownloadsContent(
	items: ImmutableList<DownloadUI>,
	selectedDownloadState: SelectedDownloadsState,
	hasSelected: Boolean,
	pauseSelection: () -> Unit,
	startSelection: () -> Unit,
	startFailedSelection: () -> Unit,
	deleteSelected: () -> Unit,
	toggleSelection: (DownloadUI) -> Unit,
	onInverseSelection: () -> Unit,
	onSelectAll: () -> Unit,
	onDeselectAll: () -> Unit,
	onSelectBetween: () -> Unit,
	onDeleteAll: () -> Unit,
	onSetAllPending: () -> Unit,
	isPaused: Boolean,
	togglePause: () -> Unit,
	onBack: () -> Unit
) {
	Scaffold(
		topBar = {
			DownloadsAppBar(
				hasSelected,
				onInverseSelection,
				onSelectAll,
				onDeselectAll,
				onSelectBetween,
				onDeleteAll,
				onSetAllPending,
				onBack
			)
		},
		snackbarHost = {
		},
		floatingActionButton = {
			DownloadsFAB(
				isPaused,
				togglePause
			)
		}
	) { padding ->
		if (items.isNotEmpty()) {
			Box(
				modifier = Modifier
					.fillMaxSize()
					.padding(padding)
			) {
				val state = rememberLazyListState()

				LazyColumnScrollbar(
					listState = state,
					thumbColor = MaterialTheme.colorScheme.primary,
					thumbSelectedColor = Color.Gray,
				) {
					LazyColumn(
						modifier = Modifier.fillMaxSize(),
						contentPadding = PaddingValues(bottom = 140.dp),
						state = state
					) {
						items(items, key = { it.chapterID }) {
							DownloadContent(
								it,
								onClick = {
									if (hasSelected)
										toggleSelection(it)
								},
								onLongClick = {
									toggleSelection(it)
								}
							)
						}
					}
				}

				if (hasSelected) {
					Card(
						modifier = Modifier
							.align(BiasAlignment(0f, 0.7f))
					) {
						Row {
							IconButton(
								onClick = pauseSelection,
								enabled = selectedDownloadState.pauseVisible
							) {
								Icon(
									painterResource(R.drawable.pause),
									stringResource(R.string.pause)
								)
							}
							IconButton(
								onClick = startSelection,
								enabled = selectedDownloadState.startVisible
							) {
								Icon(
									painterResource(R.drawable.play_arrow),
									stringResource(R.string.start)
								)
							}
							IconButton(
								onClick = startFailedSelection,
								enabled = selectedDownloadState.restartVisible
							) {
								Icon(
									painterResource(R.drawable.refresh),
									stringResource(R.string.restart)
								)
							}
							IconButton(
								onClick = deleteSelected,
								enabled = selectedDownloadState.deleteVisible
							) {
								Icon(
									painterResource(R.drawable.trash),
									stringResource(R.string.delete)
								)
							}
						}
					}
				}
			}
		} else {
			ErrorContent(
				stringResource(R.string.empty_downloads_message),
				modifier = Modifier.padding(padding)
			)
		}
	}
}

/**
 * Preview [DownloadsFAB]
 */
@Preview
@Composable
fun PreviewDownloadsFAB() {
	Surface {
		DownloadsFAB(
			false,
			onToggle = {}
		)
	}
}

/**
 * Floating Action Button for [DownloadsContent]
 */
@Composable
fun DownloadsFAB(
	isPaused: Boolean,
	onToggle: () -> Unit
) {
	ExtendedFloatingActionButton(
		onClick = onToggle,
		text = {
			Text(
				stringResource(
					if (isPaused) {
						R.string.start
					} else {
						R.string.pause
					}
				)
			)
		},
		icon = {
			if (isPaused) {
				Icon(Icons.Default.PlayArrow, stringResource(R.string.start))
			} else {
				Icon(painterResource(R.drawable.pause), stringResource(R.string.pause))
			}
		}
	)
}

/**
 * Preview [DownloadsAppBar]
 */
@Preview
@Composable
fun PreviewDownloadsAppBar() {
	Surface {
		DownloadsAppBar(
			hasSelected = false,
			onInverseSelection = {},
			onSelectAll = {},
			onDeselectAll = {},
			onSelectBetween = {},
			onDeleteAll = {},
			onSetAllPending = {},
			onBack = {}
		)
	}
}

/**
 * Top bar of [DownloadsContent]
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsAppBar(
	hasSelected: Boolean,
	onInverseSelection: () -> Unit,
	onSelectAll: () -> Unit,
	onDeselectAll: () -> Unit,
	onSelectBetween: () -> Unit,
	onDeleteAll: () -> Unit,
	onSetAllPending: () -> Unit,
	onBack: () -> Unit
) {
	@Composable
	fun title() {
		Text(stringResource(R.string.downloads))
	}

	val behavior = enterAlwaysScrollBehavior()

	if (hasSelected) {
		LargeTopAppBar(
			title = { title() },
			scrollBehavior = behavior,
			actions = {
				InverseSelectionButton(onInverseSelection)
				SelectAllButton(onSelectAll)
				SelectBetweenButton(onSelectBetween)
				DeselectAllButton(onDeselectAll)
			},
			navigationIcon = {
				NavigateBackButton(onBack)
			}
		)
	} else {
		TopAppBar(
			title = { title() },
			scrollBehavior = behavior,
			actions = {
				DownloadsMoreOption(onDeleteAll, onSetAllPending)
			},
			navigationIcon = {
				NavigateBackButton(onBack)
			}
		)
	}
}

@Composable
fun DownloadsMoreOption(
	onDeleteAll: () -> Unit,
	onSetAllPending: () -> Unit
) {
	var showDropDown by remember { mutableStateOf(false) }

	Box {
		IconButton(
			onClick = {
				showDropDown = true
			}
		) {
			Icon(Icons.Default.MoreVert, stringResource(R.string.more))
		}

		DropdownMenu(
			showDropDown,
			onDismissRequest = {
				showDropDown = false
			}
		) {
			DropdownMenuItem(
				text = {
					Text(stringResource(R.string.fragment_downloads_set_all_pending_title))
				},
				onClick = onSetAllPending
			)

			DropdownMenuItem(
				text = {
					Text(stringResource(R.string.fragment_downloads_delete_all_title))
				},
				onClick = onDeleteAll
			)
		}
	}
}

@Preview
@Composable
fun PreviewDownloadContent() {
	ShosetsuTheme {
		DownloadContent(
			DownloadUI(
				0,
				0,
				"aaa",
				"Chpater",
				"Novel",
				0,
				DOWNLOADING,
				false
			),
			{},
			{}
		)
	}
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DownloadContent(
	item: DownloadUI,
	onClick: () -> Unit,
	onLongClick: () -> Unit,
) {
	SelectableBox(
		item.isSelected,
		modifier = Modifier
			.combinedClickable(
				onClick = onClick,
				onLongClick = onLongClick
			)
	) {
		Column(
			Modifier
				.padding(16.dp)
				.fillMaxWidth()
		) {
			Text(
				text = item.novelName,
				style = MaterialTheme.typography.bodyLarge
			)
			Text(
				text = item.chapterName,
				style = MaterialTheme.typography.bodyMedium
			)

			Row(
				Modifier
					.padding(top = 8.dp)
					.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically
			) {
				val status = item.status
				if (status == DOWNLOADING || status == WAITING) {
					LinearProgressIndicator(modifier = Modifier.fillMaxWidth(.7f))
				} else {
					LinearProgressIndicator(0.0f, modifier = Modifier.fillMaxWidth(.7f))
				}

				Text(
					text = stringResource(
						id = when (status) {
							PENDING -> {
								R.string.pending
							}

							DOWNLOADING -> {
								R.string.downloading
							}

							PAUSED -> {
								R.string.paused
							}

							ERROR -> {
								R.string.error
							}

							WAITING -> {
								R.string.waiting
							}

							else -> {
								R.string.completed
							}
						}
					),
					textAlign = TextAlign.End,
					style = MaterialTheme.typography.bodySmall,
					modifier = Modifier
						.padding(start = 8.dp)
						.fillMaxWidth()
				)
			}
		}
	}
}