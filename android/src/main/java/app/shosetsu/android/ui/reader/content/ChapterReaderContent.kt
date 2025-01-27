package app.shosetsu.android.ui.reader.content

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.shosetsu.android.R
import app.shosetsu.android.ui.theme.ShosetsuTheme
import app.shosetsu.android.view.uimodels.StableHolder
import app.shosetsu.android.view.uimodels.model.NovelReaderSettingUI
import app.shosetsu.android.viewmodel.abstracted.AChapterReaderViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableSharedFlow
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
@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun PreviewChapterReaderContent() {
	ShosetsuTheme {
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
					pageJumper = StableHolder(MutableSharedFlow()),
					createPage = {
					}
				)
			},
			sheetContent = {
				ChapterReaderBottomSheetContent(
					scaffoldState = it,
					ttsPlayback = AChapterReaderViewModel.TtsPlayback.Stopped,
					isBookmarked = false,
					isRotationLocked = false,
					setting = NovelReaderSettingUI(-1, 0, 0f),
					toggleRotationLock = {},
					toggleBookmark = {},
					exit = {},
					onPlayTTS = {},
					onPauseTTS = {},
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
@OptIn(ExperimentalMaterial3Api::class)
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

	BackHandler(
		scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded
	) {
		scope.launch {
			scaffoldState.bottomSheetState.partialExpand()
		}
	}

	BottomSheetScaffold(
		scaffoldState = scaffoldState,
		sheetContent = {
			sheetContent(scaffoldState)
		},
		sheetPeekHeight = if (!isFocused) BottomSheetDefaults.SheetPeekHeight else 0.dp,
		content = { paddingValues ->
			content(paddingValues)
		},
		sheetShape = RectangleShape,
		sheetDragHandle = null,
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