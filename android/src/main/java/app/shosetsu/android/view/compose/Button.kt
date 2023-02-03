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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape

@Composable
fun TextButton(
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	onLongClick: (() -> Unit)? = null,
	enabled: Boolean = true,
	shape: Shape = ButtonDefaults.textShape,
	colors: ButtonColors = ButtonDefaults2.textButtonColors(),
	elevation: ButtonElevation? = null,
	border: BorderStroke? = null,
	contentPadding: PaddingValues = ButtonDefaults.TextButtonContentPadding,
	interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
	content: @Composable RowScope.() -> Unit,
) =
	Button(
		onClick = onClick,
		modifier = modifier,
		onLongClick = onLongClick,
		enabled = enabled,
		interactionSource = interactionSource,
		elevation = elevation,
		shape = shape,
		border = border,
		colors = colors,
		contentPadding = contentPadding,
		content = content,
	)

@Composable
fun Button(
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	onLongClick: (() -> Unit)? = null,
	enabled: Boolean = true,
	interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
	elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
	shape: Shape = MaterialTheme.shapes.small,
	border: BorderStroke? = null,
	colors: ButtonColors = ButtonDefaults2.buttonColors(),
	contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
	content: @Composable RowScope.() -> Unit
) {
	val containerColor = colors.containerColor(enabled).value
	val contentColor = colors.contentColor(enabled).value
	Surface(
		onClick = onClick,
		onLongClick = onLongClick,
		color = containerColor,
		contentColor = contentColor,
		modifier = modifier,
		enabled = enabled,
		shape = shape,
		border = border,
		interactionSource = interactionSource,
	) {
		CompositionLocalProvider(LocalContentColor provides contentColor) {
			ProvideTextStyle(value = MaterialTheme.typography.labelLarge) {
				Row(
					Modifier
						.defaultMinSize(
							minWidth = ButtonDefaults.MinWidth,
							minHeight = ButtonDefaults.MinHeight,
						)
						.padding(contentPadding),
					horizontalArrangement = Arrangement.Center,
					verticalAlignment = Alignment.CenterVertically,
					content = content,
				)
			}
		}
	}
}

@Immutable
class ButtonColors internal constructor(
	private val containerColor: Color,
	private val contentColor: Color,
	private val disabledContainerColor: Color,
	private val disabledContentColor: Color,
) {
	/**
	 * Represents the container color for this button, depending on [enabled].
	 *
	 * @param enabled whether the button is enabled
	 */
	@Composable
	internal fun containerColor(enabled: Boolean): State<Color> {
		return rememberUpdatedState(if (enabled) containerColor else disabledContainerColor)
	}

	/**
	 * Represents the content color for this button, depending on [enabled].
	 *
	 * @param enabled whether the button is enabled
	 */
	@Composable
	internal fun contentColor(enabled: Boolean): State<Color> {
		return rememberUpdatedState(if (enabled) contentColor else disabledContentColor)
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other == null || other !is ButtonColors) return false

		if (containerColor != other.containerColor) return false
		if (contentColor != other.contentColor) return false
		if (disabledContainerColor != other.disabledContainerColor) return false
		if (disabledContentColor != other.disabledContentColor) return false

		return true
	}

	override fun hashCode(): Int {
		var result = containerColor.hashCode()
		result = 31 * result + contentColor.hashCode()
		result = 31 * result + disabledContainerColor.hashCode()
		result = 31 * result + disabledContentColor.hashCode()
		return result
	}
}

object ButtonDefaults2 {
	/**
	 * Creates a [ButtonColors] that represents the default container and content colors used in a
	 * [Button].
	 *
	 * @param containerColor the container color of this [Button] when enabled.
	 * @param contentColor the content color of this [Button] when enabled.
	 * @param disabledContainerColor the container color of this [Button] when not enabled.
	 * @param disabledContentColor the content color of this [Button] when not enabled.
	 */
	@Composable
	fun buttonColors(
		containerColor: Color = MaterialTheme.colorScheme.primary,
		contentColor: Color = MaterialTheme.colorScheme.onPrimary,
		disabledContainerColor: Color =
			MaterialTheme.colorScheme.onSurface
				.copy(alpha = 0.12f),
		disabledContentColor: Color = MaterialTheme.colorScheme.onSurface
			.copy(alpha = 0.38f),
	): ButtonColors = ButtonColors(
		containerColor = containerColor,
		contentColor = contentColor,
		disabledContainerColor = disabledContainerColor,
		disabledContentColor = disabledContentColor
	)

	/**
	 * Creates a [ButtonColors] that represents the default container and content colors used in a
	 * [TextButton].
	 *
	 * @param containerColor the container color of this [TextButton] when enabled
	 * @param contentColor the content color of this [TextButton] when enabled
	 * @param disabledContainerColor the container color of this [TextButton] when not enabled
	 * @param disabledContentColor the content color of this [TextButton] when not enabled
	 */
	@Composable
	fun textButtonColors(
		containerColor: Color = Color.Transparent,
		contentColor: Color = MaterialTheme.colorScheme.primary,
		disabledContainerColor: Color = Color.Transparent,
		disabledContentColor: Color = MaterialTheme.colorScheme.onSurface
			.copy(alpha = 0.38f),
	): ButtonColors = ButtonColors(
		containerColor = containerColor,
		contentColor = contentColor,
		disabledContainerColor = disabledContainerColor,
		disabledContentColor = disabledContentColor
	)
}