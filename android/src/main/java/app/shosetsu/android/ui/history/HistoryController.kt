package app.shosetsu.android.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import app.shosetsu.android.R
import app.shosetsu.android.common.ext.openChapter
import app.shosetsu.android.common.ext.viewModelDi
import app.shosetsu.android.view.compose.ErrorContent
import app.shosetsu.android.view.compose.ImageLoadingError
import app.shosetsu.android.view.compose.ShosetsuCompose
import app.shosetsu.android.view.compose.coverRatio
import app.shosetsu.android.view.controller.ShosetsuController
import app.shosetsu.android.view.uimodels.model.ChapterHistoryUI
import app.shosetsu.android.viewmodel.abstracted.HistoryViewModel
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.google.accompanist.placeholder.material.placeholder

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
class HistoryFragment : ShosetsuController() {
	override val viewTitleRes: Int = R.string.fragment_history

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?, savedViewState: Bundle?
	): View = ComposeView(requireContext()).apply {
		setViewTitle()
		setContent {
			HistoryView {
				activity?.openChapter(it.chapterId, it.novelId)
			}
		}
	}
}

@Composable
fun HistoryView(
	openChapter: (ChapterHistoryUI) -> Unit
) {
	val viewModel = viewModelDi<HistoryViewModel>()
	val items by viewModel.items.collectAsState(listOf())

	ShosetsuCompose {
		HistoryContent(items, openChapter)
	}
}

@Composable
fun HistoryContent(
	items: List<ChapterHistoryUI>, openChapter: (ChapterHistoryUI) -> Unit
) {
	if (items.isEmpty()) {
		ErrorContent(R.string.fragment_history_error_empty)
	} else {
		LazyColumn(
			contentPadding = PaddingValues(top = 8.dp, bottom = 112.dp),
			verticalArrangement = Arrangement.spacedBy(4.dp)
		) {
			items(items, key = { it.id }) {
				HistoryItemContent(it) {
					openChapter(it)
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
		)
	) {}
}


@Composable
fun HistoryItemContent(updateUI: ChapterHistoryUI, onClick: () -> Unit) {
	Row(
		Modifier
			.fillMaxWidth()
			.height(72.dp)
			.clickable(onClick = onClick)
			.padding(start = 8.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically
	) {
		if (updateUI.novelImageURL.isNotEmpty()) {
			SubcomposeAsyncImage(ImageRequest.Builder(LocalContext.current)
				.data(updateUI.novelImageURL).crossfade(true).build(),
				contentDescription = null,
				contentScale = ContentScale.Crop,
				modifier = Modifier
					.clip(MaterialTheme.shapes.medium)
					.aspectRatio(coverRatio),
				error = {
					ImageLoadingError()
				},
				loading = {
					Box(Modifier.placeholder(true))
				})
		} else {
			ImageLoadingError(
				Modifier.aspectRatio(coverRatio)
			)
		}
		Column(
			verticalArrangement = Arrangement.Center,
			modifier = Modifier
				.fillMaxWidth()
				.padding(4.dp),
		) {
			Text(
				updateUI.chapterTitle, maxLines = 1, overflow = TextOverflow.Ellipsis
			)
			Text(
				updateUI.novelTitle,
				fontSize = 14.sp,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
				modifier = Modifier.alpha(.75f)
			)
			Text(
				updateUI.endedTime ?: updateUI.startedTime,
				fontSize = 12.sp,
				maxLines = 1,
				modifier = Modifier.alpha(.5f)
			)
		}
	}
}