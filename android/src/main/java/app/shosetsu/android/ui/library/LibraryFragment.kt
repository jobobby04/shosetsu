package app.shosetsu.android.ui.library

import android.content.Intent
import android.content.res.Configuration
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult.ActionPerformed
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.enterAlwaysScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.shosetsu.android.R
import app.shosetsu.android.common.OfflineException
import app.shosetsu.android.common.enums.NovelCardType
import app.shosetsu.android.common.enums.NovelCardType.COMPRESSED
import app.shosetsu.android.common.enums.NovelCardType.COZY
import app.shosetsu.android.common.enums.NovelCardType.NORMAL
import app.shosetsu.android.common.ext.onIO
import app.shosetsu.android.common.ext.viewModelDi
import app.shosetsu.android.ui.novel.CategoriesDialog
import app.shosetsu.android.ui.theme.ShosetsuTheme
import app.shosetsu.android.view.BottomSheetDialog
import app.shosetsu.android.view.compose.ErrorContent
import app.shosetsu.android.view.compose.NovelCardCompressedContent
import app.shosetsu.android.view.compose.NovelCardCozyContent
import app.shosetsu.android.view.compose.NovelCardNormalContent
import app.shosetsu.android.view.compose.pagerTabIndicatorOffset
import app.shosetsu.android.view.compose.rememberFakePullRefreshState
import app.shosetsu.android.view.uimodels.model.LibraryNovelUI
import app.shosetsu.android.view.uimodels.model.LibraryUI
import app.shosetsu.android.viewmodel.abstracted.ALibraryViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
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
 * Shosetsu
 * 9 / June / 2019
 *
 * @author github.com/doomsdayrs
 *
 * Main view of the users saved novels.
 */
