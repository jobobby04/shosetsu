package app.shosetsu.android.viewmodel.impl.settings

import android.annotation.SuppressLint
import android.app.Application
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.shosetsu.android.R
import app.shosetsu.android.common.SettingKey.*
import app.shosetsu.android.domain.repository.base.ISettingsRepository
import app.shosetsu.android.domain.usecases.load.LoadReaderThemes
import app.shosetsu.android.view.compose.setting.FloatSliderSettingContent
import app.shosetsu.android.view.compose.setting.SliderSettingContent
import app.shosetsu.android.view.compose.setting.SwitchSettingContent
import app.shosetsu.android.view.uimodels.StableHolder
import app.shosetsu.android.view.uimodels.model.ColorChoiceUI
import app.shosetsu.android.viewmodel.abstracted.settings.AReaderSettingsViewModel
import app.shosetsu.android.viewmodel.base.ExposedSettingsRepoViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

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
 * shosetsu
 * 31 / 08 / 2020
 */
class ReaderSettingsViewModel(
	iSettingsRepository: ISettingsRepository,
	private val app: Application,
	val loadReaderThemes: LoadReaderThemes
) : AReaderSettingsViewModel(iSettingsRepository) {

	override fun getReaderThemes(): Flow<List<ColorChoiceUI>> =
		loadReaderThemes().combine(settingsRepo.getIntFlow(ReaderTheme)) { a, b ->
			a.map { if (it.id == b.toLong()) it.copy(isSelected = true) else it }
		}.onIO()

}

@SuppressLint("ComposableNaming")
@Composable
fun ExposedSettingsRepoViewModel.stringAsHtmlOption() {
	SwitchSettingContent(
		stringResource(R.string.settings_reader_title_string_to_html),
		stringResource(R.string.settings_reader_desc_string_to_html),
		settingsRepo,
		ReaderStringToHtml, modifier = Modifier
			.fillMaxWidth()
	)
}

@SuppressLint("ComposableNaming")
@Composable
fun ExposedSettingsRepoViewModel.horizontalSwitchOption() {
	SwitchSettingContent(
		stringResource(R.string.settings_reader_title_horizontal_option),
		stringResource(R.string.settings_reader_desc_horizontal_option),
		settingsRepo,
		ReaderHorizontalPageSwap, modifier = Modifier
			.fillMaxWidth()
	)
}

@SuppressLint("ComposableNaming")
@Composable
fun ExposedSettingsRepoViewModel.invertChapterSwipeOption() {
	SwitchSettingContent(
		stringResource(R.string.settings_reader_inverted_swipe_title),
		stringResource(R.string.settings_reader_inverted_swipe_desc),
		settingsRepo,
		ReaderIsInvertedSwipe, modifier = Modifier
			.fillMaxWidth()
	)
}

@SuppressLint("ComposableNaming")
@Composable
fun ExposedSettingsRepoViewModel.showReaderDivider() {
	SwitchSettingContent(
		stringResource(R.string.settings_reader_show_divider),
		stringResource(R.string.settings_reader_show_divider_desc),
		settingsRepo,
		ReaderShowChapterDivider, modifier = Modifier
			.fillMaxWidth()
	)
}

@SuppressLint("ComposableNaming")
@Composable
fun ExposedSettingsRepoViewModel.enableFullscreen() {
	SwitchSettingContent(
		stringResource(R.string.settings_reader_fullscreen),
		stringResource(R.string.settings_reader_fullscreen_desc),
		settingsRepo,
		ReaderEnableFullscreen,
		modifier = Modifier
			.fillMaxWidth(),
	)
}

@SuppressLint("ComposableNaming")
@Composable
fun ExposedSettingsRepoViewModel.readerTextSelectionToggle() {
	SwitchSettingContent(
		stringResource(R.string.settings_reader_text_sel_title),
		stringResource(R.string.settings_reader_text_sel_desc),
		settingsRepo,
		ReaderDisableTextSelection,
		modifier = Modifier
			.fillMaxWidth(),
	)
}

@SuppressLint("ComposableNaming")
@Composable
fun ExposedSettingsRepoViewModel.matchFullscreenToFocus() {
	val enableFullscreen by remember {
		settingsRepo.getBooleanFlow(ReaderEnableFullscreen)
	}.collectAsState()
	SwitchSettingContent(
		stringResource(R.string.settings_reader_fullscreen_focus),
		stringResource(R.string.settings_reader_fullscreen_focus_desc),
		settingsRepo,
		ReaderMatchFullscreenToFocus, modifier = Modifier
			.fillMaxWidth(),
		enabled = enableFullscreen
	)
}

@SuppressLint("ComposableNaming")
@Composable
fun ExposedSettingsRepoViewModel.doubleTapFocus() {
	SwitchSettingContent(
		stringResource(R.string.settings_reader_double_tap),
		stringResource(R.string.settings_reader_double_tap_desc),
		settingsRepo,
		ReaderDoubleTapFocus, modifier = Modifier
			.fillMaxWidth()
	)
}

