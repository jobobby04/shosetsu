package app.shosetsu.android.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import app.shosetsu.android.R
import app.shosetsu.android.common.ext.viewModelDi
import app.shosetsu.android.view.compose.ImageLoadingError
import app.shosetsu.android.view.compose.NavigateBackButton
import app.shosetsu.android.view.compose.NovelCardCozyContent
import app.shosetsu.android.view.compose.NovelCardNormalContent
import app.shosetsu.android.view.compose.PlaceholderNovelCardCozyContent
import app.shosetsu.android.view.compose.PlaceholderNovelCardNormalContent
import app.shosetsu.android.view.compose.rememberFakePullRefreshState
import app.shosetsu.android.view.uimodels.StableHolder
import app.shosetsu.android.view.uimodels.model.catlog.ACatalogNovelUI
import app.shosetsu.android.view.uimodels.model.search.SearchRowUI
import app.shosetsu.android.viewmodel.abstracted.ASearchViewModel
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.google.accompanist.placeholder.material.placeholder
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

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
 * Shosetsu
 * 9 / June / 2019
 *
 * @author github.com/doomsdayrs
 */
@Composable
fun SearchView(
	initalQuery: String?,
	openNovel: (novelId: Int) -> Unit,
	onBack: () -> Unit
) {
	val viewModel: ASearchViewModel = viewModelDi()

	LaunchedEffect(initalQuery) {
		viewModel.initQuery(initalQuery)
	}

	val query by viewModel.query.collectAsState()
	val rows by viewModel.listings.collectAsState()
	val isCozy by viewModel.isCozy.collectAsState()

	SearchContent(
		rows = rows,
		isCozy = isCozy,
		getChildren = {
			if (it == -1)
				viewModel.searchLibrary()
			else
				viewModel.searchExtension(it)
		},
		getException = viewModel::getException,
		onClick = {
			openNovel(it.id)
		},
		onRefresh = viewModel::refresh,
		onRefreshAll = viewModel::refresh,
		query = query,
		onBack = onBack,
		onSetQuery = viewModel::setQuery,
		onApply = viewModel::applyQuery
	)
}

