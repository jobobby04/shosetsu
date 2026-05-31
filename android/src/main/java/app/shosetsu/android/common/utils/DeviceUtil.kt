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
 *
 */

package app.shosetsu.android.common.utils

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.os.Build
import androidx.core.content.getSystemService
import app.shosetsu.android.common.ext.logW

object DeviceUtil {

	val isMiui: Boolean by lazy {
		getSystemProperty("ro.miui.ui.version.name")?.isNotEmpty() ?: false
	}

	val isSamsung: Boolean by lazy {
		Build.MANUFACTURER.equals("samsung", ignoreCase = true)
	}

	/**
	 * ActivityManager#isLowRamDevice is based on a system property, which isn't
	 * necessarily trustworthy. 1GB is supposedly the regular threshold.
	 *
	 * Instead, we consider anything with less than 3GB of RAM as low memory
	 * considering how heavy image processing can be.
	 */
	fun isLowRamDevice(context: Context): Boolean {
		val memInfo = ActivityManager.MemoryInfo()
		context.getSystemService<ActivityManager>()!!.getMemoryInfo(memInfo)
		val totalMemBytes = memInfo.totalMem
		return totalMemBytes < 3L * 1024 * 1024 * 1024
	}

	@SuppressLint("PrivateApi")
	private fun getSystemProperty(key: String?): String? {
		return try {
			Class.forName("android.os.SystemProperties")
				.getDeclaredMethod("get", String::class.java)
				.invoke(null, key) as String
		} catch (e: Exception) {
			logW("Unable to use SystemProperties.get()", e)
			null
		}
	}
}
