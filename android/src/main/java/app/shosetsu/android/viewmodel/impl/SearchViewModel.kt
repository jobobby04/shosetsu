package app.shosetsu.android.viewmodel.impl

import android.app.Application
import android.database.sqlite.SQLiteException
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.paging.map
import app.shosetsu.android.R
import app.shosetsu.android.common.IncompatibleExtensionException
import app.shosetsu.android.common.MissingExtensionException
import app.shosetsu.android.common.enums.NovelCardType
import app.shosetsu.android.common.ext.generify
import app.shosetsu.android.common.ext.launchIO
import app.shosetsu.android.common.ext.logE
import app.shosetsu.android.common.ext.logI
import app.shosetsu.android.domain.model.local.InstalledExtensionEntity
import app.shosetsu.android.domain.repository.base.IExtensionEntitiesRepository
import app.shosetsu.android.domain.repository.base.IExtensionsRepository
import app.shosetsu.android.domain.usecases.SearchBookMarkedNovelsUseCase
import app.shosetsu.android.domain.usecases.get.GetCatalogueQueryDataUseCase
import app.shosetsu.android.domain.usecases.get.GetExtensionUseCase
import app.shosetsu.android.domain.usecases.load.LoadNovelUITypeUseCase
import app.shosetsu.android.view.uimodels.model.catlog.ACatalogNovelUI
import app.shosetsu.android.view.uimodels.model.search.SearchRowUI
import app.shosetsu.android.viewmodel.abstracted.ASearchViewModel
import app.shosetsu.lib.Novel
import app.shosetsu.lib.exceptions.MissingOrInvalidKeysException
import app.shosetsu.lib.mapify
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerializationException
import org.acra.ACRA

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
	private val context: Application,
	private val searchBookMarkedNovelsUseCase: SearchBookMarkedNovelsUseCase,
	private val loadNovelUITypeUseCase: LoadNovelUITypeUseCase,
	private val iExtensionsRepository: IExtensionsRepository,
	private val extEntitiesRepo: IExtensionEntitiesRepository,
	private val loadCatalogueQueryDataUseCase: GetCatalogueQueryDataUseCase,
	private val getExtensionUseCase: GetExtensionUseCase,
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
	override val query: MutableStateFlow<String> = MutableStateFlow("")
	override val exceptions: MutableSharedFlow<String> = MutableSharedFlow()

	private val searchFlows =
		HashMap<Pair<Int, String?>, Flow<PagingData<ACatalogNovelUI>>>()

	private val refreshFlows =
		HashMap<Int, MutableSharedFlow<Unit>>()

	private val exceptionFlows =
		HashMap<Int, MutableStateFlow<Throwable?>>()

	@OptIn(ExperimentalCoroutinesApi::class)
	private val searchRows: Flow<List<SearchRowUI>> = iExtensionsRepository.loadExtensionsFLow()
		.transformLatest { result ->
			emit(
				result.let { list ->
					val arrayList = arrayListOf<InstalledExtensionEntity>()
					list.forEach { extension ->
						try {
							extEntitiesRepo.get(extension.generify()).let { entity ->
								if (entity.getListing(null).search != null) {
									arrayList.add(extension)
								}
							}
						} catch (e: SerializationException) {
							logE("Broken extension, ignoring", e)
							exceptions.emit(
								context.getString(
									R.string.search_error_ext_broken,
									extension.name,
									extension.id,
								),
							)
						} catch (e: IncompatibleExtensionException) {
							logE("Incompatible extension, ignoring", e)
							exceptions.emit(
								context.getString(
									R.string.search_error_ext_incompatible,
									extension.name,
									extension.id,
								),
							)
						} catch (e: MissingOrInvalidKeysException) {
							logE("Extension is missing keys, ignoring", e)
							exceptions.emit(
								context.getString(
									R.string.search_error_ext_incomplete,
									extension.name,
									extension.id,
								),
							)
						} catch (e: Exception) {
							logE("Unhandled exception, reporting!")
							exceptions.emit(
								context.getString(
									R.string.search_error_ext_generic,
									extension.name,
									extension.id,
								),
							)
							ACRA.errorReporter.handleSilentException(e)
						}
					}
					arrayList.map { (id, _, name, _, imageURL, _, _, _, _, _, _) ->
						SearchRowUI(id, name, imageURL)
					}
				},
			)
		}
		.mapLatest { list ->
			ArrayList(list).apply {
				add(0, SearchRowUI(-1, "My Library", ""))
				sortBy { (_, name, _, _) -> name }
			}
		}

	@OptIn(ExperimentalCoroutinesApi::class)
	override val listings: StateFlow<ImmutableList<SearchRowUI>> by lazy {
		searchRows.flatMapLatest { ogList ->
			combine(
				ogList.map { rowUI ->
					getExceptionFlow(rowUI.extensionID).map {
						if (it != null) {
							rowUI.copy(hasError = true)
						} else {
							rowUI
						}
					}
				},
			) {
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
			if (string != null && query.value.isEmpty()) {
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

	override fun searchExtension(
		extensionId: Int,
		listing: String?,
	): Flow<PagingData<ACatalogNovelUI>> = searchFlows.getOrPut(extensionId to listing) {
		loadExtension(extensionId, listing).cachedIn(viewModelScope)
	}

	override fun getException(id: Int): Flow<Throwable?> = getExceptionFlow(id)

	override fun refresh() {
		launchIO {
			refreshFlows.values.forEach {
				it.emit(Unit)
			}
		}
	}

	override fun refresh(id: Int) {
		logI("$id")
		launchIO {
			getRefreshFlow(id).emit(Unit)
		}
	}

	private fun getRefreshFlow(id: Int) = refreshFlows.getOrPut(id) {
		MutableSharedFlow<Unit>(replay = 1).apply {
			viewModelScopeIO.launch { emit(Unit) }
		}
	}

	private fun getExceptionFlow(id: Int) = exceptionFlows.getOrPut(id) {
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
							PagingConfig(10),
						) {
							searchBookMarkedNovelsUseCase(query)
						}.flow.map { data ->
							val ids = HashSet<Int>()
							data.filter { ids.add(it.id) }
								.map { (id, title, imageURL) ->
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
										favoriteCount = null,
									)
								}
						},
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
	private fun loadExtension(extensionID: Int, listing: String?): Flow<PagingData<ACatalogNovelUI>> =
		flow {
			val ext = getExtensionUseCase(extensionID)!!
			val exceptionFlow = getExceptionFlow(extensionID)

			emitAll(
				appliedQueryFlow.combine(getRefreshFlow(extensionID)) { query, _ -> query }
					.filterNotNull()
					.transformLatest { query ->
						exceptionFlow.value = null

						emitAll(
							Pager(
								PagingConfig(10),
							) {
								runBlocking {
									val listing = ext.getListing(listing).search
										?: throw MissingExtensionException(extensionID)
									loadCatalogueQueryDataUseCase(
										ext,
										query,
										listing.filters.toList().mapify(),
										listing,
									)
								}
							}.flow,
						)
					}.catch {
						exceptionFlow.value = it
					},
			)
		}.onIO()
}
