package app.shosetsu.android.ui.catalogue

import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import app.shosetsu.android.R
import app.shosetsu.android.common.consts.BundleKeys
import app.shosetsu.android.common.consts.BundleKeys.BUNDLE_EXTENSION
import app.shosetsu.android.common.enums.NovelCardType
import app.shosetsu.android.common.enums.NovelCardType.*
import app.shosetsu.android.common.ext.*
import app.shosetsu.android.ui.catalogue.listeners.CatalogueSearchQuery
import app.shosetsu.android.ui.novel.CategoriesDialog
import app.shosetsu.android.view.BottomSheetDialog
import app.shosetsu.android.view.compose.*
import app.shosetsu.android.view.controller.ShosetsuFragment
import app.shosetsu.android.view.controller.base.ExtendedFABController
import app.shosetsu.android.view.controller.base.ExtendedFABController.EFabMaintainer
import app.shosetsu.android.view.controller.base.syncFABWithCompose
import app.shosetsu.android.view.uimodels.model.catlog.ACatalogNovelUI
import app.shosetsu.android.viewmodel.abstracted.ACatalogViewModel
import app.shosetsu.android.viewmodel.abstracted.ACatalogViewModel.BackgroundNovelAddProgress
import app.shosetsu.android.viewmodel.abstracted.ACatalogViewModel.BackgroundNovelAddProgress.Added
import app.shosetsu.android.viewmodel.abstracted.ACatalogViewModel.BackgroundNovelAddProgress.Adding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Job
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
class CatalogFragment : ShosetsuFragment(), ExtendedFABController, MenuProvider {
	private var bsg: BottomSheetDialog? = null

	/***/
	val viewModel: ACatalogViewModel by viewModel()
	//private val progressAdapter by lazy { ItemAdapter<ProgressItem>() }

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedViewState: Bundle?
	): View {
		activity?.addMenuProvider(this, viewLifecycleOwner)
		setViewTitle()
		return ComposeView {
			CatalogueView(
				viewModel = viewModel,
				fab,
				onOpenNovel = {
					try {
						findNavController().navigateSafely(
							R.id.action_catalogController_to_novelController, bundleOf(
								BundleKeys.BUNDLE_NOVEL_ID to it.id,
								BUNDLE_EXTENSION to requireArguments().getInt(
									BUNDLE_EXTENSION
								)
							),
							navOptions {
								setShosetsuTransition()
							}
						)
					} catch (ignored: Exception) {
						// ignore dup
					}
				},
				errorMessage = { message, action ->
					makeSnackBar(message)
						?.setAction(R.string.retry) {
							action()
						}
						?.show()
				},
				openInWebView = ::openInWebView,
				makeSnackBar = { res, arg ->
					makeSnackBar(
						if (arg != null) {
							getString(res, arg)
						} else {
							getString(res)
						}
					)
				}
			)
		}
	}


	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		viewModel.setExtensionID(requireArguments().getInt(BUNDLE_EXTENSION))
		setupObservers()
	}

	override fun onDestroy() {
		super.onDestroy()
		viewModel.destroy()
	}

	/***/
	override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
		menu.clear()
		inflater.inflate(R.menu.toolbar_catalogue, menu)
	}

	private var optionSyncJob: Job? = null

	override fun onPrepareMenu(menu: Menu) {
		logI("Preparing option menu")
		optionSyncJob?.cancel()
		optionSyncJob =
			viewModel.novelCardTypeLive.collectLA(this@CatalogFragment, catch = {}) {
				when (it) {
					NORMAL -> {
						menu.findItem(R.id.view_type_normal)?.isChecked = true
					}

					COMPRESSED -> {
						menu.findItem(R.id.view_type_comp)?.isChecked = true
					}

					COZY -> menu.findItem(R.id.view_type_cozy)?.isChecked = true

					EXTENDED -> menu.findItem(R.id.view_type_extended)?.isChecked = true
				}
			}

		menu.findItem(R.id.search_item)?.let { searchItem ->
			viewModel.hasSearchLive.collectLA(this, catch = {}) {
				if (!it) {
					logV("Hiding search icon")
					menu.removeItem(R.id.search_item)
				} else {
					logV("Showing search icon")
					(searchItem.actionView as SearchView).apply {
						setOnQueryTextListener(CatalogueSearchQuery(this@CatalogFragment))
						setOnCloseListener {
							logV("closing search view")
							viewModel.applyQuery("")
							viewModel.resetView()
							true
						}
					}
				}
			}

		}
	}

	override fun onMenuItemSelected(item: MenuItem): Boolean =
		when (item.itemId) {
			R.id.view_type_normal -> {
				item.isChecked = true
				viewModel.setViewType(NORMAL)
				true
			}

			R.id.view_type_comp -> {
				item.isChecked = true
				viewModel.setViewType(COMPRESSED)
				true
			}

			R.id.view_type_cozy -> {
				item.isChecked = true
				viewModel.setViewType(COZY)
				true
			}

			R.id.view_type_extended -> {
				item.isChecked = true
				viewModel.setViewType(EXTENDED)
				true
			}

			R.id.web_view -> {
				openInWebView()
				true
			}

			else -> false
		}

	private fun openInWebView() {
		viewModel.getBaseURL().firstLa(
			this,
			catch = {
				makeSnackBar(
					getString(
						R.string.fragment_catalogue_error_base_url,
						it.message ?: "Unknown exception"
					)
				)?.setAction(R.string.report) { _ ->
					ACRA.errorReporter.handleSilentException(it)
				}?.show()
			}
		) {
			activity?.openInWebView(it)
		}
	}

	private fun setupObservers() {
		setViewTitle(getString(R.string.loading))
		viewModel.extensionName.observe(catch = {
			makeSnackBar(
				getString(
					R.string.fragment_catalogue_error_name,
					it.message ?: "Unknown exception"
				)
			)?.setAction(R.string.report) { _ ->
				ACRA.errorReporter.handleSilentException(it)
			}?.show()
		}) {
			setViewTitle(it)
		}

		viewModel.hasSearchLive.observe(catch = {
			makeSnackBar(
				getString(
					R.string.fragment_catalogue_error_has_search,
					it.message ?: "Unknown exception"
				)
			)?.setAction(R.string.report) { _ ->
				ACRA.errorReporter.handleSilentException(it)
			}?.show()
		}) {
			activity?.invalidateOptionsMenu()
		}
	}

	private lateinit var fab: EFabMaintainer
	override fun manipulateFAB(fab: EFabMaintainer) {
		this.fab = fab
		fab.setIconResource(R.drawable.filter)
		fab.setText(R.string.filter)
		fab.setOnClickListener {
			viewModel.showFilterMenu()
		}
		viewModel.hasFilters.collectLatestLA(this, catch = {}) {
			if (it)
				fab.show()
			else {
				fab.hide()
			}
		}
	}
}

