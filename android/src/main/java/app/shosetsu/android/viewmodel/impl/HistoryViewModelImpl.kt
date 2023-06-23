package app.shosetsu.android.viewmodel.impl

import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import app.shosetsu.android.domain.repository.base.ChapterHistoryRepository
import app.shosetsu.android.domain.repository.base.IChaptersRepository
import app.shosetsu.android.domain.repository.base.INovelsRepository
import app.shosetsu.android.view.uimodels.model.ChapterHistoryUI
import app.shosetsu.android.viewmodel.abstracted.HistoryViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

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
 * Shosetsu
 *
 * @since 12 / 02 / 2023
 * @author Doomsdayrs
 */
class HistoryViewModelImpl(
	private val historyRepo: ChapterHistoryRepository,
	private val chapterRepo: IChaptersRepository,
	private val novelRepo: INovelsRepository
) : HistoryViewModel() {
	/**
	 * History items, generated from combining data from 3 repositories
	 */
	override val items: Flow<PagingData<ChapterHistoryUI>> =
		Pager(PagingConfig(10)) {
			historyRepo.getHistory()
		}.flow.map { list ->
			list.map { (id, novelId, chapterId, startedReadingAt, endedReadingAt) ->
				val novel = novelRepo.getNovel(novelId)!!
				val chapter = chapterRepo.getChapter(chapterId)!!
				ChapterHistoryUI(
					id = id!!,
					novelId = novelId,
					novelTitle = novel.title,
					novelImageURL = novel.imageURL,
					chapterId = chapterId,
					chapterTitle = chapter.title,
					startedReadingAt = startedReadingAt,
					endedReadingAt = endedReadingAt,
				)
			}
		}

	override val isClearBeforeDialogShown = MutableStateFlow<Boolean>(false)

	override fun showClearBeforeDialog() {
		isClearBeforeDialogShown.tryEmit(true)
	}

	override fun hideClearBeforeDialog() {
		isClearBeforeDialogShown.tryEmit(false)
	}

	override fun clearAll() {
		viewModelScope.launch {
			historyRepo.clearAll()
		}
	}

	override fun clearBefore(date: Long) {
		viewModelScope.launch {
			historyRepo.clearBefore(date)
		}
	}
}