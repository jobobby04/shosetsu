package app.shosetsu.android.viewmodel.impl

import android.database.sqlite.SQLiteException
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import app.shosetsu.android.common.enums.NovelCardType
import app.shosetsu.android.common.ext.launchIO
import app.shosetsu.android.common.ext.logI
import app.shosetsu.android.domain.usecases.SearchBookMarkedNovelsUseCase
import app.shosetsu.android.domain.usecases.get.GetCatalogueQueryDataUseCase
import app.shosetsu.android.domain.usecases.get.GetExtensionUseCase
import app.shosetsu.android.domain.usecases.load.LoadNovelUITypeUseCase
import app.shosetsu.android.domain.usecases.load.LoadSearchRowUIUseCase
import app.shosetsu.android.view.uimodels.model.catlog.ACatalogNovelUI
import app.shosetsu.android.view.uimodels.model.search.SearchRowUI
import app.shosetsu.android.viewmodel.abstracted.ASearchViewModel
import app.shosetsu.lib.Novel
import app.shosetsu.lib.PAGE_INDEX
import app.shosetsu.lib.mapify
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking

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
class SearchViewModel(
	private val searchBookMarkedNovelsUseCase: SearchBookMarkedNovelsUseCase,
	private val loadNovelUITypeUseCase: LoadNovelUITypeUseCase,
	private val loadSearchRowUIUseCase: LoadSearchRowUIUseCase,
	private val loadCatalogueQueryDataUseCase: GetCatalogueQueryDataUseCase,
	private val getExtensionUseCase: GetExtensionUseCase
) : ASearchViewModel() {

	/**
	 * Holds the applied query
	 *
	 * Applied means it is used for data retrieval
	 */
	private val appliedQueryFlow: MutableStateFlow<String?> = MutableStateFlow(null)

	/**
	 * Holds the current query
	 *
	 * Used to save user input
	 */
	override val query: MutableStateFlow<String?> = MutableStateFlow(null)

	private val searchFlows =
		HashMap<Int, Flow<PagingData<ACatalogNovelUI>>>()

	private val refreshFlows =
		HashMap<Int, MutableStateFlow<Int>>()

	private val exceptionFlows =
		HashMap<Int, MutableStateFlow<Throwable?>>()

	@OptIn(ExperimentalCoroutinesApi::class)
	override val listings: StateFlow<ImmutableList<SearchRowUI>> by lazy {
		loadSearchRowUIUseCase().flatMapLatest { ogList ->
			combine(ogList.map { rowUI ->
				getExceptionFlow(rowUI.extensionID).map {
					if (it != null)
						rowUI.copy(hasError = true)
					else rowUI
				}
			}) {
				it.toList()
			}
		}.map { list ->
			list.sortedBy { it.name }
				.sortedBy { it.extensionID != -1 }
				.sortedBy { it.hasError }
				.toImmutableList()
		}.onIO()
			.stateIn(viewModelScopeIO, SharingStarted.Lazily, persistentListOf())
	}

	override val isCozy: StateFlow<Boolean> by lazy {
		loadNovelUITypeUseCase().map { it == NovelCardType.COZY }.onIO()
			.stateIn(viewModelScopeIO, SharingStarted.Lazily, false)
	}

	override fun initQuery(string: String?) {
		launchIO {
			if (string != null && query.value == null) {
				query.value = string
				appliedQueryFlow.value = string
			}
		}
	}

	override fun setQuery(query: String) {
		this.query.value = query
	}

	override fun applyQuery(query: String) {
		this.query.value = query
		appliedQueryFlow.value = query
	}

	override fun searchLibrary(): Flow<PagingData<ACatalogNovelUI>> =
		libraryResultFlow.cachedIn(viewModelScope).onIO()

	override fun searchExtension(extensionId: Int): Flow<PagingData<ACatalogNovelUI>> =
		searchFlows.getOrPut(extensionId) {
			loadExtension(extensionId).cachedIn(viewModelScope)
		}

	override fun getException(id: Int): Flow<Throwable?> =
		getExceptionFlow(id)

	override fun refresh() {
		launchIO {
			refreshFlows.values.forEach {
				it.emit(it.value++)
			}
		}
	}

	override fun refresh(id: Int) {
		logI("$id")
		launchIO {
			val flow = getRefreshFlow(id)
			// todo ++ probably already sets the value
			flow.value = flow.value++
		}
	}

	private fun getRefreshFlow(id: Int) =
		refreshFlows.getOrPut(id) {
			MutableStateFlow(0)
		}

	private fun getExceptionFlow(id: Int) =
		exceptionFlows.getOrPut(id) {
			MutableStateFlow(null)
		}

	/**
	 * Creates a flow for a library query
	 */
	@OptIn(ExperimentalCoroutinesApi::class)
	private val libraryResultFlow: Flow<PagingData<ACatalogNovelUI>> by lazy {
		appliedQueryFlow.combine(getRefreshFlow(-1)) { query, _ -> query }
			.filterNotNull()
			.transformLatest { query ->
				val exceptionFlow = getExceptionFlow(-1)

				exceptionFlow.value = null

				try {
					emitAll(
						Pager(
							PagingConfig(10)
						) {
							searchBookMarkedNovelsUseCase(query)
						}.flow.map { data ->
							val ids = arrayListOf<Int>()
							data.filter {
								if (ids.contains(it.id)) {
									false
								} else {
									ids.add(it.id)
									true
								}
							}.map { (id, title, imageURL) ->
								ACatalogNovelUI(
									id = id,
									title = title,
									imageURL = imageURL,
									bookmarked = false,
									language = "",
									description = "",
									status = Novel.Status.UNKNOWN,
									tags = persistentListOf(),
									genres = persistentListOf(),
									authors = persistentListOf(),
									artists = persistentListOf(),
									chapters = persistentListOf(),
									chapterCount = null,
									wordCount = null,
									commentCount = null,
									viewCount = null,
									favoriteCount = null
								)
							}
						}
					)
				} catch (e: SQLiteException) {
					exceptionFlow.value = e
				}
			}
	}

	/**
	 * Creates a flow for an extension query
	 */
	@OptIn(ExperimentalCoroutinesApi::class)
	private fun loadExtension(extensionID: Int): Flow<PagingData<ACatalogNovelUI>> {
		return flow {
			val ext = getExtensionUseCase(extensionID)!!
			val exceptionFlow = getExceptionFlow(extensionID)

			emitAll(
				appliedQueryFlow.combine(getRefreshFlow(extensionID)) { query, _ -> query }
					.filterNotNull()
					.transformLatest { query ->
						exceptionFlow.value = null

						emitAll(
							Pager(
								PagingConfig(10)
							) {
								runBlocking {
									loadCatalogueQueryDataUseCase(
										extensionID,
										query,
										HashMap<Int, Any>().apply {
											putAll(ext.searchFiltersModel.toList().mapify())
											this[PAGE_INDEX] = ext.startIndex
										}
									)
								}
							}.flow
						)
					}.catch {
						exceptionFlow.value = it
					}
			)
		}.onIO()
	}
}