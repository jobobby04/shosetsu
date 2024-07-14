package app.shosetsu.android.viewmodel.impl.settings

import android.annotation.SuppressLint
import android.app.Application
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import app.shosetsu.android.R
import app.shosetsu.android.common.SettingKey
import app.shosetsu.android.common.SettingKey.ReaderContinuousScroll
import app.shosetsu.android.common.SettingKey.ReaderDisableTextSelection
import app.shosetsu.android.common.SettingKey.ReaderDoubleTapFocus
import app.shosetsu.android.common.SettingKey.ReaderDoubleTapSystem
import app.shosetsu.android.common.SettingKey.ReaderEnableFullscreen
import app.shosetsu.android.common.SettingKey.ReaderEngine
import app.shosetsu.android.common.SettingKey.ReaderHorizontalPageSwap
import app.shosetsu.android.common.SettingKey.ReaderIndentSize
import app.shosetsu.android.common.SettingKey.ReaderIsInvertedSwipe
import app.shosetsu.android.common.SettingKey.ReaderIsTapToScroll
import app.shosetsu.android.common.SettingKey.ReaderKeepScreenOn
import app.shosetsu.android.common.SettingKey.ReaderLanguage
import app.shosetsu.android.common.SettingKey.ReaderMatchFullscreenToFocus
import app.shosetsu.android.common.SettingKey.ReaderNextChapter
import app.shosetsu.android.common.SettingKey.ReaderParagraphSpacing
import app.shosetsu.android.common.SettingKey.ReaderPitch
import app.shosetsu.android.common.SettingKey.ReaderShowChapterDivider
import app.shosetsu.android.common.SettingKey.ReaderSpeed
import app.shosetsu.android.common.SettingKey.ReaderStringToHtml
import app.shosetsu.android.common.SettingKey.ReaderTableHack
import app.shosetsu.android.common.SettingKey.ReaderTextSize
import app.shosetsu.android.common.SettingKey.ReaderTheme
import app.shosetsu.android.common.SettingKey.ReaderTrackLongReading
import app.shosetsu.android.common.SettingKey.ReaderVoice
import app.shosetsu.android.common.SettingKey.ReaderVolumeScroll
import app.shosetsu.android.common.ext.toast
import app.shosetsu.android.domain.repository.base.ISettingsRepository
import app.shosetsu.android.domain.usecases.load.LoadReaderThemes
import app.shosetsu.android.view.compose.setting.ButtonSettingContent
import app.shosetsu.android.view.compose.setting.DropdownSettingContent
import app.shosetsu.android.view.compose.setting.FloatSliderSettingContent
import app.shosetsu.android.view.compose.setting.SliderSettingContent
import app.shosetsu.android.view.compose.setting.SwitchSettingContent
import app.shosetsu.android.view.uimodels.StableHolder
import app.shosetsu.android.view.uimodels.model.ColorChoiceUI
import app.shosetsu.android.viewmodel.abstracted.settings.AReaderSettingsViewModel
import app.shosetsu.android.viewmodel.base.ExposedSettingsRepoViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Locale

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

@SuppressLint("ComposableNaming")
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

@SuppressLint("ComposableNaming")
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

@SuppressLint("ComposableNaming")
@Composable
fun ExposedSettingsRepoViewModel.readerEngineOption() {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()
	var isInitialized by remember {
		mutableStateOf(false)
	}
	val tts = remember {
		TextToSpeech(context) { isInitialized = true}
	}
	val engines = remember(isInitialized) {
		if (isInitialized) {
			tts.engines.toList()
		} else {
			emptyList()
		}
	}
	val selection by remember {
		settingsRepo.getStringFlow(ReaderEngine)
	}.collectAsState()
	if (!isInitialized) {
		DropdownSettingContent(
			title = stringResource(R.string.reader_engine),
			description = "",
			modifier = Modifier
				.fillMaxWidth(),
			choices = remember {
				listOf(context.getString(R.string.loading)).toImmutableList()
			},
			selection = 0,
			onSelection = {},
		)
		return
	}
	DropdownSettingContent(
		title = stringResource(R.string.reader_engine),
		description = "",
		modifier = Modifier
			.fillMaxWidth(),
		choices = remember {
			engines.map { it.label }.toImmutableList()
		},
		selection = engines.indexOfFirst { it.name == selection }.takeUnless { it < 0 }
			?: engines.indexOfFirst { it.name == tts.defaultEngine }.takeUnless { it < 0 }
			?: 0,
		onSelection = {
			scope.launch {
				settingsRepo.setString(ReaderEngine, engines[it].name)
				settingsRepo.setString(ReaderLanguage, "")
				settingsRepo.setString(ReaderVoice, "")
			}
		}
	)
}

