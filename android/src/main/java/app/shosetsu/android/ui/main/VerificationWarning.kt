package app.shosetsu.android.ui.main

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.shosetsu.android.R

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
 * @since 2025/09/27
 * @author Clocks
 */
@Composable
fun VerificationWarning(
	onDismissRequest: () -> Unit
) {
	AlertDialog(
		onDismissRequest = onDismissRequest,
		title = {
			Text(stringResource(R.string.android_verification_warning_title))
		},
		confirmButton = {
			TextButton(onDismissRequest) {
				Text(stringResource(android.R.string.ok))
			}
		},
		text = {
			Text(
				stringResource(R.string.android_verification_warning_desc)
			)
		}
	)
}