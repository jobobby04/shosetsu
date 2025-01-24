package app.shosetsu.android.view.compose.setting

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.shosetsu.android.common.consts.SUB_TEXT_SIZE
import app.shosetsu.android.ui.theme.ShosetsuTheme

@Preview
@Composable
fun PreviewGenericBottomSetting() {
	ShosetsuTheme {
		GenericBottomSettingLayout(
			"Test",
			"Description"
		) {

		}
	}
}

@Composable
fun GenericBottomSettingLayout(
	title: String,
	description: String,
	modifier: Modifier = Modifier,
	bottom: @Composable () -> Unit
) {
	Column(
		modifier = modifier then Modifier
			.defaultMinSize(minHeight = 56.dp)
			.padding(horizontal = 16.dp),
	) {
		Column {
			Text(title)
			Text(
				description,
				style = SUB_TEXT_SIZE,
				modifier = Modifier.alpha(0.7f)
			)
		}
		bottom()
	}
}