package app.shosetsu.android.ui.main

import android.annotation.SuppressLint
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import app.shosetsu.android.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
 */

/**
 * Shosetsu
 *
 * @since 09 / 01 / 2024
 * @author Doomsdayrs
 */
@Composable
fun ShosetsuBackHandler(
	navController: NavHostController,
	protectBack: Boolean,
	isDrawerOpen: Boolean,
	onCloseDrawer: suspend () -> Unit
) {
	@SuppressLint("RestrictedApi") // fuck u google devs
	val backStack by navController.currentBackStack.collectAsState()

	val scope = rememberCoroutineScope()
	val context = LocalContext.current

	var protect by remember { mutableStateOf(true) }

	BackHandler(isDrawerOpen || protectBack && protect && backStack.size == 2) {
		// If drawer is open, close it
		if (isDrawerOpen) {
			scope.launch {
				onCloseDrawer()
			}
			return@BackHandler
		}

		if (protectBack) {
			protect = false

			val toast = Toast.makeText(
				context,
				R.string.double_back_message,
				Toast.LENGTH_SHORT
			)

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
				toast.addCallback(object : Toast.Callback() {
					override fun onToastHidden() {
						super.onToastHidden()
						protect = true
					}
				})
			} else {
				// If we can't observe the toast callback, we can assume 2 seconds to reset
				scope.launch {
					delay(2000)
					protect = true
				}
			}

			toast.show()
		}
	}
}