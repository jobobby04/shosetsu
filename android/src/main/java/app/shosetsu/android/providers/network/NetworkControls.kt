package app.shosetsu.android.providers.network

import androidx.core.text.isDigitsOnly
import app.shosetsu.android.common.consts.USER_AGENT
import app.shosetsu.android.common.ext.logE
import app.shosetsu.android.common.ext.logI
import app.shosetsu.android.common.utils.CookieJarSync
import app.shosetsu.android.common.utils.SiteProtector
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Response
import java.text.DateFormat
import java.text.ParseException
import java.util.logging.Level
import java.util.logging.Logger

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
 * shosetsu
 * 04 / 05 / 2020
 */

fun createOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
	.cookieJar(CookieJarSync)
	.addInterceptor { chain ->
		val r = chain.request().newBuilder().header("User-Agent", USER_AGENT).build()
		val response = runBlocking {
			// Await for chance to access the site
			SiteProtector.await(r.url.host) {
				chain.logI("Sending Request: $r")
				chain.proceed(r)
			}
		}
		if (response.hasRetryAfter()) {
			chain.logI("Received Retry-After from ${r.url}")
			// This can be two things, either a date or a second
			val retryAfter = response.header("Retry-After")

			if (retryAfter != null) {
				val delay: Long = if (retryAfter.isDigitsOnly()) {
					// only digits, assume secconds
					(retryAfter.toInt().toLong() / 100)
				} else {
					// Assume some form of date format

					val rightNow = System.currentTimeMillis()
					val formattedTime = try {
						DateFormat.getInstance().parse(retryAfter)?.time
					} catch (e: ParseException) {
						chain.logE("Failed to parse Retry-After header", e)
						0
					}

					rightNow - (formattedTime ?: 0)
				}
				SiteProtector.setRetryAfter(r.url.host, delay)
			}
		}
		chain.logI("Received response $response")
		return@addInterceptor response
	}.apply {
		Logger.getLogger(OkHttpClient::class.java.name).level = Level.ALL
	}
	.build()

/**
 * If the response has a retry after header. (not a success)
 */
private fun Response.hasRetryAfter() =
	code == 503 || code == 429 || code == 301