package app.shosetsu.android.ui.css

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

import android.annotation.SuppressLint
import android.content.ClipDescription.MIMETYPE_TEXT_PLAIN
import android.content.ClipboardManager
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.view.Window
import android.view.WindowManager
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.getSystemService
import app.shosetsu.android.R
import app.shosetsu.android.common.consts.SUB_TEXT_SIZE
import app.shosetsu.android.common.ext.logI
import app.shosetsu.android.common.ext.openInWebView
import app.shosetsu.android.common.ext.viewModel
import app.shosetsu.android.view.compose.ShosetsuCompose
import app.shosetsu.android.view.compose.pagerTabIndicatorOffset
import app.shosetsu.android.viewmodel.abstracted.ACSSEditorViewModel
import kotlinx.coroutines.launch
import okhttp3.internal.immutableListOf
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI

/**
 * Shosetsu
 * 21 / October / 2021
 *
 * @author github.com/doomsdayrs
 */
class CSSEditorActivity : AppCompatActivity(), DIAware {
	private val viewModel: ACSSEditorViewModel by viewModel()
	override val di: DI by closestDI()

	private val clipboardManager by lazy { getSystemService<ClipboardManager>()!! }

	companion object {
		const val CSS_ID = "css-id"
		private const val HELP_WEBSITE = "https://developer.mozilla.org/en-US/docs/Learn/CSS"
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			window.setDecorFitsSystemWindows(false)
		} else {
			window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		}
		if (savedInstanceState != null)
			viewModel.setCSSId(savedInstanceState.getInt(CSS_ID, -1))


		setContent {
			val cssTitle by viewModel.cssTitle.collectAsState()
			val cssContent by viewModel.cssContent.collectAsState()


			val isCSSInvalid by viewModel.isCSSValid.collectAsState()
			val cssInvalidReason by viewModel.cssInvalidReason.collectAsState()

			val canRedo by viewModel.canRedo.collectAsState()
			val canUndo by viewModel.canUndo.collectAsState()

			ShosetsuCompose {
				CSSEditorContent(
					cssTitle = cssTitle,
					cssContent = cssContent,
					isCSSInvalid = isCSSInvalid,
					cssInvalidReason = cssInvalidReason,
					onUndo = { viewModel.undo() },
					onRedo = { viewModel.redo() },
					onBack = { onBackPressedDispatcher.onBackPressed() },
					onHelp = { openInWebView(HELP_WEBSITE) },
					onExport = {
						// TODO Add exporting
					},
					onNewText = viewModel::write,
					onPaste = {
						val text = clipboardManager.primaryClip?.getItemAt(0)?.text
						if (text == null) {
							// TODO Handle no paste content
						} else {
							viewModel.appendText(text.toString())
						}
					},
					hasPaste = when {
						!clipboardManager.hasPrimaryClip() -> {
							false
						}

						!(clipboardManager.primaryClipDescription!!.hasMimeType(MIMETYPE_TEXT_PLAIN)) -> {
							// This disables the paste menu item, since the clipboard has data but it is not plain text
							false
						}

						else -> {
							// This enables the paste menu item, since the clipboard contains plain text.
							true
						}
					},
					canRedo = canRedo,
					canUndo = canUndo
				) {
					viewModel.saveCSS()
				}
			}
		}
	}
}

@Preview
@Composable
fun PreviewCSSEditorContent() {
	ShosetsuCompose {
		CSSEditorContent(
			"TestCSS",
			"",
			onBack = {},
			onNewText = {},
			onUndo = {},
			onRedo = {},
			isCSSInvalid = false,
			cssInvalidReason = "This is not CSS",
			onPaste = {},
			onExport = {},
			onHelp = {},
			canRedo = true,
			canUndo = true,
		) {}
	}
}

