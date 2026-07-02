package app.shosetsu.android.common

import okio.ArrayIndexOutOfBoundsException
import java.io.Serial

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
 * Thrown when Shosetsu tries to load an invalid listing from an extension.
 * @since 2026/06/24
 * @author Clocks
 * @param index The invalid index
 * @param e The source error
 */
class InvalidListingIndex(val index: Int, e: ArrayIndexOutOfBoundsException) :
	Exception("Invalid listing index! $index", e) {
	companion object {
		@Serial
		private const val serialVersionUID: Long = -2329602494997922292L
	}
}