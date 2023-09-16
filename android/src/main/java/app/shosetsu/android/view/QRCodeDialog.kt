package app.shosetsu.android.view

import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import app.shosetsu.android.R
import app.shosetsu.android.view.uimodels.model.QRCodeData

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
 * @since 06 / 03 / 2022
 * @author Doomsdayrs
 */

@Preview
@Composable
fun PreviewLoadingQRCodeShareDialog() {
	QRCodeShareDialog(
		null,
		hide = {},
		title = "Test"
	)
}

@Preview
@Composable
fun PreviewQRCodeShareDialog() {
	QRCodeShareDialog(
		QRCodeData(
			ImageBitmap(50, 50),
			"url"
		),
		hide = {},
		title = "Test"
	)
}

@Composable
@OptIn(ExperimentalAnimationGraphicsApi::class)
fun QRCodeShareDialog(
	qrCodeData: QRCodeData?,
	hide: () -> Unit,
	title: String? = null
) {
	Dialog(
		onDismissRequest = hide
	) {
		Card {
			Column(
				modifier = Modifier
					.padding(16.dp)
					.fillMaxWidth(),
				horizontalAlignment = Alignment.CenterHorizontally,
				verticalArrangement = Arrangement.spacedBy(8.dp)
			) {
				if (title != null)
					Text(title, style = MaterialTheme.typography.titleLarge)
				val clipboard = LocalClipboardManager.current

				Box(
					modifier = Modifier
						.aspectRatio(1.0f)
						.fillMaxWidth(),
					contentAlignment = Alignment.Center
				) {
					if (qrCodeData != null) {
						Image(
							qrCodeData.imageBitmap,
							"",
							modifier = Modifier
								.background(androidx.compose.ui.graphics.Color.White)
								.padding(16.dp)
								.aspectRatio(1.0f)
								.fillMaxSize()
						)
					} else {
						val image =
							AnimatedImageVector.animatedVectorResource(R.drawable.animated_refresh)

						Image(
							rememberAnimatedVectorPainter(image, false),
							stringResource(R.string.loading),
						)
					}
				}

				TextButton(
					onClick = {
						clipboard.setText(AnnotatedString(qrCodeData!!.data))
					},
					enabled = qrCodeData != null
				) {
					Text(stringResource(android.R.string.copy))
				}
			}
		}
	}
}