package app.shosetsu.android.view.uimodels.model.reader

import java.util.UUID
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.jsoup.select.Elements

private const val SOFT_SPLIT_THRESHOLD = 5
private const val HARD_SPLIT_THRESHOLD = 40

class TtsElementExtractor {
	val result = Elements()

	fun traverse(root: Element) {
		// DFS, skipping child nodes if handle returns false
		var node: Node = root
		var traversingUp = false
		while (true) {
			val traverseChildren = !traversingUp && (node !is Element || handle(node))
			node = when {
				traverseChildren && node.childNodeSize() > 0 -> node.childNode(0)

				node == root -> return

				node.nextSibling() != null -> {
					traversingUp = false
					node.nextSibling()!!
				}

				else -> {
					traversingUp = true
					node.parent() ?: return
				}
			}
		}
	}

	/**
	 * If a direct descendant is text, mark it as a TTS textElement for use by [ElementToTTSTextIterator]
	 * @return true if children should be traversed
	 */
	private fun handle(element: Element): Boolean {
		if (element.tagName() == "br") return true
		var size = element.childNodeSize()
		var segmentStart = 0
		var i = 0
		var containsTextNode = false

		fun flushSegment(node: Node = element.childNode(i)): Boolean {
			if (segmentStart == 0 && i == size - 1) {
				// Can occur due to the soft threshold and final flush
				// If this happens, creating a wrapper would cause an infinite loop, so avoid it
				if (containsTextNode) {
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
			containsTextNode = false
			segmentStart += 1
			i = segmentStart
			return true
		}

		while (i < size) {
			val node = element.childNode(i)

			if (!containsTextNode) {
				if (node is TextNode && node.wholeText.isNotBlank()) {
					containsTextNode = true
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

	private fun createWrapper(): Element =
		Element("div") // should be Element("span") but that would make the borders ugly

	private fun Node.plausiblyEndsSentence() = (this is Element && tagName() == "br") ||
		(this is TextNode && wholeText.plausiblyEndsSentence())

	private fun String.plausiblyEndsSentence(): Boolean {
		for (i in indices.reversed()) {
			if (this[i].isWhitespace()) continue
			if (this[i] == '.' || this[i] == ')') return true
			return false
		}
		return false
	}
}
