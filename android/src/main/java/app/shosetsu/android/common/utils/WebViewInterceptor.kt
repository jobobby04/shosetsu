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

package app.shosetsu.android.common.utils

import android.content.Context
import android.os.Build
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.Toast
import app.shosetsu.android.R
import app.shosetsu.android.common.ext.launchUI
import app.shosetsu.android.common.ext.toast
import app.shosetsu.android.common.utils.webview.WebViewUtil
import app.shosetsu.android.common.utils.webview.setDefaultSettings
import kotlinx.coroutines.DelicateCoroutinesApi
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.util.Locale
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

abstract class WebViewInterceptor(
	private val context: Context,
	private val defaultUserAgentProvider: () -> String,
) : Interceptor {

	/**
	 * When this is called, it initializes the WebView if it wasn't already. We use this to avoid
	 * blocking the main thread too much. If used too often we could consider moving it to the
	 * Application class.
	 */
	private val initWebView by lazy {
		// Crashes on some devices. We skip this in some cases since the only impact is slower
		// WebView init in those rare cases.
		// See https://bugs.chromium.org/p/chromium/issues/detail?id=1279562
		if (DeviceUtil.isMiui || (Build.VERSION.SDK_INT == Build.VERSION_CODES.S && DeviceUtil.isSamsung)) {
			return@lazy
		}

		try {
			WebSettings.getDefaultUserAgent(context)
		} catch (_: Exception) {
			// Avoid some crashes like when Chrome/WebView is being updated.
		}
	}

	abstract fun shouldIntercept(response: Response): Boolean

	abstract fun intercept(chain: Interceptor.Chain, request: Request, response: Response): Response

	@OptIn(DelicateCoroutinesApi::class)
	override fun intercept(chain: Interceptor.Chain): Response {
		val request = chain.request()
		val response = chain.proceed(request)
		if (!shouldIntercept(response)) {
			return response
		}

		if (!WebViewUtil.supportsWebView(context)) {
			launchUI {
				context.toast(
					context.getString(R.string.webview_required),
					Toast.LENGTH_LONG
				)
			}
			return response
		}
		initWebView

		return intercept(chain, request, response)
	}

	fun parseHeaders(headers: Headers): Map<String, String> {
		return headers
			// Keeping unsafe header makes webview throw [net::ERR_INVALID_ARGUMENT]
			.filter { (name, value) ->
				isRequestHeaderSafe(name, value)
			}
			.groupBy(keySelector = { (name, _) -> name }) { (_, value) -> value }
			.mapValues { it.value.getOrNull(0).orEmpty() }
	}

	fun CountDownLatch.awaitFor30Seconds() {
		await(30, TimeUnit.SECONDS)
	}

	fun createWebView(request: Request): WebView {
		return WebView(context).apply {
			setDefaultSettings()
			// Avoid sending empty User-Agent, Chromium WebView will reset to default if empty
			settings.userAgentString = request.header("User-Agent") ?: defaultUserAgentProvider()
		}
	}
}

// Based on [IsRequestHeaderSafe] in
// https://source.chromium.org/chromium/chromium/src/+/main:services/network/public/cpp/header_util.cc
private fun isRequestHeaderSafe(_name: String, _value: String): Boolean {
	val name = _name.lowercase(Locale.ENGLISH)
	val value = _value.lowercase(Locale.ENGLISH)
	if (name in unsafeHeaderNames || name.startsWith("proxy-")) return false
	if (name == "connection" && value == "upgrade") return false
	return true
}

private val unsafeHeaderNames = listOf(
	"content-length",
	"host",
	"trailer",
	"te",
	"upgrade",
	"cookie2",
	"keep-alive",
	"transfer-encoding",
	"set-cookie",
)
