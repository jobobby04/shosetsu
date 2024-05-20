package app.shosetsu.android.domain.repository.base

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
 * 09 / 03 / 2021
 *
 * Defines how a setting repository for extensions is.
 * Provides all functions needed for extension settings
 */
interface IExtensionSettingsRepository {
	// -- suspended getters

	suspend fun getInt(extensionID: Int, settingID: Int, default: Int): Int
	suspend fun getString(extensionID: Int, settingID: Int, default: String): String
	suspend fun getBoolean(extensionID: Int, settingID: Int, default: Boolean): Boolean
	suspend fun getFloat(extensionID: Int, settingID: Int, default: Float): Float

	// -- flow getters

	fun getIntFlow(extensionID: Int, settingID: Int, default: Int): StateFlow<Int>
	fun getStringFlow(extensionID: Int, settingID: Int, default: String): StateFlow<String>
	fun getBooleanFlow(extensionID: Int, settingID: Int, default: Boolean): StateFlow<Boolean>
	fun getFloatFlow(extensionID: Int, settingID: Int, default: Float): StateFlow<Float>

	// -- setters

	suspend fun setInt(extensionID: Int, settingID: Int, value: Int)
	suspend fun setString(extensionID: Int, settingID: Int, value: String)
	suspend fun setBoolean(extensionID: Int, settingID: Int, value: Boolean)
	suspend fun setFloat(extensionID: Int, settingID: Int, value: Float)
}