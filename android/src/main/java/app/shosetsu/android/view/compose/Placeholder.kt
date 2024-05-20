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

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.spring
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.isSpecified
import com.google.accompanist.placeholder.PlaceholderDefaults
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.color
import com.google.accompanist.placeholder.material.placeholder

@Suppress("DEPRECATION")
@Deprecated(
	"""
accompanist/placeholder is deprecated and the API is no longer maintained. 
We recommend forking the implementation and customising it to your needs. 
For more information please visit https://google.github.io/accompanist/placeholder
"""
)
fun Modifier.placeholder(
	visible: Boolean,
	color: Color = Color.Unspecified,
	shape: Shape? = null,
	highlight: PlaceholderHighlight? = null,
	placeholderFadeTransitionSpec: @Composable Transition.Segment<Boolean>.() -> FiniteAnimationSpec<Float> = { spring() },
	contentFadeTransitionSpec: @Composable Transition.Segment<Boolean>.() -> FiniteAnimationSpec<Float> = { spring() },
): Modifier = this.composed {
	placeholder(
		visible,
		color = if (color.isSpecified) color else PlaceholderDefaults.color(
			backgroundColor = MaterialTheme.colorScheme.surface,
			contentColor = contentColorFor(backgroundColor = MaterialTheme.colorScheme.surface)
		),
		shape = shape ?: MaterialTheme.shapes.small,
		highlight,
		placeholderFadeTransitionSpec,
		contentFadeTransitionSpec
	)
}