@SuppressLint("ComposableNaming")
@Composable
fun ExposedSettingsRepoViewModel.readerLanguageOption() {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()
	var isInitialized by remember {
		mutableStateOf(false)
	}
	val engine by remember {
		settingsRepo.getStringFlow(ReaderEngine)
	}.collectAsState()
	val tts = remember(engine) {
		isInitialized = false
		if (engine.isEmpty()) {
			TextToSpeech(context) { isInitialized = true }
		} else {
			TextToSpeech(context, { isInitialized = true }, engine)
		}
	}
	val languages = remember(isInitialized, tts) {
		if (isInitialized) {
			tts.availableLanguages.toList().sortedBy { it.displayName }
		} else {
			emptyList()
		}
	}
	val selection by remember {
		settingsRepo.getStringFlow(ReaderLanguage)
	}.collectAsState()

	if (!isInitialized || languages.isEmpty()) {
		DropdownSettingContent(
			title = stringResource(R.string.reader_language),
			description = "",
			modifier = Modifier
				.fillMaxWidth(),
			choices = remember {
				listOf(context.getString(R.string.loading)).toImmutableList()
			},
			selection = 0,
			onSelection = {},
		)
		return
	}
	DropdownSettingContent(
		title = stringResource(R.string.reader_language),
		description = "",
		modifier = Modifier
			.fillMaxWidth(),
		choices = remember {
			languages.map { it.displayName }.toImmutableList()
		},
		selection = remember(engine, selection, languages) {
			languages.indexOfFirst { it.toLanguageTag() == selection }.takeUnless { it < 0 }
				?: languages.indexOfFirst { it.toLanguageTag() == Locale.getDefault().toLanguageTag() }.takeUnless { it < 0 }
				?: 0
		},
		onSelection = {
			scope.launch {
				settingsRepo.setString(ReaderLanguage, languages[it].toLanguageTag())
				settingsRepo.setString(ReaderVoice, "")
			}
		}
	)
}

@SuppressLint("ComposableNaming")
@Composable
fun ExposedSettingsRepoViewModel.readerVoiceOption() {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()
	var isInitialized by remember {
		mutableStateOf(false)
	}
	val engine by remember {
		settingsRepo.getStringFlow(ReaderEngine)
	}.collectAsState()
	val tts = remember(engine) {
		isInitialized = false
		if (engine.isEmpty()) {
			TextToSpeech(context) { isInitialized = true }
		} else {
			TextToSpeech(context, { isInitialized = true }, engine)
		}
	}
	val language by remember {
		settingsRepo.getStringFlow(ReaderLanguage)
	}.collectAsState()
	val voices = remember(isInitialized, language) {
		if (isInitialized) {
			val locale = language.ifEmpty { Locale.getDefault().toLanguageTag() }
			tts.voices.filter { it.locale.toLanguageTag() == locale }.toList()
		} else {
			emptyList()
		}
	}

	val selection by remember {
		settingsRepo.getStringFlow(ReaderVoice)
	}.collectAsState()

	if (!isInitialized || voices.isEmpty()) {
		DropdownSettingContent(
			title = stringResource(R.string.reader_voice),
			description = "",
			modifier = Modifier
				.fillMaxWidth(),
			choices = remember {
				listOf(context.getString(R.string.loading)).toImmutableList()
			},
			selection = 0,
			onSelection = {},
		)
		return
	}
	DropdownSettingContent(
		title = stringResource(R.string.reader_voice),
		description = "",
		modifier = Modifier
			.fillMaxWidth(),
		choices = remember {
			voices.map { it.name }.toImmutableList()
		},
		selection = remember(engine, language, selection, voices) {
			voices.indexOfFirst { it.name == selection }.takeUnless { it < 0 }
				?: voices.indexOfFirst { it.name == tts.defaultVoice?.name }.takeUnless { it < 0 }
				?: 0
		},
		onSelection = {
			scope.launch {
				settingsRepo.setString(ReaderVoice, voices[it].name)
			}
		}
	)
}

