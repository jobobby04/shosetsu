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
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.util.ArrayDeque
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

/**
 * Class dedicated to protecting sites from what is essentially a ddos attack from shosetsu
 */
object SiteProtector {
	private val lastUsed = ConcurrentHashMap<String, Long>()

	/**
	 * Filled with delays provided by HTTP 429s
	 * If entry is found, then it should be removed afterwards.
	 */
	private val retryAfter = ConcurrentHashMap<String, Long>()

	/**
	 * @param after delay in ms
	 */
	fun setRetryAfter(host: String, after: Long) {
		retryAfter[host] = after
	}

	/**
	 * The delay between each request to a site in milliseconds
	 */
	var requestDelay: Long = SettingKey.SiteProtectionDelay.default.toLong()

	/**
	 * Get delay, respects [retryAfter] defaults to [requestDelay]
	 */
	@Suppress("NOTHING_TO_INLINE")
	private inline fun getDelay(host: String) =
		retryAfter[host] ?: requestDelay

	const val permits = 4
	const val period = 1L
	val unit = TimeUnit.SECONDS
	private val requestQueue = ArrayDeque<Long>(permits)
	private val rateLimitMillis = unit.toMillis(period)
	private val fairLock = Semaphore(1, true)

	/**
	 * Check if we can continue operating.
	 *
	 * @return true if we can, false if delay must occur
	 */
	@Suppress("NOTHING_TO_INLINE")
	private inline fun checkIfCan(host: String, lastUsedTime: Long): Boolean =
		(lastUsedTime + getDelay(host)) > System.currentTimeMillis()

	/**
	 * Ask to use the site, once received
	 *
	 * @param host domain to check against
	 * @param block code block to execute
	 * @return whatever [block] returns
	 */
	@Suppress("BlockingMethodInNonBlockingContext")
	fun await(chain: Interceptor.Chain): Response {
		val call = chain.call()
		if (call.isCanceled()) throw IOException("Canceled")

		val request = chain.request()

		try {
			fairLock.acquire()
		} catch (e: InterruptedException) {
			throw IOException(e)
		}

		val requestQueue = this.requestQueue
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