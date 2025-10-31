package app.shosetsu.android.ui.css

import androidx.activity.compose.LocalActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalClipboard
import app.shosetsu.android.common.consts.URL_HELP_CSS
import app.shosetsu.android.common.ext.openInWebView
import app.shosetsu.android.common.ext.viewModelDi
import app.shosetsu.android.ui.theme.ShosetsuTheme
import app.shosetsu.android.viewmodel.abstracted.ACSSEditorViewModel
import kotlinx.coroutines.launch

@Composable
fun CSSEditorView(
	cssId: Int,
	onBackPressed: () -> Unit
) {
	val viewModel: ACSSEditorViewModel = viewModelDi()
	LaunchedEffect(cssId) {
		if (cssId != -2)
			viewModel.setCSSId(cssId)
	}

	val cssTitle by viewModel.cssTitle.collectAsState()
	val cssContent by viewModel.cssContent.collectAsState()

	val shosetsuCss by viewModel.shosetsuCss.collectAsState()

	val isCSSValid by viewModel.isCSSValid.collectAsState()
	val cssInvalidReason by viewModel.cssInvalidReason.collectAsState()

	val canRedo by viewModel.canRedo.collectAsState()
	val canUndo by viewModel.canUndo.collectAsState()
	val activity = LocalActivity.current!!

	val theme by viewModel.appTheme.collectAsState()

	ShosetsuTheme(theme) {
		val colorScheme = MaterialTheme.colorScheme
		LaunchedEffect(colorScheme) {
			viewModel.colorScheme.value = colorScheme
		}
		val clipboard = LocalClipboard.current
		val scope = rememberCoroutineScope()
		var hasPaste by remember { mutableStateOf(false) }
		LaunchedEffect(clipboard) {
			hasPaste = clipboard.getClipEntry() != null
		}
		CSSEditorPagerContent(
			cssTitle = cssTitle,
			cssContent = cssContent,
			shosetsuCss = shosetsuCss,
			isCSSValid = isCSSValid,
			cssInvalidReason = cssInvalidReason,
			onUndo = { viewModel.undo() },
			onRedo = { viewModel.redo() },
			onBack = { onBackPressed() },
			onHelp = { activity.openInWebView(URL_HELP_CSS) },
			onExport = {
				// TODO Add exporting
			},
			onNewText = viewModel::write,
			onPaste = {
				scope.launch {
					val text = clipboard.getClipEntry()
					if (text == null) {
						// TODO Handle no paste content
					} else {
						viewModel.appendText(text.clipData.toString())
					}
				}
			},
			hasPaste = hasPaste,
			canRedo = canRedo,
			canUndo = canUndo
		) {
			viewModel.saveCSS()
		}
	}
}
