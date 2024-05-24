package app.shosetsu.android.ui.css

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import app.shosetsu.android.common.enums.AppThemes
import app.shosetsu.android.common.ext.openInWebView
import app.shosetsu.android.common.ext.viewModelDi
import app.shosetsu.android.ui.theme.ShosetsuTheme
import app.shosetsu.android.viewmodel.abstracted.ACSSEditorViewModel

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
	val clipboardManager = LocalClipboardManager.current

	val isCSSValid by viewModel.isCSSValid.collectAsState()
	val cssInvalidReason by viewModel.cssInvalidReason.collectAsState()

	val canRedo by viewModel.canRedo.collectAsState()
	val canUndo by viewModel.canUndo.collectAsState()
	val activity = LocalContext.current as Activity

	val theme by viewModel.appTheme.collectAsState()

	ShosetsuTheme(
		darkTheme = when (theme) {
			AppThemes.FOLLOW_SYSTEM -> isSystemInDarkTheme()
			AppThemes.LIGHT -> false
			AppThemes.DARK -> true
		}
	) {
		CSSEditorPagerContent(
			cssTitle = cssTitle,
			cssContent = cssContent,
			isCSSValid = isCSSValid,
			cssInvalidReason = cssInvalidReason,
			onUndo = { viewModel.undo() },
			onRedo = { viewModel.redo() },
			onBack = { onBackPressed() },
			onHelp = { activity.openInWebView(CSSEditorActivity.HELP_WEBSITE) },
			onExport = {
				// TODO Add exporting
			},
			onNewText = viewModel::write,
			onPaste = {
				val text = clipboardManager.getText()
				if (text == null) {
					// TODO Handle no paste content
				} else {
					viewModel.appendText(text.toString())
				}
			},
			hasPaste = clipboardManager.getText() != null,
			canRedo = canRedo,
			canUndo = canUndo
		) {
			viewModel.saveCSS()
		}
	}
}

