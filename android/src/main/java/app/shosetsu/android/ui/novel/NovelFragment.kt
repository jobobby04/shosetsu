package app.shosetsu.android.ui.novel

import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.provider.Settings
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedSuggestionChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.text.isDigitsOnly
import app.shosetsu.android.R
import app.shosetsu.android.common.ChapterLoadException
import app.shosetsu.android.common.NovelLoadException
import app.shosetsu.android.common.OfflineException
import app.shosetsu.android.common.RefreshException
import app.shosetsu.android.common.enums.ReadingStatus
import app.shosetsu.android.common.ext.openShare
import app.shosetsu.android.common.ext.viewModelDi
import app.shosetsu.android.view.BottomSheetDialog
import app.shosetsu.android.view.NovelShareMenu
import app.shosetsu.android.view.QRCodeShareDialog
import app.shosetsu.android.view.compose.ImageLoadingError
import app.shosetsu.android.view.compose.LazyColumnScrollbar
import app.shosetsu.android.view.compose.LongClickTextButton
import app.shosetsu.android.view.compose.SelectableBox
import app.shosetsu.android.view.compose.coverRatio
import app.shosetsu.android.view.compose.placeholder
import app.shosetsu.android.view.uimodels.NovelSettingUI
import app.shosetsu.android.view.uimodels.model.CategoryUI
import app.shosetsu.android.view.uimodels.model.ChapterUI
import app.shosetsu.android.view.uimodels.model.NovelUI
import app.shosetsu.android.viewmodel.abstracted.ANovelViewModel
import app.shosetsu.android.viewmodel.abstracted.ANovelViewModel.JumpState
import app.shosetsu.android.viewmodel.abstracted.ANovelViewModel.SelectedChaptersState
import app.shosetsu.android.viewmodel.abstracted.ANovelViewModel.ToggleBookmarkResponse
import app.shosetsu.lib.Novel
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch

/*
 * This file is part of Shosetsu.
 *
 * Shosetsu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Shosetsu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Shosetsu.  If not, see <https://www.gnu.org/licenses/>.
 */

/**
 * Shosetsu
 * 9 / June / 2019
 *
 * The page you see when you select a novel
 */

