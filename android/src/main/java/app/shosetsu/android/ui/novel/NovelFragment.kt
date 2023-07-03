package app.shosetsu.android.ui.novel

import android.app.Activity
import android.content.res.Resources
import android.database.sqlite.SQLiteException
import android.os.Bundle
import android.view.*
import android.widget.NumberPicker
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.os.bundleOf
import androidx.core.text.isDigitsOnly
import androidx.core.view.MenuProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import app.shosetsu.android.R
import app.shosetsu.android.activity.MainActivity
import app.shosetsu.android.common.FilePermissionException
import app.shosetsu.android.common.NoSuchExtensionException
import app.shosetsu.android.common.enums.ReadingStatus
import app.shosetsu.android.common.ext.*
import app.shosetsu.android.ui.migration.MigrationFragment.Companion.TARGETS_BUNDLE_KEY
import app.shosetsu.android.view.ComposeBottomSheetDialog
import app.shosetsu.android.view.compose.*
import app.shosetsu.android.view.controller.ShosetsuFragment
import app.shosetsu.android.view.controller.base.ExtendedFABController
import app.shosetsu.android.view.controller.base.ExtendedFABController.EFabMaintainer
import app.shosetsu.android.view.controller.base.syncFABWithCompose
import app.shosetsu.android.view.openQRCodeShareDialog
import app.shosetsu.android.view.openShareMenu
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
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.placeholder.material.placeholder
import com.google.android.material.snackbar.Snackbar
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.acra.ACRA
import javax.security.auth.DestroyFailedException

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
class NovelFragment : ShosetsuFragment(),
	ExtendedFABController, MenuProvider {

	/*
	/** Fixes invalid adapter postion errors */
	override fun createLayoutManager(): RecyclerView.LayoutManager =
			object : LinearLayoutManager(context) {
				override fun supportsPredictiveItemAnimations(): Boolean = false
			}
	*/

	internal val viewModel: ANovelViewModel by viewModel()

	override val viewTitle: String
		get() = ""

	private var resume: EFabMaintainer? = null

	private var actionMode: ActionMode? = null

	private fun startSelectionAction() {
		if (actionMode != null) return
		hideFAB(resume!!)
		actionMode = activity?.startActionMode(SelectionActionMode())
	}

	private fun finishSelectionAction() {
		actionMode?.finish()
		//	recyclerView.postDelayed(400) { (activity as MainActivity?)?.supportActionBar?.show() }
	}

	/** Refreshes the novel */
	private fun refresh() {
		logI("Refreshing the novel data")
		viewModel.refresh().observe(
			catch = {
				logE("Failed refreshing the novel data", it)
				makeSnackBar(it.message ?: "Unknown exception")?.show()
			}
		) {
			logI("Successfully reloaded novel")
		}
	}

	override fun showFAB(fab: EFabMaintainer) {
		if (actionMode == null) super.showFAB(fab)
	}

	override fun manipulateFAB(fab: EFabMaintainer) {
		resume = fab
		fab.setOnClickListener { openLastRead() }
		fab.setIconResource(R.drawable.play_arrow)
		fab.setText(R.string.resume)
	}

	private fun openLastRead() {
		viewModel.openLastRead().firstLa(this, catch = {
			logE("Loading last read hit an error")
		}) { chapterUI ->
			if (chapterUI != null) {
				activity?.openChapter(chapterUI)
			} else {
				makeSnackBar(R.string.fragment_novel_snackbar_finished_reading)?.show()
			}
		}
	}

	@Suppress("unused")
	fun migrateOpen() {
		findNavController().navigateSafely(
			R.id.action_novelController_to_migrationController,
			bundleOf(
				TARGETS_BUNDLE_KEY to arrayOf(requireArguments().getNovelID()).toIntArray()
			),
			navOptions {
				setShosetsuTransition()
			}
		)
	}

	private fun openShare() {
		openShareMenu(
			requireActivity(),
			this,
			activity as MainActivity,
			shareBasicURL = {
				viewModel.getShareInfo().observe(
					catch = {
						makeSnackBar(
							getString(
								R.string.fragment_novel_error_share,
								it.message ?: "Unknown"
							)
						)
							?.setAction(R.string.report) { _ ->
								ACRA.errorReporter.handleSilentException(it)
							}?.show()
					}
				) { info ->
					if (info != null)
						activity?.openShare(info.novelURL, info.novelTitle)
				}
			},
			shareQRCode = {
				openQRCodeShareDialog(
					requireActivity(),
					this,
					activity as MainActivity,
					viewModel.getQRCode()
				)
			}
		)
	}

	override fun onMenuItemSelected(item: MenuItem): Boolean = when (item.itemId) {
		R.id.source_migrate -> {
			migrateOpen()
			true
		}

		R.id.share -> {
			openShare()
			true
		}

		R.id.option_chapter_jump -> {
			viewModel.showChapterJumpDialog()
			true
		}

		R.id.download_next -> {
			viewModel.downloadNextChapter()
			true
		}

		R.id.download_next_5 -> {
			viewModel.downloadNext5Chapters()
			true
		}

		R.id.download_next_10 -> {
			viewModel.downloadNext10Chapters()
			true
		}

		R.id.download_custom -> {
			downloadCustom()
			true
		}

		R.id.download_unread -> {
			viewModel.downloadAllUnreadChapters()
			true
		}

		R.id.download_all -> {
			viewModel.downloadAllChapters()
			true
		}

		R.id.set_categories -> {
			viewModel.showCategoriesDialog()
			true
		}

		else -> false
	}


	/**
	 * download a custom amount of chapters
	 */
	private fun downloadCustom() {
		if (context == null) return
		val max = viewModel.getChapterCount()

		AlertDialog.Builder(requireActivity()).apply {
			setTitle(R.string.download_custom_chapters)
			val numberPicker = NumberPicker(requireActivity()).apply {
				minValue = 0
				maxValue = max
			}
			setView(numberPicker)

			setPositiveButton(android.R.string.ok) { d, _ ->
				viewModel.downloadNextCustomChapters(numberPicker.value)
				d.dismiss()
			}
			setNegativeButton(android.R.string.cancel) { d, _ ->
				d.cancel()
			}
		}.show()
	}

	override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
		inflater.inflate(R.menu.toolbar_novel, menu)

		runBlocking {
			menu.findItem(R.id.source_migrate).isVisible = viewModel.isBookmarked().first()
			menu.findItem(R.id.set_categories).isVisible = viewModel.categories.first().isNotEmpty()
		}
	}

	private var state = LazyListState(0)

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedViewState: Bundle?
	): View {
		activity?.addMenuProvider(this, viewLifecycleOwner)
		setViewTitle()
		return ComposeView {
			NovelInfoView(
				viewModel,
				resume,
				state,
				invalidateOptionsMenu = {
					activity?.invalidateOptionsMenu()
				},
				{
					if (it != null) {
						displayOfflineSnackBar(it)
					} else {
						displayOfflineSnackBar()
					}
				},
				::refresh,
				makeSnackBar = {
					makeSnackBar(it)
				},
				openFilterMenu = ::openFilterMenu
			)
		}
	}

	private fun Flow<Boolean>.collectDeletePrevious() {
		collectLA(this@NovelFragment, catch = {
			when (it) {
				is SQLiteException ->
					makeSnackBar(
						getString(
							R.string.fragment_novel_delete_previous_fail,
							it.message ?: ""
						)
					)?.show()

				is FilePermissionException ->
					makeSnackBar(
						getString(
							R.string.fragment_novel_delete_previous_fail,
							it.message ?: ""
						)
					)?.show()

				is NoSuchExtensionException ->
					makeSnackBar(
						getString(
							R.string.missing_extension,
							it.extensionId
						)
					)?.show()
			}
			emit(false)
		}) {
		}
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		viewModel.setNovelID(requireArguments().getNovelID())

		viewModel.hasSelected.collectLatestLA(this, catch = {}) { hasSelected ->
			if (hasSelected) {
				startSelectionAction()
			} else {
				finishSelectionAction()
			}
		}

		viewModel.novelException.collectLatestLA(this, catch = {}) {
			if (it != null)
				makeSnackBar(
					getString(
						R.string.fragment_novel_error_load,
						it.message ?: "Unknown"
					)
				)?.setAction(R.string.report) { _ ->
					ACRA.errorReporter.handleSilentException(it)
				}?.show()
		}

		viewModel.chaptersException.collectLatestLA(this, catch = {}) {
			if (it != null)
				makeSnackBar(
					getString(
						R.string.fragment_novel_error_load_chapters,
						it.message ?: "Unknown"
					)
				)?.setAction(R.string.report) { _ ->
					ACRA.errorReporter.handleSilentException(it)
				}?.show()
		}

		viewModel.otherException.collectLatestLA(this, catch = {}) {
			// TODO Figure out use of other exception
		}

	}

	private fun openWebView() {
		viewModel.novelURL.firstLa(
			this,
			catch = {
				makeSnackBar(
					getString(
						R.string.fragment_novel_error_url,
						it.message ?: "Unknown"
					)
				)
					?.setAction(R.string.report) { _ ->
						ACRA.errorReporter.handleSilentException(it)
					}?.show()
			}
		) {
			if (it != null) {
				activity?.openInWebView(it)
			}
		}
	}

	private fun openFilterMenu() {
		ComposeBottomSheetDialog(requireView().context, this, requireActivity()).apply {
			setContentView(
				ComposeView(context).apply {
					setContent {
						ShosetsuCompose {
							NovelFilterMenuView(viewModel)
						}
					}
				}
			)
		}.show()
	}

	override fun onDestroy() {
		try {
			viewModel.destroy()
		} catch (e: DestroyFailedException) {
			ACRA.errorReporter.handleException(e)
		}
		actionMode?.finish()
		state = LazyListState(0)
		super.onDestroy()
	}

	private fun selectAll() {
		viewModel.selectAll()
	}

	private fun invertSelection() {
		viewModel.invertSelection()
	}

	/**
	 * Selects all chapters between the first and last selected chapter
	 */
	private fun selectBetween() {
		viewModel.selectBetween()
	}

	private fun trueDeleteSelection() {
		viewModel.trueDeleteSelected()
	}

	private inner class SelectionActionMode : ActionMode.Callback {
		override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
			// Hides the original action bar
			// (activity as MainActivity?)?.supportActionBar?.hide()

			mode.menuInflater.inflate(R.menu.toolbar_novel_chapters_selected, menu)

			viewModel.getIfAllowTrueDelete().observe(
				catch = {
					makeSnackBar(
						getString(
							R.string.fragment_novel_error_true_delete,
							it.message ?: "Unknown"
						)
					)
						?.setAction(R.string.report) { _ ->
							ACRA.errorReporter.handleSilentException(it)
						}?.show()
				}
			) {
				menu.findItem(R.id.true_delete).isVisible = it
			}

			mode.setTitle(R.string.selection)
			return true
		}

		override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean = false

		override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean =
			when (item.itemId) {
				R.id.chapter_select_all -> {
					selectAll()
					true
				}

				R.id.chapter_select_between -> {
					selectBetween()
					true
				}

				R.id.chapter_inverse -> {
					invertSelection()
					true
				}

				R.id.true_delete -> {
					trueDeleteSelection()
					true
				}

				else -> false
			}

		override fun onDestroyActionMode(mode: ActionMode) {
			actionMode = null
			showFAB(resume!!)
			viewModel.clearSelection()
		}
	}
}

