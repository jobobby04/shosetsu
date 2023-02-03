/*
 * This file is part of Shosetsu.
 *
 * Shosetsu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Shosetsu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Shosetsu.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package app.shosetsu.android.view.compose

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
@NonRestartableComposable
fun Surface(
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	onLongClick: (() -> Unit)? = null,
	shape: Shape = RectangleShape,
	color: Color = MaterialTheme.colorScheme.surface,
	contentColor: Color = contentColorFor(color),
	border: BorderStroke? = null,
	tonalElevation: Dp = 0.dp,
	shadowElevation: Dp = 0.dp,
	interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
	enabled: Boolean = true,
	content: @Composable () -> Unit,
) {
	val absoluteElevation = LocalAbsoluteTonalElevation.current + tonalElevation
	CompositionLocalProvider(
		LocalContentColor provides contentColor,
		LocalAbsoluteTonalElevation provides absoluteElevation
	) {
		Box(
			modifier
				.minimumTouchTargetSize()
				.surface(
					shape = shape,
					backgroundColor = surfaceColorAtElevation(
						color = color,
						elevation = absoluteElevation
					),
					border = border,
					shadowElevation = shadowElevation
				)
				.combinedClickable(
					interactionSource = interactionSource,
					indication = rememberRipple(),
					enabled = enabled,
					role = Role.Button,
					onClick = onClick,
					onLongClick = onLongClick
				),
			propagateMinConstraints = true
		) {
			content()
		}
	}
}

private fun Modifier.surface(
	shape: Shape,
	backgroundColor: Color,
	border: BorderStroke?,
	shadowElevation: Dp
) = this.shadow(shadowElevation, shape, clip = false)
	.then(if (border != null) Modifier.border(border, shape) else Modifier)
	.background(color = backgroundColor, shape = shape)
	.clip(shape)

@Composable
private fun surfaceColorAtElevation(color: Color, elevation: Dp): Color {
	return if (color == MaterialTheme.colorScheme.surface) {
		MaterialTheme.colorScheme.surfaceColorAtElevation(elevation)
	} else {
		color
	}
}