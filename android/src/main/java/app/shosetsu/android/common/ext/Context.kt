package app.shosetsu.android.common.ext

import android.Manifest.permission.POST_NOTIFICATIONS
import android.Manifest.permission.WAKE_LOCK
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.content.res.Resources
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.TIRAMISU
import android.widget.Toast.LENGTH_SHORT
import android.widget.Toast.makeText
import androidx.annotation.StringRes
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat

/*
 * This file is part of shosetsu.
 *
 * shosetsu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * shosetsu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with shosetsu.  If not, see <https://www.gnu.org/licenses/>.
 * ====================================================================
 */

/**
 * shosetsu
 * 07 / 02 / 2020
 *
 * @author github.com/doomsdayrs
 * <p>
 *     I have to admit to copying tachiyomi ;-;
 * </p>
 */


/**
 * Display a toast in this context.
 *
 * @param resource the text resource.
 * @param duration the duration of the toast. Defaults to short.
 */
fun Context.toast(@StringRes resource: Int, duration: Int = LENGTH_SHORT) {
	try {
		makeText(this, resource, duration).show()
	} catch (e: Resources.NotFoundException) {
		logE("NotFoundException", e)
	}
}

fun Context.toast(string: String, duration: Int = LENGTH_SHORT) {
	makeText(this, string, duration).show()
}

fun Context.checkActivitySelfPermission(permission: String): Int =
	ActivityCompat.checkSelfPermission(this, permission)

/**
 * Property to get the notification manager from the context.
 */
val Context.notificationManager: NotificationManagerCompat
	get() = NotificationManagerCompat.from(this)

fun Context.requestPerms() {
	if (
		(SDK_INT >= TIRAMISU && checkActivitySelfPermission(POST_NOTIFICATIONS) != PERMISSION_GRANTED) ||
		checkActivitySelfPermission(WAKE_LOCK) != PERMISSION_GRANTED
	) {
		ActivityCompat.requestPermissions(
			this as Activity,
			if (SDK_INT >= TIRAMISU) {
				arrayOf(
					WAKE_LOCK,
					POST_NOTIFICATIONS,
				)
			} else {
				arrayOf(
					WAKE_LOCK,
				)
			},
			1
		)
	}
}