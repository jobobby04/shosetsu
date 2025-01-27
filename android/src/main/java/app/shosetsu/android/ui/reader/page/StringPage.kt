package app.shosetsu.android.ui.reader.page

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import app.shosetsu.android.ui.theme.ShosetsuTheme
import app.shosetsu.android.view.compose.ScrollStateBar
import kotlinx.coroutines.launch

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
 *
 * @since 18 / 03 / 2022
 * @author Doomsdayrs
 */

@Preview
@Composable
fun PreviewStringPageContent() {
	ShosetsuTheme {
		StringPageContent(
			content = "la\nla\nla\nla\nla\nla\nla\nla\nla\nla\nla\nla\nla\nla\nla\nla\nla\nla\nla\n",
			progress = 0.0,
			textSize = 16.0f,
			onScroll = {},
			textColor = Color.Black.toArgb(),
			backgroundColor = Color.White.toArgb(),
			disableTextSelection = false,
			onClick = {},
			onDoubleClick = {}
			//isTapToScroll = false
		)
	}
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StringPageContent(
	content: String,
	progress: Double,
	textSize: Float,
	onScroll: (perc: Double) -> Unit,
	textColor: Int,
	backgroundColor: Int,
	disableTextSelection: Boolean,
	onClick: () -> Unit,
	onDoubleClick: () -> Unit
//isTapToScroll: Boolean
) {
	val state = rememberScrollState()
	var first by remember { mutableStateOf(true) }

	if (state.isScrollInProgress)
		DisposableEffect(Unit) {
			onDispose {
				if (state.value != 0)
					onScroll((state.value.toDouble() / state.maxValue))
				else onScroll(0.0)
			}
		}

	@Composable
	fun content() {
		Text(
			content,
			fontSize = textSize.sp,
			modifier = Modifier
				.fillMaxSize()
				.verticalScroll(state)
				.background(Color(backgroundColor)),
			color = Color(textColor)
		)
	}

	ScrollStateBar(state) {
		if (disableTextSelection) {
			content()
		} else {
			SelectionContainer(
				modifier = Modifier.combinedClickable(
					onDoubleClick = onDoubleClick,
					onClick = onClick,
					interactionSource = remember { MutableInteractionSource() },
					indication = null,
				)
			) {
				content()
			}
		}
	}

	// Avoid scrolling when the state has not fully loaded
	if (state.maxValue != 0 && state.maxValue != Int.MAX_VALUE) {
		if (first) {
			LaunchedEffect(progress) {
				launch {
					state.scrollTo((state.maxValue * progress).toInt())
					first = false
				}
			}
		}
	}
}