@OptIn(
	ExperimentalMaterial3Api::class,
	ExperimentalFoundationApi::class
)
@Composable
fun CSSEditorContent(
	cssTitle: String,
	cssContent: String,
	isCSSInvalid: Boolean,
	cssInvalidReason: String? = null,
	onBack: () -> Unit,
	onHelp: () -> Unit,
	onNewText: (String) -> Unit,
	onUndo: () -> Unit,
	onRedo: () -> Unit,
	onPaste: () -> Unit,
	onExport: () -> Unit,
	hasPaste: Boolean = true,
	canUndo: Boolean,
	canRedo: Boolean,
	onSave: () -> Unit
) {
	val fabShape = MaterialTheme.shapes.small.copy(CornerSize(percent = 50))

	Scaffold(
		topBar = {
			Column {
				TopAppBar(
					title = {
						Text(cssTitle)
					},
					scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
					navigationIcon = {
						IconButton(
							onClick = onBack
						) {
							Icon(
								Icons.Filled.ArrowBack,
								stringResource(androidx.appcompat.R.string.abc_action_bar_up_description),
							)
						}
					},
					actions = {
						IconButton(
							onClick = onHelp
						) {
							Icon(
								painterResource(R.drawable.help_outline_24),
								stringResource(R.string.help),
							)
						}
					}
				)
			}
		},
		bottomBar = {
			Column {
				if (!isCSSInvalid && cssInvalidReason != null)
					Card(
						border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
						modifier = Modifier
							.align(Alignment.CenterHorizontally)
							.fillMaxWidth()
							.padding(16.dp)
					) {
						Column(
							modifier = Modifier.padding(8.dp)
						) {
							Text(
								"Invalid CSS"
							)
							Text(
								cssInvalidReason,
								style = SUB_TEXT_SIZE,
								modifier = Modifier
									.alpha(0.7f)
							)
						}
					}
				BottomAppBar {
					Row(
						verticalAlignment = Alignment.CenterVertically,
						horizontalArrangement = Arrangement.SpaceBetween,
						modifier = Modifier.fillMaxWidth()
					) {
						Row(
							verticalAlignment = Alignment.CenterVertically,
						) {
							IconButton(onClick = onUndo, enabled = canUndo) {
								Icon(
									painterResource(R.drawable.ic_baseline_undo_24),
									stringResource(R.string.activity_css_undo)
								)
							}

							IconButton(
								onClick = onPaste, enabled = hasPaste,
								modifier = Modifier.padding(start = 8.dp)
							) {
								Icon(
									painterResource(androidx.appcompat.R.drawable.abc_ic_menu_paste_mtrl_am_alpha),
									stringResource(R.string.activity_css_paste)
								)
							}
						}

						FloatingActionButton(
							onClick = onSave,
							shape = fabShape,
							containerColor = MaterialTheme.colorScheme.primary,
							contentColor = colorResource(android.R.color.white)
						) {
							Icon(
								painterResource(R.drawable.ic_baseline_save_24),
								stringResource(R.string.activity_css_save)
							)
						}

						Row(
							verticalAlignment = Alignment.CenterVertically,
						) {
							IconButton(
								onClick = onExport, enabled = false,
								modifier = Modifier.padding(end = 8.dp)
							) {
								Icon(
									painterResource(R.drawable.ic_baseline_save_alt_24),
									stringResource(R.string.activity_css_export)
								)
							}
							IconButton(onClick = onRedo, enabled = canRedo) {
								Icon(
									painterResource(R.drawable.ic_baseline_redo_24),
									stringResource(R.string.activity_css_redo)
								)
							}
						}
					}
				}
			}
		},
		modifier = Modifier.imePadding()
	) {
		Column(
			Modifier
				.padding(it)
				.verticalScroll(rememberScrollState())) {
			val pages = immutableListOf(
				stringResource(
					R.string.editor
				),
				stringResource(
					R.string.preview
				)
			)

			val pagerState = rememberPagerState()
			val scope = rememberCoroutineScope()

			TabRow(
				// Our selected tab is our current page
				selectedTabIndex = pagerState.currentPage,
				// Override the indicator, using the provided pagerTabIndicatorOffset modifier
				indicator = { tabPositions ->
					TabRowDefaults.Indicator(
						Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
					)
				}
			) {

				// Add tabs for all of our pages
				pages.forEachIndexed { index, title ->
					Tab(
						text = { Text(title) },
						selected = pagerState.currentPage == index,
						onClick = {
							scope.launch {
								pagerState.scrollToPage(index)
							}
						},
					)
				}
			}
			HorizontalPager(
				pages.size,
				state = pagerState,
				modifier = Modifier.fillMaxSize(),
				userScrollEnabled = false
			) { page ->
				when (page) {
					0 -> {
						BasicTextField(
							value = cssContent,
							onValueChange = onNewText,
							modifier = Modifier
								.fillMaxSize()
								.padding(bottom = 92.dp, start = 16.dp, top = 8.dp, end = 16.dp),
							cursorBrush = SolidColor(MaterialTheme.colorScheme.secondary),
							textStyle = MaterialTheme.typography.bodyMedium.copy(color = LocalContentColor.current),
						)
					}

					else -> {
						CSSPreview(cssContent)
					}
				}
			}
		}

	}
}

@Composable
fun CSSPreview(
	cssContent: String,
) {
	AndroidView(
		factory = { context ->
			WebView(context).apply {
				@SuppressLint("SetJavaScriptEnabled")
				settings.javaScriptEnabled = true
				settings.apply {
					cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
				}
				loadData(
					context.getString(R.string.activity_css_example),
					"text/html",
					"UTF-8"
				)

			}
		},
		update = { webView ->
			webView.logI("Updating style")
			val base64String =
				Base64.encodeToString(cssContent.encodeToByteArray(), Base64.DEFAULT)
			webView.evaluateJavascript(
				"""
				style = document.getElementById('example-css');
				if(!style){
					style = document.createElement('style');
					style.id = 'example-css';
					style.type = 'text/css';
					document.head.appendChild(style);
				}
				style.textContent = atob(`$base64String`);
			""".trimIndent(),
				null
			)
		},
		modifier = Modifier
			.fillMaxSize()
	)
}
