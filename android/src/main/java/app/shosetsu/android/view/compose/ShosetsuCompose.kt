package app.shosetsu.android.view.compose

import androidx.compose.runtime.Composable

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
 * To provide unified connection
 *
 * @since 28 / 06 / 2022
 * @author Doomsdayrs
 */
@Deprecated(
	"Replace with ShosetsuTheme",
	replaceWith = ReplaceWith(
		"ShosetsuTheme(content = content)",
		"app.shosetsu.android.ui.theme.ShosetsuTheme"
	)
)
@Composable
fun ShosetsuCompose(
	content: @Composable () -> Unit
) {
	content()
}