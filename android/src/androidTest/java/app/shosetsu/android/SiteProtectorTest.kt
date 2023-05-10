package app.shosetsu.android

import app.shosetsu.android.common.utils.SiteProtector
import app.shosetsu.android.providers.network.retryAfterDateFormat
import app.shosetsu.android.providers.network.slowRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.Call
import okhttp3.Connection
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.Before
import org.junit.Test
import java.util.Date
import java.util.concurrent.TimeUnit

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
 * Test the site protector
 *
 * @since 22 / 04 / 2023
 * @author Doomsdayrs
 */
class SiteProtectorTest {

	companion object {
		private const val SITE_DELAY: Long = 1000L
		private const val RETRY_AFTER_DELAY = SITE_DELAY + SITE_DELAY + SITE_DELAY
		private const val RETRY_AFTER_DELAY_SECONDS = RETRY_AFTER_DELAY / 1000
	}

	@Before
	fun setup() {
		SiteProtector.requestDelay = SITE_DELAY
	}

	/**
	 * Jobs on a thread launched sequentially must have a delay
	 */
	@Test
	fun linear() {
		val host = "linear.test"
		val startTime = System.currentTimeMillis()
		var now: Long
		println("Current time: $startTime")

		runBlocking {
			SiteProtector.await(host) {
				now = System.currentTimeMillis()
				println("A: Current time: $now")
				assert(startTime + SITE_DELAY >= now) {
					"block executed too late: $now"
				}
			}
		}

		runBlocking {
			SiteProtector.await(host) {
				now = System.currentTimeMillis()
				println("B: Current time: $now")
				assert(startTime + SITE_DELAY <= now) {
					"block executed too early: $now"
				}
			}
		}
	}

	/**
	 * 10 jobs launched sequentially onto async,
	 *  may occur out of order,
	 *  but calls after the first must be delayed.
	 */
	@Test
	fun async() {
		val host = "async.test"
		runBlocking {
			repeat(10) { testNumber ->
				launch(Dispatchers.IO) {
					val startTime = System.currentTimeMillis()

					println("Starting job #$testNumber at ${startTime}ms")

					SiteProtector.await(host) {
						val endTime = System.currentTimeMillis()

						if (testNumber > 0) assert(endTime >= startTime) {
							"Async jobs were not delayed"
						}

						println("Completed job #$testNumber at ${endTime}ms")
					}
				}
				delay(10)
			}
		}
	}

	/**
	 * Ensure retry after is respected
	 */
	@Test
	fun retryAfter() {
		val host = "retryAfter.test"
		val startTime = System.currentTimeMillis()
		runBlocking { SiteProtector.await(host) {} }
		SiteProtector.setRetryAfter(host, SITE_DELAY)

		runBlocking {
			SiteProtector.await(host) {
				val endTime = System.currentTimeMillis()
				assert(endTime >= (SITE_DELAY + startTime)) {
					"Retry after not respected. Start time: $startTime; End time: $endTime"
				}
			}
		}
	}

	/**
	 * Ensure retry after seconds response is rerequested
	 */
	@Test
	fun retryAfterSecondsResponse() {
		val host = "https://retryAfterSecondsResponse.test"
		val fauxRequest =
			Request.Builder().url(host)
				.build()

		val startTime = System.currentTimeMillis()
		val retryAfterSecondsResponse =
			Response.Builder()
				.addHeader("Retry-After", RETRY_AFTER_DELAY_SECONDS.toString())
				.code(429)
				.request(fauxRequest)
				.protocol(Protocol.HTTP_2)
				.message("")
				.build()

		retryAfterResponse(startTime, fauxRequest, retryAfterSecondsResponse)
	}

	/**
	 * Ensure retry after date response is rerequested
	 */
	@Test
	fun retryAfterDateResponse() {
		val host = "https://retryAfterDateResponse.test"
		val fauxRequest =
			Request.Builder().url(host)
				.build()


		val startTime = System.currentTimeMillis()
		val retryAfterDate = retryAfterDateFormat.format(Date(startTime + RETRY_AFTER_DELAY))
		println(retryAfterDate)
		val retryAfterDateResponse =
			Response.Builder()
				.addHeader(
					"Retry-After",
					retryAfterDate
				)
				.code(429)
				.request(fauxRequest)
				.protocol(Protocol.HTTP_2)
				.message("")
				.build()

		retryAfterResponse(startTime, fauxRequest, retryAfterDateResponse)
	}

	private fun retryAfterResponse(startTime: Long, request: Request, response: Response) {
		val chain = FakeChain(response)

		slowRequest(chain, request)
		val endTime = System.currentTimeMillis()
		assert((startTime + RETRY_AFTER_DELAY) <= endTime) {
			"Retry after not respected. Start time: $startTime; End time: $endTime"
		}
	}

	private class FakeChain(private val response: Response) : Interceptor.Chain {
		var firstCall = true
		override fun call(): Call {
			TODO("Not yet implemented")
		}

		override fun connectTimeoutMillis(): Int {
			TODO("Not yet implemented")
		}

		override fun connection(): Connection? {
			TODO("Not yet implemented")
		}

		override fun proceed(request: Request): Response =
			if (firstCall) {
				firstCall = false
				response
			} else response

		override fun readTimeoutMillis(): Int {
			TODO("Not yet implemented")
		}

		override fun request(): Request {
			TODO("Not yet implemented")
		}

		override fun withConnectTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain {
			TODO("Not yet implemented")
		}

		override fun withReadTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain {
			TODO("Not yet implemented")
		}

		override fun withWriteTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain {
			TODO("Not yet implemented")
		}

		override fun writeTimeoutMillis(): Int {
			TODO("Not yet implemented")
		}

	}
}

/*
 *	base10:	0	1	2	3	4	5	6	7	8		9		10
 * 	base3 :	0	1	2	10	11	12	100	101	102		110		111
 * 	base2 :	0	1	10	11	100	101	110	111	1000	1001	1010
 */