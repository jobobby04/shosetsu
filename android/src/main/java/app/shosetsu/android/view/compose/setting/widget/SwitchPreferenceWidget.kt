package app.shosetsu.android.view.compose.setting.widget

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewLightDark
import app.shosetsu.android.common.enums.AppThemes
import app.shosetsu.android.ui.theme.ShosetsuTheme

@Composable
fun SwitchPreferenceWidget(
	modifier: Modifier = Modifier,
	title: String,
	subtitle: String? = null,
	icon: ImageVector? = null,
	iconDescription: String?,
	checked: Boolean = false,
	enabled: Boolean = true,
	onCheckedChanged: (Boolean) -> Unit,
) {
	TextPreferenceWidget(
		modifier = modifier
			.toggleable(checked, enabled = enabled, onValueChange = { onCheckedChanged(!checked) }),
		title = title,
		subtitle = subtitle,
		icon = icon,
		widget = {
			Switch(
				checked = checked,
				onCheckedChange = null,
				modifier = Modifier.padding(start = TrailingWidgetBuffer),
				enabled = enabled
			)
		},
		iconDescription = iconDescription
	)
}

@PreviewLightDark
@Composable
private fun SwitchPreferenceWidgetPreview() = ShosetsuTheme(AppThemes.LIGHT) {
	Surface {
		Column {
			SwitchPreferenceWidget(
				title = "Switch preference with icon",
				subtitle = "Switch preference summary",
				icon = Icons.Filled.Preview,
				checked = true,
				onCheckedChanged = {},
				iconDescription = null
			)
			SwitchPreferenceWidget(
				title = "Switch preference",
				subtitle = "Switch preference summary",
				checked = false,
				onCheckedChanged = {},
				iconDescription = null
			)
			SwitchPreferenceWidget(
				title = "Switch preference no summary",
				checked = false,
				onCheckedChanged = {},
				iconDescription = null
			)
			SwitchPreferenceWidget(
				title = "Another switch preference no summary",
				checked = false,
				onCheckedChanged = {},
				iconDescription = null
			)
		}
	}
}
