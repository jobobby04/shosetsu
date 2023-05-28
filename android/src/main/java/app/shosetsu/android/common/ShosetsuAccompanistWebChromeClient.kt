package app.shosetsu.android.common

import android.webkit.ConsoleMessage
import app.shosetsu.android.common.ext.logD
import com.google.accompanist.web.AccompanistWebChromeClient

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
 * @since 09 / 12 / 2022
 * @author Doomsdayrs
 */
class ShosetsuAccompanistWebChromeClient : AccompanistWebChromeClient() {

	/**
	 * Implement logging for [app.shosetsu.android.ui.reader.page.HTMLPage]
	 */
	override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
		val sourceId = consoleMessage?.sourceId()
		val lineNumber = consoleMessage?.lineNumber()
		val message = consoleMessage?.message()
		logD("${sourceId}${lineNumber}:\t${message}")
		return true
	}
}