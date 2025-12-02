package app.shosetsu.android.viewmodel.impl

import android.webkit.CookieManager
import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.cachedIn
import app.shosetsu.android.common.SettingKey
import app.shosetsu.android.common.enums.NovelCardType
import app.shosetsu.android.common.ext.launchIO
import app.shosetsu.android.common.ext.logE
import app.shosetsu.android.common.ext.logI
import app.shosetsu.android.common.ext.logV
import app.shosetsu.android.domain.usecases.NovelBackgroundAddUseCase
import app.shosetsu.android.domain.usecases.SetNovelCategoriesUseCase
import app.shosetsu.android.domain.usecases.get.GetCatalogueListingDataUseCase
import app.shosetsu.android.domain.usecases.get.GetCatalogueQueryDataUseCase
import app.shosetsu.android.domain.usecases.get.GetCategoriesUseCase
import app.shosetsu.android.domain.usecases.get.GetExtensionUseCase
import app.shosetsu.android.domain.usecases.load.LoadNovelUIColumnsHUseCase
import app.shosetsu.android.domain.usecases.load.LoadNovelUIColumnsPUseCase
import app.shosetsu.android.domain.usecases.load.LoadNovelUITypeUseCase
import app.shosetsu.android.domain.usecases.settings.SetNovelUITypeUseCase
import app.shosetsu.android.view.uimodels.StableHolder
import app.shosetsu.android.view.uimodels.model.CategoryUI
import app.shosetsu.android.view.uimodels.model.catlog.ACatalogNovelUI
import app.shosetsu.android.viewmodel.abstracted.ACatalogViewModel
import app.shosetsu.lib.Filter
import app.shosetsu.lib.IExtension
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import java.util.concurrent.ConcurrentHashMap

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
 * shosetsu
 * 01 / 05 / 2020
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CatalogViewModel(
	private val getExtensionUseCase: GetExtensionUseCase,
	private val backgroundAddUseCase: NovelBackgroundAddUseCase,
	private val getCatalogueListingData: GetCatalogueListingDataUseCase,
	private val loadCatalogueQueryDataUseCase: GetCatalogueQueryDataUseCase,
	private val loadNovelUITypeUseCase: LoadNovelUITypeUseCase,
	private val loadNovelUIColumnsHUseCase: LoadNovelUIColumnsHUseCase,
	private val loadNovelUIColumnsPUseCase: LoadNovelUIColumnsPUseCase,
	private val setNovelUIType: SetNovelUITypeUseCase,
	private val getCategoriesUseCase: GetCategoriesUseCase,
	private val setNovelCategoriesUseCase: SetNovelCategoriesUseCase,
) : ACatalogViewModel() {
	override val queryFlow: MutableStateFlow<String> = MutableStateFlow("")

	override val exceptionFlow = MutableSharedFlow<Throwable>()

	init {
		launchIO {
			exceptionFlow.collect {
				this@CatalogViewModel.logE("Exception in CatalogViewModel", it)
			}
		}
	}

	/**
	 * Flow source for extension ID
	 */
	private val extensionIDFlow: MutableStateFlow<Int> = MutableStateFlow(-1)
	private val iExtensionFlow: StateFlow<IExtension?> by lazy {
		extensionIDFlow.mapLatest { extensionID -> getExtensionUseCase(extensionID) }
			.catch { exceptionFlow.emit(it) }
			.stateIn(viewModelScopeIO, SharingStarted.Lazily, null)
	}

	private val selectedListingLink = MutableStateFlow<String?>(null)
	override val selectedListing: StateFlow<IExtension.Listing?> = iExtensionFlow
		.combine(selectedListingLink, ::Pair)
		.mapLatest { (ext, link) -> ext?.getListing(link) }
		.stateIn(viewModelScopeIO, SharingStarted.Eagerly, null)

	override val listingOptions: StateFlow<ImmutableList<IExtension.Listing>> = selectedListing.mapLatest {
		when (it) {
			is IExtension.Listing.List -> it.getListings().toList().toImmutableList()
			else -> persistentListOf()
		}
	}
		.catch { exceptionFlow.emit(it) }
		.stateIn(viewModelScopeIO, SharingStarted.Lazily, persistentListOf())

	/**
	 * UnusedFlow warning suppressed, we are just calling the function to add them to the map.
	 */
	@Suppress("UnusedFlow")
	private fun List<Filter<*>>.init(map: ConcurrentHashMap<Int, MutableStateFlow<Any>>): ConcurrentHashMap<Int, MutableStateFlow<Any>> {
		forEach { filter ->
			when (filter) {
				is Filter.Password, is Filter.Text -> map[filter.id] = MutableStateFlow(filter.state)
				is Filter.Switch, is Filter.Checkbox -> map[filter.id] = MutableStateFlow(filter.state)
				is Filter.TriState, is Filter.Dropdown, is Filter.RadioGroup -> map[filter.id] = MutableStateFlow(filter.state)
				is Filter.FList -> filter.filters.init(map)
				is Filter.Group<*> -> filter.filters.init(map)
				is Filter.Header, is Filter.Separator -> {}
			}
		}
		return map
	}

	override val filterItemsLive: StateFlow<ImmutableList<StableHolder<Filter<*>>>> = selectedListing
		.mapLatest { listing -> listing?.search?.filters?.toList()?.map(::StableHolder) ?: emptyList() }
		.mapLatest { list -> list.toImmutableList() }
		.onIO()
		.stateIn(viewModelScopeIO, SharingStarted.Eagerly, persistentListOf())

	override val hasFilters: StateFlow<Boolean> by lazy {
		filterItemsLive.mapLatest { it.isNotEmpty() }
			.stateIn(viewModelScopeIO, SharingStarted.Lazily, false)
	}

	data class FilterApplied(val id: Int, val value: Boolean)
	private val filtersApplied: MutableStateFlow<FilterApplied> = MutableStateFlow(FilterApplied(0, false))

	/**
	 * Map of filter id to the state to pass into the extension
	 */
	private var filterDataState = filterItemsLive.mapLatest { it.map { it.item }.init(ConcurrentHashMap()) }
		.stateIn(viewModelScopeIO, SharingStarted.Eagerly, ConcurrentHashMap())
	private val filterDataFlow = filterItemsLive.mapLatest { hashMapOf<Int, Any>() }
		.stateIn(viewModelScopeIO, SharingStarted.Eagerly, hashMapOf())

	private val pagerFlow: Flow<Pager<Int, ACatalogNovelUI>?> by lazy { iExtensionFlow
			.combine(selectedListing, ::Pair)
			.transformLatest { (ext, listing) ->
				if (ext == null) {
					emit(null)
					return@transformLatest
				}
				emitAll(queryFlow
					.combine(filtersApplied, ::Pair)
					.flatMapLatest { (query, filtersApplied) ->
						if (listing == null) return@flatMapLatest flowOf(null)
						fun page(load: (Map<Int, Any>) -> PagingSource<Int, ACatalogNovelUI>) =
							filterDataFlow.mapLatest { data ->
								Pager(
									PagingConfig(10)
								) {
									load(data)
								}
							}
						if (query.isEmpty() && listing is IExtension.Listing.Item) {
                            page { data ->
								getCatalogueListingData(ext, data, listing)
							}
						} else {
							// Prevents always searching for list listings
							if (!filtersApplied.value && listing !is IExtension.Listing.Item) return@flatMapLatest flowOf(null)

							val search = listing.search ?: return@flatMapLatest flowOf(null)
							page { data ->
								loadCatalogueQueryDataUseCase(
									ext,
									query,
									data,
									search
								)
							}
						}
					}
				)
			}.onIO()
	}

	override val itemsLive: Flow<PagingData<ACatalogNovelUI>> by lazy {
		pagerFlow.combine(selectedListing, ::Pair).transformLatest { (pager, listing) ->
			if (pager != null)
				emitAll(pager.flow)
			else if (listing !is IExtension.Listing.Item) {
				emit(
					PagingData.empty(
						sourceLoadStates = LoadStates(
							LoadState.NotLoading(false),
							LoadState.NotLoading(false),
							LoadState.NotLoading(false)
						)
					)
				)
			} else {
				emit(PagingData.empty())
			}
		}
			.catch { exceptionFlow.emit(it) }
			.cachedIn(viewModelScope)
	}

	override val hasSearchLive: StateFlow<Boolean> by lazy {
		selectedListing.mapLatest { it?.search != null }
			.onIO()
			.stateIn(viewModelScopeIO, SharingStarted.Lazily, false)
	}

	override val extensionName: StateFlow<String> by lazy {
		iExtensionFlow.mapLatest { it?.name ?: "" }
			.onIO()
			.stateIn(viewModelScopeIO, SharingStarted.Lazily, "")
	}

//	/**
//	 * Listing selection data for the UI to render.
//	 */
//	override val listingSelectionData: StateFlow<ListingSelectionData?> by lazy {
//		extensionIDFlow.flatMapLatest { extensionID ->
//			val listingNames = getExtListNames(extensionID).toImmutableList()
//			getExtSelectedListingFlow(extensionID).mapLatest { selectedListing ->
//				ListingSelectionData(listingNames, selectedListing)
//			}
//				// Do not display the listing selection data if a query is being executed.
//				.combine(queryFlow) { listingSelectionData, query ->
//					if (query.isEmpty()) {
//						listingSelectionData
//					} else {
//						null
//					}
//				}
//		}.onIO()
//			.stateIn(viewModelScopeIO, SharingStarted.Lazily, null)
//	}

	override val baseURL: StateFlow<String?> =
		iExtensionFlow.map { it?.baseURL }
			.stateIn(viewModelScopeIO, SharingStarted.Lazily, null)

	override fun setListing(extensionID: Int, link: String?) {
		when {
			extensionIDFlow.value == -1 ->
				logI("Setting NovelID")

			extensionIDFlow.value != extensionID ->
				logI("NovelID not equal, resetting")

			extensionIDFlow.value == extensionID -> {
				logI("Ignore if the same")
				return
			}
		}
		extensionIDFlow.value = extensionID
		selectedListingLink.value = link
	}

	override fun applyQuery(newQuery: String) {
		queryFlow.value = newQuery
		applyFilter()
	}

	override fun resetView() {
		launchIO {
			resetFilterDataState()
			queryFlow.value = ""
            _applyFilter()
		}
	}

	private fun resetFilterDataState() {
		filterDataState.value.clear()
	}

	override fun backgroundNovelAdd(
		item: ACatalogNovelUI,
		categories: IntArray
	) {
		launchIO {
			logI("Adding novel to library in background: $item")
			if (item.bookmarked) {
				logI("Ignoring, already bookmarked: $item")
				return@launchIO
			}

			backgroundAddState.emit(BackgroundNovelAddProgress.Adding)
			try {
				backgroundAddUseCase(item.id)
				if (categories.isNotEmpty())
					setNovelCategoriesUseCase(item.id, categories)
			} catch (e: Exception) {
				backgroundAddState.emit(BackgroundNovelAddProgress.Failure(e))
				return@launchIO
			}
			backgroundAddState.emit(
				BackgroundNovelAddProgress.Added(
					item.title.let {
						if (it.length > 20)
							it.substring(0, 20) + "..."
						else it
					}
				))
			delay(100)
			backgroundAddState.emit(BackgroundNovelAddProgress.Unknown)
		}
	}

	override val backgroundAddState =
		MutableStateFlow<BackgroundNovelAddProgress>(BackgroundNovelAddProgress.Unknown)

	private val filterMutex = Mutex()

    /**
     * Locks the filter data flow mutex and sets the new value.
     */
    private fun _applyFilter() {
        if (filterMutex.tryLock()) {
            try {
                filterDataFlow.value.clear()
                filterDataFlow.value.putAll(filterDataState.value.mapValues { it.value.value })
            } finally {
                filterMutex.unlock()
            }
        }
    }

	override fun applyFilter() {
		launchIO {
            _applyFilter()
			filtersApplied.update { it.copy(it.id + 1, true) }
		}
	}

	override fun getFilterStringState(id: Filter<String>): Flow<String> =
		filterDataState.value.specialGetOrPut(id.id) {
			MutableStateFlow(id.state)
		}.onIO()

	override fun setFilterStringState(id: Filter<String>, value: String) {
		launchIO {
			filterDataState.value.specialGetOrPut(id.id) {
				MutableStateFlow(id.state)
			}.value = value
		}
	}

	override fun getFilterBooleanState(id: Filter<Boolean>): Flow<Boolean> =
		filterDataState.value.specialGetOrPut(id.id) {
			MutableStateFlow(id.state)
		}.onIO()

	override fun setFilterBooleanState(id: Filter<Boolean>, value: Boolean) {
		launchIO {
			filterDataState.value.specialGetOrPut(id.id) {
				MutableStateFlow(id.state)
			}.value = value
		}
	}

	override fun getFilterIntState(id: Filter<Int>): Flow<Int> =
		filterDataState.value.specialGetOrPut(id.id) {
			MutableStateFlow(id.state)
		}.onIO()

	override fun setFilterIntState(id: Filter<Int>, value: Int) {
		launchIO {
			filterDataState.value.specialGetOrPut(id.id) {
				MutableStateFlow(id.state)
			}.value = value
		}
	}

	override fun resetFilter() {
		launchIO {
			resetFilterDataState()
            _applyFilter()
			filtersApplied.update { it.copy(value = false) }
		}
	}

	override fun setViewType(cardType: NovelCardType) {
		launchIO { setNovelUIType(cardType) }
	}

	override val novelCardTypeLive: StateFlow<NovelCardType> by lazy {
		loadNovelUITypeUseCase().onIO()
			.stateIn(viewModelScopeIO, SharingStarted.Lazily, NovelCardType.NORMAL)
	}

	override val columnsInH: StateFlow<Int> by lazy {
		loadNovelUIColumnsHUseCase().onIO()
			.stateIn(
				viewModelScopeIO,
				SharingStarted.Lazily,
				SettingKey.ChapterColumnsInLandscape.default
			)
	}

	override val columnsInV: StateFlow<Int> by lazy {
		loadNovelUIColumnsPUseCase().onIO()
			.stateIn(
				viewModelScopeIO,
				SharingStarted.Lazily,
				SettingKey.ChapterColumnsInPortait.default
			)
	}

	override val categories: StateFlow<ImmutableList<CategoryUI>> by lazy {
		getCategoriesUseCase()
			.map { it.toImmutableList() }
			.stateIn(viewModelScopeIO, SharingStarted.Lazily, persistentListOf())
	}

	override fun destroy() {
		extensionIDFlow.value = -1
		resetView()
		System.gc()
	}

	/**
	 * @param [V] Value type of the hash map
	 * @param [O] Expected value type
	 */
	private inline fun <reified O, reified V> ConcurrentHashMap<Int, V>.specialGetOrPut(
		key: Int,
		getDefaultValue: () -> O
	): O {
		// Do not use computeIfAbsent on JVM8 as it would change locking behavior
		val value = this[key]
		return if (value is O) {
			value
		} else {
			val default = getDefaultValue()
			this[key] = default as V
			default
		}
	}

	override fun clearCookies() {
		CookieManager.getInstance().removeAllCookies {
			logV("Cookies cleared")
			resetView()
		}
	}

	override val isFilterMenuVisible = MutableStateFlow(false)

	override fun showFilterMenu() {
		isFilterMenuVisible.value = true
	}

	override fun hideFilterMenu() {
		isFilterMenuVisible.value = false
	}
}



