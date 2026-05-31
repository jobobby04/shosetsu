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

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.WebView
import android.widget.Toast
import androidx.core.content.ContextCompat
import app.shosetsu.android.common.ext.toast
import app.shosetsu.android.common.utils.webview.WebViewClientCompat
import app.shosetsu.android.common.utils.webview.isOutdated
import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.CountDownLatch

class CloudflareInterceptor(
	private val context: Context,
	private val cookieManager: CookieJarSync,
	defaultUserAgentProvider: () -> String,
) : WebViewInterceptor(context, defaultUserAgentProvider) {

	private val executor = ContextCompat.getMainExecutor(context)

	override fun shouldIntercept(response: Response): Boolean {
		// Check if Cloudflare anti-bot is on
		return response.code in ERROR_CODES && response.header("Server") in SERVER_CHECK
	}

	override fun intercept(
		chain: Interceptor.Chain,
		request: Request,
		response: Response,
	): Response {
		try {
			response.close()
			cookieManager.remove(request.url, COOKIE_NAMES, 0)
			val oldCookie = cookieManager.get(request.url)
				.firstOrNull { it.name == "cf_clearance" }
			resolveWithWebView(request, oldCookie)

			return chain.proceed(request)
		}
		// Because OkHttp's enqueue only handles IOExceptions, wrap the exception so that
		// we don't crash the entire app
		catch (e: CloudflareBypassException) {
			throw IOException("Failed to bypass Cloudflare", e)
		} catch (e: Exception) {
			throw IOException(e)
		}
	}

	@SuppressLint("SetJavaScriptEnabled")
	private fun resolveWithWebView(originalRequest: Request, oldCookie: Cookie?) {
		// We need to lock this thread until the WebView finds the challenge solution url, because
		// OkHttp doesn't support asynchronous interceptors.
		val latch = CountDownLatch(1)

		var webview: WebView? = null

		var challengeFound = false
		var cloudflareBypassed = false
		var isWebViewOutdated = false

		val origRequestUrl = originalRequest.url.toString()
		val headers = parseHeaders(originalRequest.headers)

		executor.execute {
			webview = createWebView(originalRequest)

			webview.webViewClient = object : WebViewClientCompat() {
				override fun onPageFinished(view: WebView, url: String) {
					fun isCloudFlareBypassed(): Boolean {
						return cookieManager.get(origRequestUrl.toHttpUrl())
							.firstOrNull { it.name == "cf_clearance" }
							.let { it != null && it != oldCookie }
					}

					if (isCloudFlareBypassed()) {
						cloudflareBypassed = true
						latch.countDown()
					}

					if (url == origRequestUrl && !challengeFound) {
						// The first request didn't return the challenge, abort.
						latch.countDown()
					}
				}

				override fun onReceivedErrorCompat(
					view: WebView,
					errorCode: Int,
					description: String?,
					failingUrl: String,
					isMainFrame: Boolean,
				) {
					if (isMainFrame) {
						if (errorCode in ERROR_CODES) {
							// Found the Cloudflare challenge page.
							challengeFound = true
						} else {
							// Unlock thread, the challenge wasn't found.
							latch.countDown()
						}
					}
				}
			}

			webview.loadUrl(origRequestUrl, headers)
		}

		latch.awaitFor30Seconds()

		executor.execute {
			if (!cloudflareBypassed) {
				isWebViewOutdated = webview?.isOutdated() == true
			}

			webview?.run {
				stopLoading()
				destroy()
			}
		}

		// Throw exception if we failed to bypass Cloudflare
		if (!cloudflareBypassed) {
			// Prompt user to update WebView if it seems too outdated
			if (isWebViewOutdated) {
				context.toast("Please update the WebView app for better compatibility", Toast.LENGTH_LONG)
			}

			throw CloudflareBypassException()
		}
	}
}

private val ERROR_CODES = listOf(403, 503)
private val SERVER_CHECK = arrayOf("cloudflare-nginx", "cloudflare")
private val COOKIE_NAMES = listOf("cf_clearance")

private class CloudflareBypassException : Exception()
