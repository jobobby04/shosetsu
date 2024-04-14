package app.shosetsu.android.ui.catalogue

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import app.shosetsu.android.R
import app.shosetsu.android.common.enums.NovelCardType
import app.shosetsu.android.common.enums.NovelCardType.COMPRESSED
import app.shosetsu.android.common.enums.NovelCardType.COZY
import app.shosetsu.android.common.enums.NovelCardType.NORMAL
import app.shosetsu.android.common.ext.openInWebView
import app.shosetsu.android.common.ext.viewModelDi
import app.shosetsu.android.ui.library.SearchAction
import app.shosetsu.android.ui.library.ViewTypeButton
import app.shosetsu.android.ui.novel.CategoriesDialog
import app.shosetsu.android.ui.theme.ShosetsuTheme
import app.shosetsu.android.view.BottomSheetDialog
import app.shosetsu.android.view.compose.ErrorAction
import app.shosetsu.android.view.compose.ErrorContent
import app.shosetsu.android.view.compose.NavigateBackButton
import app.shosetsu.android.view.compose.NovelCardCompressedContent
import app.shosetsu.android.view.compose.NovelCardCozyContent
import app.shosetsu.android.view.compose.NovelCardNormalContent
import app.shosetsu.android.view.compose.itemsIndexed
import app.shosetsu.android.view.uimodels.model.catlog.ACatalogNovelUI
import app.shosetsu.android.viewmodel.abstracted.ACatalogViewModel
import app.shosetsu.android.viewmodel.abstracted.ACatalogViewModel.BackgroundNovelAddProgress
import app.shosetsu.android.viewmodel.abstracted.ACatalogViewModel.BackgroundNovelAddProgress.Added
import app.shosetsu.android.viewmodel.abstracted.ACatalogViewModel.BackgroundNovelAddProgress.Adding
import kotlinx.collections.immutable.persistentListOf
import org.acra.ACRA

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
 * ====================================================================
 */

/**
 * Shosetsu
 * 9 / June / 2019
 *
 * @author github.com/doomsdayrs
 */

/**
 * A catalogue is a view showcasing novels from a given extension.
 */
