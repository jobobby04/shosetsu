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
package app.shosetsu.android.common.utils

import android.os.SystemClock
import app.shosetsu.android.common.SettingKey
import com.google.common.cache.CacheBuilder
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.util.ArrayDeque
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

/**
 * Class dedicated to protecting sites from what is essentially a ddos attack from shosetsu
 */
object SiteProtector : Interceptor {
	/**
	 * The delay between each request to a site in milliseconds
	 */
	var permits: Int = SettingKey.SiteProtectionPermits.default

	/**
	 * The delay between each request to a site in milliseconds
	 */
	var period: Long = SettingKey.SiteProtectionPeriod.default.toLong()

	val unit = TimeUnit.MILLISECONDS

	private val cache = CacheBuilder.newBuilder()
		.expireAfterAccess(10, TimeUnit.MINUTES)
		.build<String, CachedRateLimit>()

	data class CachedRateLimit(
		val requestQueue: ArrayDeque<Long> = ArrayDeque<Long>(permits),
		val rateLimitMillis: Long = unit.toMillis(period),
		val fairLock: Semaphore = Semaphore(1, true)
	)

	/**
	 * Ask to use the site, once received
	 *
	 * @param host domain to check against
	 * @param block code block to execute
	 * @return whatever [block] returns
	 */
	@Suppress("BlockingMethodInNonBlockingContext")
	override fun intercept(chain: Interceptor.Chain): Response {
		val call = chain.call()
		if (call.isCanceled()) throw IOException("Canceled")

		val request = chain.request()

		val (requestQueue, rateLimitMillis, fairLock) = cache.get(request.url.host) { CachedRateLimit() }

		try {
			fairLock.acquire()
		} catch (e: InterruptedException) {
			throw IOException(e)
		}

		val timestamp: Long

		try {
			synchronized(requestQueue) {
				while (requestQueue.size >= permits) { // queue is full, remove expired entries
					val periodStart = SystemClock.elapsedRealtime() - rateLimitMillis
					var hasRemovedExpired = false
					while (requestQueue.isEmpty().not() && requestQueue.first <= periodStart) {
						requestQueue.removeFirst()
						hasRemovedExpired = true
					}
					if (call.isCanceled()) {
						throw IOException("Canceled")
					} else if (hasRemovedExpired) {
						break
					} else {
						try { // wait for the first entry to expire, or notified by cached response
							(requestQueue as Object).wait(requestQueue.first - periodStart)
						} catch (_: InterruptedException) {
							continue
						}
					}
				}

				// add request to queue
				timestamp = SystemClock.elapsedRealtime()
				requestQueue.addLast(timestamp)
			}
		} finally {
			fairLock.release()
		}

		val response = chain.proceed(request)
		if (response.networkResponse == null) { // response is cached, remove it from queue
			synchronized(requestQueue) {
				if (requestQueue.isEmpty() || timestamp < requestQueue.first) return@synchronized
				requestQueue.removeFirstOccurrence(timestamp)
				(requestQueue as Object).notifyAll()
			}
		}

		return response
	}
}