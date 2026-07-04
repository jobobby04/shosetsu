package app.shosetsu.android.ui.reader.content

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.shosetsu.android.R
import app.shosetsu.android.common.enums.AppThemes
import app.shosetsu.android.ui.theme.ShosetsuTheme
import app.shosetsu.android.view.uimodels.StableHolder
import app.shosetsu.android.view.uimodels.model.ExceptionSnackbarModel
import app.shosetsu.android.view.uimodels.model.NovelReaderSettingUI
import app.shosetsu.android.view.uimodels.model.reader.TTSPlayback
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.acra.ACRA

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
fun PreviewChapterReaderContent() = ShosetsuTheme(AppThemes.LIGHT) {
	ChapterReaderContent(
		isFocused = false,
		isFirstFocusProvider = { false },
		onFirstFocus = {},
		content = { windowPadding, footerPadding ->
			ChapterReaderPager(
				items = persistentListOf(),
				isHorizontal = false,
				onStopTTS = {},
				markChapterAsCurrent = {},
				onChapterRead = {},
				currentPage = 0,
				onPageChanged = {},
				isSwipeInverted = false,
				pageJumper = StableHolder(MutableSharedFlow()),
				createPage = {
				}
			)
		},
		sheetContent = {
			ChapterReaderBottomSheetContent(
				scaffoldState = it,
				ttsPlayback = TTSPlayback.Stopped,
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
		},
		exception = null,
		showTTSClickHint = false
	)
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
	content: @Composable (windowPadding: PaddingValues, footerPadding: PaddingValues) -> Unit,
	sheetContent: @Composable ColumnScope.(BottomSheetScaffoldState) -> Unit,
	exception: ExceptionSnackbarModel?,
	showTTSClickHint: Boolean
) {
	val scope = rememberCoroutineScope()
	val scaffoldState = rememberBottomSheetScaffoldState()
	val context = LocalContext.current

	BackHandler(
		scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded
	) {
		scope.launch {
			scaffoldState.bottomSheetState.partialExpand()
		}
	}

	val insets = WindowInsets.safeDrawing.asPaddingValues()
	BottomSheetScaffold(
		scaffoldState = scaffoldState,
		sheetContent = {
			sheetContent(scaffoldState)
		},
		sheetPeekHeight = if (isFocused) 0.dp else insets.calculateBottomPadding() + BottomSheetDefaults.SheetPeekHeight,
		content = { paddingValues ->
			content(WindowInsets.safeDrawing.asPaddingValues(), paddingValues)
		},
		sheetShape = RectangleShape,
		sheetDragHandle = null,
		snackbarHost = {
			SnackbarHost(
				it,
				modifier = Modifier.windowInsetsPadding(
					WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)
				)
			)
		}
	)

	LaunchedEffect(exception) {
		val exception = exception
		if (exception != null) {
			// We can only show reporting if ACRA is initialized
			if (exception.exception != null && ACRA.isInitialised) {
				scope.launch {
					val result = scaffoldState.snackbarHostState.showSnackbar(
						exception.displayText,
						actionLabel = context.getString(R.string.report)
					)

					if (result == SnackbarResult.ActionPerformed) {
						ACRA.errorReporter.handleException(exception.exception, false)
					}
				}
			} else {
				scope.launch {
					scaffoldState.snackbarHostState.showSnackbar(exception.displayText)
				}
			}
		}
	}

	// Consume the TTS show boolean
	LaunchedEffect(showTTSClickHint) {
		// Store the boolean
		val showTTSClickHint = showTTSClickHint

		// Is it true?
		if (showTTSClickHint) {
			// Launch the new job
			scope.launch {
				// Show the indefinite snackbar
				scaffoldState.snackbarHostState.showSnackbar(
					context.getString(R.string.reader_hint_pause_to_change),
					duration = SnackbarDuration.Indefinite,
					withDismissAction = true
				)
			}
		}
	}

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
