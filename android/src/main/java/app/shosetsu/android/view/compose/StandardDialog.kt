package app.shosetsu.android.view.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import app.shosetsu.android.ui.theme.ShosetsuTheme

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
 * @since 22 / 12 / 2023
 * @author Doomsdayrs
 */
@Composable
fun StandardDialog(
	onDismissRequest: () -> Unit,
	dialogProperties: DialogProperties = DialogProperties(),
	title: @Composable () -> Unit = {},
	onConfirm: () -> Unit = onDismissRequest,
	confirmButton: @Composable (onConfirm: (() -> Unit)) -> Unit = {
		TextButton(onConfirm) {
			Text(stringResource(android.R.string.ok))
		}
	},
	onCancel: () -> Unit = onDismissRequest,
	cancelButton: @Composable (onCancel: () -> Unit) -> Unit = {
		TextButton(onCancel) {
			Text(stringResource(android.R.string.cancel))
		}
	},
	content: @Composable () -> Unit,
) {
	Dialog(onDismissRequest, dialogProperties) {
		Card {
			Column(
				Modifier.padding(16.dp)
			) {
				ProvideTextStyle(MaterialTheme.typography.titleLarge) {
					title()
				}

				content()

				Row(
					horizontalArrangement = Arrangement.End,
					verticalAlignment = Alignment.CenterVertically,
					modifier = Modifier.align(Alignment.End)
				) {
					cancelButton(onCancel)
					confirmButton(onConfirm)
				}
			}
		}
	}
}

@Preview
@Composable
fun PreviewStandardDialog() {
	ShosetsuTheme {
		StandardDialog(
			onDismissRequest = {},
			title = {
				Text("Test Title")
			}
		) {
			Box(Modifier.size(100.dp))
		}
	}
}