package app.shosetsu.android.ui.css

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp

@Composable
fun CSSEditorContent(cssContent: String, onNewText: (String) -> Unit) {
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

