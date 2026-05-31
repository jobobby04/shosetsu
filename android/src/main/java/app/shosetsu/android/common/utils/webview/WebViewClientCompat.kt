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

package app.shosetsu.android.common.utils.webview

import android.os.Build
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi

@Suppress("OverridingDeprecatedMember")
abstract class WebViewClientCompat : WebViewClient() {

	open fun shouldOverrideUrlCompat(view: WebView, url: String): Boolean {
		return false
	}

	open fun shouldInterceptRequestCompat(view: WebView, url: String): WebResourceResponse? {
		return null
	}

	open fun onReceivedErrorCompat(
		view: WebView,
		errorCode: Int,
		description: String?,
		failingUrl: String,
		isMainFrame: Boolean,
	) {
	}

	@RequiresApi(Build.VERSION_CODES.N)
	final override fun shouldOverrideUrlLoading(
		view: WebView,
		request: WebResourceRequest,
	): Boolean {
		return shouldOverrideUrlCompat(view, request.url.toString())
	}

	@Deprecated("shouldOverrideUrlLoading(WebView, WebResourceRequest)")
	final override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
		return shouldOverrideUrlCompat(view, url)
	}

	final override fun shouldInterceptRequest(
		view: WebView,
		request: WebResourceRequest,
	): WebResourceResponse? {
		return shouldInterceptRequestCompat(view, request.url.toString())
	}

	@Deprecated("shouldInterceptRequest(WebView, WebResourceRequest)")
	final override fun shouldInterceptRequest(view: WebView, url: String): WebResourceResponse? {
		return shouldInterceptRequestCompat(view, url)
	}

	@RequiresApi(Build.VERSION_CODES.M)
	final override fun onReceivedError(
		view: WebView,
		request: WebResourceRequest,
		error: WebResourceError,
	) {
		onReceivedErrorCompat(
			view,
			error.errorCode,
			error.description?.toString(),
			request.url.toString(),
			request.isForMainFrame,
		)
	}

	@Deprecated("onReceivedError(WebView, WebResourceRequest, WebResourceError)")
	final override fun onReceivedError(
		view: WebView,
		errorCode: Int,
		description: String?,
		failingUrl: String,
	) {
		onReceivedErrorCompat(view, errorCode, description, failingUrl, failingUrl == view.url)
	}

	final override fun onReceivedHttpError(
		view: WebView,
		request: WebResourceRequest,
		error: WebResourceResponse,
	) {
		onReceivedErrorCompat(
			view,
			error.statusCode,
			error.reasonPhrase,
			request.url
				.toString(),
			request.isForMainFrame,
		)
	}
}
