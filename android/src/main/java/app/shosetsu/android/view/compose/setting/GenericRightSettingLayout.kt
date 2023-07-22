package app.shosetsu.android.view.compose.setting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import app.shosetsu.android.common.consts.SUB_TEXT_SIZE
import app.shosetsu.android.view.compose.ShosetsuCompose


@Composable
fun PreviewGenericRightSetting() {
	ShosetsuCompose {
		GenericRightSettingLayout(
			"Test",
			"Description",
		) {

		}
	}
}

@Composable
fun GenericRightSettingLayout(
	title: String,
	description: String,
	modifier: Modifier = Modifier,
	enabled: Boolean = true,
	onClick: (() -> Unit)? = null,
	right: @Composable () -> Unit
) {
	Row(
		modifier = modifier then Modifier
			.defaultMinSize(minHeight = 56.dp)
			.fillMaxWidth()
			.let {
				if (onClick != null)
					it.clickable(onClick = onClick, enabled = enabled)
				else it
			}
			.padding(horizontal = 16.dp),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.SpaceBetween
	) {
		Column(
			Modifier.fillMaxWidth(0.6f)
		) {
			val alpha = if (enabled) 1F else 0.38f
			Text(title, color = LocalContentColor.current.copy(alpha = alpha))
			if (description.isNotBlank()) {
				Text(
					description,
					style = SUB_TEXT_SIZE,
					modifier = Modifier.alpha(0.7f),
					color = LocalContentColor.current.copy(alpha = alpha)
				)
			}
		}
		right()
	}
}
