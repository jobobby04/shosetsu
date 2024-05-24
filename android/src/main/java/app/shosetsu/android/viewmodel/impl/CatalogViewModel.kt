package app.shosetsu.android.viewmodel.impl

import android.webkit.CookieManager
import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import app.shosetsu.android.common.SettingKey
import app.shosetsu.android.common.enums.NovelCardType
import app.shosetsu.android.common.ext.launchIO
import app.shosetsu.android.common.ext.logI
import app.shosetsu.android.common.ext.logV
import app.shosetsu.android.common.utils.copy
import app.shosetsu.android.domain.usecases.NovelBackgroundAddUseCase
import app.shosetsu.android.domain.usecases.SetNovelCategoriesUseCase
import app.shosetsu.android.domain.usecases.get.GetCatalogueListingDataUseCase
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
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
	private val loadNovelUITypeUseCase: LoadNovelUITypeUseCase,
	private val loadNovelUIColumnsHUseCase: LoadNovelUIColumnsHUseCase,
	private val loadNovelUIColumnsPUseCase: LoadNovelUIColumnsPUseCase,
	private val setNovelUIType: SetNovelUITypeUseCase,
	private val getCategoriesUseCase: GetCategoriesUseCase,
	private val setNovelCategoriesUseCase: SetNovelCategoriesUseCase
) : ACatalogViewModel() {
	override val queryFlow: MutableStateFlow<String> = MutableStateFlow("")
	private val filtersApplied: MutableStateFlow<Boolean> = MutableStateFlow(false)

	/**
	 * Map of filter id to the state to pass into the extension
	 */
	private var filterDataState: ConcurrentHashMap<Int, MutableStateFlow<Any>> = ConcurrentHashMap()

	private val filterDataFlow = MutableStateFlow<Map<Int, Any>>(hashMapOf())

	/**
	 * Flow source for extension ID
	 */
	private val extensionIDFlow: MutableStateFlow<Int> = MutableStateFlow(-1)

	override val exceptionFlow = MutableSharedFlow<Throwable>()

	override val selectedListing: MutableStateFlow<IExtension.Listing?> = MutableStateFlow(null)

	private val iExtensionFlow: StateFlow<IExtension?> by lazy {
		extensionIDFlow.mapLatest { extensionID ->
			val ext = getExtensionUseCase(extensionID)

			// Ensure listings are initialized
			selectedListing.value = ext?.listings()
			ext
		}.stateIn(viewModelScopeIO, SharingStarted.Lazily, null)
	}

	override val listingOptions = selectedListing.mapLatest {
		when (it) {
			is IExtension.Listing.List -> it.getListings().toList().toImmutableList()
			else -> persistentListOf()
		}
	}.catch {
		exceptionFlow.emit(it)
	}.stateIn(viewModelScopeIO, SharingStarted.Lazily, persistentListOf())

	private fun List<Filter<*>>.init() {
		forEach { filter ->
			when (filter) {
				is Filter.Password -> getFilterStringState(filter)
				is Filter.Text -> getFilterStringState(filter)
				is Filter.Switch -> getFilterBooleanState(filter)
				is Filter.Checkbox -> getFilterBooleanState(filter)
				is Filter.TriState -> getFilterIntState(filter)
				is Filter.Dropdown -> getFilterIntState(filter)
				is Filter.RadioGroup -> getFilterIntState(filter)
				is Filter.FList -> {
					filter.filters.init()
				}

				is Filter.Group<*> -> {
					filter.filters.init()
				}

				is Filter.Header -> {
				}

				is Filter.Separator -> {
				}
			}
		}
	}

	private val pagerFlow: Flow<Pager<Int, ACatalogNovelUI>?> by lazy {
		iExtensionFlow.combine(selectedListing) { ext, listing ->
			ext to listing
		}.transformLatest { (ext, listing) ->
			if (ext == null) {
				emit(null)
			} else {
				emitAll(
					queryFlow.combine(filtersApplied) { query, filtersApplied ->
						query to filtersApplied
					}.flatMapLatest { (query, filtersApplied) ->
						if (query.isEmpty() && !filtersApplied && listing !is IExtension.Listing.Item) {
							return@flatMapLatest flowOf(null)
						}
						filterDataFlow.mapLatest { data ->
							Pager(
								PagingConfig(10)
							) {
								getCatalogueListingData(ext, query, data, listing as? IExtension.Listing.Item)
							}
						}
					}
				)
			}
		}.onIO()
	}

	override val itemsLive: Flow<PagingData<ACatalogNovelUI>> by lazy {
		pagerFlow.combine(selectedListing) { pager, listing ->
			pager to listing
		}.transformLatest {(pager, listing) ->
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
		}.catch {
			exceptionFlow.emit(it)
		}.cachedIn(viewModelScope)
	}

	override val filterItemsLive: StateFlow<ImmutableList<StableHolder<Filter<*>>>> = iExtensionFlow.combine(
		selectedListing.map { (it as? IExtension.Listing.Item)?.link }.distinctUntilChanged()
	) { a, b -> a to b }
		.mapLatest { (extension, listing) ->
			extension?.getCatalogueFilters(listing)?.toList() ?: emptyList()
		}.mapLatest {
			filterDataState.clear() // Reset filter state so no data conflicts occur
			it.init()
			it.map { StableHolder(it) }.toImmutableList()
		}
		.onIO()
		.stateIn(viewModelScopeIO, SharingStarted.Eagerly, persistentListOf())

	override val hasFilters: StateFlow<Boolean> by lazy {
		filterItemsLive.mapLatest { it.isNotEmpty() }
			.stateIn(viewModelScopeIO, SharingStarted.Lazily, false)
	}

	override val hasSearchLive: StateFlow<Boolean> by lazy {
		iExtensionFlow.mapLatest { it?.hasSearch ?: false }
			.onIO()
			.stateIn(viewModelScopeIO, SharingStarted.Lazily, false)
	}

	override val extensionName: StateFlow<String> by lazy {
		iExtensionFlow.mapLatest { it?.name ?: "" }
			.onIO()
			.stateIn(viewModelScopeIO, SharingStarted.Lazily, "")
	}

	override val baseURL: StateFlow<String?> =
		iExtensionFlow.map { it?.baseURL }
			.stateIn(viewModelScopeIO, SharingStarted.Lazily, null)

	override fun setExtensionID(extensionID: Int) {
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
	}

	override fun setSelectedListing(listing: IExtension.Listing) {
		selectedListing.value = listing
	}

	override fun applyQuery(newQuery: String) {
		queryFlow.value = newQuery
		applyFilter()
	}

	override fun resetView() {
		launchIO {
			resetFilterDataState()
			queryFlow.value = ""
			applyFilters()
		}
	}

	private fun resetFilter(filter: Filter<*>) {
		when (filter) {
			is Filter.Password -> _setFilterStringState(filter, filter.state)
			is Filter.Text -> _setFilterStringState(filter, filter.state)
			is Filter.Switch -> _setFilterBooleanState(filter, filter.state)
			is Filter.Checkbox -> _setFilterBooleanState(filter, filter.state)
			is Filter.TriState -> _setFilterIntState(filter, filter.state)
			is Filter.Dropdown -> _setFilterIntState(filter, filter.state)
			is Filter.RadioGroup -> _setFilterIntState(filter, filter.state)
			is Filter.FList -> filter.filters.forEach { resetFilter(it) }
			is Filter.Group<*> -> filter.filters.forEach { resetFilter(it) }
			is Filter.Header -> {}
			Filter.Separator -> {}
		}
	}

	private fun resetFilterDataState() {
		filterItemsLive.value.forEach { filter -> resetFilter(filter.item) }
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
			backgroundAddState.emit(BackgroundNovelAddProgress.Added(
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
	override fun applyFilter() {
		launchIO {
			applyFilters()
			filtersApplied.value = true
		}
	}

	private fun applyFilters() {
		if (filterMutex.tryLock()) {
			try {
				filterDataFlow.value = filterDataState.copy().mapValues { it.value.value }
			} finally {
				filterMutex.unlock()
			}
		}
	}

	override fun getFilterStringState(id: Filter<String>): Flow<String> =
		filterDataState.specialGetOrPut(id.id) {
			MutableStateFlow(id.state)
		}.onIO()

	private fun _setFilterStringState(id: Filter<String>, value: String) {
		filterDataState.specialGetOrPut(id.id) {
			MutableStateFlow(id.state)
		}.value = value
	}


	override fun setFilterStringState(id: Filter<String>, value: String) {
		launchIO { _setFilterStringState(id, value) }
	}

	override fun getFilterBooleanState(id: Filter<Boolean>): Flow<Boolean> =
		filterDataState.specialGetOrPut(id.id) {
			MutableStateFlow(id.state)
		}.onIO()

	private fun _setFilterBooleanState(id: Filter<Boolean>, value: Boolean) {
		filterDataState.specialGetOrPut(id.id) {
			MutableStateFlow(id.state)
		}.value = value
	}

	override fun setFilterBooleanState(id: Filter<Boolean>, value: Boolean) {
		launchIO { _setFilterBooleanState(id, value) }
	}

	override fun getFilterIntState(id: Filter<Int>): Flow<Int> =
		filterDataState.specialGetOrPut(id.id) {
			MutableStateFlow(id.state)
		}.onIO()

	private fun _setFilterIntState(id: Filter<Int>, value: Int) {
		filterDataState.specialGetOrPut(id.id) {
			MutableStateFlow(id.state)
		}.value = value
	}

	override fun setFilterIntState(id: Filter<Int>, value: Int) {
		launchIO { _setFilterIntState(id, value) }
	}

	override fun resetFilter() {
		launchIO {
			resetFilterDataState()
			applyFilters()
			filtersApplied.value = false
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
	private inline fun <reified O, V> ConcurrentHashMap<Int, V>.specialGetOrPut(
		key: Int,
		getDefaultValue: () -> O
	): O {
		// Do not use computeIfAbsent on JVM8 as it would change locking behavior
		val value = this[key]
		return if (value is O) {
			value
		} else {
			val default = getDefaultValue()
			@Suppress("UNCHECKED_CAST") // Good luck to whoever reads this
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



