package app.shosetsu.android.ui.css

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import app.shosetsu.android.R
import app.shosetsu.android.view.compose.pagerTabIndicatorOffset
import kotlinx.coroutines.launch
import okhttp3.internal.immutableListOf

@OptIn(
	ExperimentalMaterial3Api::class,
	ExperimentalFoundationApi::class
)
@Composable
fun CSSEditorPagerContent(
	cssTitle: String,
	cssContent: String,
	isCSSValid: Boolean,
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
	Scaffold(
		topBar = {
			CSSEditorTopBarContent(cssTitle, onBack, onHelp)
		},
		bottomBar = {
			CSSEditorBottomBarContent(
				isCSSValid,
				cssInvalidReason,
				onUndo,
				canUndo,
				onPaste,
				hasPaste,
				onSave,
				onExport,
				onRedo,
				canRedo
			)
		},
		modifier = Modifier.imePadding()
	) {
		Column(
			Modifier
				.padding(it)
				.verticalScroll(rememberScrollState())
		) {
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
						CSSEditorContent(cssContent, onNewText)
					}

					else -> {
						CSSPreviewContent(cssContent)
					}
				}
			}
		}
	}
}

@Preview
@Composable
fun PreviewCSSEditorPagerContent() {
	CSSEditorPagerContent(
		"TestCSS",
		"",
		onBack = {},
		onNewText = {},
		onUndo = {},
		onRedo = {},
		isCSSValid = false,
		cssInvalidReason = "This is not CSS",
		onPaste = {},
		onExport = {},
		onHelp = {},
		canRedo = true,
		canUndo = true,
	) {}
}