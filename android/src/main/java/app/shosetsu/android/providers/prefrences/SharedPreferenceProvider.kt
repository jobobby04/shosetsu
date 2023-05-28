package app.shosetsu.android.providers.prefrences

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import app.shosetsu.android.common.*
import app.shosetsu.android.common.ext.logE
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

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

typealias SettingKeyFlowMap<T> = HashMap<SettingKey<T>, MutableStateFlow<T>>

/**
 * shosetsu
 * 17 / 09 / 2020
 * This class contains all SharedPrefrenceData for shosetsu
 */
class SharedPreferenceProvider(
	/** Application context for internal use */
	val context: Context,
) {

	private val preferenceMap: HashMap<String, PreferenceHolder> = hashMapOf()

	/**
	 * Wraps a [SharedPreferences] object, providing functionality wrapping
	 */
	private data class PreferenceHolder(
		val preferences: SharedPreferences,
	) {
		private val flowHolder: SharedPreferenceFlowState = SharedPreferenceFlowState()

		fun observeLong(key: SettingKey<Long>): StateFlow<Long> =
			flowHolder.observeLong(key)

		fun observeString(key: StringKey): StateFlow<String> =
			flowHolder.observeString(key)

		fun observeInt(key: IntKey): StateFlow<Int> =
			flowHolder.observeInt(key)

		fun observeBoolean(key: BooleanKey): StateFlow<Boolean> =
			flowHolder.observeBoolean(key)

		fun observeStringSet(key: StringSetKey): StateFlow<Set<String>> =
			flowHolder.observeStringSet(key)

		fun observeFloat(key: FloatKey): StateFlow<Float> =
			flowHolder.observeFloat(key)


		fun getLong(key: SettingKey<Long>): Long =
			preferences.getLong(key.name, key.default)

		fun getString(key: StringKey): String =
			preferences.getString(key.name, key.default) ?: ""

		fun getInt(key: IntKey): Int =
			preferences.getInt(key.name, key.default)

		fun getBoolean(key: BooleanKey): Boolean =
			preferences.getBoolean(key.name, key.default)

		fun getStringSet(key: StringSetKey): Set<String> =
			preferences.getStringSet(key.name, key.default) ?: setOf()

		fun getFloat(key: FloatKey): Float =
			try {
				preferences.getFloat(key.name, key.default)
			} catch (e: ClassCastException) {
				setFloat(key, key.default)
				key.default
			}

		fun setLong(key: SettingKey<Long>, value: Long): Unit =
			preferences.edit { putLong(key.name, value) }

		fun setString(key: StringKey, value: String): Unit =
			preferences.edit { putString(key.name, value) }

		fun setInt(key: IntKey, value: Int): Unit =
			preferences.edit { putInt(key.name, value) }

		fun setBoolean(key: BooleanKey, value: Boolean): Unit =
			preferences.edit { putBoolean(key.name, value) }

		fun setStringSet(key: StringSetKey, value: Set<String>): Unit =
			preferences.edit { putStringSet(key.name, value) }

		fun setFloat(key: FloatKey, value: Float): Unit =
			preferences.edit { putFloat(key.name, value) }

		private inner class SharedPreferenceFlowState :
			SharedPreferences.OnSharedPreferenceChangeListener {

			init {
				preferences.registerOnSharedPreferenceChangeListener(this)
			}

			private fun <K, V> lazyHashMapOf(): Lazy<HashMap<K, V>> = lazy { hashMapOf() }

			private val longMap: SettingKeyFlowMap<Long> by lazyHashMapOf()

			private val stringMap: SettingKeyFlowMap<String> by lazyHashMapOf()

			private val intMap: SettingKeyFlowMap<Int> by lazyHashMapOf()

			private val booleanMap: SettingKeyFlowMap<Boolean> by lazyHashMapOf()

			private val stringSetMap: SettingKeyFlowMap<Set<String>> by lazyHashMapOf()

			private val floatMap: SettingKeyFlowMap<Float> by lazyHashMapOf()

			fun observeLong(key: SettingKey<Long>): StateFlow<Long> =
				longMap.getOrPut(key) {
					MutableStateFlow(getLong(key)).also {
						longMap[key] = it
					}
				}

			fun observeString(key: StringKey): StateFlow<String> =
				stringMap.getOrPut(key) {
					MutableStateFlow(getString(key)).also {
						stringMap[key] = it
					}
				}

			fun observeInt(key: IntKey): StateFlow<Int> =
				intMap.getOrPut(key) {
					MutableStateFlow(getInt(key)).also {
						intMap[key] = it
					}
				}

			fun observeBoolean(key: BooleanKey): StateFlow<Boolean> =
				booleanMap.getOrPut(key) {
					MutableStateFlow(getBoolean(key)).also {
						booleanMap[key] = it
					}
				}

			fun observeStringSet(key: StringSetKey): StateFlow<Set<String>> =
				stringSetMap.getOrPut(key) {
					MutableStateFlow(getStringSet(key)).also {
						stringSetMap[key] = it
					}
				}

			fun observeFloat(key: FloatKey): StateFlow<Float> =
				floatMap.getOrPut(key) {
					MutableStateFlow(getFloat(key)).also {
						floatMap[key] = it
					}
				}

			override fun onSharedPreferenceChanged(sp: SharedPreferences?, s: String) {
				// Evaluate what setting key [s] corresponds to
				val key: SettingKey<*> =
					SettingKey.valueOf(s) ?: when (s.substringBefore("_")) {
						"int" -> SettingKey.CustomInt(s.substringAfter("_"), 0)
						"string" -> SettingKey.CustomString(s.substringAfter("_"), "")
						"boolean" -> SettingKey.CustomBoolean(s.substringAfter("_"), false)
						"long" -> SettingKey.CustomLong(s.substringAfter("_"), 0L)
						"float" -> SettingKey.CustomFloat(s.substringAfter("_"), 0f)
						"stringSet" -> SettingKey.CustomStringSet(s.substringAfter("_"), setOf())
						else -> {
							logE("No SettingKey has been found matching ($s)")
							return
						}
					}

				when (key.default) {
					is String -> {
						@Suppress("UNCHECKED_CAST")
						key as StringKey
						val value = getString(key)
						stringMap[key]?.let { it.value = value } ?: MutableStateFlow(value).also {
							stringMap[key] = it
						}
					}

					is Int -> {
						@Suppress("UNCHECKED_CAST")
						key as IntKey
						val value = getInt(key)
						intMap[key]?.let { it.value = value } ?: MutableStateFlow(value).also {
							intMap[key] = it
						}
					}

					is Boolean -> {
						@Suppress("UNCHECKED_CAST")
						key as BooleanKey
						val value = getBoolean(key)
						booleanMap[key]?.let { it.value = value } ?: MutableStateFlow(value).also {
							booleanMap[key] = it
						}
					}

					is Long -> {
						@Suppress("UNCHECKED_CAST")
						key as SettingKey<Long>
						val value = getLong(key)
						longMap[key]?.let { it.value = value } ?: MutableStateFlow(value).also {
							longMap[key] = it
						}
					}

					is Float -> {
						@Suppress("UNCHECKED_CAST")
						key as FloatKey
						val value = getFloat(key)
						floatMap[key]?.let { it.value = value } ?: MutableStateFlow(value).also {
							floatMap[key] = it
						}
					}

					is Set<*> -> {
						@Suppress("UNCHECKED_CAST")
						key as StringSetKey
						val value = getStringSet(key)
						stringSetMap[key]?.let { it.value = value }
							?: MutableStateFlow(value).also {
								stringSetMap[key] = it
							}
					}
				}
			}
		}
	}


	private fun getPreferences(name: String): PreferenceHolder =
		preferenceMap.getOrPut(name) {
			PreferenceHolder(context.getSharedPreferences(name, 0))
		}


	fun observeLong(name: String, key: SettingKey<Long>): StateFlow<Long> =
		getPreferences(name).observeLong(key)

	fun observeString(name: String, key: StringKey): StateFlow<String> =
		getPreferences(name).observeString(key)

	fun observeInt(name: String, key: IntKey): StateFlow<Int> =
		getPreferences(name).observeInt(key)

	fun observeBoolean(name: String, key: BooleanKey): StateFlow<Boolean> =
		getPreferences(name).observeBoolean(key)

	fun observeStringSet(name: String, key: StringSetKey): StateFlow<Set<String>> =
		getPreferences(name).observeStringSet(key)

	fun observeFloat(name: String, key: FloatKey): StateFlow<Float> =
		getPreferences(name).observeFloat(key)


	fun getLong(name: String, key: SettingKey<Long>): Long =
		getPreferences(name).getLong(key)


	fun getString(name: String, key: StringKey): String =
		getPreferences(name).getString(key)

	fun getInt(name: String, key: IntKey): Int =
		getPreferences(name).getInt(key)


	fun getBoolean(name: String, key: BooleanKey): Boolean =
		getPreferences(name).getBoolean(key)


	fun getStringSet(name: String, key: StringSetKey): Set<String> =
		getPreferences(name).getStringSet(key)


	fun getFloat(name: String, key: FloatKey): Float =
		getPreferences(name).getFloat(key)


	fun setLong(name: String, key: SettingKey<Long>, value: Long): Unit =
		getPreferences(name).setLong(key, value)

	fun setString(name: String, key: StringKey, value: String): Unit =
		getPreferences(name).setString(key, value)

	fun setInt(name: String, key: IntKey, value: Int): Unit =
		getPreferences(name).setInt(key, value)

	fun setBoolean(name: String, key: BooleanKey, value: Boolean): Unit =
		getPreferences(name).setBoolean(key, value)

	fun setStringSet(name: String, key: StringSetKey, value: Set<String>): Unit =
		getPreferences(name).setStringSet(key, value)

	fun setFloat(name: String, key: FloatKey, value: Float): Unit =
		getPreferences(name).setFloat(key, value)

}


