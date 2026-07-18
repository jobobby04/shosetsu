package app.shosetsu.android.view.uimodels.model.reader

import org.jsoup.nodes.Element
import org.jsoup.nodes.NodeIterator
import org.jsoup.nodes.TextNode
import org.jsoup.select.Elements
import java.util.UUID

private const val AUTO_DIV_THRESHOLD = 20

class TtsElementExtractor {
	val result = Elements()

	fun traverse(element: Element) {
		val it = NodeIterator(element, Element::class.java)
		while (it.hasNext()) handle(it.next())
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
	}
}
