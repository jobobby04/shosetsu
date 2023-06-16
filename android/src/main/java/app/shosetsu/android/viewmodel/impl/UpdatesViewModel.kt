package app.shosetsu.android.viewmodel.impl

import android.annotation.SuppressLint
import androidx.lifecycle.viewModelScope
import app.shosetsu.android.common.enums.ReadingStatus
import app.shosetsu.android.common.ext.trimDate
import app.shosetsu.android.domain.repository.base.IUpdatesRepository
import app.shosetsu.android.domain.usecases.IsOnlineUseCase
import app.shosetsu.android.domain.usecases.start.StartUpdateWorkerUseCase
import app.shosetsu.android.view.uimodels.model.UpdatesUI
import app.shosetsu.android.viewmodel.abstracted.AUpdatesViewModel
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.joda.time.DateTime

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
 * 29 / 04 / 2020
 *
 * @author github.com/doomsdayrs
 */
@OptIn(ExperimentalCoroutinesApi::class)
class UpdatesViewModel(
	private val updatesRepository: IUpdatesRepository,
	private val startUpdateWorkerUseCase: StartUpdateWorkerUseCase,
	private val isOnlineUseCase: IsOnlineUseCase,
) : AUpdatesViewModel() {
	override val liveData: StateFlow<ImmutableMap<DateTime, List<UpdatesUI>>> by lazy {
		updatesRepository.getCompleteUpdatesFlow().transformLatest { list ->
			isRefreshing.value = true
			emit(
				list.map { (chapterID, novelID, time, chapterName, novelName, novelImageURL) ->
					UpdatesUI(
						chapterID = chapterID,
						novelID = novelID,
						time = time,
						chapterName = chapterName,
						novelName = novelName,
						novelImageURL = novelImageURL,
					)
				}
					.ifEmpty { emptyList() }
					.sortedByDescending { it.time }
					.groupBy {
						DateTime(it.time).trimDate()
					}.toImmutableMap()
			)
			isRefreshing.value = false
		}.onIO().stateIn(viewModelScopeIO, SharingStarted.Lazily, persistentMapOf())
	}

	override fun startUpdateManager(categoryID: Int) = startUpdateWorkerUseCase(categoryID)

	override val isOnlineFlow = isOnlineUseCase.getFlow()
		.stateIn(viewModelScopeIO, SharingStarted.Eagerly, false)

	override fun isOnline(): Boolean = isOnlineFlow.value

	override val isRefreshing: MutableStateFlow<Boolean> = MutableStateFlow(false)

	@SuppressLint("StopShip")
	override suspend fun updateChapter(
		updateUI: UpdatesUI,
		readingStatus: ReadingStatus
	) {
		@Suppress("TodoComment")
		TODO("Not yet implemented")
	}

	override fun clearAll() {
		viewModelScope.launch {
			updatesRepository.clearAll()
		}
	}

	override fun clearBefore(date: Long) {
		viewModelScope.launch {
			updatesRepository.clearBefore(date)
		}
	}
}