@SuppressLint("ComposableNaming")
@Composable
fun ExposedSettingsRepoViewModel.readerTestOption() {
	val context = LocalContext.current
	val engine = remember {
		settingsRepo.getStringFlow(ReaderEngine)
	}
	val language = remember {
		settingsRepo.getStringFlow(ReaderLanguage)
	}
	val voice = remember {
		settingsRepo.getStringFlow(ReaderVoice)
	}
	val pitch = remember {
		settingsRepo.getFloatFlow(ReaderPitch)
	}
	val speed = remember {
		settingsRepo.getFloatFlow(ReaderSpeed)
	}
	val scope = rememberCoroutineScope()
	ButtonSettingContent(
		stringResource(R.string.reader_test),
		"",
		stringResource(R.string.start),
		onClick = {
			scope.launch {
				val ttsResult = CompletableDeferred<Int>()
				val tts = if (engine.value.isEmpty()) {
					TextToSpeech(context) { ttsResult.complete(it) }
				} else {
					TextToSpeech(context, { ttsResult.complete(it) }, engine.value)
				}
				when (ttsResult.await()) {
					TextToSpeech.SUCCESS -> Unit
					else -> {
						context.toast(R.string.reader_test_invalid_engine)
						return@launch
					}
				}
				val languageSuccess: Boolean
				val locale: Locale
				if (language.value.isEmpty()) {
					locale = Locale.getDefault()
					val result = tts.setLanguage(Locale.getDefault())
					languageSuccess = when (result) {
						TextToSpeech.LANG_AVAILABLE -> true
						TextToSpeech.LANG_COUNTRY_AVAILABLE -> true
						TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE -> true
						else -> false
					}
				} else {
					val ttsLocale = tts.availableLanguages.find { it.toLanguageTag() == language.value }
					if (ttsLocale != null) {
						locale = ttsLocale
						val result = tts.setLanguage(locale)
						languageSuccess = when (result) {
							TextToSpeech.LANG_AVAILABLE -> true
							TextToSpeech.LANG_COUNTRY_AVAILABLE -> true
							TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE -> true
							else -> false
						}
					} else {
						locale = Locale.getDefault()
						languageSuccess = false
					}
				}
				if (!languageSuccess) {
					context.toast(R.string.reader_test_invalid_language)
					return@launch
				}
				val voiceSuccess: Boolean
				if (voice.value.isNotEmpty()) {
					val ttsVoice = tts.voices.filter { it.locale == locale }
						.find { it.name == voice.value }
					if (ttsVoice != null) {
						val result = tts.setVoice(ttsVoice)
						voiceSuccess = when (result) {
							TextToSpeech.SUCCESS -> true
							else -> false
						}
					} else {
						voiceSuccess = false
					}
				} else {
					voiceSuccess = tts.defaultVoice != null
				}
				if (!voiceSuccess) {
					context.toast(R.string.reader_test_invalid_voice)
					return@launch
				}
				tts.setPitch(pitch.value / 10)
				tts.setSpeechRate(speed.value / 10)
				tts.speak(
					"This is a test. I am talking so you get an idea on how I talk.",
					TextToSpeech.QUEUE_FLUSH,
					null,
					kotlin.random.Random.nextInt().toString()
				)
			}
		},
		modifier = Modifier
			.fillMaxWidth()
	)
}

@SuppressLint("ComposableNaming")
@Composable
fun ExposedSettingsRepoViewModel.readerReadNextChapter() {
	SwitchSettingContent(
		stringResource(R.string.reader_read_next_chapter_title),
		stringResource(R.string.reader_read_next_chapter_desc),
		settingsRepo,
		ReaderNextChapter, modifier = Modifier
			.fillMaxWidth()
	)
}

@Composable
fun ExposedSettingsRepoViewModel.EditCSS(openCSS: () -> Unit) {
	val context = LocalContext.current
	ButtonSettingContent(
		stringResource(R.string.settings_reader_title_html_css),
		stringResource(R.string.settings_reader_desc_html_css),
		stringResource(R.string.open_in),
		onClick = openCSS,
		modifier = Modifier
			.fillMaxWidth()
	)
}