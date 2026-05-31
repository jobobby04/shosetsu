package app.shosetsu.android.datasource.local.memory.impl

import app.shosetsu.android.datasource.local.memory.base.ICache
import com.google.common.cache.CacheBuilder
import kotlin.time.Duration
import kotlin.time.toJavaDuration

class GuavaCacheFactory : ICache.Factory {
	override fun <K : Any, V : Any> create(
		expireDuration: Duration,
		maxSize: Int
	): ICache<K, V> = Cache(expireDuration, maxSize.toLong())

	private class Cache<K : Any, V : Any>(
		expireTime: Duration,
		maxSize: Long,
	) : ICache<K, V> {
		private val cache = CacheBuilder.newBuilder()
			.maximumSize(maxSize)
			.expireAfterWrite(expireTime.toJavaDuration())
			.build<K, V>()

		override fun remove(key: K): Boolean {
			cache.invalidate(key)
			return true
		}

		override fun set(key: K, value: V) = cache.put(key, value)
		override fun contains(key: K): Boolean = cache.getIfPresent(key) != null
		override fun get(key: K): V? = cache.getIfPresent(key)

		override fun clear() {
			cache.invalidateAll()
			cache.cleanUp()
		}
	}
}
