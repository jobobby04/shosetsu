package app.shosetsu.android.ui.reader.page

import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebView
import app.shosetsu.android.common.ext.logV
import com.google.accompanist.web.AccompanistWebViewClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

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
class ChapterReaderAccompanistWebViewClient(
	private val openURI: (Uri) -> Unit,
	private val scope: CoroutineScope,
	private val ttsState: StateFlow<String?>,
	private val getChapterHTMLStyle: () -> Flow<ShosetsuStyle>,
) : AccompanistWebViewClient() {
	private var lastJob: Job? = null

	/**
	 * Block redirects by clicking links
	 *
	 * TODO possible way to prompt to open externally
	 */
	override fun shouldOverrideUrlLoading(
		view: WebView?,
		request: WebResourceRequest
	): Boolean {
		openURI(request.url)
		return true
	}

	/**
	 * Apply event listeners after a page is loaded
	 */
	override fun onPageFinished(view: WebView, url: String?) {
		super.onPageFinished(view, url)
		view.evaluateJavascript(
			"""
				if (!window._shosetsuListenersAdded) {
					window.addEventListener("click",(event)=>{ shosetsuScript.onClick(null); });
					window.addEventListener("dblclick",(event)=>{ shosetsuScript.onDClick(); });
					window._shosetsuListenersAdded = true;
				}
				document.querySelectorAll('[id]').forEach(function(element) {
					if (!element._shosetsuListenersAdded) {
						element.addEventListener('click', function(event) {
							event.stopPropagation();
							shosetsuScript.onClick(element.id);
						});
						element._shosetsuListenersAdded = true;
					}
				});
				""".trimIndent(), null
		)
		lastJob?.cancel()
		lastJob = scope.launch {
			launch {
				getChapterHTMLStyle().collect { style ->
					view.evaluateJavascript(style.toJs(), null)
				}
			}

			launch {
				var oldTtsElement: String? = null
				ttsState.collect { id ->
					if (id != null) {
						logV("Moving TTS highlight to $id")
						view.evaluateJavascript(
							"""
							var element = document.getElementById("textElement$id");
							element.classList.add("tts-border-style");
							""".trimIndent() + if (oldTtsElement != null) {
								logV("Removing old TTS highlight from $oldTtsElement")
								"""
								var element2 = document.getElementById("textElement$oldTtsElement");
								element2.classList.remove("tts-border-style");
								""".trimIndent()
							} else "",
							null,
						)
					} else if (oldTtsElement != null) {
						logV("TTS Stopped? Removing old TTS highlight from $oldTtsElement")
						view.evaluateJavascript(
							"""
							var element2 = document.getElementById("textElement$oldTtsElement");
							element2.classList.remove("tts-border-style");
							""".trimIndent(),
							null,
						)
					}
					oldTtsElement = id
				}
			}
		}
	}
}
