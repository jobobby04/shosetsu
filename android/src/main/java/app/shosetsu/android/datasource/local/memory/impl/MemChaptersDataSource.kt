package app.shosetsu.android.datasource.local.memory.impl

import app.shosetsu.android.common.consts.MEMORY_EXPIRE_CHAPTER_TIME
import app.shosetsu.android.common.consts.MEMORY_MAX_CHAPTERS
import app.shosetsu.android.datasource.local.memory.base.IMemChaptersDataSource
import app.shosetsu.android.datasource.local.memory.base.ICache
import kotlin.time.Duration.Companion.minutes

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
 * 04 / 05 / 2020
 */
class MemChaptersDataSource(factory: ICache.Factory) : IMemChaptersDataSource {
	/** Map of Chapter ID to Chapter Passage */
	private val chapters: ICache<Int, ByteArray> =
		factory.create(MEMORY_EXPIRE_CHAPTER_TIME.minutes, MEMORY_MAX_CHAPTERS)

	override fun saveChapterInCache(chapterID: Int, chapter: ByteArray) {
		chapters[chapterID] = chapter
	}

	override fun loadChapterFromCache(chapterID: Int): ByteArray? =
		chapters[chapterID]
}
