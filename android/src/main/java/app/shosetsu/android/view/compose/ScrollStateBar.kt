package app.shosetsu.android.view.compose

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

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
 * @since 12 / 04 / 2022
 * @author Doomsdayrs
 */

@Composable
fun ScrollStateBar(scrollState: ScrollState, content: @Composable () -> Unit) {
	BoxWithConstraints(
		modifier = Modifier.fillMaxSize()
	) {
		val viewMaxHeight = constraints.maxHeight.toFloat() - with(LocalDensity.current) {
			16.dp.toPx() // remove scrollbar height
		}

		// Ensure divide by zero does not occur
		val scrollPercentage =
			if (scrollState.maxValue != 0) {
				scrollState.value / scrollState.maxValue.toFloat()
			} else {
				0f
			}

		val paddingSize = (scrollPercentage * viewMaxHeight)

		content()

		Box(
			modifier = Modifier
				.align(Alignment.TopEnd)
				.graphicsLayer {
					translationY = paddingSize
				}
				.padding(horizontal = 4.dp)
				.background(MaterialTheme.colorScheme.primary)
				.width(2.dp)
				.height(16.dp),
		)
	}
}
