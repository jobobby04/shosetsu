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

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import app.shosetsu.android.common.ext.logE
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object WebViewUtil {
	private const val CHROME_PACKAGE = "com.android.chrome"
	private const val YOUTUBE_FOR_TV_PACKAGE = "com.google.android.youtube.tv"
	private const val SYSTEM_SETTINGS_PACKAGE = "com.android.settings"

	const val MINIMUM_WEBVIEW_VERSION = 118

	/**
	 * Uses the WebView's user agent string to create something similar to what Chrome on Android
	 * would return.
	 *
	 * Example of WebView user agent string:
	 *   Mozilla/5.0 (Linux; Android 13; Pixel 7 Build/TQ3A.230901.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/116.0.0.0 Mobile Safari/537.36
	 *
	 * Example of Chrome on Android:
	 *   Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Mobile Safari/537.3
	 */
	fun getInferredUserAgent(context: Context): String {
		val webView = WebView(context)
		val webViewUserAgent = webView.getDefaultUserAgentString()
		webView.destroy()
		return webViewUserAgent
			.replace("; Android .*?\\)".toRegex(), "; Android 10; K)")
			.replace("Version/.* Chrome/".toRegex(), "Chrome/")
	}

	fun getVersion(context: Context): String {
		return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			val webView = WebView.getCurrentWebViewPackage() ?: return "how did you get here?"
			val pm = context.packageManager
			val label = webView.applicationInfo!!.loadLabel(pm)
			val version = webView.versionName
			"$label $version"
		} else {
			"Unknown"
		}
	}

	fun supportsWebView(context: Context): Boolean {
		try {
			// May throw android.webkit.WebViewFactory$MissingWebViewPackageException if WebView
			// is not installed
			CookieManager.getInstance()
		} catch (e: Throwable) {
			logE(null, e)
			return false
		}

		return context.packageManager.hasSystemFeature(PackageManager.FEATURE_WEBVIEW)
	}

	fun spoofedPackageName(context: Context): String {
		return runCatching { context.packageManager.getPackageInfo(CHROME_PACKAGE, 0) }
			.recoverCatching { context.packageManager.getPackageInfo(SYSTEM_SETTINGS_PACKAGE, 0) }
			.recoverCatching { context.packageManager.getPackageInfo(YOUTUBE_FOR_TV_PACKAGE, 0) }
			.fold(
				onSuccess = { it.packageName },
				onFailure = {
					context.packageManager.getInstalledPackages(0)
						.random().packageName
				},
			)
	}
}

fun WebView.isOutdated(): Boolean {
	return getWebViewMajorVersion() < WebViewUtil.MINIMUM_WEBVIEW_VERSION
}

suspend fun WebView.getHtml(): String = suspendCancellableCoroutine {
	evaluateJavascript("document.documentElement.outerHTML") { html -> it.resume(html) }
}

@SuppressLint("SetJavaScriptEnabled")
fun WebView.setDefaultSettings() {
	with(settings) {
		javaScriptEnabled = true
		domStorageEnabled = true
		useWideViewPort = true
		loadWithOverviewMode = true
		cacheMode = WebSettings.LOAD_DEFAULT

		// Allow zooming
		setSupportZoom(true)
		builtInZoomControls = true
		displayZoomControls = false
	}

	CookieManager.getInstance().acceptThirdPartyCookies(this)
}

private fun WebView.getWebViewMajorVersion(): Int {
	val uaRegexMatch = """.*Chrome/(\d+)\..*""".toRegex().matchEntire(getDefaultUserAgentString())
	return if (uaRegexMatch != null && uaRegexMatch.groupValues.size > 1) {
		uaRegexMatch.groupValues[1].toInt()
	} else {
		0
	}
}

// Based on https://stackoverflow.com/a/29218966
private fun WebView.getDefaultUserAgentString(): String {
	val originalUA: String = settings.userAgentString

	// Next call to getUserAgentString() will get us the default
	settings.userAgentString = null
	val defaultUserAgentString = settings.userAgentString

	// Revert to original UA string
	settings.userAgentString = originalUA

	return defaultUserAgentString
}