@Composable
fun CatalogueView(
	extensionId: Int,
	onOpenNovel: (novelId: Int) -> Unit,
	onBack: () -> Unit
) {
	ShosetsuTheme {
		val viewModel: ACatalogViewModel = viewModelDi()

		LaunchedEffect(extensionId) {
			viewModel.setExtensionID(extensionId)
		}

		val type by viewModel.novelCardTypeLive.collectAsState()

		val query by viewModel.queryFlow.collectAsState()
		val baseURL by viewModel.baseURL.collectAsState()
		val extensionName by viewModel.extensionName.collectAsState()
		val hasSearch by viewModel.hasSearchLive.collectAsState()

		val columnsInV by viewModel.columnsInV.collectAsState()
		val columnsInH by viewModel.columnsInH.collectAsState()

		val items = viewModel.itemsLive.collectAsLazyPagingItems()

		val exception by viewModel.exceptionFlow.collectAsState(null)
		val hasFilters by viewModel.hasFilters.collectAsState()

		val categories by viewModel.categories.collectAsState()

		val backgroundAddState by viewModel.backgroundAddState.collectAsState()
		val isFilterMenuVisible by viewModel.isFilterMenuVisible.collectAsState()

		val context = LocalContext.current
		val hostState = remember { SnackbarHostState() }
		var categoriesDialogItem by remember { mutableStateOf<ACatalogNovelUI?>(null) }

		LaunchedEffect(backgroundAddState) {
			when (backgroundAddState) {
				is Added -> {
					hostState.showSnackbar(
						context.getString(
							R.string.fragment_catalogue_toast_background_add_success,
							(backgroundAddState as Added).title
						)
					)
				}

				Adding -> {
					hostState.showSnackbar(
						context.getString(
							R.string.fragment_catalogue_toast_background_add
						)
					)
				}

				is BackgroundNovelAddProgress.Failure -> {
					val error = (backgroundAddState as BackgroundNovelAddProgress.Failure).error

					val result = hostState.showSnackbar(
						context.getString(
							R.string.fragment_catalogue_toast_background_add_fail,
							error.message
								?: "Unknown exception"
						),
						actionLabel = context.getString(R.string.report)
					)

					if (result == SnackbarResult.ActionPerformed) {
						ACRA.errorReporter.handleSilentException(error)
					}
				}

				BackgroundNovelAddProgress.Unknown -> {
				}
			}
		}

		LaunchedEffect(exception) {
			if (exception != null) {
				val result = hostState.showSnackbar(
					exception?.message ?: "Unknown error",
					actionLabel = context.getString(R.string.reset)
				)
				if (result == SnackbarResult.ActionPerformed) {
					viewModel.resetView()
				}
			}
		}

		val prepend = items.loadState.prepend

		LaunchedEffect(prepend) {
			if (prepend is LoadState.Error) {
				val result = hostState.showSnackbar(
					prepend.error.message ?: "Unknown error",
					actionLabel = context.getString(R.string.retry)
				)
				if (result == SnackbarResult.ActionPerformed) {
					items.retry()
				}
			}
		}

		val append = items.loadState.prepend
		LaunchedEffect(append) {
			if (append is LoadState.Error) {
				val result = hostState.showSnackbar(
					append.error.message ?: "Unknown error",
					actionLabel = context.getString(R.string.retry)
				)
				if (result == SnackbarResult.ActionPerformed) {
					items.retry()
				}
			}
		}

		CatalogContent(
			items = items,
			cardType = type,
			columnsInV = columnsInV,
			columnsInH = columnsInH,
			onClick = {
				onOpenNovel(it.id)
			},
			onLongClick = {
				if (categories.isNotEmpty() && !it.bookmarked) {
					categoriesDialogItem = it
				} else {
					viewModel.backgroundNovelAdd(it)
				}
			},
			hasFilters = hasFilters,
			openWebView = {
				context.openInWebView(baseURL ?: return@CatalogContent)
			},
			clearCookies = {
				viewModel.clearCookies()
				items.refresh()
			},
			onShowFilterMenu = viewModel::showFilterMenu,
			extensionName = extensionName,
			query = query,
			onSetQuery = viewModel::applyQuery,
			onSetCardType = viewModel::setViewType,
			onBack = onBack,
			hasSearch = hasSearch,
			hostState = hostState
		)
		if (categoriesDialogItem != null) {
			CategoriesDialog(
				onDismissRequest = { categoriesDialogItem = null },
				categories = categories,
				setCategories = {
					viewModel.backgroundNovelAdd(
						item = categoriesDialogItem ?: return@CategoriesDialog,
						categories = it
					)
				},
				novelCategories = remember { persistentListOf() }
			)
		}

		if (isFilterMenuVisible) {
			val filterItems by viewModel.filterItemsLive.collectAsState()
			BottomSheetDialog(viewModel::hideFilterMenu) {
				CatalogFilterMenu(
					items = filterItems,
					getBoolean = viewModel::getFilterBooleanState,
					setBoolean = viewModel::setFilterBooleanState,
					getInt = viewModel::getFilterIntState,
					setInt = viewModel::setFilterIntState,
					getString = viewModel::getFilterStringState,
					setString = viewModel::setFilterStringState,
					applyFilter = viewModel::applyFilter,
					resetFilter = viewModel::resetFilter
				)
			}
		}
	}
}

