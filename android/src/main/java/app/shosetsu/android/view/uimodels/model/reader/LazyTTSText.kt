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
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import java.util.UUID

/**
 * Lazily loads the text content from a given [element].
 *
 * This lowers memory consumption by a great deal as the contents of the text are not persisted
 *  as a copy in memory.
 */
class LazyTTSText(val element: Element) : TTSText {
	/**
	 * The "actual" element of this element
	 */
	private val actualElement by lazy {
		// Finds the "actual" element
		var actualElement = element
		var parent = element.parent()
		if (parent?.hasOwnText?.not() ?: false) parent = parent.parent()
		// traverse upwards to find our parent
		while (parent?.hasOwnText ?: false) {
			actualElement = parent
			parent = actualElement.parent()
		}
		actualElement
	}

	private val Node.hasOwnText: Boolean get() = childNodes().any { it is TextNode && !it.isBlank }

	override val text by lazy {
		// gets all the text from the html, then trims whitespace around it
		actualElement.wholeText().trim()
	}

	/**
	 * If this element should be ignored or not
	 */
	override val ignore by lazy {
		// we do the same as text, but immediately throw away the contents of the text
		actualElement.wholeText().trim().isEmpty()
	}

	/**
	 * Get the id of this element
	 */
	override val id: String by lazy {
		// find the uuid of this element
		if (actualElement.hasAttr("id") && actualElement.attr("id")
				.contains("textElement")
		) {
			// extract UUID from id
			actualElement.attr("id").substring(11)
		} else {
			// assign a random UUID for future use
			val uuid = UUID.randomUUID()
			actualElement.attr("id", "textElement$uuid")

			// return uuid
			uuid.toString()
		}
	}
}