package app.shosetsu.android.datasource.local.memory.impl.concurrent

import java.util.concurrent.ConcurrentHashMap

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
 */

/**
 * 24 / 12 / 2020
 *
 * Abstracted memory data source
 *
 * This provides limitation features and expiration times, along with more thread safety then normally
 */
abstract class AbstractConMemoryDataSource<K : Any, V : Any> {

	/**
	 * How long something can last in memory in MS
	 *
	 * Default is 1 minute
	 */
	open val expireTime: Long = 60000

	/**
	 * how many entries to store at max
	 */
	abstract val maxSize: Long

	private val _hashMap = ConcurrentHashMap<K, Pair<Long, V>>()


	/**
	 * Recycler function, Iterates through the entries in [_hashMap] and clears out stale data
	 *
	 * Data is considered stale if it's creation point is > [expireTime]
	 */
	@Suppress("MemberVisibilityCanBePrivate")
	fun recycle() {
		// Reverses keys to go from back to front
		val keys = _hashMap.keys.reversed()

		// Saving value before hand saves 1ms~ per iteration
		val compareTime = System.currentTimeMillis()

		for (i in keys) {
			// Gets the time for entry `i`, If `i` no longer exists, continue
			val (time, _) = _hashMap[i] ?: continue

			if (time + expireTime <= compareTime) {
				_hashMap.remove(i)
			}
		}
	}

	fun remove(key: K): Boolean =
		if (!contains(key)) false
		else _hashMap.remove(key) != null

	/**
	 * Assigns [key] to [value]
	 * If [_hashMap] entries > [maxSize], removes first
	 */
	fun put(key: K, value: V) {
		if (_hashMap.size > maxSize) {
			remove(_hashMap.keys.first())
		}

		_hashMap[key] = System.currentTimeMillis() to value
	}

	fun contains(key: K): Boolean {
		if (_hashMap.size <= 0) return false

		val keys = _hashMap.keys.reversed()
		for (i in keys) {
			if (i == key)
				return true
		}
		return false
	}

	fun get(key: K): V? {
		recycle()
		return if (contains(key)) {
			_hashMap[key]?.second
		} else null
	}

}