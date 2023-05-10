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

import app.shosetsu.android.common.SettingKey
import kotlinx.coroutines.delay
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

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
	suspend fun <R> await(host: String, block: () -> R): R {
		// Query time
		var time = lastUsed[host]

		return if (time == null) {
			// Site has not been accessed, we can operate
			lastUsed[host] = System.currentTimeMillis()
			block()
		} else {
			// Site has been accessed, check if we can operate
			if (checkIfCan(host, time)) {
				// We can not operate rn, delay until we can
				/** Represents the loop count, delaying the time increasingly until 10 loops */
				var delayedCount = 0
				do {
					// Delay a random interval between (requestDelay / 1 - 10)
					// + progressive delay
					// This ensures that two awaits never occur at the same time
					delay(
						(getDelay(host) / Random.nextInt(1, 10)) +
								delayedCount * 100
					)
					if (delayedCount < 10) delayedCount++

					time = lastUsed[host]
				} while (time != null && checkIfCan(host, time))
			}
			lastUsed[host] = System.currentTimeMillis()
			retryAfter.remove(host) // Clear out retry after, we respected it.
			block()
		}
	}
}