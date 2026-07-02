package app.shosetsu.android.view.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.shosetsu.android.R
import kotlin.random.Random

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

@Composable
fun ImageLoadingError(
	modifier: Modifier = Modifier
		.fillMaxSize()
) {
	Box(
		modifier = modifier then Modifier
			.background(Color(0x1F888888)),
		contentAlignment = Alignment.Center
	) {
		Icon(
			Icons.Filled.BrokenImage,
			contentDescription = stringResource(R.string.image_description_broken),
			tint = Color(0x1F888888),
			modifier = Modifier.size(24.dp)
		)
	}
}

@Composable
fun ImageLoadingError(
	text: String?, modifier: Modifier = Modifier
		.fillMaxSize()
) {
	if (text.isNullOrBlank()) {
		ImageLoadingError(modifier)
		return
	}
	val hue = remember(text) { (0..360).random(Random(text.hashCode())).toFloat() }
	fun gradient(saturation: Float, value: Float) = Brush.horizontalGradient(
		listOf(
			Color.hsv(hue, saturation, value),
			Color.hsv((hue + 35) % 360, saturation, value)
		)
	)

	val foreground = remember(hue) { gradient(0.7f, 0.7f) }
	val background = remember(hue) { gradient(0.5f, 0.1f) }
	val density = LocalDensity.current
	var boxSize by remember { mutableStateOf(IntSize.Zero) }
	Box(
		modifier = modifier then Modifier
			.background(background)
			.onSizeChanged { boxSize = it }
			.padding(horizontal = 2.dp),
		contentAlignment = Alignment.Center
	) {
		val fontSize = remember(boxSize, density) {
			if (boxSize.width > 0) {
				with(density) { (boxSize.width / 5f).toSp() }
			} else {
				16.sp
			}
		}
		val style = LocalTextStyle.current.merge(
			fontSize = fontSize,
			lineHeight = fontSize,
		).merge(SpanStyle(brush = foreground))
		val maxLines = remember(boxSize, density, fontSize, style) {
			if (boxSize.height > 0) {
				with(density) {
					(boxSize.height / (style.lineHeight.toPx()))
						.toInt()
						.coerceAtLeast(1)
				}
			} else {
				1
			}
		}
		Text(
			text = text,
			maxLines = maxLines,
			overflow = TextOverflow.Ellipsis,
			style = style,
		)
	}
}
