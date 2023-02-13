package app.shosetsu.android.ui.updates

import android.os.Bundle
import android.view.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.core.view.MenuProvider
import app.shosetsu.android.R
import app.shosetsu.android.common.ext.displayOfflineSnackBar
import app.shosetsu.android.common.ext.openChapter
import app.shosetsu.android.common.ext.trimDate
import app.shosetsu.android.common.ext.viewModel
import app.shosetsu.android.view.compose.*
import app.shosetsu.android.view.controller.ShosetsuController
import app.shosetsu.android.view.controller.base.HomeFragment
import app.shosetsu.android.view.uimodels.StableHolder
import app.shosetsu.android.view.uimodels.model.UpdatesUI
import app.shosetsu.android.viewmodel.abstracted.AUpdatesViewModel
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.android.material.datepicker.MaterialDatePicker
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
class ComposeUpdatesController : ShosetsuController(), HomeFragment, MenuProvider {
	override val viewTitleRes: Int = R.string.updates

	private val viewModel: AUpdatesViewModel by viewModel()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedViewState: Bundle?
	): View {
		activity?.addMenuProvider(this, viewLifecycleOwner)
		return ComposeView(requireContext()).apply {
			setViewTitle()
			setContent {
				ShosetsuCompose {
					val items by viewModel.liveData.collectAsState()
					val isRefreshing by viewModel.isRefreshing.collectAsState()

					UpdatesContent(
						items = items,
						isRefreshing = isRefreshing,
						onRefresh = this@ComposeUpdatesController::onRefresh
					) { (chapterID, novelID) ->
						activity?.openChapter(chapterID, novelID)
					}
				}
			}
		}
	}

	fun onRefresh() {
		if (viewModel.isOnline())
			viewModel.startUpdateManager(-1)
		else displayOfflineSnackBar(R.string.generic_error_cannot_update_library_offline)
	}

	private fun onUserClearBefore() {
		MaterialDatePicker.Builder.datePicker()
			.setTitleText(R.string.fragment_updates_clear)
			.build()
			.apply {
				addOnPositiveButtonClickListener {
					viewModel.clearBefore(it)
				}
			}
			.show(childFragmentManager, tag)
	}

	override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
		menuInflater.inflate(R.menu.toolbar_updates, menu)
	}

	override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
		when (menuItem.itemId) {
			R.id.fragment_updates_clear_all -> {
				viewModel.clearAll()
				true
			}
			R.id.fragment_updates_clear_before -> {
				onUserClearBefore()
				true
			}
			else -> false
		}
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UpdatesContent(
	items: ImmutableMap<DateTime, List<UpdatesUI>>,
	isRefreshing: Boolean,
	onRefresh: () -> Unit,
	openChapter: (UpdatesUI) -> Unit
) {
	SwipeRefresh(
		state = rememberSwipeRefreshState(isRefreshing),
		onRefresh = onRefresh
	) {
		if (items.isEmpty()) {
			Column {
				ErrorContent(
					R.string.empty_updates_message,
					ErrorAction(R.string.empty_updates_refresh_action) {
						onRefresh()
					}
				)
			}
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
						UpdateItemContent(it) {
							openChapter(it)
						}
					}
				}
			}
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
	) {
	}
}


@Composable
fun UpdateItemContent(updateUI: UpdatesUI, onClick: () -> Unit) {
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
					.clip(MaterialTheme.shapes.medium)
					.aspectRatio(coverRatio),
				error = {
					ImageLoadingError()
				},
				loading = {
					Box(Modifier.placeholder(true))
				}
			)
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