/**
 * Content of [CatalogueView]
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CatalogContent(
	extensionName: String,
	query: String,
	onSetQuery: (String) -> Unit,
	items: LazyPagingItems<ACatalogNovelUI>,
	cardType: NovelCardType,
	onSetCardType: (NovelCardType) -> Unit,
	columnsInV: Int,
	columnsInH: Int,
	onClick: (ACatalogNovelUI) -> Unit,
	onLongClick: (ACatalogNovelUI) -> Unit,
	hasFilters: Boolean,
	clearCookies: () -> Unit,
	openWebView: () -> Unit,
	onShowFilterMenu: () -> Unit,
	onBack: () -> Unit,
	hasSearch: Boolean,
	hostState: SnackbarHostState
) {
	Scaffold(
		modifier = Modifier.fillMaxSize(),
		floatingActionButton = {
			CatalogFloatingActionButton(hasFilters, onShowFilterMenu)
		},
		topBar = {
			CatalogTopBar(
				extensionName,
				onBack,
				hasSearch,
				query,
				onSetQuery,
				cardType,
				onSetCardType,
				openWebView
			)
		},
		snackbarHost = {
			SnackbarHost(hostState)
		}
	) { padding ->
		val pullRefreshState = rememberPullRefreshState(
			items.loadState.refresh == LoadState.Loading,
			onRefresh = { items.refresh() }
		)

		Column {
			CatalogRefreshBar(items)

			val errorState = items.loadState.refresh
			if (errorState is LoadState.Error) {
				CatalogErrorContent(
					errorState,
					items,
					openWebView,
					clearCookies
				)
			} else {
				Box(
					Modifier
						.pullRefresh(pullRefreshState)
						.padding(padding)
				) {
					CatalogGrid(items, columnsInH, columnsInV, cardType, onClick, onLongClick)
				}
			}
		}
	}
}

/**
 * The refresh bar on top of the content
 */
@Composable
fun CatalogRefreshBar(items: LazyPagingItems<ACatalogNovelUI>) {
	AnimatedVisibility(items.loadState.refresh == LoadState.Loading) {
		LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
	}
}

/**
 * The filter button
 */
@Composable
fun CatalogFloatingActionButton(hasFilters: Boolean, onShowFilterMenu: () -> Unit) {
	// TODO Collapsible
	AnimatedVisibility(hasFilters) {
		ExtendedFloatingActionButton(
			text = {
				Text(stringResource(R.string.filter))
			},
			icon = {
				Icon(painterResource(R.drawable.filter), stringResource(R.string.filter))
			},
			onClick = onShowFilterMenu
		)
	}
}

/**
 * Catalogs top bar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogTopBar(
	extensionName: String,
	onBack: () -> Unit,
	hasSearch: Boolean,
	query: String,
	onSetQuery: (String) -> Unit,
	cardType: NovelCardType,
	onSetCardType: (NovelCardType) -> Unit,
	openWebView: () -> Unit
) {
	TopAppBar(
		title = {
			Text(extensionName)
		},
		navigationIcon = {
			NavigateBackButton(onBack)
		},
		actions = {
			AnimatedVisibility(hasSearch) {
				SearchAction(
					query,
					onSetQuery
				)
			}
			ViewTypeButton(
				cardType,
				onSetCardType
			)

			IconButton(
				onClick = openWebView
			) {
				Icon(
					painterResource(R.drawable.open_in_browser),
					stringResource(R.string.action_open_in_webview)
				)
			}
		}
	)
}

/**
 * Main content, the grid of items
 */
@Composable
fun CatalogGrid(
	items: LazyPagingItems<ACatalogNovelUI>,
	columnsInH: Int,
	columnsInV: Int,
	cardType: NovelCardType,
	onClick: (ACatalogNovelUI) -> Unit,
	onLongClick: (ACatalogNovelUI) -> Unit
) {
	val w = LocalConfiguration.current.screenWidthDp
	val o = LocalConfiguration.current.orientation

	val size =
		(w / when (o) {
			Configuration.ORIENTATION_LANDSCAPE -> columnsInH
			else -> columnsInV
		}).dp - 8.dp

	val state = rememberLazyGridState()

	LazyVerticalGrid(
		modifier = Modifier.fillMaxSize(),
		columns = GridCells.Adaptive(if (cardType != COMPRESSED) size else 400.dp),
		contentPadding = PaddingValues(
			bottom = 200.dp,
			start = 8.dp,
			end = 8.dp,
			top = 4.dp
		),
		state = state,
		horizontalArrangement = Arrangement.spacedBy(4.dp),
		verticalArrangement = Arrangement.spacedBy(4.dp)
	) {
		itemsIndexed(
			items,
			key = { index, item -> item.hashCode() + index }
		) { _, item ->
			when (cardType) {
				NORMAL -> CatalogNormalCard(item, onClick, onLongClick)
				COMPRESSED -> CatalogCompressedCard(item, onClick, onLongClick)
				COZY -> CatalogCozyCard(item, onClick, onLongClick)
			}
		}
		appendBar(items)
		noMoreBar(items)
	}
}

