package app.shosetsu.android.view.uimodels.model.reader

import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.jsoup.select.Elements
import java.util.UUID

private const val AUTO_DIV_THRESHOLD = 20

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
		val size = element.childNodeSize()
		for (i in 0 until size) {
			val node = element.childNode(i)
			if (node !is TextNode) continue
			if (size < AUTO_DIV_THRESHOLD) {
				result.add(element)
				element.attr("id", "textElement${UUID.randomUUID()}")
				return false // do not traverse children
			} else {
				// wrap in div
				val wrap = Element("div")
				node.replaceWith(wrap)
				wrap.appendChild(node)
			}
		}
		return true
	}
}
