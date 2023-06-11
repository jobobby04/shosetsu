package app.shosetsu.android.ui.reader.page

import android.webkit.WebResourceRequest
import android.webkit.WebView
import com.google.accompanist.web.AccompanistWebViewClient

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
 * Shosetsu
 *
 * @since 23 / 05 / 2023
 * @author Doomsdayrs
 */
class ChapterReaderAccompanistWebViewClient : AccompanistWebViewClient() {
	private var applied = false

	/**
	 * Block redirects by clicking links
	 *
	 * TODO possible way to prompt to open externally
	 */
	override fun shouldOverrideUrlLoading(
		view: WebView?,
		request: WebResourceRequest?
	): Boolean = true

	/**
	 * Apply event listeners after a page is loaded
	 */
	override fun onPageFinished(view: WebView?, url: String?) {
		super.onPageFinished(view, url)
		if (!applied) {
			view?.evaluateJavascript(
				"""
				window.addEventListener("click",(event)=>{ shosetsuScript.onClick(); });
				window.addEventListener("dblclick",(event)=>{ shosetsuScript.onDClick(); });
				""".trimIndent(), null
			)
			applied = true
		}
	}
}