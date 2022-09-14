package app.shosetsu.android.datasource.local.file.base

import app.shosetsu.android.common.SettingKey
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

/**
 * shosetsu
 * 17 / 09 / 2020
 */
interface IFileSettingsDataSource {

	// Observe

	fun observeLong(name: String, key: SettingKey<Long>): StateFlow<Long>

	fun observeString(name: String, key: SettingKey<String>): StateFlow<String>

	fun observeInt(name: String, key: SettingKey<Int>): StateFlow<Int>

	fun observeBoolean(name: String, key: SettingKey<Boolean>): StateFlow<Boolean>

	fun observeStringSet(name: String, key: SettingKey<Set<String>>): StateFlow<Set<String>>

	fun observeFloat(name: String, key: SettingKey<Float>): StateFlow<Float>

	// Get

	suspend fun getLong(name: String, key: SettingKey<Long>): Long

	suspend fun getString(name: String, key: SettingKey<String>): String

	suspend fun getInt(name: String, key: SettingKey<Int>): Int

	suspend fun getBoolean(name: String, key: SettingKey<Boolean>): Boolean

	suspend fun getStringSet(name: String, key: SettingKey<Set<String>>): Set<String>

	suspend fun getFloat(name: String, key: SettingKey<Float>): Float

	// Set

	suspend fun setLong(name: String, key: SettingKey<Long>, value: Long)

	suspend fun setString(name: String, key: SettingKey<String>, value: String)

	suspend fun setInt(name: String, key: SettingKey<Int>, value: Int)

	suspend fun setBoolean(name: String, key: SettingKey<Boolean>, value: Boolean)

	suspend fun setStringSet(
		name: String,
		key: SettingKey<Set<String>>,
		value: Set<String>,
	)

	suspend fun setFloat(name: String, key: SettingKey<Float>, value: Float)

}