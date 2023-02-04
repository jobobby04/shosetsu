package app.shosetsu.android.ui.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.state.ToggleableState.Off
import androidx.compose.ui.state.ToggleableState.On
import androidx.compose.ui.unit.dp
import app.shosetsu.android.R
import app.shosetsu.android.common.enums.NovelSortType
import app.shosetsu.android.common.enums.NovelSortType.*
import app.shosetsu.android.view.compose.pagerTabIndicatorOffset
import app.shosetsu.android.viewmodel.abstracted.ALibraryViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
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

/**
 * shosetsu
 * 22 / 11 / 2020
 */

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LibraryFilterMenuView(
	viewModel: ALibraryViewModel
) {
	val pagerState = rememberPagerState()
	val pages =
		listOf(stringResource(R.string.filter), stringResource(R.string.sort))
	val scope = rememberCoroutineScope()

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
		Surface {
			HorizontalPager(pageCount = pages.size, state = pagerState) {
				when (it) {
					0 -> {
						val genres by viewModel.genresFlow.collectAsState(persistentListOf())
						val genresIsNotEmpty by derivedStateOf { genres.isNotEmpty() }
						var genresIsExpanded by remember { mutableStateOf(false) }

						val tags by viewModel.tagsFlow.collectAsState(persistentListOf())
						val tagsIsNotEmpty by derivedStateOf { tags.isNotEmpty() }
						var tagsIsExpanded by remember { mutableStateOf(false) }

						val authors by viewModel.authorsFlow.collectAsState(persistentListOf())
						val authorsIsNotEmpty by derivedStateOf { authors.isNotEmpty() }
						var authorsIsExpanded by remember { mutableStateOf(false) }

						val artists by viewModel.artistsFlow.collectAsState(persistentListOf())
						val artistsIsNotEmpty by derivedStateOf { artists.isNotEmpty() }
						var artistsIsExpanded by remember { mutableStateOf(false) }
						val unreadStatusFilterState by viewModel.getUnreadFilter().collectAsState(Off)
						val downloadFilterState by viewModel.getDownloadedFilter().collectAsState(Off)

						LibraryFilterMenuFilterContent(
							genres,
							genresIsNotEmpty,
							genresIsExpanded,
							{
								genresIsExpanded = it
							},
							tags,
							tagsIsNotEmpty,
							tagsIsExpanded,
							{
								tagsIsExpanded = it
							},
							authors,
							authorsIsNotEmpty,
							authorsIsExpanded,
							{
								authorsIsExpanded = it
							},
							artists,
							artistsIsNotEmpty,
							artistsIsExpanded,
							{
								artistsIsExpanded = it
							},
							getFilterGenreState = viewModel::getFilterGenreState,
							cycleFilterGenreState = viewModel::cycleFilterGenreState,
							getFilterTagState = viewModel::getFilterTagState,
							cycleFilterTagState = viewModel::cycleFilterTagState,
							getFilterAuthorState = viewModel::getFilterAuthorState,
							cycleFilterAuthorState = viewModel::cycleFilterAuthorState,
							getFilterArtistState = viewModel::getFilterArtistState,
							cycleFilterArtistState = viewModel::cycleFilterArtistState,
							unreadStatusFilterState = unreadStatusFilterState,
							cycleUnreadStatusFilterState = viewModel::cycleUnreadFilter,
							downloadFilterState = downloadFilterState,
							cycleDownloadFilterState = viewModel::cycleDownloadedFilter
						)
					}

					1 -> {
						val sortType by viewModel.getSortType().collectAsState(BY_TITLE)
						val isSortReversed by viewModel.isSortReversed().collectAsState(false)
						val pinOnTopState: Boolean by viewModel.isPinnedOnTop().collectAsState(false)

						LibraryFilterMenuSortContent(
							sortType,
							isSortReversed,
							viewModel::setIsSortReversed,
							viewModel::setSortType,
							pinOnTopState,
							viewModel::setPinnedOnTop
						)
					}
				}
			}
		}
	}
}