@Composable
fun NovelInfoView(
	novelId: Int,
	windowSize: WindowSizeClass,
	onMigrate: (novelId: Int) -> Unit,
	openInWebView: (String) -> Unit,
	openChapter: (novelId: Int, chapterId: Int) -> Unit,
	onBack: () -> Unit,
	drawerIcon: @Composable () -> Unit
) {
	val viewModel: ANovelViewModel = viewModelDi()

	LaunchedEffect(novelId) {
		viewModel.setNovelID(novelId)
	}

	val novelInfo by viewModel.novelLive.collectAsState()
	val chapters by viewModel.chaptersLive.collectAsState()
	val isRefreshing by viewModel.isRefreshing.collectAsState()
	val selectedChaptersState by viewModel.selectedChaptersState.collectAsState()
	val hasSelected by viewModel.hasSelected.collectAsState()
	val itemAt by viewModel.itemIndex.collectAsState()
	val categories by viewModel.categories.collectAsState()
	val novelCategories by viewModel.novelCategories.collectAsState()
	val activity = LocalContext.current as Activity
	val novelURL by viewModel.novelURL.collectAsState()
	val isCategoriesDialogVisible by viewModel.isCategoriesDialogVisible.collectAsState()
	val toggleBookmarkResponse by viewModel.toggleBookmarkResponse.collectAsState()
	val isChapterJumpDialogVisible by viewModel.isChapterJumpDialogVisible.collectAsState()
	val jumpState by viewModel.jumpState.collectAsState()
	val isQRCodeVisible by viewModel.isQRCodeVisible.collectAsState()
	val isShareMenuVisible by viewModel.isShareMenuVisible.collectAsState()
	val isFilterMenuVisible by viewModel.isFilterMenuVisible.collectAsState()
	val error by viewModel.error.collectAsState(null)
	val isDownloadDialogVisible by viewModel.isDownloadDialogVisible.collectAsState()
	val showTrueDelete by viewModel.showTrueDelete.collectAsState()
	val openLastReadResult by viewModel.openLastReadResult.collectAsState(null)

	val hostState = remember { SnackbarHostState() }
	val context = LocalContext.current
	val scope = rememberCoroutineScope()

	LaunchedEffect(error) {
		if (error != null) {
			when (error) {
				is OfflineException -> {
					scope.launch {
						val result = hostState.showSnackbar(
							context.getString((error as OfflineException).messageRes),
							duration = SnackbarDuration.Long,
							actionLabel = context.getString(R.string.generic_wifi_settings)
						)
						if (result == SnackbarResult.ActionPerformed) {
							context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
						}
					}
				}

				is RefreshException -> {
					scope.launch {
						val result = hostState.showSnackbar(
							context.getString(
								R.string.view_novel_refresh_failed,
								error?.cause?.message ?: context.getString(R.string.unknown)
							),
							duration = SnackbarDuration.Long,
							actionLabel = context.getString(R.string.retry)
						)
						if (result == SnackbarResult.ActionPerformed) {
							viewModel.refresh()
						}
					}
				}

				is NovelLoadException -> {
					scope.launch {
						hostState.showSnackbar(
							context.getString(
								R.string.fragment_novel_error_load,
								error?.cause?.message ?: "Unknown"
							)
						)
					}
				}

				is ChapterLoadException -> {
					scope.launch {
						hostState.showSnackbar(
							context.getString(
								R.string.fragment_novel_error_load_chapters,
								error?.cause?.message ?: "Unknown"
							)
						)
					}
				}

				else -> {
					scope.launch {
						hostState.showSnackbar(
							error?.message ?: context.getString(R.string.error)
						)
					}
				}
			}
		}
	}

	LaunchedEffect(openLastReadResult) {
		when (val result = openLastReadResult) {
			ANovelViewModel.LastOpenResult.Complete -> {
				hostState.showSnackbar(
					context.getString(R.string.fragment_novel_snackbar_finished_reading)
				)
			}

			is ANovelViewModel.LastOpenResult.Open -> {
				openChapter(result.chapterUI.novelID, result.chapterUI.id)
			}

			null -> {}
		}
	}

	val state = LazyListState(0)

	// If the data is not present, loads it
	if (novelInfo != null && !novelInfo!!.loaded) {
		viewModel.refresh()
	}

	NovelInfoContent(
		novelInfo = novelInfo,
		chapters = chapters,
		selectedChaptersState = selectedChaptersState,
		itemAt = itemAt,
		isRefreshing = isRefreshing,
		onRefresh = viewModel::refresh,
		openWebView = {
			openInWebView(novelURL ?: return@NovelInfoContent)
		},
		categories = categories,
		setCategoriesDialogOpen = { viewModel.showCategoriesDialog() },
		toggleBookmark = {
			viewModel.toggleNovelBookmark()
		},
		openFilter = viewModel::showFilterMenu,
		openChapterJump = {
			viewModel.showChapterJumpDialog()
		},
		chapterContent = {
			NovelChapterContent(
				chapter = it,
				openChapter = {
					openChapter(it.novelID, it.id)
				},
				onToggleSelection = {
					viewModel.toggleSelection(it)
				},
				selectionMode = hasSelected
			)
		},
		downloadSelected = viewModel::downloadSelected,
		deleteSelected = viewModel::deleteSelected,
		markSelectedAsRead = {
			viewModel.markSelectedAs(ReadingStatus.READ)
		},
		markSelectedAsUnread = {
			viewModel.markSelectedAs(ReadingStatus.UNREAD)
		},
		bookmarkSelected = viewModel::bookmarkSelected,
		unbookmarkSelected = viewModel::removeBookmarkFromSelected,
		hasSelected = hasSelected,
		state = state,
		windowSize = windowSize,
		onSelectAll = viewModel::selectAll,
		onSelectBetween = viewModel::selectBetween,
		onInverseSelection = viewModel::invertSelection,
		showTrueDelete = showTrueDelete,
		onTrueDelete = viewModel::trueDeleteSelected,
		canMigrate = novelInfo?.bookmarked == true,
		onMigrate = {
			onMigrate(novelId)
		},
		hasCategories = categories.isNotEmpty(),
		onSetCategories = viewModel::showCategoriesDialog,
		onDownloadNext = viewModel::downloadNextChapter,
		onDownloadNext5 = viewModel::downloadNext5Chapters,
		onDownloadNext10 = viewModel::downloadNext10Chapters,
		onDownloadCustom = viewModel::showDownloadDialog,
		onDownloadAll = viewModel::downloadAllChapters,
		onDownloadUnread = viewModel::downloadAllUnreadChapters,
		onResume = viewModel::openLastRead,
		onBack = onBack,
		onOpenShareMenu = viewModel::openShareMenu
	)

	if (isCategoriesDialogVisible)
		CategoriesDialog(
			onDismissRequest = { viewModel.hideCategoriesDialog() },
			categories = categories,
			novelCategories = novelCategories,
			setCategories = viewModel::setNovelCategories
		)

	if (isChapterJumpDialogVisible) {
		JumpDialog(
			dismiss = {
				viewModel.hideChapterJumpDialog()
			},
			confirm = { query, byTitle ->
				viewModel.jump(query, byTitle)
			}
		)
	}

	if (isQRCodeVisible) {
		val qrCode by viewModel.qrCode.collectAsState(null)
		QRCodeShareDialog(qrCode, viewModel::hideQRCodeDialog, novelInfo?.title)
	}

	LaunchedEffect(toggleBookmarkResponse) {
		if (toggleBookmarkResponse is ToggleBookmarkResponse.DeleteChapters) {
			val chaptersToDelete =
				(toggleBookmarkResponse as ToggleBookmarkResponse.DeleteChapters).chapters
			val result = hostState.showSnackbar(
				try {
					context.resources.getQuantityString(
						R.plurals.fragment_novel_toggle_delete_chapters,
						chaptersToDelete,
						chaptersToDelete
					)
				} catch (e: Resources.NotFoundException) {
					"Delete $chaptersToDelete chapters?"
				},
				actionLabel = context.getString(R.string.delete)
			)

			if (result == SnackbarResult.ActionPerformed) {
				viewModel.deleteChapters()
			}
		}
	}

	LaunchedEffect(jumpState) {
		when (jumpState) {
			JumpState.UNKNOWN -> {}
			JumpState.FAILURE -> {
				val result = hostState.showSnackbar(
					context.getString(R.string.toast_error_chapter_jump_invalid_target),
					actionLabel = context.getString(R.string.generic_question_retry)
				)

				if (result == SnackbarResult.ActionPerformed) {
					viewModel.showChapterJumpDialog()
				}
			}
		}
	}

	if (isShareMenuVisible) {
		val shareInfo by viewModel.shareInfo.collectAsState(null)
		NovelShareMenu(
			shareBasicURL = {
				if (shareInfo != null)
					activity.openShare(shareInfo!!.novelURL, shareInfo!!.novelTitle)
			},
			shareQRCode = {
				viewModel.showQRCodeDialog()
			},
			dismiss = {
				viewModel.hideShareMenu()
			}
		)
	}

	if (isFilterMenuVisible) {
		val settings by viewModel.novelSettingFlow.collectAsState()
		NovelFilterMenu(settings, viewModel::updateNovelSetting, viewModel::hideFilterMenu)
	}

	if (isDownloadDialogVisible) {
		NovelCustomDownloadDialog(
			onDismissRequest = viewModel::hideDownloadDialog,
			chapterCount = chapters.size,
			onDownload = viewModel::downloadNextCustomChapters
		)
	}
}

