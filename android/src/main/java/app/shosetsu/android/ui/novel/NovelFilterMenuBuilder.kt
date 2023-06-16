package app.shosetsu.android.ui.novel

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.shosetsu.android.R
import app.shosetsu.android.common.enums.ChapterSortType
import app.shosetsu.android.common.enums.ChapterSortType.SOURCE
import app.shosetsu.android.common.enums.ChapterSortType.UPLOAD
import app.shosetsu.android.common.enums.ReadingStatus.READ
import app.shosetsu.android.common.enums.ReadingStatus.UNREAD
import app.shosetsu.android.view.compose.pagerTabIndicatorOffset
import app.shosetsu.android.view.uimodels.NovelSettingUI
import app.shosetsu.android.viewmodel.abstracted.ANovelViewModel
import com.google.accompanist.placeholder.material.placeholder
import kotlinx.coroutines.launch

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

/*
 * shosetsu
 * 22 / 11 / 2020
 */

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NovelFilterMenuView(
	viewModel: ANovelViewModel
) {
	val pagerState = rememberPagerState()
	val pages =
		listOf(stringResource(R.string.filter), stringResource(R.string.sort))
	val scope = rememberCoroutineScope()

	val novelSetting by viewModel.novelSettingFlow.collectAsState(null)

	Column {
		TabRow(
			// Our selected tab is our current page
			selectedTabIndex = pagerState.currentPage,
			// Override the indicator, using the provided pagerTabIndicatorOffset modifier
			indicator = { tabPositions ->
				TabRowDefaults.Indicator(
					Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
				)
			}
		) {
			// Add tabs for all of our pages
			pages.forEachIndexed { index, title ->
				Tab(
					text = { Text(title) },
					selected = pagerState.currentPage == index,
					onClick = {
						scope.launch {
							pagerState.animateScrollToPage(index)
						}
					},
				)
			}
		}
		HorizontalPager(pageCount = pages.size, state = pagerState) {
			when (it) {
				0 -> NovelFilterMenuFilterContent(
					novelSetting ?: NovelSettingUI(-1),
					novelSetting == null,
					updateNovelSetting = viewModel::updateNovelSetting
				)

				1 -> NovelFilterMenuSortContent(
					(novelSetting ?: NovelSettingUI(-1)).sortType,
					(novelSetting ?: NovelSettingUI(-1)).reverseOrder,
					novelSetting == null,
					update = { a, b ->
						viewModel.updateNovelSetting(
							(novelSetting ?: NovelSettingUI(-1)).copy(
								sortType = a,
								reverseOrder = b
							)
						)
					}
				)
			}
		}
	}
}

@Composable
fun NovelFilterMenuFilterContent(
	settings: NovelSettingUI,
	isLoading: Boolean,
	updateNovelSetting: (NovelSettingUI) -> Unit
) {
	Column(
		modifier = Modifier
			.fillMaxSize()
			.verticalScroll(rememberScrollState())
	) {
		val readingStatus = settings.showOnlyReadingStatusOf

		NovelFilterMenuFilterRadioButtonItem(
			title = stringResource(R.string.all),
			selected = readingStatus != UNREAD && readingStatus != READ,
			isLoading = isLoading,
			onClick = {
				updateNovelSetting(
					settings.copy(
						showOnlyReadingStatusOf = null
					)
				)
			}
		)

		NovelFilterMenuFilterRadioButtonItem(
			title = stringResource(R.string.read),
			selected = readingStatus == READ,
			isLoading = isLoading,
			onClick = {
				updateNovelSetting(
					settings.copy(
						showOnlyReadingStatusOf = READ
					)
				)
			}
		)

		NovelFilterMenuFilterRadioButtonItem(
			title = stringResource(R.string.unread),
			selected = readingStatus == UNREAD,
			isLoading = isLoading,
			onClick = {
				updateNovelSetting(
					settings.copy(
						showOnlyReadingStatusOf = UNREAD
					)
				)
			}
		)

		NovelFilterMenuFilterCheckboxItem(
			stringResource(R.string.bookmarked),
			settings.showOnlyBookmarked,
			isLoading,
			onCheckedChange = {
				updateNovelSetting(
					settings.copy(
						showOnlyBookmarked = it
					)
				)
			},
		)

		NovelFilterMenuFilterCheckboxItem(
			stringResource(R.string.downloaded),
			settings.showOnlyDownloaded,
			isLoading,
			onCheckedChange = {
				updateNovelSetting(
					settings.copy(
						showOnlyDownloaded = it
					)
				)
			},
		)
	}
}

