package app.shosetsu.android.datasource.local.memory.impl

import app.shosetsu.android.datasource.local.memory.base.ICache
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration

class ConCacheFactory : ICache.Factory {
	override fun <K : Any, V : Any> create(
		expireDuration: Duration,
		maxSize: Int
	): ICache<K, V> = Cache(expireDuration.inWholeMilliseconds, maxSize)

	private class Cache<K : Any, V : Any>(
		private val expireTime: Long,
		private val maxSize: Int,
	) : ICache<K, V> {
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

		override fun remove(key: K): Boolean =
			if (!contains(key)) false
			else _hashMap.remove(key) != null

		override fun set(key: K, value: V) {
			if (_hashMap.size > maxSize) {
				remove(_hashMap.keys.first())
			}

			_hashMap[key] = System.currentTimeMillis() to value
		}

		override fun contains(key: K): Boolean {
			if (_hashMap.isEmpty()) return false

			val keys = _hashMap.keys.reversed()
			for (i in keys) {
				if (i == key)
					return true
			}
			return false
		}

		override fun get(key: K): V? {
			recycle()
			return if (contains(key)) {
				_hashMap[key]?.second
			} else null
		}

		override fun clear() {
			_hashMap.clear()
		}
	}
}
