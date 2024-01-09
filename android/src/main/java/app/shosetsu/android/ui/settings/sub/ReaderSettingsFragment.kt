package app.shosetsu.android.ui.settings.sub

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.shosetsu.android.R
import app.shosetsu.android.common.SettingKey
import app.shosetsu.android.common.SettingKey.ChaptersResumeFirstUnread
import app.shosetsu.android.common.SettingKey.ReaderKeepScreenOn
import app.shosetsu.android.common.SettingKey.ReaderMarkReadAsReading
import app.shosetsu.android.common.SettingKey.ReaderTextAlignment
import app.shosetsu.android.common.SettingKey.ReaderTheme
import app.shosetsu.android.common.SettingKey.ReaderVoice
import app.shosetsu.android.common.SettingKey.ReadingMarkingType
import app.shosetsu.android.common.consts.SELECTED_STROKE_WIDTH
import app.shosetsu.android.common.enums.MarkingType
import app.shosetsu.android.common.ext.ComposeView
import app.shosetsu.android.common.ext.launchIO
import app.shosetsu.android.common.ext.viewModelDi
import app.shosetsu.android.view.compose.NavigateBackButton
import app.shosetsu.android.view.compose.setting.DropdownSettingContent
import app.shosetsu.android.view.compose.setting.GenericBottomSettingLayout
import app.shosetsu.android.view.compose.setting.GenericRightSettingLayout
import app.shosetsu.android.view.compose.setting.SwitchSettingContent
import app.shosetsu.android.view.controller.ShosetsuFragment
import app.shosetsu.android.viewmodel.abstracted.settings.AReaderSettingsViewModel
import app.shosetsu.android.viewmodel.impl.settings.EditCSS
import app.shosetsu.android.viewmodel.impl.settings.doubleTapFocus
import app.shosetsu.android.viewmodel.impl.settings.doubleTapSystem
import app.shosetsu.android.viewmodel.impl.settings.enableFullscreen
import app.shosetsu.android.viewmodel.impl.settings.invertChapterSwipeOption
import app.shosetsu.android.viewmodel.impl.settings.matchFullscreenToFocus
import app.shosetsu.android.viewmodel.impl.settings.paragraphIndentOption
import app.shosetsu.android.viewmodel.impl.settings.paragraphSpacingOption
import app.shosetsu.android.viewmodel.impl.settings.readerPitchOption
import app.shosetsu.android.viewmodel.impl.settings.readerSpeedOption
import app.shosetsu.android.viewmodel.impl.settings.readerTableHackOption
import app.shosetsu.android.viewmodel.impl.settings.readerTextSelectionToggle
import app.shosetsu.android.viewmodel.impl.settings.showReaderDivider
import app.shosetsu.android.viewmodel.impl.settings.stringAsHtmlOption
import app.shosetsu.android.viewmodel.impl.settings.textSizeOption
import app.shosetsu.android.viewmodel.impl.settings.trackLongReadingOption
import kotlinx.collections.immutable.toImmutableList
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
 * Shosetsu
 *
 * @since 04 / 10 / 2021
 * @author Doomsdayrs
 */
@Deprecated("Composed")
class ReaderSettingsFragment : ShosetsuFragment() {
	override val viewTitleRes: Int = R.string.settings_reader

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedViewState: Bundle?
	): View {
		setViewTitle()
		return ComposeView {
		}
	}
}

