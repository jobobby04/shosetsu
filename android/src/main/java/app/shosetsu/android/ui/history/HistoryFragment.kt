package app.shosetsu.android.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.navigation.findNavController
import androidx.navigation.navOptions
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import app.shosetsu.android.R
import app.shosetsu.android.common.consts.BundleKeys
import app.shosetsu.android.common.ext.navigateSafely
import app.shosetsu.android.common.ext.openChapter
import app.shosetsu.android.common.ext.setShosetsuTransition
import app.shosetsu.android.common.ext.viewModel
import app.shosetsu.android.common.ext.viewModelDi
import app.shosetsu.android.view.compose.ErrorContent
import app.shosetsu.android.view.compose.ImageLoadingError
import app.shosetsu.android.view.compose.ShosetsuCompose
import app.shosetsu.android.view.compose.coverRatio
import app.shosetsu.android.view.controller.ShosetsuFragment
import app.shosetsu.android.view.uimodels.model.ChapterHistoryUI
import app.shosetsu.android.viewmodel.abstracted.HistoryViewModel
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.google.accompanist.placeholder.material.placeholder
import com.google.android.material.datepicker.MaterialDatePicker

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
class HistoryFragment : ShosetsuFragment(), MenuProvider {
	override val viewTitleRes: Int = R.string.fragment_history

	private val viewModel: HistoryViewModel by viewModel()

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?, savedViewState: Bundle?
	): View {
		activity?.addMenuProvider(this, viewLifecycleOwner)
		return ComposeView(requireContext()).apply {
			setViewTitle()
			setContent {
				HistoryView(
					viewModel = viewModel,
					openNovel = { history ->
						findNavController().navigateSafely(
							R.id.action_historyFragment_to_novelController, bundleOf(
								BundleKeys.BUNDLE_NOVEL_ID to history.novelId
							),
							navOptions = navOptions {
								launchSingleTop = true
								setShosetsuTransition()
							}
						)
					},
					openChapter = {
						activity?.openChapter(it.chapterId, it.novelId)
					}
				)
			}
		}
	}

	override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
		menuInflater.inflate(R.menu.toolbar_history, menu)
	}

	override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
		when (menuItem.itemId) {
			R.id.fragment_history_clear_all -> {
				viewModel.clearAll()
				true
			}
			R.id.fragment_history_clear_before -> {
				onUserClearBefore()
				true
			}
			else -> false
		}

	private fun onUserClearBefore() {
		MaterialDatePicker.Builder.datePicker()
			.setTitleText(R.string.fragment_history_picker_date)
			.build()
			.apply {
				addOnPositiveButtonClickListener {
					viewModel.clearBefore(it)
				}
			}.show(parentFragmentManager, tag)
	}
}

@Composable
fun HistoryView(
	viewModel: HistoryViewModel = viewModelDi(),
	openNovel: (ChapterHistoryUI) -> Unit,
	openChapter: (ChapterHistoryUI) -> Unit
) {
	val items = viewModel.items.collectAsLazyPagingItems()

	ShosetsuCompose {
		HistoryContent(items, openNovel, openChapter)
	}
}

@Composable
fun HistoryContent(
	items: LazyPagingItems<ChapterHistoryUI>,
	openNovel: (ChapterHistoryUI) -> Unit,
	openChapter: (ChapterHistoryUI) -> Unit
) {
	if (items.itemCount == 0) {
		ErrorContent(R.string.fragment_history_error_empty)
	} else {
		LazyColumn(
			contentPadding = PaddingValues(top = 8.dp, bottom = 112.dp),
			verticalArrangement = Arrangement.spacedBy(4.dp)
		) {
			items(items, key = { it.id }) {
				if (it != null) {
					HistoryItemContent(
						updateUI = it,
						openNovel = {
							openNovel(it)
						},
						onClick = {
							openChapter(it)
						}
					)
				}
			}
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
fun HistoryItemContent(updateUI: ChapterHistoryUI?, openNovel: () -> Unit, onClick: () -> Unit) {
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