/**
 * [NovelCardNormalContent] adapted for [CatalogueView]
 */
@Composable
fun CatalogNormalCard(
	item: ACatalogNovelUI?,
	onClick: (ACatalogNovelUI) -> Unit,
	onLongClick: (ACatalogNovelUI) -> Unit
) {
	if (item != null)
		NovelCardNormalContent(
			item.title,
			item.imageURL,
			onClick = {
				onClick(item)
			},
			onLongClick = {
				onLongClick(item)
			},
			isBookmarked = item.bookmarked
		)
}

/**
 * [NovelCardCompressedContent] adapted for [CatalogueView]
 */
@Composable
fun CatalogCompressedCard(
	item: ACatalogNovelUI?,
	onClick: (ACatalogNovelUI) -> Unit,
	onLongClick: (ACatalogNovelUI) -> Unit
) {
	if (item != null)
		NovelCardCompressedContent(
			item.title,
			item.imageURL,
			onClick = {
				onClick(item)
			},
			onLongClick = {
				onLongClick(item)
			},
			isBookmarked = item.bookmarked
		)
}

/**
 * [NovelCardCozyContent] adapted for [CatalogueView]
 */
@Composable
fun CatalogCozyCard(
	item: ACatalogNovelUI?,
	onClick: (ACatalogNovelUI) -> Unit,
	onLongClick: (ACatalogNovelUI) -> Unit
) {
	if (item != null)
		NovelCardCozyContent(
			item.title,
			item.imageURL,
			onClick = {
				onClick(item)
			},
			onLongClick = {
				onLongClick(item)
			},
			isBookmarked = item.bookmarked
		)
}

/**
 * Catalogs error view
 */
@Composable
fun CatalogErrorContent(
	errorState: LoadState.Error,
	items: LazyPagingItems<ACatalogNovelUI>,
	openWebView: () -> Unit,
	clearCookies: () -> Unit
) {
	ErrorContent(
		errorState.error.message ?: "Unknown",
		actions = arrayOf(
			ErrorAction(R.string.retry, items::refresh),
			ErrorAction(R.string.action_open_in_webview, openWebView),
			ErrorAction(R.string.settings_advanced_clear_cookies_title, clearCookies),
		),
		stackTrace = errorState.error.stackTraceToString()
	)
}

/**
 * Loading bar appended to the bottom of [CatalogGrid]
 */
fun LazyGridScope.appendBar(items: LazyPagingItems<ACatalogNovelUI>) {
	if (items.loadState.append == LoadState.Loading) {
		item(span = { GridItemSpan(maxLineSpan) }) {
			LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
		}
	}
}

/**
 * No more message appended to the bottom of [CatalogGrid]
 */
fun LazyGridScope.noMoreBar(items: LazyPagingItems<ACatalogNovelUI>) {
	if (items.loadState.refresh.endOfPaginationReached ||
		items.loadState.append.endOfPaginationReached
	) {
		item(span = { GridItemSpan(maxLineSpan) }) {
			CatalogContentNoMore()
		}
	}
}


/**
 * Preview [CatalogContentNoMore]
 */
@Preview
@Composable
fun PreviewCatalogContentNoMore() {
	ShosetsuTheme {
		CatalogContentNoMore()
	}
}

/**
 * Tells the user there is no more content to see.
 * Appears at the bottom of the listing.
 */
@Composable
fun CatalogContentNoMore() {
	Box(
		modifier = Modifier.fillMaxWidth()
	) {
		Text(
			stringResource(R.string.fragment_catalogue_no_more),
			modifier = Modifier
				.padding(32.dp)
				.align(Alignment.Center)
		)
	}
}