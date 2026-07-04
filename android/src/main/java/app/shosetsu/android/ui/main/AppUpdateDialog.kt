package app.shosetsu.android.ui.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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

@Preview
@Composable
fun PreviewAppUpdateDialog() {
	AppUpdateDialog(
		AppUpdateEntity(
			"mew",
			100,
			1000,
			url = "hehe",
			archURLs = null,
			notes = buildList {
				repeat(100) {
					add("mew")
				}
			}
		),
		{}
	) { }
}

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
			Column(
				modifier = Modifier
					.graphicsLayer {
						alpha = 0.99f
					}
					.drawWithContent {
						// Draw the text
						drawContent()

						// Draw the fade
						drawRect(
							brush = Brush.verticalGradient(
								colors = listOf(
									Color.Black,
									Color.Transparent
								),
								startY = this.size.height * .8f
							),
							blendMode = BlendMode.DstIn
						)
					}) {

				Text(update.version, style = MaterialTheme.typography.titleMedium)

				if (update.versionCode != -1) {
					Text(stringResource(R.string.update_label_version_code, update.versionCode))
				}

				if (update.commit != -1) {
					Text(stringResource(R.string.update_label_commit, update.commit))
				}

				Text(
					update.notes.joinToString("\n"),
					modifier = Modifier
						.heightIn(max = 200.dp)
						.verticalScroll(rememberScrollState())
				)
			}

		}
	)
}
