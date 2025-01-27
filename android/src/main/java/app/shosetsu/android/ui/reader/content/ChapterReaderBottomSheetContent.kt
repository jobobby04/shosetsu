package app.shosetsu.android.ui.reader.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.shosetsu.android.R
import app.shosetsu.android.view.compose.DiscreteSlider
import app.shosetsu.android.view.compose.setting.GenericBottomSettingLayout
import app.shosetsu.android.view.uimodels.StableHolder
import app.shosetsu.android.view.uimodels.model.NovelReaderSettingUI
import app.shosetsu.android.viewmodel.abstracted.AChapterReaderViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterReaderBottomSheetContent(
	scaffoldState: BottomSheetScaffoldState,
	ttsPlayback: AChapterReaderViewModel.TtsPlayback,
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
		IconButton(onClick = exit) {
			Icon(Icons.Filled.ArrowBack, null)
		}

		Row {
			IconButton(onClick = toggleFocus) {
				Icon(
					painterResource(R.drawable.ic_baseline_visibility_off_24),
					null
				)
			}
			IconButton(onClick = toggleBookmark) {
				Icon(
					painterResource(
						if (!isBookmarked) {
							R.drawable.empty_bookmark
						} else {
							R.drawable.filled_bookmark
						}
					),
					null
				)
			}

			IconButton(onClick = toggleRotationLock) {
				Icon(
					painterResource(
						if (!isRotationLocked)
							R.drawable.ic_baseline_screen_rotation_24
						else R.drawable.ic_baseline_screen_lock_rotation_24
					),
					null
				)
			}

			if (ttsPlayback != AChapterReaderViewModel.TtsPlayback.Playing)
				IconButton(onClick = onPlayTTS) {
					Icon(
						painterResource(R.drawable.ic_baseline_audiotrack_24),
						null
					)
				}

			if (ttsPlayback == AChapterReaderViewModel.TtsPlayback.Playing)
				IconButton(onClick = onPauseTTS) {
					Icon(
						painterResource(R.drawable.ic_pause_circle_outline_24dp),
						null
					)
				}

			if (ttsPlayback != AChapterReaderViewModel.TtsPlayback.Stopped)
				IconButton(onClick = onStopTTS) {
					Icon(
						painterResource(R.drawable.ic_baseline_stop_circle_24),
						null
					)
				}

			if (onShowNavigation != null) {
				IconButton(onClick = onShowNavigation) {
					Icon(
						painterResource(R.drawable.unfold_less),
						null
					)
				}
			}
		}

		IconButton(onClick = {
			coroutineScope.launch {
				if (scaffoldState.bottomSheetState.currentValue != SheetValue.Expanded) {
					scaffoldState.bottomSheetState.expand()
				} else {
					scaffoldState.bottomSheetState.partialExpand()
				}
			}
		}) {
			Icon(
				if (scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
					painterResource(R.drawable.expand_more)
				} else {
					painterResource(R.drawable.expand_less)
				},
				null
			)
		}
	}

	LazyColumn(
		contentPadding = PaddingValues(vertical = 16.dp),
		verticalArrangement = Arrangement.spacedBy(8.dp)
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