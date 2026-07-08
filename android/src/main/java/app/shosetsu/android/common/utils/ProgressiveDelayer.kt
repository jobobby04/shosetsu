package app.shosetsu.android.common.utils

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.times
import kotlinx.coroutines.delay

/**
 * Delay maintainer. Handles a progressively increasing delay.
 */
class ProgressiveDelayer(private val delayTime: Duration = 100.milliseconds) {
	var count: Int = 0

	suspend fun delay() {
		count++
		delay(count * delayTime)
	}

	fun reset() {
		count = 0
	}
}