@Suppress("IncompleteDestructuring")
@Composable
fun LibraryView(
	onOpenNovel: (novelId: Int) -> Unit,
	onMigrate: (ids: List<Int>) -> Unit,
	drawerIcon: @Composable () -> Unit,
) {
	ShosetsuTheme {
		val viewModel = viewModelDi<ALibraryViewModel>()

		val items by viewModel.liveData.collectAsState()
		val isEmpty by viewModel.isEmptyFlow.collectAsState()
		val hasSelected by viewModel.hasSelection.collectAsState()
		val type by viewModel.novelCardTypeFlow.collectAsState()
		val badgeToast by viewModel.badgeUnreadToastFlow.collectAsState()

		val columnsInV by viewModel.columnsInV.collectAsState()
		val columnsInH by viewModel.columnsInH.collectAsState()
		val isCategoriesDialogOpen by viewModel.isCategoryDialogOpen.collectAsState()
		val isFilterMenuVisible by viewModel.isFilterMenuVisible.collectAsState()
		val query by viewModel.queryFlow.collectAsState()
		val error by viewModel.error.collectAsState(null)
		val selectedIds by viewModel.selectedIds.collectAsState()

		BackHandler(hasSelected) {
			viewModel.deselectAll()
		}

		val context = LocalContext.current
		val scope = rememberCoroutineScope()
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
						if (result == ActionPerformed) {
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

		LibraryContent(
			items = items,
			isEmpty = isEmpty,
			setActiveCategory = viewModel::setActiveCategory,
			cardType = type,
			columnsInV = columnsInV,
			columnsInH = columnsInH,
			hasSelected = hasSelected,
			onRefresh = viewModel::startUpdateManager,
			onOpen = { (id) -> onOpenNovel(id) },
			toggleSelection = viewModel::toggleSelection,
			toastNovel = if (badgeToast) {
				{ item ->
					scope.launch {
						hostState.showSnackbar(
							context.resources.getQuantityString(
								R.plurals.toast_unread_count,
								item.unread,
								item.unread
							)
						)
					}
				}
			} else null,
			onInverseSelection = viewModel::invertSelection,
			onSelectAll = viewModel::selectAll,
			onRemove = viewModel::removeSelectedFromLibrary,
			onMigrate = {
				viewModel.deselectAll()
				onMigrate(selectedIds)
			},
			onTogglePin = viewModel::togglePinSelected,
			onSetCategories = viewModel::showCategoryDialog,
			onDeselectAll = viewModel::deselectAll,
			onSelectBetween = viewModel::selectBetween,
			query = query,
			onSearch = viewModel::setQuery,
			selectedType = type,
			onSetType = viewModel::setViewType,
			hostState = hostState,
			onShowFilterMenu = viewModel::showFilterMenu,
			drawerIcon = drawerIcon
		)
		if (isCategoriesDialogOpen) {
			CategoriesDialog(
				onDismissRequest = { viewModel.hideCategoryDialog() },
				categories = remember(items?.categories) {
					items?.categories ?: persistentListOf()
				},
				novelCategories = remember { persistentListOf() },
				setCategories = viewModel::setCategories
			)
		}

		if (isFilterMenuVisible) {
			BottomSheetDialog(viewModel::hideFilterMenu) {
				LibraryFilterMenuView(viewModel)
			}
		}
	}
}

/**
 * Content of [LibraryView]
 */
@Composable
fun LibraryContent(
	items: LibraryUI?,
	isEmpty: Boolean,
	setActiveCategory: (Int) -> Unit,
	cardType: NovelCardType,
	columnsInV: Int,
	columnsInH: Int,
	hasSelected: Boolean,
	onRefresh: (Int) -> Unit,
	onOpen: (LibraryNovelUI) -> Unit,
	toggleSelection: (LibraryNovelUI) -> Unit,
	toastNovel: ((LibraryNovelUI) -> Unit)?,
	onInverseSelection: () -> Unit,
	onSelectAll: () -> Unit,
	onRemove: () -> Unit,
	onMigrate: () -> Unit,
	onTogglePin: () -> Unit,
	onSetCategories: () -> Unit,
	onDeselectAll: () -> Unit,
	onSelectBetween: () -> Unit,
	query: String,
	onSearch: (String) -> Unit,
	selectedType: NovelCardType,
	onSetType: (NovelCardType) -> Unit,
	hostState: SnackbarHostState,
	onShowFilterMenu: () -> Unit,
	drawerIcon: @Composable () -> Unit
) {
	Scaffold(
		topBar = {
			LibraryAppBar(
				hasSelected = hasSelected,
				onInverseSelection = onInverseSelection,
				onSelectAll = onSelectAll,
				onRemove = onRemove,
				onMigrate = onMigrate,
				onTogglePin = onTogglePin,
				onSetCategories = onSetCategories,
				onDeselectAll = onDeselectAll,
				onSelectBetween = onSelectBetween,
				query = query,
				onSearch = onSearch,
				selectedType = selectedType,
				onSetType = onSetType,
				onRefresh = {
					onRefresh(-1) // default, TODO maybe make better?
				},
				isEmpty = isEmpty,
				drawerIcon = drawerIcon
			)
		},
		snackbarHost = {
			SnackbarHost(hostState)
		},
		floatingActionButton = {
			// TODO Collapsible
			AnimatedVisibility(!isEmpty) {
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
	) { paddingValues ->
		if (!isEmpty) {
			if (items == null) {
				Box(
					modifier = Modifier
						.fillMaxSize()
						.padding(paddingValues)
				) {
					LinearProgressIndicator(
						Modifier
							.fillMaxWidth()
							.align(Alignment.TopCenter)
					)
				}
			} else {
				LibraryPager(
					paddingValues = paddingValues,
					library = items,
					setActiveCategory = setActiveCategory,
					cardType = cardType,
					columnsInV = columnsInV,
					columnsInH = columnsInH,
					hasSelected = hasSelected,
					onRefresh = onRefresh,
					onOpen = onOpen,
					toggleSelection = toggleSelection,
					toastNovel = toastNovel,
				)
			}
		} else {
			ErrorContent(
				stringResource(R.string.empty_library_message),
				modifier = Modifier.padding(paddingValues)
			)
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryAppBar(
	hasSelected: Boolean,
	onInverseSelection: () -> Unit,
	onSelectAll: () -> Unit,
	onRemove: () -> Unit,
	onMigrate: () -> Unit,
	onTogglePin: () -> Unit,
	onSetCategories: () -> Unit,
	onDeselectAll: () -> Unit,
	onSelectBetween: () -> Unit,
	query: String,
	onSearch: (String) -> Unit,
	selectedType: NovelCardType,
	onSetType: (NovelCardType) -> Unit,
	onRefresh: () -> Unit,
	isEmpty: Boolean,
	drawerIcon: @Composable () -> Unit
) {
	@Composable
	fun title() {
		Text(stringResource(R.string.library))
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
				RemoveAllButton(onRemove)
				LibrarySelectedMoreButton(onMigrate, onTogglePin, onSetCategories)
			},
			navigationIcon = drawerIcon
		)
	} else {
		TopAppBar(
			title = { title() },
			scrollBehavior = behavior,
			actions = {
				AnimatedVisibility(!isEmpty) {
					Row {
						SearchAction(query, onSearch)
						ViewTypeButton(selectedType, onSetType)
						RefreshButton(onRefresh)
					}
				}
			},
			navigationIcon = drawerIcon
		)
	}
}


/**
 * Pager for categories
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LibraryPager(
	paddingValues: PaddingValues,
	library: LibraryUI,
	setActiveCategory: (Int) -> Unit,
	cardType: NovelCardType,
	columnsInV: Int,
	columnsInH: Int,
	hasSelected: Boolean,
	onRefresh: (Int) -> Unit,
	onOpen: (LibraryNovelUI) -> Unit,
	toggleSelection: (LibraryNovelUI) -> Unit,
	toastNovel: ((LibraryNovelUI) -> Unit)?
) {
	val scope = rememberCoroutineScope()
	val categoryPagerState = rememberPagerState { library.categories.size }
	LaunchedEffect(categoryPagerState.currentPage) {
		setActiveCategory(library.categories[categoryPagerState.currentPage].id)
	}

	Column(
		Modifier
			.padding(paddingValues)
			.fillMaxWidth()
	) {
		if (!(library.categories.size == 1 && library.categories.first().id == 0)) {
			ScrollableTabRow(
				selectedTabIndex = categoryPagerState.currentPage,
				indicator = { tabPositions ->
					TabRowDefaults.Indicator(
						Modifier.pagerTabIndicatorOffset(categoryPagerState, tabPositions)
					)
				},
				containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1F),
				edgePadding = 0.dp,
				contentColor = MaterialTheme.colorScheme.onPrimary,
				divider = {}
			) {
				library.categories.forEachIndexed { index, category ->
					Tab(
						text = { Text(category.name) },
						selected = categoryPagerState.currentPage == index,
						onClick = {
							scope.launch {
								categoryPagerState.animateScrollToPage(index)
							}
						},
					)
				}
			}
			Divider()
		}
		HorizontalPager(
			state = categoryPagerState,
			modifier = Modifier.fillMaxSize()
		) {
			val id by derivedStateOf {
				library.categories[it].id
			}
			val items by produceState(persistentListOf(), library, it, id) {
				value = onIO {
					library.novels[id] ?: persistentListOf()
				}
			}
			LibraryCategory(
				items = items,
				cardType = cardType,
				columnsInV = columnsInV,
				columnsInH = columnsInH,
				hasSelected = hasSelected,
				onRefresh = { onRefresh(id) },
				onOpen = onOpen,
				toggleSelection = toggleSelection,
				toastNovel = toastNovel,
			)
		}
	}
}

/**
 * A page of novels fitting in a category.
 *
 * Also is used for the default page.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun LibraryCategory(
	items: ImmutableList<LibraryNovelUI>,
	cardType: NovelCardType,
	columnsInV: Int,
	columnsInH: Int,
	hasSelected: Boolean,
	onRefresh: () -> Unit,
	onOpen: (LibraryNovelUI) -> Unit,
	toggleSelection: (LibraryNovelUI) -> Unit,
	toastNovel: ((LibraryNovelUI) -> Unit)?,
) {
	val (isRefreshing, pullRefreshState) = rememberFakePullRefreshState(onRefresh)
	Box(Modifier.pullRefresh(pullRefreshState)) {
		val w = LocalConfiguration.current.screenWidthDp
		val o = LocalConfiguration.current.orientation

		val size =
			(w / when (o) {
				Configuration.ORIENTATION_LANDSCAPE -> columnsInH
				else -> columnsInV
			}).dp - 16.dp


		val state = rememberLazyGridState()

		LazyVerticalGrid(
			modifier = Modifier.fillMaxSize(),
			columns = GridCells.Adaptive(if (cardType != COMPRESSED) size else 400.dp),
			contentPadding = PaddingValues(
				bottom = 300.dp,
				start = 8.dp,
				end = 8.dp,
				top = 4.dp
			),
			state = state,
			horizontalArrangement = Arrangement.spacedBy(4.dp),
			verticalArrangement = Arrangement.spacedBy(4.dp)
		) {
			fun onClick(item: LibraryNovelUI) {
				if (hasSelected)
					toggleSelection(item)
				else onOpen(item)
			}

			fun onLongClick(item: LibraryNovelUI) {
				if (!hasSelected)
					toggleSelection(item)
			}
			items(
				items,
				key = { it.id }
			) { item ->
				val onClickBadge = if (toastNovel != null) {
					{ toastNovel(item) }
				} else null

				@Composable
				fun badge() {
					if (item.unread > 0)
						Badge(
							modifier = Modifier.clickable(
								onClick = {
									onClickBadge?.invoke()
								}
							),
							containerColor = MaterialTheme.colorScheme.secondaryContainer
						) {
							Text(item.unread.toString())
						}
				}

				@Composable
				fun pin() {
					if (item.pinned)
						Badge(
							modifier = Modifier.clickable { },
							containerColor = MaterialTheme.colorScheme.secondaryContainer
						) {
							Icon(
								painterResource(R.drawable.ic_baseline_push_pin_24),
								stringResource(R.string.pin_on_top),
								modifier = Modifier.size(16.dp)
							)
						}
				}

				@Composable
				fun BoxScope.topBar() {
					Row(
						modifier = Modifier
							.align(Alignment.TopStart)
							.padding(4.dp),
						verticalAlignment = Alignment.CenterVertically,
						horizontalArrangement = Arrangement.spacedBy(4.dp)
					) {
						badge()
						pin()
					}
				}
				when (cardType) {
					NORMAL -> {
						NovelCardNormalContent(
							item.title,
							item.imageURL,
							onClick = {
								onClick(item)
							},
							onLongClick = {
								onLongClick(item)
							},
							overlay = {
								topBar()
							},
							isSelected = item.isSelected
						)
					}

					COMPRESSED -> {
						NovelCardCompressedContent(
							item.title,
							item.imageURL,
							onClick = {
								onClick(item)
							},
							onLongClick = {
								onLongClick(item)
							},
							overlay = {
								pin()
								badge()
							},
							isSelected = item.isSelected
						)
					}

					COZY -> {
						NovelCardCozyContent(
							item.title,
							item.imageURL,
							onClick = {
								onClick(item)
							},
							onLongClick = {
								onLongClick(item)
							},
							overlay = {
								topBar()
							},
							isSelected = item.isSelected
						)
					}
				}
			}
		}

		PullRefreshIndicator(isRefreshing, pullRefreshState, Modifier.align(Alignment.TopCenter))
	}
}