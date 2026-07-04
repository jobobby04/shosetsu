package app.shosetsu.android.ui.reader.page

import android.webkit.JavascriptInterface
import app.shosetsu.android.common.ext.logV
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import app.shosetsu.android.common.ext.logI as pLogI

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
 * Represents javascript interface class for [HTMLPage].
 *
 * @since 18 / 03 / 2022
 * @author Doomsdayrs
 *
 * @param onClickMethod called by javascript when the window is clicked in the web view
 * @param onDClickMethod called by javascript when the window is double clicked in the web view
 */
class ShosetsuScript(
	val onClickMethod: (String?) -> Unit,
	val onDClickMethod: () -> Unit,
	val scope: CoroutineScope
) {
	/**
	 * JavaScript function for [onClickMethod], passes event to UI thread.
	 */
	@Suppress("unused")
	@JavascriptInterface
	fun onClick(id: String?) {
		logV("Arguments: id='$id'")
		scope.launch {
			onClickMethod(id)
		}
	}

	/**
	 * JavaScript function for [onDClickMethod], passes event to UI thread.
	 */
	@Suppress("unused")
	@JavascriptInterface
	fun onDClick() {
		scope.launch {
			onDClickMethod()
		}
	}

	/**
	 * JavaScript function for [logI], passes event to UI thread.
	 */
	@Suppress("unused")
	@JavascriptInterface
	fun logI(text: String) {
		pLogI(text)
	}
}
