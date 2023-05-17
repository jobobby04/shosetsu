package app.shosetsu.android.ui.reader.content

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.shosetsu.android.R
import app.shosetsu.android.view.compose.ShosetsuCompose
import app.shosetsu.android.view.uimodels.model.NovelReaderSettingUI
import kotlinx.collections.immutable.persistentListOf
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
 */

/**
 * Shosetsu
 *
 * @since 26 / 05 / 2022
 * @author Doomsdayrs
 */
@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
fun PreviewChapterReaderContent() {
	ShosetsuCompose {
		ChapterReaderContent(
			isFirstFocusProvider = { false },
			onFirstFocus = {},
			isFocused = false,
			content = {
				ChapterReaderPagerContent(
					items = persistentListOf(),
					isHorizontal = false,
					onStopTTS = {},
					markChapterAsCurrent = {},
					onChapterRead = {},
					currentPage = 0,
					onPageChanged = {},
					isSwipeInverted = false,
					paddingValues = PaddingValues(),
					createPage = {
					}
				)
			},
			sheetContent = {
				ChapterReaderBottomSheetContent(
					scaffoldState = it,
					isTTSCapable = false,
					isTTSPlaying = false,
					isBookmarked = false,
					isRotationLocked = false,
					setting = NovelReaderSettingUI(-1, 0, 0f),
					toggleRotationLock = {},
					toggleBookmark = {},
					exit = {},
					onPlayTTS = {},
					onStopTTS = {},
					updateSetting = {},
					lowerSheet = {},
					toggleFocus = {}
				) {}
			}
		)
	}
}

/**
 * Main reader content
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ChapterReaderContent(
	isFocused: Boolean,
	isFirstFocusProvider: () -> Boolean,

	onFirstFocus: () -> Unit,
	content: @Composable (PaddingValues) -> Unit,
	sheetContent: @Composable ColumnScope.(BottomSheetScaffoldState) -> Unit
) {
	val scope = rememberCoroutineScope()
	val scaffoldState = rememberBottomSheetScaffoldState()

	BackHandler(scaffoldState.bottomSheetState.isExpanded) {
		scope.launch {
			scaffoldState.bottomSheetState.collapse()
		}
	}

	BottomSheetScaffold(
		scaffoldState = scaffoldState,
		sheetContent = {
			sheetContent(scaffoldState)
		},
		sheetPeekHeight = if (!isFocused) BottomSheetScaffoldDefaults.SheetPeekHeight else 0.dp,
		content = { paddingValues ->
			content(paddingValues)
		},
		sheetShape = RectangleShape
	)

	if (isFocused && isFirstFocusProvider()) {
		val string = stringResource(R.string.reader_first_focus)
		val dismiss = stringResource(R.string.reader_first_focus_dismiss)
		LaunchedEffect(scaffoldState.snackbarHostState) {
			launch {
				when (scaffoldState.snackbarHostState.showSnackbar(string, dismiss)) {
					SnackbarResult.Dismissed -> onFirstFocus()
					SnackbarResult.ActionPerformed -> onFirstFocus()
				}
			}
		}
	}

}