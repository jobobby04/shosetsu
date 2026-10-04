package app.shosetsu.android.view.uimodels.model.reader

import java.util.UUID
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.jsoup.select.Elements

/**
 * Number of child elements after which the end of a sentence causes a split to be inserted
 */
private const val SOFT_SPLIT_THRESHOLD = 5

/**
 * Number of child elements after which a split is inserted regardless of whether a sentence has ended
 */
private const val HARD_SPLIT_THRESHOLD = 40

/**
 * @see [TtsElementExtractor.traverse]
 */
class TtsElementExtractor private constructor(val element: Element, val result: Elements) {
	companion object {
		/**
		 * Finds all TTS text elements.
		 * May also modify the DOM to keep the elements small.
		 */
		fun traverse(root: Element): Elements {
			val result = Elements()
			// DFS, skipping child nodes if handle returns false
			var node: Node = root
			var traversingUp = false
			while (true) {
				val traverseChildren =
					!traversingUp && (node !is Element || TtsElementExtractor(node, result = result).handle())
				node = when {
					traverseChildren && node.childNodeSize() > 0 -> node.childNode(0)

					node == root -> return result

					node.nextSibling() != null -> {
						traversingUp = false
						node.nextSibling()!!
					}

					else -> {
						traversingUp = true
						node.parent() ?: return result
					}
				}
			}
		}
	}

	/**
	 * Size of the array of children for the current element
	 */
	private var size = element.childNodeSize()

	/**
	 * Index in the array of children for the current element where the current segment starts
	 */
	private var segmentStart = 0

	/**
	 * Current index in the array of children for the current element
	 */
	private var i = 0

	/**
	 * Whether a text node exists in the current segment
	 */
	private var segmentContainsTextNode = false

	/**
	 * If a direct descendant is text, mark it as a TTS textElement for use by [ElementToTTSTextIterator]
	 * @return true if children should be traversed
	 */
	private fun handle(): Boolean {
		require(i == 0) { "Cannot be called more than once" }

		while (i < size) {
			val node = element.childNode(i)

			if (!segmentContainsTextNode) {
				if (node is TextNode && node.wholeText.isNotBlank()) {
					segmentContainsTextNode = true
					if (size < SOFT_SPLIT_THRESHOLD) {
						result.add(element)
						element.attr("id", "textElement${UUID.randomUUID()}")
						return false // do not traverse children
					}
				} else {
					i++
					continue
				}
			}

			if (i - segmentStart >= SOFT_SPLIT_THRESHOLD && node.plausiblyEndsSentence()) {
				if (!flushSegment(node = node)) return false
			} else if (i - segmentStart >= HARD_SPLIT_THRESHOLD) {
				if (!flushSegment(node = node)) return false
			} else {
				i++
			}
		}

		if (segmentStart < size) {
			i = size - 1
			if (!flushSegment()) return false
		}

		return true
	}

	/**
	 * Marks the current segment ([segmentStart] to [i]) as finished, possibly wrapping it with [createWrapper].
	 *
	 * Updates [segmentStart] and [segmentContainsTextNode] which are used internally,
	 * and, if a segment is wrapped, updates [size] and [i] to account for the incurred size difference.
	 * @param node always `element.childNode[i]`, a parameter to avoid recomputing if available locally
	 */
	private fun flushSegment(node: Node = element.childNode(i)): Boolean {
		if (segmentStart == 0 && i == size - 1) {
			// Can occur due to the soft threshold and final flush
			// If this happens, creating a wrapper would cause an infinite loop, so avoid it
			if (segmentContainsTextNode) {
				result.add(element)
				element.attr("id", "textElement${UUID.randomUUID()}")
				return false
			}
		} else if (i == segmentStart) {
			if (node is TextNode) {
				val wrap = createWrapper()
				node.replaceWith(wrap)
				wrap.appendChild(node)
			}
		} else {
			val wrap = createWrapper()
			node.replaceWith(wrap)
			wrap.appendChild(node)
			for (j in (segmentStart..<i).reversed()) {
				val inner = element.childNode(j)
				inner.remove()
				wrap.prependChild(inner)
			}
			size = element.childNodeSize()
		}
		segmentContainsTextNode = false
		segmentStart += 1
		i = segmentStart
		return true
	}

	/**
	 * Creates an [Element] in which to wrap elements when splitting.
	 * This should be a span, but that would make the borders ugly, so a div it is.
	 * Extracted to avoid inconsistent behavior.
	 */
	private fun createWrapper(): Element = Element("div")

	/**
	 * Checks whether this node could plausibly be the end of a sentence,
	 * or, in case it is text, if that text plausibly ends with the end of a sentence.
	 * Since the ends of sentences are reasonably safe for TTS splits,
	 * this makes the node eligible to be a soft split edge.
	 */
	private fun Node.plausiblyEndsSentence() = (this is Element && tagName() == "br") ||
		(this is TextNode && wholeText.plausiblyEndsSentence())

	/**
	 * Checks whether this string plausibly ends with the end of a sentence.
	 * See [Node.plausiblyEndsSentence].
	 */
	private fun String.plausiblyEndsSentence(): Boolean {
		for (i in indices.reversed()) {
			if (this[i].isWhitespace()) continue
			if (this[i] == '.' || this[i] == ')') return true
			return false
		}
		return false
	}
}
