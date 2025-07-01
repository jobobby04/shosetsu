package app.shosetsu.android.ui.css

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp

@Composable
fun CSSEditorContent(cssContent: String, onNewText: (String) -> Unit) {
	val focusRequester = remember { FocusRequester() }
	LaunchedEffect(Unit) {
		focusRequester.requestFocus()
	}
	Column(
		Modifier.fillMaxSize()
			.padding(start = 16.dp, end = 16.dp)
			.verticalScroll(rememberScrollState())
	) {
		BasicTextField(
			value = cssContent,
			onValueChange = onNewText,
			modifier = Modifier
				.fillMaxWidth()
				.defaultMinSize(minHeight = 500.dp)
				.padding(vertical = 8.dp)
				.focusRequester(focusRequester),
			cursorBrush = SolidColor(MaterialTheme.colorScheme.secondary),
			textStyle = MaterialTheme.typography.bodyMedium.copy(color = LocalContentColor.current),
		)
	}
}

