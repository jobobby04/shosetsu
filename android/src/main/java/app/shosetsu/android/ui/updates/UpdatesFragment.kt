package app.shosetsu.android.ui.updates

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.DropdownMenu
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.enterAlwaysScrollBehavior
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import app.shosetsu.android.R
import app.shosetsu.android.common.OfflineException
import app.shosetsu.android.common.ext.ComposeView
import app.shosetsu.android.common.ext.trimDate
import app.shosetsu.android.common.ext.viewModelDi
import app.shosetsu.android.view.compose.ErrorAction
import app.shosetsu.android.view.compose.ErrorContent
import app.shosetsu.android.view.compose.ImageLoadingError
import app.shosetsu.android.view.compose.ShosetsuCompose
import app.shosetsu.android.view.compose.coverRatio
import app.shosetsu.android.view.compose.rememberFakePullRefreshState
import app.shosetsu.android.view.controller.ShosetsuFragment
import app.shosetsu.android.view.controller.base.HomeFragment
import app.shosetsu.android.view.uimodels.StableHolder
import app.shosetsu.android.view.uimodels.model.UpdatesUI
import app.shosetsu.android.viewmodel.abstracted.AUpdatesViewModel
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.google.accompanist.placeholder.material.placeholder
import kotlinx.collections.immutable.ImmutableMap
import org.joda.time.DateTime

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
@Deprecated("Composed")
class UpdatesFragment : ShosetsuFragment(), HomeFragment {
	override val viewTitleRes: Int = R.string.updates

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
fun UpdatesView(
	openNovel: (Int) -> Unit,
	openChapter: (novelId: Int, chapterId: Int) -> Unit,
) {
	ShosetsuCompose {
		val viewModel = viewModelDi<AUpdatesViewModel>()
		val items by viewModel.liveData.collectAsState()
		val error by viewModel.error.collectAsState(null)
		val isClearBeforeVisible by viewModel.isClearBeforeVisible.collectAsState()

		val context = LocalContext.current
		val hostState = remember { SnackbarHostState() }

		LaunchedEffect(error) {
			when (error) {
				is OfflineException -> {
					val result = hostState.showSnackbar(
						context.getString(R.string.generic_error_cannot_update_library_offline),
						duration = SnackbarDuration.Long,
						actionLabel = context.getString(R.string.generic_wifi_settings)
					)
					if (result == SnackbarResult.ActionPerformed) {
						context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
					}
				}
			}
		}

		UpdatesContent(
			items = items,
			onRefresh = {
				viewModel.startUpdateManager(-1)
			},
			openNovel = {
				openNovel(it.novelID)
			},
			openChapter = {
				openChapter(it.novelID, it.chapterID)
			},
			onClearAll = viewModel::clearAll,
			onClearBefore = viewModel::showClearBefore,
			hostState = hostState
		)

		if (isClearBeforeVisible) {
			ClearBeforeDialog(
				onHideClearBefore = viewModel::hideClearBefore,
				onClearBefore = viewModel::clearBefore
			)
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClearBeforeDialog(
	onHideClearBefore: () -> Unit,
	onClearBefore: (Long) -> Unit
) {
	val datePickerState = rememberDatePickerState()

	DatePickerDialog(
		onDismissRequest = onHideClearBefore,
		confirmButton = {
			TextButton(
				onClick = {
					if (datePickerState.selectedDateMillis != null)
						onClearBefore(datePickerState.selectedDateMillis!!)
					onHideClearBefore()
				},
				enabled = datePickerState.selectedDateMillis != null
			) {
				Text(stringResource(android.R.string.ok))
			}
		},
		dismissButton = {
			TextButton(
				onClick = onHideClearBefore
			) {
				Text(stringResource(android.R.string.cancel))
			}
		},
	) {
		DatePicker(
			datePickerState,
			title = {
				Text(stringResource(R.string.fragment_updates_clear))
			}
		)
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdatesAppBar(
	onClearAll: () -> Unit,
	onClearBefore: () -> Unit,
	isEmpty: Boolean
) {
	TopAppBar(
		title = {
			Text(stringResource(R.string.updates))
		},
		actions = {
			AnimatedVisibility(!isEmpty) {
				Box {
					var showDropwDown by remember { mutableStateOf(false) }
					IconButton(
						onClick = {
							showDropwDown = !showDropwDown
						}
					) {
						Icon(Icons.Default.MoreVert, stringResource(R.string.clear))
					}

					DropdownMenu(
						showDropwDown,
						onDismissRequest = {
							showDropwDown = false
						}
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
							onClick = onClearBefore
						)
					}
				}
			}
		},
		scrollBehavior = enterAlwaysScrollBehavior()
	)
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun UpdatesContent(
	items: ImmutableMap<DateTime, List<UpdatesUI>>,
	onRefresh: () -> Unit,
	openNovel: (UpdatesUI) -> Unit,
	openChapter: (UpdatesUI) -> Unit,
	onClearAll: () -> Unit,
	onClearBefore: () -> Unit,
	hostState: SnackbarHostState
) {
	val (isRefreshing, pullRefreshState) = rememberFakePullRefreshState(onRefresh)
	Scaffold(
		topBar = {
			UpdatesAppBar(onClearAll, onClearBefore, items.isEmpty())
		},
		snackbarHost = {
			SnackbarHost(hostState)
		}
	) { padding ->
		Box(
			Modifier
				.pullRefresh(pullRefreshState)
				.padding(padding)
		) {
			if (items.isEmpty()) {
				ErrorContent(
					R.string.empty_updates_message,
					ErrorAction(R.string.empty_updates_refresh_action) {
						onRefresh()
					}
				)
			} else {
				LazyColumn(
					contentPadding = PaddingValues(bottom = 112.dp),
					verticalArrangement = Arrangement.spacedBy(4.dp)
				) {
					items.forEach { (header, updateItems) ->
						stickyHeader {
							UpdateHeaderItemContent(remember(header) { StableHolder(header) })
						}

						items(updateItems, key = { it.chapterID }) {
							UpdateItemContent(
								it,
								onCoverClick = { openNovel(it) },
								onClick = { openChapter(it) }
							)
						}
					}
				}
			}

			PullRefreshIndicator(
				isRefreshing,
				pullRefreshState,
				Modifier.align(Alignment.TopCenter)
			)
		}
	}
}

@Preview
@Composable
fun PreviewUpdateHeaderItemContent() {
	UpdateHeaderItemContent(StableHolder(DateTime().trimDate()))
}

@ExperimentalMaterial3Api
@Preview
@Composable
fun PreviewUpdateItemContent() {
	UpdateItemContent(
		UpdatesUI(
			1,
			1,
			System.currentTimeMillis(),
			"This is a chapter",
			"This is a novel",
			""
		),
		{},
		{}
	)
}


@Composable
fun UpdateItemContent(
	updateUI: UpdatesUI,
	onCoverClick: () -> Unit,
	onClick: () -> Unit
) {
	Row(
		Modifier
			.fillMaxWidth()
			.height(72.dp)
			.clickable(onClick = onClick)
			.padding(start = 8.dp, end = 8.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		if (updateUI.novelImageURL.isNotEmpty()) {
			SubcomposeAsyncImage(
				ImageRequest.Builder(LocalContext.current)
					.data(updateUI.novelImageURL)
					.crossfade(true)
					.build(),
				contentDescription = null,
				contentScale = ContentScale.Crop,
				modifier = Modifier
					.aspectRatio(coverRatio)
					.clip(MaterialTheme.shapes.small)
					.clickable(onClick = onCoverClick),
				error = {
					ImageLoadingError()
				},
				loading = {
					Box(Modifier.placeholder(true))
				}
			)
		} else {
			ImageLoadingError(
				Modifier
					.aspectRatio(coverRatio)
					.clip(MaterialTheme.shapes.small)
					.clickable(onClick = onCoverClick)
			)
		}
		Column(
			verticalArrangement = Arrangement.Center,
			modifier = Modifier
				.fillMaxWidth()
				.padding(4.dp),
		) {
			Text(
				updateUI.chapterName,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)
			Text(
				updateUI.novelName,
				fontSize = 14.sp,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
				modifier = Modifier.alpha(.75f)
			)
			Text(
				updateUI.displayTime,
				fontSize = 12.sp,
				maxLines = 1,
				modifier = Modifier.alpha(.5f)
			)
		}
	}
}

@Composable
fun UpdateHeaderItemContent(dateTime: StableHolder<DateTime>) {
	Surface(
		modifier = Modifier.fillMaxWidth(),
		shadowElevation = 2.dp,
		tonalElevation = 2.dp
	) {
		val context = LocalContext.current
		val text = remember(dateTime, context) {
			when (dateTime.item) {
				DateTime(System.currentTimeMillis()).trimDate() ->
					context.getString(R.string.today)

				DateTime(System.currentTimeMillis()).trimDate().minusDays(1) ->
					context.getString(R.string.yesterday)

				else -> "${dateTime.item.dayOfMonth}/${dateTime.item.monthOfYear}/${dateTime.item.year}"
			}
		}
		Text(
			text,
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 16.dp, vertical = 8.dp),
			fontSize = 14.sp
		)
	}
}
