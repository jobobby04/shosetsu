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

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun LongClickTextButton(
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
	LongClickButton(
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
fun LongClickButton(
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	onLongClick: (() -> Unit)? = null,
	enabled: Boolean = true,
	interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
	elevation: ButtonElevation? = ButtonDefaults2.buttonElevation(),
	shape: Shape = MaterialTheme.shapes.small,
	border: BorderStroke? = null,
	colors: ButtonColors = ButtonDefaults2.buttonColors(),
	contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
	content: @Composable RowScope.() -> Unit
) {
	val containerColor = colors.containerColor(enabled).value
	val contentColor = colors.contentColor(enabled).value
	val shadowElevation = elevation?.shadowElevation(enabled, interactionSource)?.value ?: 0.dp
	val tonalElevation = elevation?.tonalElevation(enabled, interactionSource)?.value ?: 0.dp
	Surface(
		onClick = onClick,
		onLongClick = onLongClick,
		color = containerColor,
		contentColor = contentColor,
		tonalElevation = tonalElevation,
		shadowElevation = shadowElevation,
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


/**
 * Represents the elevation for a button in different states.
 *
 * - See [ButtonDefaults.buttonElevation] for the default elevation used in a [LongClickButton].
 * - See [ButtonDefaults.elevatedButtonElevation] for the default elevation used in a
 * [ElevatedButton].
 */
@Stable
class ButtonElevation internal constructor(
	private val defaultElevation: Dp,
	private val pressedElevation: Dp,
	private val focusedElevation: Dp,
	private val hoveredElevation: Dp,
	private val disabledElevation: Dp,
) {
	/**
	 * Represents the tonal elevation used in a button, depending on its [enabled] state and
	 * [interactionSource]. This should typically be the same value as the [shadowElevation].
	 *
	 * Tonal elevation is used to apply a color shift to the surface to give the it higher emphasis.
	 * When surface's color is [ColorScheme.surface], a higher elevation will result in a darker
	 * color in light theme and lighter color in dark theme.
	 *
	 * See [shadowElevation] which controls the elevation of the shadow drawn around the button.
	 *
	 * @param enabled whether the button is enabled
	 * @param interactionSource the [InteractionSource] for this button
	 */
	@Composable
	internal fun tonalElevation(enabled: Boolean, interactionSource: InteractionSource): State<Dp> {
		return animateElevation(enabled = enabled, interactionSource = interactionSource)
	}

	/**
	 * Represents the shadow elevation used in a button, depending on its [enabled] state and
	 * [interactionSource]. This should typically be the same value as the [tonalElevation].
	 *
	 * Shadow elevation is used to apply a shadow around the button to give it higher emphasis.
	 *
	 * See [tonalElevation] which controls the elevation with a color shift to the surface.
	 *
	 * @param enabled whether the button is enabled
	 * @param interactionSource the [InteractionSource] for this button
	 */
	@Composable
	internal fun shadowElevation(
		enabled: Boolean,
		interactionSource: InteractionSource
	): State<Dp> {
		return animateElevation(enabled = enabled, interactionSource = interactionSource)
	}

	@Composable
	private fun animateElevation(
		enabled: Boolean,
		interactionSource: InteractionSource
	): State<Dp> {
		val interactions = remember { mutableStateListOf<Interaction>() }
		LaunchedEffect(interactionSource) {
			interactionSource.interactions.collect { interaction ->
				when (interaction) {
					is HoverInteraction.Enter -> {
						interactions.add(interaction)
					}
					is HoverInteraction.Exit -> {
						interactions.remove(interaction.enter)
					}
					is FocusInteraction.Focus -> {
						interactions.add(interaction)
					}
					is FocusInteraction.Unfocus -> {
						interactions.remove(interaction.focus)
					}
					is PressInteraction.Press -> {
						interactions.add(interaction)
					}
					is PressInteraction.Release -> {
						interactions.remove(interaction.press)
					}
					is PressInteraction.Cancel -> {
						interactions.remove(interaction.press)
					}
				}
			}
		}

		val interaction = interactions.lastOrNull()

		val target =
			if (!enabled) {
				disabledElevation
			} else {
				when (interaction) {
					is PressInteraction.Press -> pressedElevation
					is HoverInteraction.Enter -> hoveredElevation
					is FocusInteraction.Focus -> focusedElevation
					else -> defaultElevation
				}
			}

		val animatable = remember { Animatable(target, Dp.VectorConverter) }

		if (!enabled) {
			// No transition when moving to a disabled state
			LaunchedEffect(target) { animatable.snapTo(target) }
		} else {
			LaunchedEffect(target) {
				val lastInteraction = when (animatable.targetValue) {
					pressedElevation -> PressInteraction.Press(Offset.Zero)
					hoveredElevation -> HoverInteraction.Enter()
					focusedElevation -> FocusInteraction.Focus()
					else -> null
				}
				animatable.animateElevation(
					from = lastInteraction,
					to = interaction,
					target = target
				)
			}
		}

		return animatable.asState()
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other == null || other !is ButtonElevation) return false

		if (defaultElevation != other.defaultElevation) return false
		if (pressedElevation != other.pressedElevation) return false
		if (focusedElevation != other.focusedElevation) return false
		if (hoveredElevation != other.hoveredElevation) return false
		if (disabledElevation != other.disabledElevation) return false

		return true
	}

	override fun hashCode(): Int {
		var result = defaultElevation.hashCode()
		result = 31 * result + pressedElevation.hashCode()
		result = 31 * result + focusedElevation.hashCode()
		result = 31 * result + hoveredElevation.hashCode()
		result = 31 * result + disabledElevation.hashCode()
		return result
	}
}

internal suspend fun Animatable<Dp, *>.animateElevation(
	target: Dp,
	from: Interaction? = null,
	to: Interaction? = null
) {
	val spec = when {
		// Moving to a new state
		to != null -> ElevationDefaults.incomingAnimationSpecForInteraction(to)
		// Moving to default, from a previous state
		from != null -> ElevationDefaults.outgoingAnimationSpecForInteraction(from)
		// Loading the initial state, or moving back to the baseline state from a disabled /
		// unknown state, so just snap to the final value.
		else -> null
	}
	if (spec != null) animateTo(target, spec) else snapTo(target)
}

private object ElevationDefaults {
	/**
	 * Returns the [AnimationSpec]s used when animating elevation to [interaction], either from a
	 * previous [Interaction], or from the default state. If [interaction] is unknown, then
	 * returns `null`.
	 *
	 * @param interaction the [Interaction] that is being animated to
	 */
	fun incomingAnimationSpecForInteraction(interaction: Interaction): AnimationSpec<Dp>? {
		return when (interaction) {
			is PressInteraction.Press -> DefaultIncomingSpec
			is DragInteraction.Start -> DefaultIncomingSpec
			is HoverInteraction.Enter -> DefaultIncomingSpec
			is FocusInteraction.Focus -> DefaultIncomingSpec
			else -> null
		}
	}

	/**
	 * Returns the [AnimationSpec]s used when animating elevation away from [interaction], to the
	 * default state. If [interaction] is unknown, then returns `null`.
	 *
	 * @param interaction the [Interaction] that is being animated away from
	 */
	fun outgoingAnimationSpecForInteraction(interaction: Interaction): AnimationSpec<Dp>? {
		return when (interaction) {
			is PressInteraction.Press -> DefaultOutgoingSpec
			is DragInteraction.Start -> DefaultOutgoingSpec
			is HoverInteraction.Enter -> HoveredOutgoingSpec
			is FocusInteraction.Focus -> DefaultOutgoingSpec
			else -> null
		}
	}
}

private val OutgoingSpecEasing: Easing = CubicBezierEasing(0.40f, 0.00f, 0.60f, 1.00f)

private val DefaultIncomingSpec = TweenSpec<Dp>(
	durationMillis = 120,
	easing = FastOutSlowInEasing
)

private val DefaultOutgoingSpec = TweenSpec<Dp>(
	durationMillis = 150,
	easing = OutgoingSpecEasing
)

private val HoveredOutgoingSpec = TweenSpec<Dp>(
	durationMillis = 120,
	easing = OutgoingSpecEasing
)


object ButtonDefaults2 {
	/**
	 * Creates a [ButtonColors] that represents the default container and content colors used in a
	 * [LongClickButton].
	 *
	 * @param containerColor the container color of this [LongClickButton] when enabled.
	 * @param contentColor the content color of this [LongClickButton] when enabled.
	 * @param disabledContainerColor the container color of this [LongClickButton] when not enabled.
	 * @param disabledContentColor the content color of this [LongClickButton] when not enabled.
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
	 * [LongClickTextButton].
	 *
	 * @param containerColor the container color of this [LongClickTextButton] when enabled
	 * @param contentColor the content color of this [LongClickTextButton] when enabled
	 * @param disabledContainerColor the container color of this [LongClickTextButton] when not enabled
	 * @param disabledContentColor the content color of this [LongClickTextButton] when not enabled
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

	/**
	 * Creates a [ButtonElevation] that will animate between the provided values according to the
	 * Material specification for a [LongClickButton].
	 *
	 * @param defaultElevation the elevation used when the [LongClickButton] is enabled, and has no other
	 * [Interaction]s.
	 * @param pressedElevation the elevation used when this [LongClickButton] is enabled and pressed.
	 * @param focusedElevation the elevation used when the [LongClickButton] is enabled and focused.
	 * @param hoveredElevation the elevation used when the [LongClickButton] is enabled and hovered.
	 * @param disabledElevation the elevation used when the [LongClickButton] is not enabled.
	 */
	@Composable
	fun buttonElevation(
		defaultElevation: Dp = 0.0.dp,
		pressedElevation: Dp = 0.0.dp,
		focusedElevation: Dp = 0.0.dp,
		hoveredElevation: Dp = 1.0.dp,
		disabledElevation: Dp = 0.0.dp,
	): ButtonElevation = ButtonElevation(
		defaultElevation = defaultElevation,
		pressedElevation = pressedElevation,
		focusedElevation = focusedElevation,
		hoveredElevation = hoveredElevation,
		disabledElevation = disabledElevation,
	)
}