@Composable
fun NovelFilterMenuFilterRadioButtonItem(
	title: String,
	selected: Boolean,
	isLoading: Boolean,
	onClick: () -> Unit
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clickable {
				if (!isLoading) onClick()
			},
		horizontalArrangement = Arrangement.spacedBy(4.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		RadioButton(
			selected = selected,
			onClick = onClick,
			modifier = Modifier
				.placeholder(isLoading)
				.fillMaxWidth(0.25f)
		)
		Text(title)
	}
}

@Composable
fun NovelFilterMenuFilterCheckboxItem(
	title: String,
	isChecked: Boolean,
	isLoading: Boolean,
	onCheckedChange: (Boolean) -> Unit
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clickable {
				if (!isLoading) onCheckedChange(!isChecked)
			},
		horizontalArrangement = Arrangement.spacedBy(4.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Checkbox(
			isChecked,
			onCheckedChange = {
				if (!isLoading) onCheckedChange(it)
			},
			modifier = Modifier
				.placeholder(isLoading)
				.fillMaxWidth(0.25f)
		)
		Text(title)
	}
}

@Composable
fun NovelFilterMenuSortContent(
	chapterSortType: ChapterSortType,
	isReversed: Boolean,
	isLoading: Boolean,
	update: (ChapterSortType, Boolean) -> Unit
) {
	Column(
		modifier = Modifier
			.fillMaxSize()
			.verticalScroll(rememberScrollState())
	) {

		NovelFilterMenuSortItemContent(
			stringResource(R.string.fragment_library_menu_tri_by_source),
			state = chapterSortType,
			expectedState = SOURCE,
			reversed = isReversed,
			isPlaceholder = isLoading,
			setIsSortReversed = {
				update(SOURCE, it)
			},
			setSortType = {
				update(it, false)
			},
		)

		NovelFilterMenuSortItemContent(
			stringResource(R.string.fragment_library_menu_tri_by_date),
			state = chapterSortType,
			expectedState = UPLOAD,
			reversed = isReversed,
			isPlaceholder = isLoading,
			setIsSortReversed = {
				update(UPLOAD, it)
			},
			setSortType = {
				update(it, false)
			}
		)
	}
}


@Composable
fun NovelFilterMenuSortItemContent(
	name: String,
	state: ChapterSortType,
	expectedState: ChapterSortType,
	reversed: Boolean,
	isPlaceholder: Boolean,
	setIsSortReversed: (Boolean) -> Unit,
	setSortType: (ChapterSortType) -> Unit
) {
	val isExpected = state == expectedState
	Box(
		modifier = Modifier
			.clickable {
				if (isExpected)
					setIsSortReversed(!reversed)
				else setSortType(expectedState)
			}
			.padding(8.dp)
			.placeholder(isPlaceholder)
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.padding(8.dp)
				.fillMaxWidth()
		) {
			Box(modifier = Modifier.size(32.dp)) {
				if (isExpected)
					Icon(
						painterResource(
							if (reversed) {
								R.drawable.expand_less
							} else {
								R.drawable.expand_more
							}
						),
						null,
						modifier = Modifier.align(Alignment.Center)
					)
			}
			Text(name, modifier = Modifier.padding(start = 8.dp))
		}
	}
}
