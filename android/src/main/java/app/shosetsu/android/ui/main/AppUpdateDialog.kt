package app.shosetsu.android.ui.main

import androidx.compose.material.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.shosetsu.android.R
import app.shosetsu.android.domain.model.local.AppUpdateEntity

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
 * @since 26 / 12 / 2023
 * @author Doomsdayrs
 */
@Composable
fun AppUpdateDialog(
	update: AppUpdateEntity,
	onDismissRequest: () -> Unit,
	onUpdate: () -> Unit
) {
	AlertDialog(
		onDismissRequest = onDismissRequest,
		title = {
			Text(stringResource(R.string.update_app_now_question))
		},
		confirmButton = {
			TextButton(
				onClick = {
					onUpdate()
					onDismissRequest()
				}
			) {
				Text(stringResource(R.string.update))
			}
		},
		dismissButton = {
			TextButton(onDismissRequest) {
				Text(stringResource(R.string.update_not_interested))
			}
		},
		text = {
			Text(
				"${update.version}\t${update.versionCode}\n" +
						update.notes.joinToString("\n")
			)
		}
	)
}