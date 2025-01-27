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
package app.shosetsu.android.ui.reader

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import app.shosetsu.android.R
import app.shosetsu.android.common.consts.MAX_CONTINOUS_READING_TIME
import app.shosetsu.android.common.ext.viewModelDi
import app.shosetsu.android.ui.css.CSSEditorActivity
import app.shosetsu.android.ui.reader.content.ChapterReaderBottomSheetContent
import app.shosetsu.android.ui.reader.content.ChapterReaderContent
import app.shosetsu.android.ui.reader.content.ChapterReaderHTMLContent
import app.shosetsu.android.ui.reader.content.ChapterReaderPagerContent
import app.shosetsu.android.ui.reader.content.ChapterReaderStringContent
import app.shosetsu.android.ui.reader.page.DividierPageContent
import app.shosetsu.android.ui.theme.ShosetsuTheme
import app.shosetsu.android.view.uimodels.StableHolder
import app.shosetsu.android.view.uimodels.model.reader.ReaderUIItem
import app.shosetsu.android.viewmodel.abstracted.AChapterReaderViewModel
import app.shosetsu.android.viewmodel.impl.settings.EditCSS
import app.shosetsu.android.viewmodel.impl.settings.doubleTapFocus
import app.shosetsu.android.viewmodel.impl.settings.doubleTapSystem
import app.shosetsu.android.viewmodel.impl.settings.enableFullscreen
import app.shosetsu.android.viewmodel.impl.settings.invertChapterSwipeOption
import app.shosetsu.android.viewmodel.impl.settings.matchFullscreenToFocus
import app.shosetsu.android.viewmodel.impl.settings.readerEngineOption
import app.shosetsu.android.viewmodel.impl.settings.readerKeepScreenOnOption
import app.shosetsu.android.viewmodel.impl.settings.readerLanguageOption
import app.shosetsu.android.viewmodel.impl.settings.readerPitchOption
import app.shosetsu.android.viewmodel.impl.settings.readerReadNextChapter
import app.shosetsu.android.viewmodel.impl.settings.readerSpeedOption
import app.shosetsu.android.viewmodel.impl.settings.readerTableHackOption
import app.shosetsu.android.viewmodel.impl.settings.readerTestOption
import app.shosetsu.android.viewmodel.impl.settings.readerTextSelectionToggle
import app.shosetsu.android.viewmodel.impl.settings.readerVoiceOption
import app.shosetsu.android.viewmodel.impl.settings.showReaderDivider
import app.shosetsu.android.viewmodel.impl.settings.stringAsHtmlOption
import app.shosetsu.android.viewmodel.impl.settings.textSizeOption
import app.shosetsu.android.viewmodel.impl.settings.trackLongReadingOption
import app.shosetsu.lib.Novel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterReaderView(
	viewModel: AChapterReaderViewModel = viewModelDi(),
	onExit: () -> Unit
) {
	val uiController = rememberSystemUiController()
	uiController.systemBarsBehavior =
		WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

	val isSystemVisible by viewModel.isSystemVisible.collectAsState()
	uiController.isSystemBarsVisible = isSystemVisible

	val items by viewModel.liveData.collectAsState()
	val isHorizontalReading by viewModel.isHorizontalReading.collectAsState()
	val isBookmarked by viewModel.isCurrentChapterBookmarked.collectAsState()
	val isRotationLocked by viewModel.liveIsScreenRotationLocked.collectAsState()
	val isFocused by viewModel.isFocused.collectAsState()
	val enableFullscreen by viewModel.enableFullscreen.collectAsState()
	val matchFullscreenToFocus by viewModel.matchFullscreenToFocus.collectAsState()
	val chapterType by viewModel.chapterType.collectAsState()
	val currentChapterID by viewModel.currentChapterID.collectAsState()
	val ttsPlayback by viewModel.ttsPlayback.collectAsState()
	val setting by viewModel.getSettings().collectAsState()
	val currentPage by viewModel.currentPage.collectAsState()
	val chapterHistory by viewModel.chapterHistory.collectAsState()
	BackHandler(chapterHistory.size >= 2) {
		viewModel.popHistory()
	}

	val isFirstFocus by viewModel.isFirstFocusFlow.collectAsState()
	val isSwipeInverted by viewModel.isSwipeInverted.collectAsState()
	val owner = LocalLifecycleOwner.current

	val isReadingTooLong by viewModel.isReadingTooLong.collectAsState()
	val trackLongReading by viewModel.trackLongReading.collectAsState()

	val context = LocalContext.current
	val scope = rememberCoroutineScope()
	val uriHandler = LocalUriHandler.current

	if (trackLongReading)
		LaunchedEffect(isReadingTooLong) {
			while (!isReadingTooLong) {
				val startTime = System.currentTimeMillis()
				delay(MAX_CONTINOUS_READING_TIME)
				val currentTime = System.currentTimeMillis()
				if ((currentTime - startTime) < ((MAX_CONTINOUS_READING_TIME / .25)))
					viewModel.userIsReadingTooLong()
			}
		}

	//val isTapToScroll by viewModel.tapToScroll.collectAsState(false)
	ShosetsuTheme {
		ChapterReaderContent(
			isFirstFocusProvider = { isFirstFocus },
			isFocused = isFocused,
			onFirstFocus = viewModel::onFirstFocus,
			sheetContent = { state ->
				ChapterReaderBottomSheetContent(
					scaffoldState = state,
					ttsPlayback = ttsPlayback,
					isBookmarked = isBookmarked,
					isRotationLocked = isRotationLocked,
					setting = setting,
					toggleRotationLock = viewModel::toggleScreenRotationLock,
					toggleBookmark = viewModel::toggleBookmark,
					exit = onExit,
					onPlayTTS = {
						viewModel.onPlayTts(context)
					},
					onPauseTTS = viewModel::onPauseTts,
					onStopTTS = viewModel::onStopTts,
					updateSetting = viewModel::updateSetting,
					lowerSheet = {
						item { viewModel.textSizeOption() }
						//item { viewModel.tapToScrollOption() }
						//item { viewModel.volumeScrollingOption() }
						//item { viewModel.horizontalSwitchOption() }
						item { viewModel.invertChapterSwipeOption() }
						item { viewModel.readerKeepScreenOnOption() }
						item { viewModel.enableFullscreen() }
						item { viewModel.matchFullscreenToFocus() }
						item { viewModel.showReaderDivider() }
						item { viewModel.stringAsHtmlOption() }
						item { viewModel.doubleTapFocus() }
						item { viewModel.doubleTapSystem() }
						item { viewModel.readerTableHackOption() }
						item {
							viewModel.EditCSS(
								openCSS = {
									ContextCompat.startActivity(
										context,
										Intent(context, CSSEditorActivity::class.java).apply {
											putExtra(CSSEditorActivity.CSS_ID, -1)
										},
										null
									)
								}
							)
						}
						item { viewModel.readerTextSelectionToggle() }
						item { viewModel.trackLongReadingOption() }
						item { viewModel.readerPitchOption() }
						item { viewModel.readerSpeedOption() }
						item { viewModel.readerEngineOption() }
						item { viewModel.readerLanguageOption() }
						item { viewModel.readerVoiceOption() }
						item { viewModel.readerTestOption() }
						item { viewModel.readerReadNextChapter() }
					},
					toggleFocus = viewModel::toggleFocus,
					onShowNavigation = viewModel::toggleSystemVisible.takeIf { enableFullscreen && !matchFullscreenToFocus },
				)
			},
			content = { paddingValues ->
				ChapterReaderPagerContent(
					paddingValues = paddingValues,
					items = items ?: persistentListOf(),
					isHorizontal = isHorizontalReading,
					isSwipeInverted = isSwipeInverted,
					currentPage = currentPage,
					onPageChanged = viewModel::setCurrentPage,
					markChapterAsCurrent = {
						viewModel.onViewed(it)
						viewModel.setCurrentChapterID(it.id)
					},
					onChapterRead = viewModel::updateChapterAsRead,
					onStopTTS = viewModel::onStopTts,
					pageJumper = StableHolder(viewModel.pageJumper),
					createPage = { page ->
						when (val item = items.orEmpty()[page]) {
							is ReaderUIItem.ReaderChapterUI -> {
								when (chapterType) {
									Novel.ChapterType.STRING -> {
										ChapterReaderStringContent(
											item = item,
											getStringContent = viewModel::getChapterStringPassage,
											retryChapter = viewModel::retryChapter,
											textSizeFlow = { viewModel.liveTextSize },
											textColorFlow = { viewModel.textColor },
											backgroundColorFlow = { viewModel.backgroundColor },
											disableTextSelFlow = { viewModel.disableTextSelection },
											onScroll = viewModel::onScroll,
											onClick = { viewModel.onReaderClicked(null) },
											onDoubleClick = viewModel::onReaderDoubleClicked,
											progressFlow = {
												viewModel.getChapterProgress(item)
											}
										)
									}

									Novel.ChapterType.HTML -> {
										ChapterReaderHTMLContent(
											item = item,
											getHTMLContent = viewModel::getChapterHTMLPassage,
											retryChapter = viewModel::retryChapter,
											onScroll = viewModel::onScroll,
											onClick = viewModel::onReaderClicked,
											onDoubleClick = viewModel::onReaderDoubleClicked,
											progressFlow = {
												viewModel.getChapterProgress(item)
											},
											openUri = {
												scope.launch {
													if (!viewModel.jumpToChapter(it)) {
														uriHandler.openUri(it)
													}
												}
											},
											ttsProgress = remember {
												StableHolder(viewModel.ttsProgress)
											}
										)
									}

									else -> {
									}
								}
							}

							is ReaderUIItem.ReaderDividerUI -> {
								DividierPageContent(
									item.prev.title,
									item.next?.title
								)
							}
						}
					}
				)
			},
			//isTapToScroll = isTapToScroll
		)
		if (isReadingTooLong) {
			AlertDialog(
				onDismissRequest = {},
				title = {
					Text(stringResource(R.string.reader_long_reading_title))
				},
				text = {
					Text(stringResource(R.string.reader_long_reading_desc))

				},
				confirmButton = {
					var isEnabled by remember { mutableStateOf(false) }
					var timeLeft by remember { mutableStateOf(20) }

					LaunchedEffect(Unit) {
						repeat(20) {
							delay(1000)
							timeLeft--
						}
						isEnabled = true
					}

					TextButton(onClick = {
						if (isEnabled) {
							viewModel.dismissReadingTooLong()
						}
					}) {
						if (isEnabled) {
							Text(stringResource(android.R.string.ok))
						} else {
							Text("$timeLeft")
						}
					}
				},
				properties = DialogProperties(
					dismissOnBackPress = false,
					dismissOnClickOutside = false
				)
			)
		}
	}
}