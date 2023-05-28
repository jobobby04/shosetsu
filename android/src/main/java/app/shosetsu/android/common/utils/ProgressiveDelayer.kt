package app.shosetsu.android.common.utils

import kotlinx.coroutines.delay

/**
 * Delay maintainer. Handles a progressively increasing delay.
 */
class ProgressiveDelayer(
	private val delayTime: Long = 100
) {
	var count: Int = 0

	suspend fun delay() {
		count++
		delay(count * delayTime)
	}

	fun reset() {
		count = 0
	}
}