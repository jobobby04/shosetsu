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

import org.jsoup.nodes.Element

/**
 * Alright, lets explain what is happening here.
 *
 * This iterator is designed to wrap
 *
 * @param modelFactory The factory function to create a new mutable list iterator, to go back before the first element.
 */
@Suppress("UNCHECKED_CAST")
class ElementToTTSTextIterator(
	private val modelFactory: () -> MutableListIterator<Element>
) : RewindableMutableListIterator<LazyTTSText>() {
	private var model: MutableListIterator<Element> = emptyList<Element>().toMutableList().listIterator()

	override fun add(element: LazyTTSText) = model.add(element.element)

	override fun hasNext(): Boolean = model.hasNext()

	override fun hasPrevious(): Boolean = model.hasPrevious()

	override fun next(): LazyTTSText = LazyTTSText(model.next())

	override fun nextIndex(): Int = model.nextIndex()

	override fun previous(): LazyTTSText = LazyTTSText(model.previous())

	override fun previousIndex(): Int = model.previousIndex()

	override fun remove() = model.remove()

	override fun set(element: LazyTTSText) = model.set(element.element)

	/**
	 * This override exists to prevent the creation of a new LazyTTSText per element per rewind
	 */
	override fun recreate() {
		// Create our new model
		model = modelFactory()

		// clean up memory fast!!!
		System.gc()
	}

	/**
	 * This override exists to prevent the creation of a new LazyTTSText per element per iteration
	 */
	override fun lastOrNull(): LazyTTSText? {
		// the position we start at
		val ogNextPosition = nextIndex()

		// keep track of element that is being moved through
		var last: Element? = null

		// go to the last element
		while (hasNext()) {
			last = model.next()
		}

		// Check where the original position was
		if (ogNextPosition == 0) {
			// Recreate the iterator if the original next position was 0
			recreate()
		} else {
			// rewind to exact starting position, where ogNextPosition was next
			while (previousIndex() >= ogNextPosition) {
				previous() // go back
			}
		}

		// return the last element or null
		return last?.let(::LazyTTSText)
	}

	override fun clone(): RewindableMutableListIterator<LazyTTSText> =
		ElementToTTSTextIterator(modelFactory)
}