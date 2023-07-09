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
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import app.shosetsu.android.R
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalFoundationApi::class)
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
	val pages = persistentListOf(
		stringResource(
			R.string.editor
		),
		stringResource(
			R.string.preview
		)
	)

	val pagerState = rememberPagerState()
	val scope = rememberCoroutineScope()
	Scaffold(
		topBar = {
			CSSEditorTopBarContent(pagerState, pages, cssTitle, onBack, onHelp)
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