@Composable
fun LibraryFilterMenuFilterContent(
	genres: ImmutableList<String>,
	genresIsNotEmpty: Boolean,
	genresIsExpanded: Boolean,
	setGenresIsExpanded: (Boolean) -> Unit,

	tags: ImmutableList<String>,
	tagsIsNotEmpty: Boolean,
	tagsIsExpanded: Boolean,
	setTagsIsExpanded: (Boolean) -> Unit,

	authors: ImmutableList<String>,
	authorsIsNotEmpty: Boolean,
	authorsIsExpanded: Boolean,
	setAuthorsIsExpanded: (Boolean) -> Unit,

	artists: ImmutableList<String>,
	artistsIsNotEmpty: Boolean,
	artistsIsExpanded: Boolean,
	setArtistsIsExpanded: (Boolean) -> Unit,

	getFilterGenreState: (String) -> Flow<ToggleableState>,
	cycleFilterGenreState: (String, ToggleableState) -> Unit,
	getFilterTagState: (String) -> Flow<ToggleableState>,
	cycleFilterTagState: (String, ToggleableState) -> Unit,
	getFilterAuthorState: (String) -> Flow<ToggleableState>,
	cycleFilterAuthorState: (String, ToggleableState) -> Unit,
	getFilterArtistState: (String) -> Flow<ToggleableState>,
	cycleFilterArtistState: (String, ToggleableState) -> Unit,


	unreadStatusFilterState: ToggleableState,
	cycleUnreadStatusFilterState: (ToggleableState) -> Unit,

	downloadFilterState: ToggleableState,
	cycleDownloadFilterState: (ToggleableState) -> Unit
) {
	Column(
		modifier = Modifier
			.fillMaxSize()
			.verticalScroll(rememberScrollState()),
	) {
		UnreadStatusFilter(
			unreadStatusFilterState,
			cycleUnreadStatusFilterState
		)
		DownloadedFilter(
			downloadFilterState,
			cycleDownloadFilterState
		)

		if (genresIsNotEmpty)
			FilterContent(
				R.string.genres,
				genres,
				genresIsExpanded,
				toggleExpansion = {
					setGenresIsExpanded(!genresIsExpanded)
				},
				getState = getFilterGenreState,
				cycleState = cycleFilterGenreState
			)

		if (tagsIsNotEmpty)
			FilterContent(
				R.string.tags,
				tags,
				tagsIsExpanded,
				toggleExpansion = {
					setTagsIsExpanded(!tagsIsExpanded)
				},
				getState = getFilterTagState,
				cycleState = cycleFilterTagState
			)

		if (authorsIsNotEmpty)
			FilterContent(
				R.string.authors,
				authors,
				authorsIsExpanded,
				toggleExpansion = {
					setAuthorsIsExpanded(!authorsIsExpanded)
				},
				getState = getFilterAuthorState,
				cycleState = cycleFilterAuthorState
			)

		if (artistsIsNotEmpty)
			FilterContent(
				R.string.artists,
				artists,
				artistsIsExpanded,
				toggleExpansion = {
					setArtistsIsExpanded(!artistsIsExpanded)
				},
				getState = getFilterArtistState,
				cycleState = cycleFilterArtistState
			)
	}
}

@Composable
fun UnreadStatusFilter(
	state: ToggleableState,
	cycleState: (ToggleableState) -> Unit
) {
	SimpleTriStateFilter(
		name = stringResource(R.string.unread_status),
		state = state,
		cycleState = cycleState,
		modifier = Modifier.padding(top = 8.dp)
	)
}

@Composable
fun DownloadedFilter(
	state: ToggleableState,
	cycleState: (ToggleableState) -> Unit
) {
	SimpleTriStateFilter(
		name = stringResource(R.string.downloaded),
		state = state,
		cycleState = cycleState,
		modifier = Modifier.padding(top = 8.dp)
	)
}

