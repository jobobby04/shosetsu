package app.shosetsu.android.view.compose.setting

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.shosetsu.android.common.enums.AppThemes
import app.shosetsu.android.ui.theme.ShosetsuTheme
import app.shosetsu.android.view.compose.secondaryItemAlpha
import app.shosetsu.android.view.compose.setting.widget.BasePreferenceWidget
import app.shosetsu.android.view.compose.setting.widget.PrefsHorizontalPadding

@Preview
@Composable
fun PreviewGenericBottomSetting() = ShosetsuTheme(AppThemes.LIGHT) {
	GenericBottomSettingLayout(
		"Test",
		"Description",
		iconDescription = null // No icon
	) {

	}
}

@Composable
fun GenericBottomSettingLayout(
	title: String,
	description: String,
	modifier: Modifier = Modifier,
	icon: ImageVector? = null,
	iconDescription: String?,
	iconTint: Color = MaterialTheme.colorScheme.primary,
	bottom: @Composable () -> Unit
) {
	BasePreferenceWidget(
		modifier = modifier,
		title = title,
		subcomponent = {
			if (description.isNotBlank()) {
				Text(
					text = description,
					modifier = Modifier
						.padding(horizontal = PrefsHorizontalPadding)
						.secondaryItemAlpha(),
					style = MaterialTheme.typography.bodySmall,
					maxLines = 10,
				)
			}
			Row(modifier = Modifier.padding(horizontal = 16.dp)) {
				bottom()
			}
		},
		icon = if (icon != null) {
			{
				Icon(
					imageVector = icon,
					tint = iconTint,
					contentDescription = iconDescription,
				)
			}
		} else {
			null
		},
	)
}
