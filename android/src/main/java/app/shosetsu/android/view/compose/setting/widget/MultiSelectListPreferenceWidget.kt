package app.shosetsu.android.view.compose.setting.widget

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import app.shosetsu.android.view.compose.LabeledCheckbox

@Composable
inline fun <reified T> MultiSelectListPreferenceWidget(
	title: String,
	subtitle: String,
	icon: ImageVector? = null,
	possibleValues: List<T>,
	selectedValues: Set<T>,
	crossinline stringify: @Composable (T) -> String,
	crossinline onValuesChange: (Set<T>) -> Unit,
) {
	var isDialogShown by remember { mutableStateOf(false) }

	TextPreferenceWidget(
		title = title,
		subtitle = subtitle,
		icon = icon,
		onPreferenceClick = { isDialogShown = true },
	)

	if (isDialogShown) {
		val selected = remember { mutableStateListOf(*selectedValues.toTypedArray()) }
		AlertDialog(
			onDismissRequest = { isDialogShown = false },
			title = { Text(text = title) },
			text = {
				LazyColumn {
					possibleValues.forEach { current ->
						item {
							val isSelected = selected.contains(current)
							LabeledCheckbox(
								label = stringify(current),
								checked = isSelected,
								onCheckedChange = {
									if (it) {
										selected.add(current)
									} else {
										selected.remove(current)
									}
								},
							)
						}
					}
				}
			},
			properties = DialogProperties(
				usePlatformDefaultWidth = true,
			),
			confirmButton = {
				TextButton(
					onClick = {
						onValuesChange(selected.toMutableSet())
						isDialogShown = false
					},
				) {
					Text(text = stringResource(android.R.string.ok))
				}
			},
			dismissButton = {
				TextButton(onClick = { isDialogShown = false }) {
					Text(text = stringResource(android.R.string.cancel))
				}
			},
		)
	}
}
