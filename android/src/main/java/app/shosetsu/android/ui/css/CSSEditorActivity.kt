package app.shosetsu.android.ui.css

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

import android.os.Build
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI

/**
 * Shosetsu
 * 21 / October / 2021
 *
 * @author github.com/doomsdayrs
 */
class CSSEditorActivity : AppCompatActivity(), DIAware {
	override val di: DI by closestDI()

	companion object {
		const val CSS_ID = "css-id"
		const val HELP_WEBSITE = "https://developer.mozilla.org/en-US/docs/Learn/CSS"
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			window.setDecorFitsSystemWindows(false)
		} else {
			window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		}

		setContent {
			CSSEditorView(
				cssId = savedInstanceState?.getInt(CSS_ID, -1) ?: -2,
				onBackPressed = {
					onBackPressedDispatcher.onBackPressed()
				}
			)
		}
	}
}

