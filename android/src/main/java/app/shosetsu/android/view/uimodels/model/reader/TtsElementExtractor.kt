package app.shosetsu.android.view.uimodels.model.reader

import java.util.UUID
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import org.jsoup.select.Elements

private const val AUTO_DIV_THRESHOLD = 20

class TtsElementExtractor {
	val result = Elements()
	private val queue = ArrayDeque<Element>()

	fun traverse(element: Element) {
		require(queue.isEmpty())
		queue.add(element)
		while (!queue.isEmpty()) {
			handle(queue.removeFirst())
		}
	}

	private fun handle(element: Element) {
		if (element.tagName() == "br") return
		val size = element.childNodeSize()
		for (i in 0 until size) {
			val node = element.childNode(i)
			if (node !is TextNode) continue
			if (size < AUTO_DIV_THRESHOLD) {
				result.add(element)
				element.attr("id", "textElement${UUID.randomUUID()}")
				return // do not traverse children
			} else {
				// wrap in div
				val wrap = Element("div")
				node.replaceWith(wrap)
				wrap.appendChild(node)
			}
		}
		// children might have been modified, recompute size
		for (i in 0 until element.childNodeSize()) {
			val node = element.childNode(i)
			if (node is Element) {
				queue.add(node)
			}
		}
	}
}
