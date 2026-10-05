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
import app.shosetsu.android.common.ext.logI
import app.shosetsu.android.common.ext.logV
import app.shosetsu.android.domain.repository.base.ISettingsRepository
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
import app.shosetsu.lib.*
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
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
	private val settingsRepository: ISettingsRepository,
) : ACatalogViewModel() {
	override val queryFlow: MutableStateFlow<String> by lazy { MutableStateFlow("") }

	/**
	 * Map of filter id to the state to pass into the extension
	 */
	private val filterDataState: ConcurrentHashMap<Int, MutableStateFlow<Any>> = ConcurrentHashMap()

	private val filterDataFlow = MutableStateFlow<Pair<Boolean, Map<Int, Any>>>(false to emptyMap())

	/**
	 * Flow source for extension ID
	 */
	private val extensionIDFlow: MutableStateFlow<Int> = MutableStateFlow(-1)

	override val exceptionFlow = MutableSharedFlow<Throwable>()

	private val iExtensionFlow: StateFlow<Extension?> by lazy {
		extensionIDFlow.mapLatest { extensionID -> getExtensionUseCase(extensionID) }
			.catch { exceptionFlow.emit(it) }
			.stateIn(viewModelScopeIO, SharingStarted.Lazily, null)
	}

	private val selectedListingLink = MutableStateFlow<String?>(null)
	override val selectedListing: StateFlow<Extension.Listing?> = iExtensionFlow
		.combine(selectedListingLink, ::Pair)
		.mapLatest { (ext, link) -> ext?.getListing(link) }
		.stateIn(viewModelScopeIO, SharingStarted.Eagerly, null)

	override val listingOptions: StateFlow<ImmutableList<Extension.Listing>> =
		combine(selectedListing, queryFlow, filterDataFlow, ::Triple)
			.mapLatest { (listing, query, filters) ->
				if (listing == null || query.isNotEmpty() || filters.first) {
					return@mapLatest persistentListOf()
				}
				listing.listings?.get()?.toList().orEmpty().toImmutableList()
			}
			.catch { exceptionFlow.emit(it) }
			.stateIn(viewModelScopeIO, SharingStarted.Lazily, persistentListOf())

	/**
	 * UnusedFlow warning suppressed, we are just calling the function to add them to the map.
	 */
	@Suppress("UnusedFlow")
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

	override val filterItemsLive: StateFlow<ImmutableList<StableHolder<Filter<*>>>> = selectedListing
		.mapLatest { listing ->
			listing?.search?.filters
				?.toList()
				?.also { it.init() }
				?.map(::StableHolder)
				.orEmpty()
				.toImmutableList()
		}
		.onIO()
		.stateIn(viewModelScopeIO, SharingStarted.Eagerly, persistentListOf())

	override val hasFilters: StateFlow<Boolean> by lazy {
		filterItemsLive.mapLatest { it.isNotEmpty() }
			.stateIn(viewModelScopeIO, SharingStarted.Lazily, false)
	}
	private val pagerFlow: Flow<Pager<Int, ACatalogNovelUI>?> by lazy {
		combine(iExtensionFlow, selectedListing, ::Pair)
			.transformLatest { (ext, listing) ->
				if (ext == null || listing == null) {
					emit(null)
					return@transformLatest
				}
				emitAll(
					combine(queryFlow, filterDataFlow, ::Pair)
						.flatMapLatest { (query, filters) ->
							fun pageFlow(load: (Map<Int, Any>) -> PagingSource<Int, ACatalogNovelUI>) =
								filterDataFlow.mapLatest { data ->
									Pager(
										PagingConfig(10),
									) {
										load(data.second)
									}
								}
							if ((query.isNotEmpty() || filters.first) && listing.search != null) {
								pageFlow { data ->
									loadCatalogueQueryDataUseCase(
										ext,
										query,
										data,
										listing.search!!,
									)
								}
							} else if (listing.novels != null) {
								pageFlow { data ->
									getCatalogueListingData(
										ext,
										data,
										listing.novels!!,
									)
								}
							} else {
								flowOf(null)
							}
						},
				)
			}.onIO()
	}

	override val itemsLive: Flow<PagingData<ACatalogNovelUI>> by lazy {
		combine(pagerFlow, selectedListing, ::Pair).transformLatest { (pager, listing) ->
			if (pager != null) {
				emitAll(pager.flow)
			} else if (listing?.novels != null) {
				emit(
					PagingData.empty(
						sourceLoadStates = LoadStates(
							LoadState.NotLoading(false),
							LoadState.NotLoading(false),
							LoadState.NotLoading(false),
						),
					),
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
			.catch { exceptionFlow.emit(it) }
			.onIO()
			.stateIn(viewModelScopeIO, SharingStarted.Lazily, false)
	}

	override val extensionName: StateFlow<String> by lazy {
		iExtensionFlow.mapLatest { it?.name ?: "" }
			.catch { exceptionFlow.emit(it) }.onIO()
			.stateIn(viewModelScopeIO, SharingStarted.Lazily, "")
	}

	override val baseURL: StateFlow<String?> =
		iExtensionFlow.map { it?.baseURL }
			.catch { exceptionFlow.emit(it) }
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
			applyFilter()
		}
	}

	private fun resetFilter(filter: Filter<*>) {
		when (filter) {
			is Filter.Password -> setFilterStringStateInternal(filter, filter.state)
			is Filter.Text -> setFilterStringStateInternal(filter, filter.state)
			is Filter.Switch -> setFilterBooleanStateInternal(filter, filter.state)
			is Filter.Checkbox -> setFilterBooleanStateInternal(filter, filter.state)
			is Filter.TriState -> setFilterIntStateInternal(filter, filter.state)
			is Filter.Dropdown -> setFilterIntStateInternal(filter, filter.state)
			is Filter.RadioGroup -> setFilterIntStateInternal(filter, filter.state)
			is Filter.FList -> filter.filters.forEach { resetFilter(it) }
			is Filter.Group<*> -> filter.filters.forEach { resetFilter(it) }
			is Filter.Header -> {}
			Filter.Separator -> {}
		}
	}

	private fun resetFilterDataState() {
		filterItemsLive.value.forEach { filter -> resetFilter(filter.item) }
		filterDataFlow.value = false to filterDataState.toMap().mapValues { it.value.value }
	}

	override fun backgroundNovelAdd(item: ACatalogNovelUI, categories: IntArray) {
		launchIO {
			// fyi, the function handles exceptions
			_backgroundNovelAdd(item, categories)
		}
	}

	/**
	 * @see [ACatalogViewModel.backgroundNovelAdd]
	 */
	@Suppress("KDocMissingDocumentation", "FunctionName")
	private suspend fun _backgroundNovelAdd(item: ACatalogNovelUI, categories: IntArray) {
		try {
			logI("Adding novel to library in background: $item")
			if (item.bookmarked) {
				logI("Ignoring, already bookmarked: $item")
				return
			}

			// Notify that the novel is currently being added.
			backgroundAddState.emit(BackgroundNovelAddProgress.Adding)

			try {
				backgroundAddUseCase(item.id)
				if (categories.isNotEmpty()) {
					setNovelCategoriesUseCase(item.id, categories)
				}
			} catch (e: Exception) {
				backgroundAddState.emit(BackgroundNovelAddProgress.Failure(e))
				return
			}

			backgroundAddState.emit(
				BackgroundNovelAddProgress.Added(
					item.title.let {
						if (it.length > 20) {
							it.substring(0, 20) + "..."
						} else {
							it
						}
					},
				),
			)
			delay(100)
			backgroundAddState.emit(BackgroundNovelAddProgress.Unknown)
		} catch (e: Exception) {
			exceptionFlow.emit(e)
		}
	}

	override val backgroundAddState: MutableStateFlow<BackgroundNovelAddProgress> =
		MutableStateFlow(BackgroundNovelAddProgress.Unknown)

	private val filterMutex = Mutex()

	/**
	 * Locks the filter data flow mutex and sets the new value.
	 */
	private fun applyFilterInternal() {
		if (filterMutex.tryLock()) {
			try {
				filterDataFlow.value = true to filterDataState.toMap().mapValues { it.value.value }
			} finally {
				filterMutex.unlock()
			}
		}
	}

	override fun applyFilter() {
		launchIO {
			applyFilterInternal()
		}
	}

	override fun getFilterStringState(id: Filter<String>): Flow<String> =
		filterDataState.specialGetOrPut(id.id) {
			MutableStateFlow(id.state)
		}.onIO()

	private fun setFilterStringStateInternal(id: Filter<String>, value: String) {
		filterDataState.specialGetOrPut(id.id) {
			MutableStateFlow(id.state)
		}.value = value
	}

	override fun setFilterStringState(id: Filter<String>, value: String) {
		launchIO { setFilterStringStateInternal(id, value) }
	}

	override fun getFilterBooleanState(id: Filter<Boolean>): Flow<Boolean> =
		filterDataState.specialGetOrPut(id.id) {
			MutableStateFlow(id.state)
		}.onIO()

	private fun setFilterBooleanStateInternal(id: Filter<Boolean>, value: Boolean) {
		filterDataState.specialGetOrPut(id.id) {
			MutableStateFlow(id.state)
		}.value = value
	}

	override fun setFilterBooleanState(id: Filter<Boolean>, value: Boolean) {
		launchIO { setFilterBooleanStateInternal(id, value) }
	}

	override fun getFilterIntState(id: Filter<Int>): Flow<Int> =
		filterDataState.specialGetOrPut(id.id) {
			MutableStateFlow(id.state)
		}.onIO()

	private fun setFilterIntStateInternal(id: Filter<Int>, value: Int) {
		filterDataState.specialGetOrPut(id.id) {
			MutableStateFlow(id.state)
		}.value = value
	}

	override fun setFilterIntState(id: Filter<Int>, value: Int) {
		launchIO { setFilterIntStateInternal(id, value) }
	}

	override fun resetFilter() {
		launchIO {
			queryFlow.value = ""
			resetFilterDataState()
		}
	}

	override fun setViewType(cardType: NovelCardType) {
		launchIO { setNovelUIType(cardType) }
	}

	override val novelCardTypeLive: StateFlow<NovelCardType> by lazy {
		loadNovelUITypeUseCase().onIO()
			.stateIn(viewModelScopeIO, SharingStarted.Lazily, NovelCardType.NORMAL)
	}

	override val showImages: StateFlow<Boolean> =
		settingsRepository.getBooleanFlow(SettingKey.NoImages)
			.map { !it }
			.stateIn(
				viewModelScopeIO,
				SharingStarted.Lazily,
				true,
			)

	override val columnsInH: StateFlow<Int> by lazy {
		loadNovelUIColumnsHUseCase().onIO()
			.stateIn(
				viewModelScopeIO,
				SharingStarted.Lazily,
				SettingKey.ChapterColumnsInLandscape.default,
			)
	}

	override val columnsInV: StateFlow<Int> by lazy {
		loadNovelUIColumnsPUseCase().onIO()
			.stateIn(
				viewModelScopeIO,
				SharingStarted.Lazily,
				SettingKey.ChapterColumnsInPortait.default,
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
		getDefaultValue: () -> O,
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

	override val isFilterMenuVisible: MutableStateFlow<Boolean> = MutableStateFlow(false)

	override fun showFilterMenu() {
		isFilterMenuVisible.value = true
	}

	override fun hideFilterMenu() {
		isFilterMenuVisible.value = false
	}
}
