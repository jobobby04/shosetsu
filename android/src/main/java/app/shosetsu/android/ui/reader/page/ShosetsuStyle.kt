package app.shosetsu.android.ui.reader.page

import org.jsoup.nodes.Document

data class ShosetsuStyle(private val shoCSS: String, private val useCSS: String) {
	fun insert(document: Document) {
		inject(document, SHOSETSU_STYLE, shoCSS)
		inject(document, USER_STYLE, useCSS)
	}

	fun toJs() = inject(SHOSETSU_STYLE, shoCSS) + inject(USER_STYLE, useCSS)

	companion object {
		private const val SHOSETSU_STYLE = "shosetsu-style"
		private const val USER_STYLE = "user-style"

		private fun String.jsString() = "`" + this
			.replace("\\", "\\\\")
			.replace("`", "\\`")
			.replace("\${", "\\\${") + "`"

		private fun inject(id: String, style: String) = """
			var styleElement = document.getElementById(${id.jsString()});
			if (styleElement) {
				shosetsuScript.logI("Updating style " + ${id.jsString()});
				styleElement.textContent = ${style.jsString()};
			} else {
				shosetsuScript.logI("Injecting style " + ${id.jsString()});
				styleElement = document.createElement('style');
				styleElement.id = ${id.jsString()};
				styleElement.textContent = ${style.jsString()};
				document.head.appendChild(styleElement);
			}
		""".trimIndent()

		private fun inject(document: Document, id: String, style: String) {
			val styleElement = document.getElementById(id) ?: document.createElement("style").apply {
				id(id)
				attr("type", "text/css")
				document.head().appendChild(this)
			}
			styleElement.text(style)
		}
	}
}
