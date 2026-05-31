package app.shosetsu.android.ui.reader.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Audiotrack
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.PauseCircle
import androidx.compose.material.icons.outlined.ScreenLockRotation
import androidx.compose.material.icons.outlined.ScreenRotation
import androidx.compose.material.icons.outlined.StopCircle
import androidx.compose.material.icons.outlined.UnfoldLess
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.shosetsu.android.R
import app.shosetsu.android.view.compose.DiscreteSlider
import app.shosetsu.android.view.compose.SimpleIconButton
import app.shosetsu.android.view.compose.setting.GenericBottomSettingLayout
import app.shosetsu.android.view.uimodels.StableHolder
import app.shosetsu.android.view.uimodels.model.NovelReaderSettingUI
import app.shosetsu.android.view.uimodels.model.reader.TTSPlayback
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterReaderBottomSheetContent(
	scaffoldState: BottomSheetScaffoldState,
	ttsPlayback: TTSPlayback,
	isBookmarked: Boolean,
	isRotationLocked: Boolean,
	setting: NovelReaderSettingUI,

	toggleRotationLock: () -> Unit,
	toggleBookmark: () -> Unit,
	exit: () -> Unit,
	onPlayTTS: () -> Unit,
	onPauseTTS: () -> Unit,
	onStopTTS: () -> Unit,
	updateSetting: (NovelReaderSettingUI) -> Unit,
	lowerSheet: LazyListScope.() -> Unit,
	toggleFocus: () -> Unit,
	onShowNavigation: (() -> Unit)?
) {
	val coroutineScope = rememberCoroutineScope()
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.height(56.dp),
		horizontalArrangement = Arrangement.SpaceBetween,
		verticalAlignment = Alignment.CenterVertically
	) {
		SimpleIconButton(Icons.AutoMirrored.Filled.ArrowBack, null, onClick = exit)

		Row {
			SimpleIconButton(
				Icons.Outlined.VisibilityOff,
				null,
				onClick = toggleFocus
			)
			SimpleIconButton(
				if (!isBookmarked) Icons.Outlined.BookmarkBorder
				else Icons.Outlined.Bookmark,
				null,
				onClick = toggleBookmark
			)

			SimpleIconButton(
				if (!isRotationLocked) Icons.Outlined.ScreenRotation
				else Icons.Outlined.ScreenLockRotation,
				null,
				onClick = toggleRotationLock
			)

			if (ttsPlayback != TTSPlayback.Playing)
				SimpleIconButton(
					Icons.Outlined.Audiotrack,
					null,
					onClick = onPlayTTS
				)

			if (ttsPlayback == TTSPlayback.Playing)
				SimpleIconButton(
					Icons.Outlined.PauseCircle,
					null,
					onClick = onPauseTTS
				)

			if (ttsPlayback != TTSPlayback.Stopped)
				SimpleIconButton(
					Icons.Outlined.StopCircle,
					null,
					onClick = onStopTTS
				)

			if (onShowNavigation != null) {
				SimpleIconButton(
					Icons.Outlined.UnfoldLess,
					null,
					onClick = onShowNavigation
				)
			}
		}

		SimpleIconButton(
			if (scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
				Icons.Outlined.ExpandMore
			} else {
				Icons.Outlined.ExpandLess
			},
			null,
			onClick = {
				coroutineScope.launch {
					if (scaffoldState.bottomSheetState.currentValue != SheetValue.Expanded) {
						scaffoldState.bottomSheetState.expand()
					} else {
						scaffoldState.bottomSheetState.partialExpand()
					}
				}
			})
	}

	LazyColumn(
		contentPadding = PaddingValues(vertical = 16.dp)
	) {
		item {
			GenericBottomSettingLayout(
				stringResource(R.string.paragraph_spacing),
				"",
			) {
				DiscreteSlider(
					setting.paragraphSpacingSize,
					"${setting.paragraphSpacingSize}",
					{ it, a ->
						updateSetting(
							setting.copy(
								paragraphSpacingSize = if (!a)
									it.roundToInt().toFloat()
								else it
							)
						)
					},
					remember { StableHolder(0..10) },
				)
			}
		}

		item {
			GenericBottomSettingLayout(
				stringResource(R.string.paragraph_indent),
				"",
			) {
				DiscreteSlider(
					setting.paragraphIndentSize,
					"${setting.paragraphIndentSize}",
					{ it, _ ->
						updateSetting(setting.copy(paragraphIndentSize = it))
					},
					remember { StableHolder(0..10) },
				)
			}
		}
		lowerSheet()
	}
}
