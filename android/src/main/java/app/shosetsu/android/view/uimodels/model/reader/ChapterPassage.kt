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
 *
 */
package app.shosetsu.android.view.uimodels.model.reader

/**
 * Represents the contents of a given chapter.
 */
sealed class ChapterPassage {
	/**
	 * Currently loading this chapter
	 */
	data object Loading : ChapterPassage()

	/**
	 * There was an error attempting to load the chapter
	 */
	data class Error(val throwable: Throwable?) : ChapterPassage()

	/**
	 * Successfully loaded the chapter content
	 *
	 * @param content content of this chapter
	 * @param ttsElements text to speech elements of this chapter
	 */
	data class Success(
		val content: String,
		val ttsElements: RewindableMutableListIterator<TTSText>
	) : ChapterPassage()
}