@Composable
fun NovelFilterMenu(
	settings: NovelSettingUI?,
	updateSettings: (NovelSettingUI) -> Unit,
	onDismiss: () -> Unit
) {
	BottomSheetDialog(onDismiss) {
		NovelFilterMenuView(settings, updateSettings)
	}
}

@Preview
@Composable
fun PreviewJumpDialog() {
	JumpDialog(
		dismiss = {},
		confirm = { _, _ -> }
	)
}

@Composable
fun JumpDialog(
	dismiss: () -> Unit,
	confirm: (query: String, byTitle: Boolean) -> Unit
) {
	var query by remember { mutableStateOf("") }
	var byTitle by remember { mutableStateOf(false) }
	val isError = if (!byTitle) !query.isDigitsOnly() else false
	AlertDialog(
		onDismissRequest = dismiss,
		confirmButton = {
			TextButton(
				onClick = {
					confirm(query, byTitle)
					dismiss()
				},
				enabled = !isError
			) {
				Text(stringResource(android.R.string.ok))
			}
		},
		dismissButton = {
			TextButton(
				onClick = dismiss
			) {
				Text(stringResource(android.R.string.cancel))
			}
		},
		title = {
			Text(stringResource(R.string.jump_to_chapter))
		},
		text = {
			Column(
				verticalArrangement = Arrangement.spacedBy(4.dp)
			) {
				OutlinedTextField(
					query,
					onValueChange = {
						if (!it.contains('\n'))
							query = it
					},
					isError = isError,
					placeholder = {
						Text(
							stringResource(
								if (byTitle) {
									R.string.fragment_novel_jump_dialog_hint_chapter_title
								} else {
									R.string.fragment_novel_jump_dialog_hint_chapter_number
								}
							)
						)
					},
					keyboardOptions = KeyboardOptions(
						keyboardType = if (byTitle) KeyboardType.Ascii else KeyboardType.Decimal
					),
					singleLine = true
				)

				Row(
					Modifier
						.clickable {
							byTitle = !byTitle
						}
						.fillMaxWidth(),
					horizontalArrangement = Arrangement.SpaceBetween,
					verticalAlignment = Alignment.CenterVertically
				) {
					Text(stringResource(R.string.fragment_novel_jum_dialog_by_title))
					Switch(byTitle, { byTitle = it })
				}
			}
		}
	)
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview
@Composable
fun PreviewNovelInfoContent() {

	val info = NovelUI(
		id = 0,
		novelURL = "",
		extID = 1,
		extName = "Test",
		bookmarked = false,
		title = "Title",
		imageURL = "",
		description = "laaaaaaaaaaaaaaaaaaaaaaaaaa\nlaaaaaaaaaaaaaaaaaaa\nklaaaaaaaaaaaaa",
		loaded = true,
		language = "eng",
		genres = listOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L"),
		authors = listOf("A", "B", "C"),
		artists = listOf("A", "B", "C"),
		tags = listOf("A", "B", "C"),
		status = Novel.Status.COMPLETED
	)

	val chapters = List(10) {
		ChapterUI(
			id = it,
			novelID = 0,
			link = "",
			extensionID = 0,
			title = "Test",
			releaseDate = "10/10/10",
			order = it.toDouble(),
			readingPosition = 0.95,
			readingStatus = when {
				it % 2 == 0 -> {
					ReadingStatus.READING
				}

				else -> {
					ReadingStatus.READ
				}
			},
			bookmarked = it % 2 == 0,
			isSaved = it % 2 != 0
		)

	}.toImmutableList()

	val width = 900.dp
	val height = 300.dp

	Surface(Modifier.size(width = width, height = height)) {
		NovelInfoContent(
			novelInfo = info,
			chapters = chapters,
			selectedChaptersState = remember {
				SelectedChaptersState()
			},
			itemAt = 0,
			isRefreshing = false,
			onRefresh = {},
			openWebView = {},
			categories = persistentListOf(),
			setCategoriesDialogOpen = {},
			toggleBookmark = {},
			openFilter = {},
			openChapterJump = {},
			chapterContent = {
				NovelChapterContent(
					chapter = it,
					openChapter = { },
					selectionMode = false
				) {}
			},
			downloadSelected = {},
			deleteSelected = {},
			markSelectedAsRead = {},
			markSelectedAsUnread = {},
			bookmarkSelected = {},
			unbookmarkSelected = {},
			hasSelected = false,
			state = rememberLazyListState(0),
			windowSize = WindowSizeClass.calculateFromSize(DpSize(width = width, height = height)),
			onSelectAll = {},
			onSelectBetween = {},
			onInverseSelection = {},
			showTrueDelete = false,
			onTrueDelete = {},
			canMigrate = false,
			onMigrate = {},
			hasCategories = false,
			onSetCategories = {},
			onDownloadNext = {},
			onDownloadNext5 = {},
			onDownloadNext10 = {},
			onDownloadCustom = {},
			onDownloadAll = {},
			onDownloadUnread = {},
			onResume = {},
			onBack = {},
			onOpenShareMenu = {}
		)
	}
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun NovelInfoContent(
	novelInfo: NovelUI?,
	chapters: ImmutableList<ChapterUI>?,
	selectedChaptersState: SelectedChaptersState,
	itemAt: Int,
	isRefreshing: Boolean,
	onRefresh: () -> Unit,
	openWebView: () -> Unit,
	categories: ImmutableList<CategoryUI>,
	setCategoriesDialogOpen: () -> Unit,
	toggleBookmark: () -> Unit,
	openFilter: () -> Unit,
	openChapterJump: () -> Unit,
	chapterContent: @Composable (ChapterUI) -> Unit,
	downloadSelected: () -> Unit,
	deleteSelected: () -> Unit,
	markSelectedAsRead: () -> Unit,
	markSelectedAsUnread: () -> Unit,
	bookmarkSelected: () -> Unit,
	unbookmarkSelected: () -> Unit,
	hasSelected: Boolean,
	state: LazyListState,
	windowSize: WindowSizeClass,
	onSelectAll: () -> Unit,
	onSelectBetween: () -> Unit,
	onInverseSelection: () -> Unit,
	showTrueDelete: Boolean,
	onTrueDelete: () -> Unit,
	canMigrate: Boolean,
	onMigrate: () -> Unit,
	hasCategories: Boolean,
	onSetCategories: () -> Unit,
	onDownloadNext: () -> Unit,
	onDownloadNext5: () -> Unit,
	onDownloadNext10: () -> Unit,
	onDownloadCustom: () -> Unit,
	onDownloadAll: () -> Unit,
	onDownloadUnread: () -> Unit,
	onResume: () -> Unit,
	onBack: () -> Unit,
	onOpenShareMenu: () -> Unit
) {
	val splitColumn = windowSize.widthSizeClass == WindowWidthSizeClass.Expanded

	@Composable
	fun header(
		novelInfo: NovelUI
	) {
		NovelInfoHeaderContent(
			novelInfo = novelInfo,
			openWebview = openWebView,
			categories = categories,
			setCategoriesDialogOpen = setCategoriesDialogOpen,
			toggleBookmark = toggleBookmark
		)
	}

	val pullRefreshState = rememberPullRefreshState(isRefreshing, onRefresh)

	Scaffold(
		topBar = {
			NovelAppBar(
				onBack = onBack,
				hasSelected = hasSelected,
				onSelectAll = onSelectAll,
				onSelectBetween = onSelectBetween,
				onInverseSelection = onInverseSelection,
				showTrueDelete = showTrueDelete,
				onTrueDelete = onTrueDelete,
				canMigrate = canMigrate,
				onMigrate = onMigrate,
				onJump = openChapterJump,
				hasCategories = hasCategories,
				onSetCategories = onSetCategories,
				onDownloadNext = onDownloadNext,
				onDownloadNext5 = onDownloadNext5,
				onDownloadNext10 = onDownloadNext10,
				onDownloadCustom = onDownloadCustom,
				onDownloadUnread = onDownloadUnread,
				onDownloadAll = onDownloadAll,
				onOpenShareMenu = onOpenShareMenu
			)
		},
		floatingActionButton = {
			ExtendedFloatingActionButton(
				text = {
					Text(stringResource(R.string.resume))
				},
				icon = {
					Icon(Icons.Default.PlayArrow, stringResource(R.string.resume))
				},
				onClick = onResume
			)
		}
	) { paddingValues ->
		Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(paddingValues)
		) {
			Row(
				Modifier
					.fillMaxSize()
					.pullRefresh(pullRefreshState)
			) {
				if (splitColumn) {
					Box(
						Modifier
							.fillMaxWidth(0.5f)
							.fillMaxHeight()
							.verticalScroll(rememberScrollState())
					) {
						if (novelInfo != null)
							header(novelInfo)
					}
				}

				Box(Modifier.fillMaxSize()) {
					LazyColumnScrollbar(
						listState = state,
						thumbColor = MaterialTheme.colorScheme.primary,
						thumbSelectedColor = Color.Gray,
					) {
						LazyColumn(
							modifier = Modifier.fillMaxSize(),
							state = state,
							contentPadding = PaddingValues(bottom = 256.dp)
						) {
							if (novelInfo != null) {
								if (!splitColumn)
									item {
										header(novelInfo)
									}
							} else {
								item {
									LinearProgressIndicator(
										modifier = Modifier.fillMaxWidth()
									)
								}
							}

							stickyHeader {
								Surface(tonalElevation = 1.dp) {
									NovelChapterBar(
										chapters?.size ?: 0,
										openChapterJump,
										openFilter
									)
								}
							}

							if (chapters != null)
								NovelInfoChaptersContent(
									chapters,
									chapterContent
								)
						}
					}

					// Do not save progress when there is nothing being displayed
					if (novelInfo != null && chapters != null) {
						LaunchedEffect(itemAt) {
							launch {
								if (!state.isScrollInProgress)
									state.scrollToItem(itemAt)
							}
						}
					}
				}
			}

			PullRefreshIndicator(
				isRefreshing,
				pullRefreshState,
				Modifier.align(Alignment.TopCenter)
			)

			// Chapter Selection Bar
			if (chapters != null && hasSelected) {
				ChapterSelectionBar(
					selectedChaptersState,
					downloadSelected,
					deleteSelected,
					markSelectedAsRead,
					markSelectedAsUnread,
					bookmarkSelected,
					unbookmarkSelected
				)
			}

			// Loading indicator
			if (isRefreshing)
				LinearProgressIndicator(
					modifier = Modifier.fillMaxWidth()
				)
		}
	}
}

@Preview
@Composable
fun PreviewChapterSelectionBar() {
	Box {
		ChapterSelectionBar(
			SelectedChaptersState(),
			{},
			{},
			{},
			{},
			{},
			{},
		)
	}
}

@Composable
fun BoxScope.ChapterSelectionBar(
	selectedChaptersState: SelectedChaptersState,
	downloadSelected: () -> Unit,
	deleteSelected: () -> Unit,
	markSelectedAsRead: () -> Unit,
	markSelectedAsUnread: () -> Unit,
	bookmarkSelected: () -> Unit,
	unbookmarkSelected: () -> Unit,
) {
	Card(
		modifier = Modifier
			.align(BiasAlignment(0f, 0.7f))
	) {
		Row {
			IconButton(
				onClick = downloadSelected,
				enabled = selectedChaptersState.showDownload
			) {
				Icon(
					painterResource(R.drawable.download),
					stringResource(R.string.fragment_novel_selected_download)
				)
			}
			IconButton(
				onClick = deleteSelected,
				enabled = selectedChaptersState.showDelete
			) {
				Icon(
					painterResource(R.drawable.trash),
					stringResource(R.string.fragment_novel_selected_delete)
				)
			}
			IconButton(
				onClick = markSelectedAsRead,
				enabled = selectedChaptersState.showMarkAsRead
			) {
				Icon(
					painterResource(R.drawable.read_mark),
					stringResource(R.string.fragment_novel_selected_read)
				)
			}
			IconButton(
				onClick = markSelectedAsUnread,
				enabled = selectedChaptersState.showMarkAsUnread
			) {
				Icon(
					painterResource(R.drawable.unread_mark),
					stringResource(R.string.fragment_novel_selected_unread)
				)
			}
			IconButton(
				onClick = bookmarkSelected,
				enabled = selectedChaptersState.showBookmark
			) {
				Icon(
					painterResource(R.drawable.ic_outline_bookmark_add_24),
					stringResource(R.string.fragment_novel_selected_bookmark)
				)
			}
			IconButton(
				onClick = unbookmarkSelected,
				enabled = selectedChaptersState.showRemoveBookmark
			) {
				Icon(
					painterResource(R.drawable.ic_baseline_bookmark_remove_24),
					stringResource(R.string.fragment_novel_selected_unbookmark)
				)
			}
		}
	}
}

@Preview
@Composable
fun PreviewChapterContent() {
	val chapter = ChapterUI(
		id = 0,
		novelID = 0,
		link = "",
		extensionID = 0,
		title = "Test",
		releaseDate = "10/10/10",
		order = 0.0,
		readingPosition = 0.95,
		readingStatus = ReadingStatus.READING,
		bookmarked = true,
		isSaved = true
	)

	Surface {
		NovelChapterContent(
			chapter,
			openChapter = {},
			onToggleSelection = {},
			selectionMode = false
		)
	}
}

fun LazyListScope.NovelInfoChaptersContent(
	chapters: List<ChapterUI>,
	chapterContent: @Composable (ChapterUI) -> Unit
) {
	items(chapters) { chapterContent(it) }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NovelChapterContent(
	chapter: ChapterUI,
	selectionMode: Boolean,
	openChapter: () -> Unit,
	onToggleSelection: () -> Unit
) {
	SelectableBox(
		chapter.isSelected,
		modifier = Modifier
			.let {
				if (chapter.readingStatus == ReadingStatus.READ)
					it.alpha(.5f)
				else it
			}
			.combinedClickable(
				onClick =
				if (!selectionMode)
					openChapter
				else onToggleSelection,
				onLongClick = onToggleSelection
			)
			.fillMaxWidth(),
	) {
		Column(
			modifier = Modifier.padding(16.dp)
		) {
			Text(
				chapter.title,
				maxLines = 1,
				modifier = Modifier
					.fillMaxWidth()
					.padding(bottom = 8.dp),
				color = if (chapter.bookmarked) MaterialTheme.colorScheme.primary else Color.Unspecified
			)

			Row(
				horizontalArrangement = Arrangement.SpaceBetween,
				modifier = Modifier.fillMaxWidth()
			) {
				Row {
					Text(
						chapter.releaseDate,
						fontSize = 12.sp,
						modifier = Modifier.padding(end = 8.dp)
					)

					if (chapter.readingStatus == ReadingStatus.READING)
						Row {
							Text(
								stringResource(R.string.fragment_novel_chapter_position),
								fontSize = 12.sp,
								modifier = Modifier.padding(end = 4.dp)
							)
							Text(
								chapter.displayPosition,
								fontSize = 12.sp
							)
						}
				}

				if (chapter.isSaved)
					Text(
						stringResource(R.string.downloaded),
						fontSize = 12.sp
					)
			}
		}
	}
}

@Preview
@Composable
fun PreviewHeaderContent() {
	val info = NovelUI(
		id = 0,
		novelURL = "",
		extID = 1,
		extName = "Test",
		bookmarked = false,
		title = "Title",
		imageURL = "",
		description = "laaaaaaaaaaaaaaaaaaaaaaaaaa\nlaaaaaaaaaaaaaaaaaaa\nklaaaaaaaaaaaaa",
		loaded = true,
		language = "eng",
		genres = listOf("A", "B", "C"),
		authors = listOf("A", "B", "C"),
		artists = listOf("A", "B", "C"),
		tags = listOf("A", "B", "C"),
		status = Novel.Status.COMPLETED
	)

	Surface {
		NovelInfoHeaderContent(
			info,
			{},
			persistentListOf(),
			{},
			{}
		)
	}
}

@Composable
fun NovelInfoCoverContent(
	imageURL: String,
	modifier: Modifier = Modifier,
	contentScale: ContentScale = ContentScale.Fit,
	onClick: () -> Unit,
) {
	SubcomposeAsyncImage(
		ImageRequest.Builder(LocalContext.current)
			.data(imageURL)
			.crossfade(true)
			.build(),
		stringResource(R.string.fragment_novel_info_image),
		modifier = modifier
			.clickable(onClick = onClick),
		contentScale = contentScale,
		error = {
			ImageLoadingError()
		},
		loading = {
			Box(Modifier.placeholder(true))
		}
	)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovelInfoHeaderContent(
	novelInfo: NovelUI,
	openWebview: () -> Unit,
	categories: ImmutableList<CategoryUI>,
	toggleBookmark: () -> Unit,
	setCategoriesDialogOpen: () -> Unit,
) {
	var isCoverClicked: Boolean by remember { mutableStateOf(false) }
	if (isCoverClicked)
		Dialog(onDismissRequest = { isCoverClicked = false }) {
			NovelInfoCoverContent(
				novelInfo.imageURL,
				modifier = Modifier.fillMaxWidth()
			) {
				isCoverClicked = false
			}
		}

	Column(
		modifier = Modifier.fillMaxWidth(),
	) {
		// Novel information
		Box(
			modifier = Modifier.fillMaxWidth(),
		) {
			SubcomposeAsyncImage(
				ImageRequest.Builder(LocalContext.current)
					.data(novelInfo.imageURL)
					.crossfade(true)
					.build(),
				stringResource(R.string.fragment_novel_info_image),
				modifier = Modifier
					.matchParentSize()
					.alpha(.10f),
				contentScale = ContentScale.Crop,
				error = {
					ImageLoadingError()
				},
				loading = {
					Box(Modifier.placeholder(true))
				}
			)

			Column(
				modifier = Modifier.fillMaxWidth(),
			) {
				SelectionContainer {
					Row(
						modifier = Modifier
							.fillMaxWidth()
							.padding(end = 8.dp),
						verticalAlignment = Alignment.CenterVertically
					) {
						NovelInfoCoverContent(
							novelInfo.imageURL,
							modifier = Modifier
								.fillMaxWidth(.35f)
								.aspectRatio(coverRatio)
								.padding(top = 8.dp, start = 4.dp)
								.clip(MaterialTheme.shapes.medium),
							contentScale = ContentScale.Crop
						) {
							isCoverClicked = true
						}
						Column(
							modifier = Modifier.padding(
								top = 16.dp,
								start = 8.dp,
								end = 8.dp
							),
							verticalArrangement = Arrangement.Center
						) {
							Text(
								novelInfo.title,
								style = MaterialTheme.typography.titleLarge,
								modifier = Modifier
									.padding(bottom = 8.dp)
									.fillMaxWidth(),
							)
							if (novelInfo.authors.isNotEmpty() && novelInfo.authors.all { it.isNotEmpty() })
								Row(
									modifier = Modifier.padding(bottom = 8.dp)
								) {
									if (novelInfo.artists.isEmpty() && novelInfo.artists.none { it.isNotEmpty() })
										Text(
											stringResource(R.string.novel_author),
											style = MaterialTheme.typography.titleSmall
										)
									Text(
										novelInfo.displayAuthors,
										style = MaterialTheme.typography.titleSmall
									)
								}

							if (novelInfo.artists.isNotEmpty() && novelInfo.artists.all { it.isNotEmpty() })
								Row(
									modifier = Modifier.padding(bottom = 8.dp)
								) {
									if (novelInfo.authors.isEmpty() && novelInfo.authors.none { it.isNotEmpty() })
										Text(
											stringResource(R.string.artist_s),
											style = MaterialTheme.typography.titleSmall
										)
									Text(
										novelInfo.displayArtists,
										style = MaterialTheme.typography.titleSmall
									)
								}

							Row {
								Text(
									when (novelInfo.status) {
										Novel.Status.PUBLISHING -> stringResource(R.string.publishing)
										Novel.Status.COMPLETED -> stringResource(R.string.completed)
										Novel.Status.PAUSED -> stringResource(R.string.paused)
										Novel.Status.UNKNOWN -> stringResource(R.string.unknown)
									},
									style = MaterialTheme.typography.titleSmall
								)
								Text(
									" • ",
									style = MaterialTheme.typography.titleSmall
								)
								Text(
									novelInfo.extName,
									style = MaterialTheme.typography.titleSmall
								)
							}
						}
					}
				}

				// Bookmark & Web view
				Row(
					modifier = Modifier
						.fillMaxWidth()
						.padding(bottom = 8.dp),
					horizontalArrangement = Arrangement.SpaceEvenly,
					verticalAlignment = Alignment.CenterVertically
				) {
					LongClickTextButton(
						onClick = {
							if (novelInfo.bookmarked || categories.isEmpty()) {
								toggleBookmark()
							} else {
								setCategoriesDialogOpen()
							}
						},
						onLongClick = {
							setCategoriesDialogOpen()
						},
						modifier = Modifier
							.padding(vertical = 8.dp, horizontal = 4.dp)
							.weight(1F)
					) {
						Column(
							horizontalAlignment = Alignment.CenterHorizontally
						) {
							Icon(
								if (novelInfo.bookmarked) {
									painterResource(R.drawable.ic_heart_svg_filled)
								} else {
									painterResource(R.drawable.ic_heart_svg)
								},
								null,
								tint = if (novelInfo.bookmarked)
									MaterialTheme.colorScheme.primary
								else
									MaterialTheme.colorScheme.onSurface,
								modifier = Modifier.size(20.dp)
							)
							Spacer(Modifier.height(4.dp))
							Text(
								stringResource(
									if (novelInfo.bookmarked) {
										R.string.fragment_novel_in_library
									} else {
										R.string.fragment_novel_add_to_library
									}
								),
								style = MaterialTheme.typography.bodyLarge,
								color = if (novelInfo.bookmarked)
									MaterialTheme.colorScheme.primary
								else
									MaterialTheme.colorScheme.onSurface,
								fontSize = 12.sp,
								textAlign = TextAlign.Center,
							)
						}
					}

					if (categories.isNotEmpty())
						TextButton(
							onClick = {
								setCategoriesDialogOpen()
							},
							modifier = Modifier
								.padding(vertical = 8.dp, horizontal = 4.dp)
								.weight(1F)
						) {
							Column(
								horizontalAlignment = Alignment.CenterHorizontally
							) {
								Icon(
									painterResource(R.drawable.ic_baseline_label_24),
									stringResource(R.string.categories),
									modifier = Modifier.size(20.dp),
									tint = MaterialTheme.colorScheme.onSurface
								)
								Spacer(Modifier.height(4.dp))
								Text(
									stringResource(R.string.set_categories),
									color = MaterialTheme.colorScheme.onSurface,
									fontSize = 12.sp,
									textAlign = TextAlign.Center,
								)
							}
						}
					TextButton(
						onClick = openWebview,
						modifier = Modifier
							.padding(vertical = 8.dp, horizontal = 4.dp)
							.weight(1F)
					) {
						Column(
							horizontalAlignment = Alignment.CenterHorizontally
						) {
							Icon(
								painterResource(R.drawable.open_in_browser),
								stringResource(R.string.action_open_in_webview),
								modifier = Modifier.size(20.dp),
								tint = MaterialTheme.colorScheme.onSurface
							)
							Spacer(Modifier.height(4.dp))
							Text(
								stringResource(R.string.fragment_novel_info_open_web_text),
								color = MaterialTheme.colorScheme.onSurface,
								fontSize = 12.sp,
								textAlign = TextAlign.Center,
							)
						}
					}
				}
			}
		}

		// Description
		SelectionContainer {
			ExpandedText(
				modifier = Modifier
					.fillMaxWidth()
					.padding(top = 8.dp),
				text = novelInfo.description,
				genre = novelInfo.displayGenre,
				mappedGenre = novelInfo.mappedGenre
			)
		}
	}
}

@Preview
@Composable
fun PreviewNovelChapterBar() {
	Surface {
		NovelChapterBar(100, {}, {})
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovelChapterBar(
	chapterCount: Int,
	openChapterJump: () -> Unit,
	openFilter: () -> Unit
) {
	// Chapters header bar
	Row(
		horizontalArrangement = Arrangement.SpaceBetween,
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 16.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Row {
			Text(stringResource(R.string.chapters))
			Text("$chapterCount", modifier = Modifier.padding(start = 8.dp))
		}

		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.padding(8.dp)
				.height(34.dp)
		) {
			Card(
				onClick = openChapterJump,
				modifier = Modifier.height(32.dp),
			) {
				Box(
					modifier = Modifier
						.fillMaxHeight()
						.padding(horizontal = 4.dp),
					contentAlignment = Alignment.Center
				) {
					Text(
						stringResource(R.string.jump_to_chapter_short),
					)
				}
			}

			Card(
				onClick = openFilter,
				modifier = Modifier
					.padding(start = 8.dp)
					.height(32.dp)
			) {
				Row(
					Modifier
						.fillMaxHeight()
						.padding(horizontal = 4.dp),
					verticalAlignment = Alignment.CenterVertically,
				) {
					Icon(
						painterResource(R.drawable.filter),
						null,
					)
					Text(stringResource(R.string.filter))
				}
			}
		}
	}
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExpandedText(
	modifier: Modifier = Modifier,
	text: String,
	genre: ImmutableList<String>,
	mappedGenre: ImmutableMap<String, ImmutableList<String>>
) {
	var isExpanded by remember { mutableStateOf(false) }

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = modifier then Modifier.clickable(
			indication = null,
			onClick = { isExpanded = !isExpanded },
			interactionSource = remember { MutableInteractionSource() }
		)
	) {
		Text(
			if (isExpanded) {
				text
			} else {
				text.let {
					if (it.length > 200)
						it.substring(0, 200) + "..."
					else it
				}
			},
			style = MaterialTheme.typography.bodyMedium,
			modifier = Modifier.padding(start = 8.dp, end = 8.dp)
		)

		if (!isExpanded) {
			LazyRow(
				modifier = Modifier
					.fillMaxWidth()
					.padding(vertical = 8.dp),
				horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
				contentPadding = PaddingValues(horizontal = 8.dp)
			) {
				items(genre) {
					NovelGenre(it)
				}
			}
		} else {
			if (mappedGenre.isNotEmpty()) {
				mappedGenre.forEach { (namespace, genre) ->
					Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
						NovelGenre(text = namespace)
						FlowRow(
							modifier = Modifier
								.fillMaxWidth()
								.padding(horizontal = 8.dp),
							horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
							verticalArrangement = Arrangement.spacedBy(4.dp)
						) {
							genre.forEach {
								NovelGenre(it)
							}
						}
					}
				}
			} else {
				FlowRow(
					modifier = Modifier
						.fillMaxWidth()
						.padding(horizontal = 8.dp, vertical = 8.dp),
					horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
					verticalArrangement = Arrangement.spacedBy(4.dp)
				) {
					genre.forEach {
						NovelGenre(it)
					}
				}
			}
		}
		Icon(
			painter = if (!isExpanded) {
				painterResource(R.drawable.expand_more)
			} else {
				painterResource(R.drawable.expand_less)
			},
			contentDescription = if (!isExpanded) {
				stringResource(R.string.more)
			} else {
				stringResource(R.string.less)
			},
			modifier = Modifier.padding(bottom = 8.dp)
		)
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NovelGenre(
	text: String
) {
	ElevatedSuggestionChip(
		onClick = {},
		label = { Text(text) },
	)
}

@Composable
fun CategoriesDialog(
	onDismissRequest: () -> Unit,
	categories: ImmutableList<CategoryUI>,
	novelCategories: ImmutableList<Int>,
	setCategories: (IntArray) -> Unit
) {
	val selectedCategories = remember(novelCategories) {
		novelCategories.toMutableStateList()
	}
	AlertDialog(
		onDismissRequest = onDismissRequest,
		confirmButton = {
			TextButton(
				onClick = {
					setCategories(selectedCategories.toIntArray())
					onDismissRequest()
				}
			) {
				Text(stringResource(android.R.string.ok))
			}
		},
		dismissButton = {
			TextButton(onClick = onDismissRequest) {
				Text(stringResource(android.R.string.cancel))
			}
		},
		title = {
			Text(stringResource(R.string.set_categories))
		},
		text = {
			Column(Modifier.verticalScroll(rememberScrollState())) {
				categories.filterNot { it.id == 0 }.forEach {
					Row(
						Modifier
							.fillMaxWidth()
							.height(56.dp)
							.clickable {
								if (it.id in selectedCategories) {
									selectedCategories -= it.id
								} else {
									selectedCategories += it.id
								}
							},
						verticalAlignment = Alignment.CenterVertically
					) {
						Checkbox(
							checked = it.id in selectedCategories,
							onCheckedChange = null,
							modifier = Modifier.padding(horizontal = 8.dp)
						)
						Text(it.name)
					}
				}
			}
		}
	)
}