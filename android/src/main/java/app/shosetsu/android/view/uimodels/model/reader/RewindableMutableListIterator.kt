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
 * Children must implement recreate function.
 */
abstract class RewindableMutableListIterator<T> : MutableListIterator<T> {

	/**
	 * Get the last element
	 */
	open fun lastOrNull(): T? {
		// the position we start at
		val ogNextPosition = nextIndex()

		// keep track of element that is being moved through
		var last: T? = null

		// go to the last element
		while (hasNext()) {
			last = next()
		}

		// rewind to exact starting position, where ogNextPosition was next
		while (previousIndex() >= ogNextPosition) {
			previous() // go back
		}

		// return the last element
		return last
	}

	/**
	 * Recreate the iterator, enabling us to go back before the first element
	 */
	open fun recreate() {
		throw IllegalStateException("Must implement recreate")
	}

	companion object {
		fun <T> MutableListIterator<T>.toRewindable(): RewindableMutableListIterator<T> {
			return object : RewindableMutableListIterator<T>(),
				MutableListIterator<T> by this@toRewindable {
			}
		}
	}
}