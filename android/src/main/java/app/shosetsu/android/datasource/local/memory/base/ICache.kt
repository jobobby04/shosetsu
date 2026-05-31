package app.shosetsu.android.datasource.local.memory.base

import kotlin.time.Duration

interface ICache<K : Any, V : Any> {
	fun remove(key: K): Boolean
	operator fun set(key: K, value: V)
	fun contains(key: K): Boolean
	operator fun get(key: K): V?
	fun clear()

	interface Factory {
		fun <K : Any, V : Any> create(expireDuration: Duration, maxSize: Int): ICache<K, V>
	}
}