@Composable
fun NovelInfoView(
	viewModel: ANovelViewModel = viewModelDi(),
	resume: EFabMaintainer?,
	state: LazyListState = LazyListState(0),
	invalidateOptionsMenu: () -> Unit,
	displayOfflineSnackBar: (Int?) -> Unit,
	refresh: () -> Unit,
	makeSnackBar: (String) -> Snackbar?,
	openFilterMenu: () -> Unit
) {
	if (resume != null)
		syncFABWithCompose(state, resume!!)
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


	invalidateOptionsMenu()
	// If the data is not present, loads it
	if (novelInfo != null && !novelInfo!!.loaded) {
		if (viewModel.isOnline()) {
			refresh()
		} else {
			displayOfflineSnackBar(R.string.fragment_novel_snackbar_cannot_inital_load_offline)
		}
	}


	ShosetsuCompose {
		NovelInfoContent(
			novelInfo = novelInfo,
			chapters = chapters,
			selectedChaptersState = selectedChaptersState,
			itemAt = itemAt,
			isRefreshing = isRefreshing,
			onRefresh = {
				if (viewModel.isOnline())
					refresh()
				else displayOfflineSnackBar(null)
			},
			openWebView = {
				if (novelURL != null)
					activity.openInWebView(novelURL!!)
			},
			categories = categories,
			setCategoriesDialogOpen = { viewModel.showCategoriesDialog() },
			toggleBookmark = {
				viewModel.toggleNovelBookmark()
			},
			openFilter = openFilterMenu,
			openChapterJump = {
				viewModel.showChapterJumpDialog()
			},
			chapterContent = {
				NovelChapterContent(
					chapter = it,
					openChapter = {
						activity?.openChapter(it)
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
			state = state
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

		val context = LocalContext.current
		LaunchedEffect(toggleBookmarkResponse) {
			if (toggleBookmarkResponse is ToggleBookmarkResponse.DeleteChapters) {
				val chaptersToDelete =
					(toggleBookmarkResponse as ToggleBookmarkResponse.DeleteChapters).chapters
				makeSnackBar(
					try {
						context.resources.getQuantityString(
							R.plurals.fragment_novel_toggle_delete_chapters,
							chaptersToDelete,
							chaptersToDelete
						)
					} catch (e: Resources.NotFoundException) {
						"Delete $chaptersToDelete chapters?"
					}
				)?.setAction(R.string.delete) {
					viewModel.deleteChapters()
				}?.show()
			}
		}

		LaunchedEffect(jumpState) {
			when (jumpState) {
				JumpState.UNKNOWN -> {}
				JumpState.FAILURE -> {
					makeSnackBar(context.getString(R.string.toast_error_chapter_jump_invalid_target))
						?.setAction(R.string.generic_question_retry) {
							viewModel.showChapterJumpDialog()
						}?.show()
				}
			}
		}
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

	ShosetsuCompose {
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
			state = rememberLazyListState(0)
		)
	}
}

@OptIn(ExperimentalMaterialApi::class)
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
	setCategoriesDialogOpen: (Boolean) -> Unit,
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
	state: LazyListState
) {
	Box(
		modifier = Modifier.fillMaxSize()
	) {
		val pullRefreshState = rememberPullRefreshState(isRefreshing, onRefresh)
		Box(Modifier.pullRefresh(pullRefreshState)) {
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
					if (novelInfo != null)
						item {
							NovelInfoHeaderContent(
								novelInfo = novelInfo,
								openWebview = openWebView,
								categories = categories,
								setCategoriesDialogOpen = setCategoriesDialogOpen,
								toggleBookmark = toggleBookmark,
								openChapterJump = openChapterJump,
								openFilter = openFilter,
								chapterCount = chapters?.size ?: 0
							)
						}
					else {
						item {
							LinearProgressIndicator(
								modifier = Modifier.fillMaxWidth()
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

			PullRefreshIndicator(
				isRefreshing,
				pullRefreshState,
				Modifier.align(Alignment.TopCenter)
			)
		}

		if (chapters != null && hasSelected) {
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
		if (isRefreshing)
			LinearProgressIndicator(
				modifier = Modifier.fillMaxWidth()
			)
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

	ShosetsuCompose {
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
			),
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

	ShosetsuCompose {
		NovelInfoHeaderContent(
			info,
			chapterCount = 0,
			{},
			persistentListOf(),
			{},
			{},
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
	chapterCount: Int,
	openWebview: () -> Unit,
	categories: ImmutableList<CategoryUI>,
	toggleBookmark: () -> Unit,
	openFilter: () -> Unit,
	openChapterJump: () -> Unit,
	setCategoriesDialogOpen: (Boolean) -> Unit,
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
								setCategoriesDialogOpen(true)
							}
						},
						onLongClick = {
							setCategoriesDialogOpen(true)
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
				genre = novelInfo.displayGenre
			)
		}
		Divider()

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

		Divider()
	}
}

@Composable
fun ExpandedText(
	modifier: Modifier = Modifier,
	text: String,
	genre: ImmutableList<String>
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
				horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
				contentPadding = PaddingValues(horizontal = 8.dp)
			) {
				items(genre) {
					NovelGenre(it)
				}
			}
		} else {
			FlowRow(
				modifier = Modifier
					.fillMaxWidth()
					.padding(vertical = 8.dp),
				mainAxisSpacing = 8.dp,
				crossAxisSpacing = 4.dp,
				mainAxisAlignment = FlowMainAxisAlignment.Center,
			) {
				genre.forEach {
					NovelGenre(it)
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