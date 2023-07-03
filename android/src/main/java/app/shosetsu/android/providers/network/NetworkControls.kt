package app.shosetsu.android.providers.network

import app.shosetsu.android.common.SettingKey
import app.shosetsu.android.common.ext.logE
import app.shosetsu.android.common.utils.CookieJarSync
import app.shosetsu.android.common.utils.SiteProtector
import app.shosetsu.android.domain.repository.base.ISettingsRepository
import app.shosetsu.lib.ShosetsuSharedLib
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import java.net.Authenticator
import java.net.InetSocketAddress
import java.net.PasswordAuthentication
import java.net.Proxy
import java.text.ParseException
import java.text.SimpleDateFormat
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

fun createOkHttpClient(iSettingsRepository: ISettingsRepository): OkHttpClient {

	val useProxy = runBlocking {
		iSettingsRepository.getBoolean(SettingKey.UseProxy)
	}

	val builder = OkHttpClient.Builder()
		.cookieJar(CookieJarSync)
		.addInterceptor(::slowRequest)
		.addNetworkInterceptor {
			val request = it.request().newBuilder()
			ShosetsuSharedLib.shosetsuHeaders.forEach { (name, value) ->
				request.header(name, value)
			}
			it.proceed(request.build())
		}.apply {
			Logger.getLogger(OkHttpClient::class.java.name).level = Level.ALL
		}

	if (useProxy) {
		try {
			val proxyString = runBlocking {
				iSettingsRepository.getString(SettingKey.ProxyHost)
			}

			val (auth, hostname) = proxyString.split('@')
			val (host, rawPort) = hostname.split(':')
			val port = rawPort.toInt()

			val proxy = Proxy(Proxy.Type.SOCKS, InetSocketAddress(host, port))
			builder.proxy(proxy)

			if (auth.isNotEmpty()) {
				val (user, pass) = auth.split(':')

				Authenticator.setDefault(object : Authenticator() {
					override fun getPasswordAuthentication(): PasswordAuthentication? {
						if (requestingHost.equals(
								host,
								ignoreCase = true
							) and (requestingPort == port)
						) {
							return PasswordAuthentication(user, pass.toCharArray())
						}
						return null;
					}
				})
			}
		} catch (e: Exception) {
			// TODO: report invalid proxy configuration here
		}
	}

	return builder.build()
}

/**
 * Represents the format expected from an HTTP Retry-After response
 */
val retryAfterDateFormat: SimpleDateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz")

/**
 * Represents the pattern expected from an HTTP Retry-After response
 */
val retryAfterDateRegex: Regex =
	Regex("^([a-zA-Z]{3}),\\s(\\d{2})\\s(\\w{3})\\s(\\d{4})\\s(\\d{2}):(\\d{2}):(\\d{2})\\s([a-zA-Z]{3})")


/**
 * Parse a Retry-After date value.
 *
 * @return time in milliseconds till the date
 */
fun parseRetryAfterDate(chain: Interceptor.Chain, retryAfter: String): Long {
	val rightNow = System.currentTimeMillis()
	val formattedTime = try {
		retryAfterDateFormat.parse(retryAfter)?.time
			?: SiteProtector.requestDelay
	} catch (e: ParseException) {
		chain.logE("Failed to parse Retry-After header", e)
		SiteProtector.requestDelay
	}
	return formattedTime - rightNow
}

fun slowRequest(chain: Interceptor.Chain): Response {
	/*if (isRetry) {
		chain.logI("Retrying")
	}*/
	val response = SiteProtector.await(chain)
	/*if (response.hasRetryAfter()) {
		chain.logI("Received Retry-After from ${r.url}")
		// This can be two things, either a date or a second
		val retryAfter = response.header("Retry-After")

		if (retryAfter != null) {
			val delay: Long = if (retryAfter.matches(Regex("^\\d*$"))) {
				chain.logD("Retry-After is in seconds")
				// only digits, assume secconds
				(retryAfter.toInt().toLong() * 1000)
			} else {
				chain.logD("Retry-After should be date")
				// Assume some form of date format
				if (retryAfter.matches(retryAfterDateRegex)) {
					parseRetryAfterDate(chain, retryAfter)
				} else {
					chain.logW("Retry-After ($retryAfter) does not match regex, using default.")
					SiteProtector.requestDelay // go default since nothing else worked
				}
			}
			SiteProtector.setRetryAfter(r.url.host, delay)

			// Do not infinitely repeat the request
			return if (isRetry) response else slowRequest(chain, r, isRetry = true)
		}
	}*/
	return response;
}

/**
 * If the response has a retry after header. (not a success)
 */
private fun Response.hasRetryAfter() =
	code == 503 || code == 429 || code == 301