@Composable
fun PinOnTopOption(
	state: ToggleableState,
	cycleState: (ToggleableState) -> Unit
) {
	SimpleTriStateFilter(
		name = stringResource(R.string.pin_on_top),
		state = state,
		cycleState = cycleState,
		modifier = Modifier.padding(top = 8.dp)
	)
}

@Composable
fun SimpleTriStateFilter(
	name: String,
	state: ToggleableState,
	cycleState: (ToggleableState) -> Unit,
	modifier: Modifier = Modifier,
) {
	Box(modifier) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.fillMaxWidth()
				.clickable {
					cycleState(state)
				}
				.padding(vertical = 8.dp, horizontal = 16.dp)
		) {
			TriStateCheckbox(state = state, null)

			Text(name, modifier = Modifier.padding(start = 8.dp))
		}
	}
}


@Composable
fun LibraryFilterMenuSortItemContent(
	name: Int,
	state: NovelSortType,
	expectedState: NovelSortType,
	reversed: Boolean,
	setIsSortReversed: (Boolean) -> Unit,
	setSortType: (NovelSortType) -> Unit
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
			Text(stringResource(name), modifier = Modifier.padding(start = 8.dp))
		}
	}
}

@Composable
fun LibraryFilterMenuSortContent(
	state: NovelSortType,
	reversed: Boolean,
	setIsSortReversed: (Boolean) -> Unit,
	setSortType: (NovelSortType) -> Unit,
	pinOnTopState: Boolean,
	setPinOnTopState: (Boolean) -> Unit
) {
	Column(
		modifier = Modifier
			.fillMaxSize()
			.verticalScroll(rememberScrollState()),
	) {
		PinOnTopOption(
			state = if (pinOnTopState) On else Off,
			cycleState = { state ->
				setPinOnTopState(state != On)
			}
		)

		LibraryFilterMenuSortItemContent(
			R.string.fragment_library_menu_tri_by_title,
			state,
			BY_TITLE,
			reversed,
			setIsSortReversed,
			setSortType
		)
		LibraryFilterMenuSortItemContent(
			R.string.fragment_library_menu_tri_by_unread,
			state,
			BY_UNREAD_COUNT,
			reversed,
			setIsSortReversed,
			setSortType
		)
		LibraryFilterMenuSortItemContent(
			R.string.fragment_library_menu_tri_by_id,
			state,
			BY_ID,
			reversed,
			setIsSortReversed,
			setSortType
		)
		LibraryFilterMenuSortItemContent(
			R.string.fragment_library_menu_tri_by_updated,
			state,
			BY_UPDATED,
			reversed,
			setIsSortReversed,
			setSortType
		)
		LibraryFilterMenuSortItemContent(
			R.string.fragment_library_menu_tri_by_read_time,
			state,
			BY_READ_TIME,
			reversed,
			setIsSortReversed,
			setSortType
		)
	}
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColumnScope.FilterContent(
	name: Int,
	items: ImmutableList<String>,
	isExpanded: Boolean,
	toggleExpansion: () -> Unit,
	getState: (String) -> Flow<ToggleableState>,
	cycleState: (String, ToggleableState) -> Unit
) {
	Card(
		onClick = toggleExpansion,
		modifier = Modifier.padding(horizontal = 8.dp)
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(8.dp)
		) {
			Icon(
				painterResource(if (isExpanded) R.drawable.expand_less else R.drawable.expand_more),
				null
			)
			Text(stringResource(name), modifier = Modifier.padding(start = 8.dp))
		}
	}

	AnimatedVisibility(isExpanded) {
		Column {
			items.forEach { item ->
				val state by getState(item).collectAsState(Off)
				SimpleTriStateFilter(
					name = item,
					state = state,
					cycleState = {
						cycleState(item, it)
					}
				)
			}
		}
	}
}