@Preview
@Composable
fun PreviewSearchContent() {
	SearchContent(
		rows = persistentListOf(SearchRowUI(-1, "Library", null)).addAll(
			List(5) {
				SearchRowUI(
					it,
					"Test",
					null
				)
			}
		),
		getException = {
			flow { emit(null) }
		},
		onClick = {},
		onRefresh = {},
		onRefreshAll = {},
		getChildren = {
			flow { }
		},
		query = "",
		onBack = {},
		onSetQuery = {},
		onApply = {},
	)
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SearchContent(
	rows: ImmutableList<SearchRowUI>,
	isCozy: Boolean = false,
	getChildren: (id: Int) -> Flow<PagingData<ACatalogNovelUI>>,
	getException: (id: Int) -> Flow<Throwable?>,
	onClick: (ACatalogNovelUI) -> Unit,
	onRefresh: (id: Int) -> Unit,
	onRefreshAll: () -> Unit,

	onBack: () -> Unit,

	query: String,
	onSetQuery: (String) -> Unit,
	onApply: (String) -> Unit
) {
	val (isRefreshing, pullRefreshState) = rememberFakePullRefreshState(onRefreshAll)
	Scaffold(
		topBar = {
			TopAppBar(
				title = {
					Text(stringResource(R.string.global_search))
				},
				navigationIcon = {
					NavigateBackButton(onBack)
				}
			)
		}
	) { paddingValue ->
		Box(
			Modifier
				.pullRefresh(pullRefreshState)
				.padding(paddingValue)
				.fillMaxSize()
		) {
			Column {
				SearchBar(
					query,
					onSetQuery,
					onApply,
					false,
					onActiveChange = {},
					modifier = Modifier.fillMaxWidth(),
					leadingIcon = {
						Icon(Icons.Default.Search, stringResource(R.string.search))
					},
					placeholder = {
						Text(stringResource(R.string.search))
					}
				) {
				}

				LazyColumn(
					modifier = Modifier.fillMaxSize(),
					contentPadding = PaddingValues(top = 8.dp, bottom = 64.dp)
				) {
					items(rows, key = { row -> row.extensionID }) { row ->
						val children: LazyPagingItems<ACatalogNovelUI> =
							getChildren(row.extensionID).collectAsLazyPagingItems()

						SearchRowContent(
							row = row,
							loadingBar = {
								if (children.loadState.refresh == LoadState.Loading)
									LinearProgressIndicator(
										modifier = Modifier.fillMaxWidth()
									)
							},
							items = {
								items(
									children.itemCount,
								) { index ->
									val novelUI = children[index]
									Box(
										modifier = Modifier.width(105.dp)
									) {
										if (novelUI != null)
											if (!isCozy)
												NovelCardNormalContent(
													novelUI.title,
													novelUI.imageURL,
													onClick = {
														onClick(novelUI)
													},
													onLongClick = {},
													isBookmarked = novelUI.bookmarked
												)
											else NovelCardCozyContent(
												novelUI.title,
												novelUI.imageURL,
												onClick = {
													onClick(novelUI)
												},
												onLongClick = {},
												isBookmarked = novelUI.bookmarked
											)
										else if (!isCozy) PlaceholderNovelCardNormalContent() else PlaceholderNovelCardCozyContent()
									}
								}
							},
							exception = {
								val exception by getException(row.extensionID).collectAsState(null)
								if (exception != null)
									ExceptionBar(
										remember(exception) {
											StableHolder(exception!!)
										},
										onRefresh = {
											onRefresh(row.extensionID)
										}
									)
								else {
									val refreshState = children.loadState.refresh
									if (refreshState is LoadState.Error) {
										ExceptionBar(
											remember(refreshState.error) {
												StableHolder(refreshState.error)
											},
											onRefresh = {
												children.refresh()
											}
										)
									}
								}
							}
						)
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

@Composable
fun ExceptionBar(
	exception: StableHolder<Throwable>,
	onRefresh: () -> Unit
) {
	Row(
		verticalAlignment = Alignment.CenterVertically
	) {
		Text(
			exception.item.message ?: stringResource(R.string.unknown),
			modifier = Modifier.fillMaxWidth(.75f)
		)
		Button(onRefresh) {
			Text(stringResource(R.string.retry))
		}
	}
}


@Preview
@Composable
fun PreviewSearchRowContent() {
	SearchRowContent(
		SearchRowUI(
			extensionID = 0,
			name = "Name",
			imageURL = null
		), loadingBar = {}, items = {}, exception = {})
}

@Composable
fun SearchRowContent(
	row: SearchRowUI,
	loadingBar: @Composable () -> Unit,
	items: LazyListScope.() -> Unit,
	exception: @Composable () -> Unit,
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.padding(top = 8.dp),
	) {
		Row(
			modifier = Modifier.padding(8.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			SubcomposeAsyncImage(
				ImageRequest.Builder(LocalContext.current)
					.data(if (!row.imageURL.isNullOrEmpty()) row.imageURL else R.drawable.library)
					.crossfade(true)
					.build(),
				contentDescription = row.name,
				modifier = Modifier.size(32.dp),
				error = {
					ImageLoadingError()
				},
				loading = {
					Box(Modifier.placeholder(true))
				}
			)
			Text(row.name, modifier = Modifier.padding(start = 8.dp))
		}
		loadingBar()

		LazyRow(
			horizontalArrangement = Arrangement.spacedBy(4.dp),
			contentPadding = PaddingValues(horizontal = 4.dp)
		) {
			items()
		}

		exception()

		Divider(modifier = Modifier.fillMaxWidth())
	}
}