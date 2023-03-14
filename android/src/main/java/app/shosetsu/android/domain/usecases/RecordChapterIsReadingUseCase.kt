package app.shosetsu.android.domain.usecases

import android.database.sqlite.SQLiteException
import app.shosetsu.android.domain.model.local.ChapterEntity
import app.shosetsu.android.domain.model.local.ReaderChapterEntity
import app.shosetsu.android.domain.repository.base.ChapterHistoryRepository
import app.shosetsu.android.domain.repository.base.IChaptersRepository
import app.shosetsu.android.view.uimodels.model.ChapterUI
import app.shosetsu.android.view.uimodels.model.reader.ReaderUIItem.ReaderChapterUI

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
 * @since 11 / 11 / 2021
 * @author Doomsdayrs
 */
class RecordChapterIsReadingUseCase(
	private val iChapterHistoryRepository: ChapterHistoryRepository,
	private val iChapterRepository: IChaptersRepository
) {
	suspend operator fun invoke(chapter: ChapterEntity) {
		iChapterHistoryRepository.markChapterAsReading(chapter)
	}

	@Throws(SQLiteException::class)
	suspend operator fun invoke(readerChapter: ReaderChapterEntity) =
		iChapterRepository.getChapter(readerChapter.id)?.let { invoke(it) }

	suspend operator fun invoke(chapter: ChapterUI) {
		invoke(chapter.convertTo())
	}

	@Throws(SQLiteException::class)
	suspend operator fun invoke(chapter: ReaderChapterUI) {
		invoke(chapter.convertTo())
	}
}