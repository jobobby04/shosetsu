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
 * Represents text that TTS reads aloud to the user
 */
sealed interface TTSText {

	/**
	 * Unique identification of this specific text
	 */
	val id: String

	/**
	 * The actual content of the text
	 */
	val text: String

	/**
	 * If this text should be skipped from being read or not
	 */
	val ignore: Boolean
}