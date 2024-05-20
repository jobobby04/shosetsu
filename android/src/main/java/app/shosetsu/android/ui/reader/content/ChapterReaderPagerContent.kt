package app.shosetsu.android.ui.reader.content

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.shosetsu.android.R
import app.shosetsu.android.view.uimodels.StableHolder
import app.shosetsu.android.view.uimodels.model.reader.ReaderUIItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged

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
 * @since 26 / 05 / 2022
 * @author Doomsdayrs
 * Content of pager itself
 */
@OptIn(ExperimentalFoundationApi::class)
@Suppress("FunctionName", "DEPRECATION")
@Composable
fun ChapterReaderPagerContent(
	paddingValues: PaddingValues,

	items: ImmutableList<ReaderUIItem>,
	isHorizontal: Boolean,

	isSwipeInverted: Boolean,

	currentPage: Int?,
	pageJumper: StableHolder<SharedFlow<Int>>,
	onPageChanged: (Int) -> Unit,

	markChapterAsCurrent: (item: ReaderUIItem.ReaderChapterUI) -> Unit,
	onChapterRead: (item: ReaderUIItem.ReaderChapterUI) -> Unit,

	onStopTTS: () -> Unit,

	createPage: @Composable (page: Int) -> Unit
) {
	// Do not create the pager if the currentPage has not been set yet
	if (currentPage == null) {
		Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
			Text(stringResource(R.string.loading))
		}
		return
	}

	val pagerState = rememberPagerState(initialPage = currentPage, pageCount = { items.size })

	LaunchedEffect(pageJumper) {
		pageJumper.item.collectLatest {
			pagerState.scrollToPage(it)
		}
	}

	var curChapter: ReaderUIItem.ReaderChapterUI? by remember { mutableStateOf(null) }
	if (items.isNotEmpty())
		LaunchedEffect(pagerState) {
			snapshotFlow { pagerState.currentPage }.distinctUntilChanged().collect { newPage ->
				onStopTTS()
				val item = items.getOrNull(newPage) ?: return@collect

				when (item) {
					is ReaderUIItem.ReaderChapterUI -> {
						markChapterAsCurrent(item)
						curChapter = item
					}

					is ReaderUIItem.ReaderDividerUI -> {
						// Do not mark read backwards
						if (item.next?.id != curChapter?.id)
							item.prev.let(onChapterRead)
					}
				}
				onPageChanged(newPage)
			}
		}

	if (isHorizontal) {
		HorizontalPager(
			state = pagerState,
			modifier = Modifier
				.fillMaxSize()
				.padding(
					top = paddingValues.calculateTopPadding(),
					bottom = paddingValues.calculateBottomPadding()
				),
			reverseLayout = isSwipeInverted,
			pageContent = {
				createPage(it)
			}
		)
	} else {
		VerticalPager(
			state = pagerState,
			modifier = Modifier
				.fillMaxSize()
				.padding(
					top = paddingValues.calculateTopPadding(),
					bottom = paddingValues.calculateBottomPadding()
				),
			pageContent = {
				createPage(it)
			}
		)
	}
}