@SuppressLint("ComposableNaming")
@Composable
fun ExposedSettingsRepoViewModel.doubleTapSystem() {
	val enableFullscreen by remember {
		settingsRepo.getBooleanFlow(ReaderEnableFullscreen)
	}.collectAsState()
	val matchFullscreenToFocus by remember {
		settingsRepo.getBooleanFlow(ReaderMatchFullscreenToFocus)
	}.collectAsState()

	SwitchSettingContent(
		stringResource(R.string.settings_reader_double_tap_system),
		stringResource(R.string.settings_reader_double_tap_system_desc),
		settingsRepo,
		ReaderDoubleTapSystem, modifier = Modifier
			.fillMaxWidth(),
		enabled = enableFullscreen && !matchFullscreenToFocus
	)
}


@SuppressLint("ComposableNaming")
@Composable
fun ExposedSettingsRepoViewModel.continuousScrollOption() {
	SwitchSettingContent(
		stringResource(R.string.settings_reader_title_continous_scroll),
		stringResource(R.string.settings_reader_desc_continous_scroll),
		settingsRepo,
		ReaderContinuousScroll, modifier = Modifier
			.fillMaxWidth()
	)
}

@SuppressLint("ComposableNaming")
@Composable
fun ExposedSettingsRepoViewModel.tapToScrollOption() {
	SwitchSettingContent(
		stringResource(R.string.settings_reader_tap_to_scroll_title),
		"",
		settingsRepo,
		ReaderIsTapToScroll, modifier = Modifier
			.fillMaxWidth()
	)
}

@SuppressLint("ComposableNaming")
@Composable
fun ExposedSettingsRepoViewModel.readerKeepScreenOnOption() {
	SwitchSettingContent(
		stringResource(R.string.settings_reader_keep_screen_on),
		stringResource(R.string.settings_reader_keep_screen_on_desc),
		settingsRepo,
		ReaderKeepScreenOn, modifier = Modifier
			.fillMaxWidth()
	)
}

@SuppressLint("ComposableNaming")
@Composable
fun ExposedSettingsRepoViewModel.readerTableHackOption() {
	SwitchSettingContent(
		stringResource(R.string.settings_reader_table_hack_title),
		stringResource(R.string.settings_reader_table_hack_desc),
		settingsRepo,
		ReaderTableHack, modifier = Modifier
			.fillMaxWidth()
	)
}

@SuppressLint("ComposableNaming")
@Composable
fun ExposedSettingsRepoViewModel.volumeScrollingOption() {
	SwitchSettingContent(
		stringResource(R.string.settings_reader_volume_scroll_title),
		"",
		settingsRepo,
		ReaderVolumeScroll, modifier = Modifier
			.fillMaxWidth()
	)
}

@SuppressLint("ComposableNaming")
@Composable
fun ExposedSettingsRepoViewModel.trackLongReadingOption() {
	SwitchSettingContent(
		stringResource(R.string.settings_reader_track_long_reading_title),
		stringResource(R.string.settings_reader_track_long_reading_desc),
		settingsRepo,
		ReaderTrackLongReading, modifier = Modifier
			.fillMaxWidth()
	)
}

@SuppressLint("ComposableNaming")
@Composable
fun ExposedSettingsRepoViewModel.textSizeOption() {
	FloatSliderSettingContent(
		stringResource(R.string.text_size),
		"",
		remember { StableHolder(7..50) },
		parseValue = { "$it" },
		settingsRepo,
		ReaderTextSize,
		haveSteps = false,
		flip = true,
		modifier = Modifier
			.fillMaxWidth()
	)
}

@SuppressLint("ComposableNaming")
@Composable
fun ExposedSettingsRepoViewModel.paragraphIndentOption() {
	SliderSettingContent(
		stringResource(R.string.paragraph_indent),
		"",
		remember { StableHolder(0..10) },
		{ "$it" },
		settingsRepo,
		ReaderIndentSize, modifier = Modifier
			.fillMaxWidth()
	)
}

@SuppressLint("ComposableNaming")
@Composable
fun ExposedSettingsRepoViewModel.paragraphSpacingOption() {
	FloatSliderSettingContent(
		stringResource(R.string.paragraph_spacing),
		"",
		remember { StableHolder(0..10) },
		{ "$it" },
		settingsRepo,
		ReaderParagraphSpacing,
		flip = true,
		modifier = Modifier
			.fillMaxWidth()
	)
}

@Composable
fun ExposedSettingsRepoViewModel.readerPitchOption() {
	FloatSliderSettingContent(
		stringResource(R.string.reader_pitch),
		"",
		remember { StableHolder(1..30) },
		{ "${it / 10}" },
		settingsRepo,
		ReaderPitch,
		flip = true,
		modifier = Modifier
			.fillMaxWidth()
	)
}

@Composable
fun ExposedSettingsRepoViewModel.readerSpeedOption() {
	FloatSliderSettingContent(
		stringResource(R.string.reader_speed),
		"",
		remember { StableHolder(1..30) },
		{ "${it / 10}" },
		settingsRepo,
		ReaderSpeed,
		flip = true,
		modifier = Modifier
			.fillMaxWidth()
	)
}