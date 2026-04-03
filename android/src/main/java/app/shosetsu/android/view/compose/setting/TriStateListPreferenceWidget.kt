package app.shosetsu.android.view.compose.setting

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import app.shosetsu.android.view.compose.setting.widget.TextPreferenceWidget
import app.shosetsu.android.view.compose.setting.widget.TriStateListDialog

@Composable
fun <T> TriStateListPreferenceWidget(
	title: String,
	subtitle: String? = null,
	dialogMessage: String? = null,
	icon: ImageVector? = null,
	possibleValues: List<T>,
	initialChecked: List<T>,
	initialInversed: List<T>,
	stringify: @Composable (T) -> String,
	onValuesChange: (newIncluded: List<T>, newExcluded: List<T>) -> Unit,
) {
	var isDialogShown by remember { mutableStateOf(false) }

	TextPreferenceWidget(
		title = title,
		subtitle = subtitle,
		icon = icon,
		onPreferenceClick = { isDialogShown = true },
	)

	if (isDialogShown) {
		TriStateListDialog(
			title = title,
			message = dialogMessage,
			items = possibleValues,
			initialChecked = initialChecked,
			initialInversed = initialInversed,
			itemLabel = stringify,
			onDismissRequest = { isDialogShown = false },
			onValueChanged = { newIncluded, newExcluded ->
				onValuesChange(newIncluded, newExcluded)
				isDialogShown = false
			},
		)
	}
}