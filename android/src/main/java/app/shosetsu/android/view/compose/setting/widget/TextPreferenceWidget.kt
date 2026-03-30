package app.shosetsu.android.view.compose.setting.widget

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewLightDark
import app.shosetsu.android.common.enums.AppThemes
import app.shosetsu.android.ui.theme.ShosetsuTheme
import app.shosetsu.android.view.compose.secondaryItemAlpha

/**
 * @param isCompact If you want to compact the UI, this will make the subtitle appear to the left of the text instead.
 */
@Composable
fun TextPreferenceWidget(
	modifier: Modifier = Modifier,
	title: String? = null,
	subtitle: String? = null,
	icon: ImageVector? = null,
	iconTint: Color = MaterialTheme.colorScheme.primary,
	isCompact: Boolean = false,
	widget: @Composable (() -> Unit)? = null,
	onPreferenceClick: (() -> Unit)? = null,
) {
	BasePreferenceWidget(
		modifier = modifier,
		title = title,
		subcomponent = if (!subtitle.isNullOrBlank() && !isCompact) {
			{
				Text(
					text = subtitle,
					modifier = Modifier
						.padding(horizontal = PrefsHorizontalPadding)
						.secondaryItemAlpha(),
					style = MaterialTheme.typography.bodySmall,
					maxLines = 10,
				)
			}
		} else {
			null
		},
		sideComponent = if (!subtitle.isNullOrBlank() && isCompact) {
			{
				Text(
					text = subtitle,
					modifier = Modifier
						.padding(horizontal = PrefsHorizontalPadding)
						.secondaryItemAlpha(),
					style = MaterialTheme.typography.bodyMedium,
					maxLines = 10,
				)
			}
		} else {
			null
		},
		icon = if (icon != null) {
			{
				Icon(
					imageVector = icon,
					tint = iconTint,
					contentDescription = null,
				)
			}
		} else {
			null
		},
		onClick = onPreferenceClick,
		widget = widget,
	)
}

@PreviewLightDark
@Composable
private fun TextPreferenceWidgetPreview() = ShosetsuTheme(AppThemes.LIGHT) {
	Surface {
		Column {
			TextPreferenceWidget(
				title = "Text preference with icon",
				subtitle = "Text preference summary",
				icon = Icons.Filled.Build,
				onPreferenceClick = {},
			)
			TextPreferenceWidget(
				title = "Text preference",
				subtitle = "Text preference summary",
				onPreferenceClick = {},
			)
			TextPreferenceWidget(
				title = "Compact Text preference",
				subtitle = "Text preference summary",
				isCompact = true,
				onPreferenceClick = {},
			)
		}
	}
}