@Composable
fun ReaderSettingsView(
	onBack: () -> Unit,
	openCSS: () -> Unit
) {
	val viewModel: AReaderSettingsViewModel = viewModelDi()

	val scope = rememberCoroutineScope()
	val context = LocalContext.current

	val hostState = remember { SnackbarHostState() }

	ReaderSettingsContent(
		viewModel = viewModel,
		openHTMLEditor = openCSS,
		showStyleAddSnackBar = {
			scope.launch {
				hostState.showSnackbar(context.getString(R.string.style_wait))
			}
		},
		hostState = hostState,
		onBack = onBack
	)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderSettingsContent(
	viewModel: AReaderSettingsViewModel,
	openHTMLEditor: () -> Unit,
	showStyleAddSnackBar: () -> Unit,
	hostState: SnackbarHostState,
	onBack: () -> Unit,
) {
	Scaffold(
		topBar = {
			TopAppBar(
				title = {
					Text(stringResource(R.string.settings_reader))
				},
				navigationIcon = {
					NavigateBackButton(onBack)
				}
			)
		},
		snackbarHost = {
			SnackbarHost(hostState)
		}
	) { paddingValues ->
		LazyColumn(
			contentPadding = PaddingValues(top = 16.dp, bottom = 64.dp),
			verticalArrangement = Arrangement.spacedBy(8.dp),
			modifier = Modifier.padding(paddingValues)
		) {
			//TODO Text Preview at top

			item {
				viewModel.paragraphSpacingOption()
			}

			item {
				DropdownSettingContent(
					title = stringResource(R.string.settings_reader_text_alignment_title),
					description = stringResource(R.string.settings_reader_text_alignment_desc),
					choices = stringArrayResource(R.array.text_alignments).toList()
						.toImmutableList(),
					modifier = Modifier
						.fillMaxWidth(),
					repo = viewModel.settingsRepo,
					ReaderTextAlignment
				)
			}

			item {
				viewModel.textSizeOption()
			}

			item {
				viewModel.paragraphIndentOption()
			}

			item {
				GenericBottomSettingLayout(
					stringResource(R.string.theme),
					""
				) {
					val themes by viewModel.getReaderThemes().collectAsState(emptyList())

					LazyRow(
						contentPadding = PaddingValues(16.dp),
						horizontalArrangement = Arrangement.spacedBy(8.dp)
					) {
						items(themes, key = { it.id }) { themeItem ->
							Card(
								border = if (themeItem.isSelected) BorderStroke(
									SELECTED_STROKE_WIDTH.dp,
									MaterialTheme.colorScheme.tertiary
								) else null,
								onClick = {
									launchIO {
										viewModel.settingsRepo.setInt(
											ReaderTheme,
											themeItem.id.toInt()
										)
									}
								}
							) {
								Box(
									modifier = Modifier.background(Color(themeItem.backgroundColor)),
									contentAlignment = Alignment.Center
								) {
									Text(
										"T",
										color = Color(themeItem.textColor),
										modifier = Modifier
											.size(64.dp)
											.padding(8.dp),
										textAlign = TextAlign.Center,
										fontSize = 32.sp
									)
								}
							}
						}

						item {
							Card(
								onClick = {
									showStyleAddSnackBar()
								}
							) {
								Box(
									contentAlignment = Alignment.Center
								) {
									Image(
										painterResource(R.drawable.add_circle_outline),
										stringResource(R.string.style_add),
										modifier = Modifier
											.size(64.dp)
											.padding(8.dp)
									)
								}

							}
						}
					}
				}
			}


			item {
				viewModel.invertChapterSwipeOption()
			}

			//item { viewModel.tapToScrollOption() }

			//item { viewModel.volumeScrollingOption() }

			item {
				SwitchSettingContent(
					stringResource(R.string.settings_reader_title_mark_read_as_reading),
					stringResource(R.string.settings_reader_desc_mark_read_as_reading),
					viewModel.settingsRepo,
					ReaderMarkReadAsReading,
					modifier = Modifier
						.fillMaxWidth()
				)
			}

			//item { viewModel.horizontalSwitchOption() }

			item {
				viewModel.EditCSS(openHTMLEditor)
			}

			item {
				viewModel.stringAsHtmlOption()
			}

			item {
				//viewModel.continuousScrollOption()
			}

			item {
				DropdownSettingContent(
					stringResource(R.string.marking_mode),
					stringResource(R.string.settings_reader_marking_mode_desc),
					choices = stringArrayResource(R.array.marking_names)
						.toList()
						.toImmutableList(),
					repo = viewModel.settingsRepo,
					key = ReadingMarkingType,
					stringToInt = {
						when (MarkingType.valueOf(it)) {
							MarkingType.ONSCROLL -> 1
							MarkingType.ONVIEW -> 0
						}
					},
					intToString = {
						when (it) {
							0 -> MarkingType.ONVIEW.name
							1 -> MarkingType.ONSCROLL.name
							else -> {
								Log.e("MarkingMode", "UnknownType, defaulting")
								MarkingType.ONVIEW.name
							}
						}
					},
					modifier = Modifier
						.fillMaxWidth()
				)
			}

			item {
				SwitchSettingContent(
					stringResource(R.string.settings_reader_resume_behavior_title),
					stringResource(R.string.settings_reader_resume_behavior_desc),
					viewModel.settingsRepo,
					ChaptersResumeFirstUnread,
					modifier = Modifier
						.fillMaxWidth()
				)
			}

			item {
				SwitchSettingContent(
					stringResource(R.string.settings_reader_keep_screen_on),
					stringResource(R.string.settings_reader_keep_screen_on_desc),
					viewModel.settingsRepo,
					ReaderKeepScreenOn,
					modifier = Modifier
						.fillMaxWidth()
				)
			}

			item { viewModel.enableFullscreen() }

			item { viewModel.readerTextSelectionToggle() }

			item { viewModel.matchFullscreenToFocus() }

			item {
				viewModel.showReaderDivider()
			}

			item {
				viewModel.readerTableHackOption()
			}

			item { viewModel.doubleTapFocus() }
			item { viewModel.doubleTapSystem() }
			item {
				viewModel.trackLongReadingOption()
			}
			item { viewModel.readerPitchOption() }
			item { viewModel.readerSpeedOption() }
			item {
				val context = LocalContext.current
				val tts = remember {
					TextToSpeech(
						context
					) {
					}
				}
				val selectedVoice by
				viewModel.settingsRepo.getStringFlow(SettingKey.ReaderVoice).collectAsState()
				val voices by remember {
					derivedStateOf {
						tts.voices?.toImmutableList() ?: emptyList()
					}
				}
				ReaderSettingsVoiceOption(
					selectedVoice,
					voices
				) {
					launchIO {
						viewModel.settingsRepo.setString(ReaderVoice, it)
					}
				}
			}
		}
	}
}

@Composable
fun ReaderSettingsVoiceOption(
	selectedVoice: String?,
	voices: List<Voice>,
	onVoiceSelected: (String) -> Unit
) {
	var expanded by remember { mutableStateOf(false) }

	Column {
		GenericRightSettingLayout(
			title = stringResource(R.string.settings_reader_voice_title),
			description = stringResource(R.string.settings_reader_voice_desc),
			onClick = { expanded = !expanded }
		) {
			IconToggleButton(
				onCheckedChange = {
					expanded = it
				},
				checked = expanded,
				modifier = Modifier.wrapContentWidth()
			) {
				if (expanded)
					Icon(painterResource(R.drawable.expand_less), "")
				else
					Icon(painterResource(R.drawable.expand_more), "")
			}
		}

		val sortedVoices by remember { derivedStateOf { voices.sortedByDescending { it.quality } } }

		AnimatedVisibility(
			expanded
		) {
			Column {
				sortedVoices.forEach {
					ReaderSettingsVoiceItem(
						voice = it,
						isSelected = it.name == selectedVoice,
						onVoiceSelected = onVoiceSelected
					)
				}
			}
		}
	}
}

@Preview
@Composable
fun PreviewReaderSettingsVoiceItem() {
	ReaderSettingsVoiceItem(
		Voice("test", Locale.ENGLISH, Voice.QUALITY_HIGH, Voice.LATENCY_HIGH, true, emptySet()),
		true,
		onVoiceSelected = {}
	)
}

@Composable
fun ReaderSettingsVoiceItem(
	voice: Voice,
	isSelected: Boolean,
	onVoiceSelected: (String) -> Unit
) {
	Row {
		Checkbox(
			isSelected,
			onCheckedChange = {
				onVoiceSelected(voice.name)
			}
		)

		Text(voice.name)

		Text(
			stringResource(
				when (voice.quality) {
					Voice.QUALITY_VERY_HIGH -> {
						R.string.voice_very_high
					}

					Voice.QUALITY_HIGH -> {
						R.string.voice_high
					}

					Voice.QUALITY_NORMAL -> {
						R.string.voice_normal
					}

					Voice.QUALITY_LOW -> {
						R.string.voice_low
					}

					Voice.QUALITY_VERY_LOW -> {
						R.string.voice_very_low
					}

					else -> {
						R.string.unknown
					}
				}
			)
		)
	}
}