@Composable
fun CatalogueView(
	viewModel: ACatalogViewModel = viewModelDi(),
	fab: EFabMaintainer,
	onOpenNovel: (ACatalogNovelUI) -> Unit,
	errorMessage: (String, () -> Unit) -> Unit,
	openInWebView: () -> Unit,
	makeSnackBar: (Int, String?) -> Snackbar?
) {
	ShosetsuCompose {
		val type by viewModel.novelCardTypeLive.collectAsState()

		val columnsInV by viewModel.columnsInV.collectAsState()
		val columnsInH by viewModel.columnsInH.collectAsState()

		val items = viewModel.itemsLive.collectAsLazyPagingItems()

		val exception by viewModel.exceptionFlow.collectAsState()
		val hasFilters by viewModel.hasFilters.collectAsState()

		val categories by viewModel.categories.collectAsState()
		var categoriesDialogItem by remember { mutableStateOf<ACatalogNovelUI?>(null) }

		val backgroundAddState by viewModel.backgroundAddState.collectAsState()
		val isFilterMenuVisible by viewModel.isFilterMenuVisible.collectAsState()

		LaunchedEffect(backgroundAddState) {
			when (backgroundAddState) {
				is Added -> {
					makeSnackBar(
						R.string.fragment_catalogue_toast_background_add_success,
						(backgroundAddState as Added).title
					)?.show()
				}

				Adding -> {
					makeSnackBar(R.string.fragment_catalogue_toast_background_add, null)?.show()
				}

				is BackgroundNovelAddProgress.Failure -> {
					val error = (backgroundAddState as BackgroundNovelAddProgress.Failure).error
					makeSnackBar(
						R.string.fragment_catalogue_toast_background_add_fail,
						error.message
							?: "Unknown exception"
					)?.setAction(R.string.report) { _ ->
						ACRA.errorReporter.handleSilentException(error)
					}?.show()
				}

				BackgroundNovelAddProgress.Unknown -> {
				}
			}
		}

		if (exception != null)
			LaunchedEffect(Unit) {
				launchUI {
					errorMessage(exception!!.message ?: "Unknown error") {
						viewModel.resetView()
					}
				}
			}

		val prepend = items.loadState.prepend
		if (prepend is LoadState.Error) {
			LaunchedEffect(Unit) {
				launchUI {
					errorMessage(prepend.error.message ?: "Unknown error") {
						items.retry()
					}
				}
			}
		}
		val append = items.loadState.prepend
		if (append is LoadState.Error) {
			LaunchedEffect(Unit) {
				launchUI {
					errorMessage(append.error.message ?: "Unknown error") {
						items.retry()
					}
				}
			}
		}

		CatalogContent(
			items,
			type,
			columnsInV,
			columnsInH,
			onClick = onOpenNovel,
			onLongClick = {
				if (categories.isNotEmpty() && !it.bookmarked) {
					categoriesDialogItem = it
				} else {
					viewModel.backgroundNovelAdd(it)
				}
			},
			hasFilters = hasFilters,
			fab,
			openWebView = openInWebView,
			clearCookies = {
				viewModel.clearCookies()
				items.refresh()
			}
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
			val items by viewModel.filterItemsLive.collectAsState()
			BottomSheetDialog(viewModel::hideFilterMenu) {
				CatalogFilterMenu(
					items = items,
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CatalogContent(
	items: LazyPagingItems<ACatalogNovelUI>,
	cardType: NovelCardType,
	columnsInV: Int,
	columnsInH: Int,
	onClick: (ACatalogNovelUI) -> Unit,
	onLongClick: (ACatalogNovelUI) -> Unit,
	hasFilters: Boolean,
	fab: EFabMaintainer,
	clearCookies: () -> Unit,
	openWebView: () -> Unit
) {
	Box(
		modifier = Modifier.fillMaxSize(),
	) {
		val pullRefreshState = rememberPullRefreshState(
			items.loadState.refresh == LoadState.Loading,
			onRefresh = { items.refresh() }
		)

		Box(Modifier.pullRefresh(pullRefreshState)) {
			val w = LocalConfiguration.current.screenWidthDp
			val o = LocalConfiguration.current.orientation

			val size =
				(w / when (o) {
					Configuration.ORIENTATION_LANDSCAPE -> columnsInH
					else -> columnsInV
				}).dp - 8.dp

			val state = rememberLazyGridState()
			if (hasFilters)
				syncFABWithCompose(state, fab)

			if (cardType == EXTENDED) {
				LazyColumn(
					verticalArrangement = Arrangement.spacedBy(4.dp)
				) {
					itemsIndexed(
						items,
						key = { index, item -> item.hashCode() + index }
					) { _, item ->
						if (item != null)
							Surface(tonalElevation = 2.dp) {
								NovelCardExtendedContent(
									item,
									onClick = {
										onClick(item)
									},
									onLongClick = {
										onLongClick(item)
									},
								)
							}

					}

					if (items.loadState.append == LoadState.Loading) {
						item {
							LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
						}
					}
					if (items.loadState.refresh.endOfPaginationReached && items.loadState.append.endOfPaginationReached) {
						item {
							CatalogContentNoMore()
						}
					}
					val errorState = items.loadState.refresh
					if (errorState is LoadState.Error) {
						item {
							ErrorContent(
								errorState.error.message ?: "Unknown",
								actions = arrayOf(
									ErrorAction(R.string.retry) {
										items.refresh()
									},
									ErrorAction(R.string.action_open_in_webview) {
										openWebView()
									},
									ErrorAction(R.string.settings_advanced_clear_cookies_title) {
										clearCookies()
									},
								),
								stackTrace = errorState.error.stackTraceToString()
							)
						}
					}
				}
			} else {
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
							NORMAL -> {
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

							COMPRESSED -> {
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

							COZY -> {
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
							EXTENDED -> Unit
						}
					}
					if (items.loadState.append == LoadState.Loading) {
						item(span = { GridItemSpan(maxLineSpan) }) {
							LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
						}
					}
					if (items.loadState.refresh.endOfPaginationReached && items.loadState.append.endOfPaginationReached) {
						item(span = { GridItemSpan(maxLineSpan) }) {
							CatalogContentNoMore()
						}
					}
					val errorState = items.loadState.refresh
					if (errorState is LoadState.Error) {
						item(span = { GridItemSpan(maxLineSpan) }) {
							ErrorContent(
								errorState.error.message ?: "Unknown",
								actions = arrayOf(
									ErrorAction(R.string.retry) {
										items.refresh()
									},
									ErrorAction(R.string.action_open_in_webview) {
										openWebView()
									},
									ErrorAction(R.string.settings_advanced_clear_cookies_title) {
										clearCookies()
									},
								),
								stackTrace = errorState.error.stackTraceToString()
							)
						}
					}
				}
			}
		}

		if (items.loadState.refresh == LoadState.Loading)
			LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
	}
}


@Preview
@Composable
fun PreviewCatalogContentNoMore() {
	ShosetsuCompose {
		CatalogContentNoMore()
	}
}

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