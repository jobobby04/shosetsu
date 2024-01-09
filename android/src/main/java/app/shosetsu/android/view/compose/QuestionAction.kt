package app.shosetsu.android.view.compose

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.shosetsu.android.R
import app.shosetsu.android.common.ext.openInWebView

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
 * @since 20 / 12 / 2023
 * @author Doomsdayrs
 */
@Composable
fun HelpButton(helpURL: String) {
	val context = LocalContext.current

	IconButton(
		onClick = {
			context.openInWebView(helpURL)
		}
	) {
		Icon(painterResource(R.drawable.help_outline_24), stringResource(R.string.help))
	}
}