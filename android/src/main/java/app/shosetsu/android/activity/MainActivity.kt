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
package app.shosetsu.android.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import app.shosetsu.android.ui.main.MainView
import app.shosetsu.android.ui.theme.ShosetsuTheme

/**
 * Shosetsu
 * 9 / June / 2019
 *
 * @author github.com/doomsdayrs
 */
class MainActivity : ComponentActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		@Suppress("UNUSED_VARIABLE") // We keep this value
		val splashScreen = installSplashScreen()

		super.onCreate(savedInstanceState)

		// Do not let the launcher create a new activity http://stackoverflow.com/questions/16283079
		if (!isTaskRoot) {
			finish()
			return
		}

		setContent {
			ShosetsuTheme {
				MainView()
			}
		}
	}
}