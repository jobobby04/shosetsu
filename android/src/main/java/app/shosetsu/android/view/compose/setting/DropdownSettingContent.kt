package app.shosetsu.android.view.compose.setting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import app.shosetsu.android.view.compose.setting.widget.TextPreferenceWidget
import kotlinx.collections.immutable.ImmutableList

@Composable
fun DropdownSettingContent(
	title: String,
	description: String,
	selection: Int,
	choices: ImmutableList<String>,
	modifier: Modifier = Modifier,
	onSelection: (newValue: Int) -> Unit
) {
	// nothing to render if nothing is given
	if (choices.isEmpty()) return

	var expanded by remember { mutableStateOf(false) }

	TextPreferenceWidget(
		title = title,
		subtitle = description,
		modifier = modifier,
		onPreferenceClick = { expanded = !expanded },
		widget = {
			Row(
				verticalAlignment = Alignment.CenterVertically,
			) {
				Text(
					text = AnnotatedString(choices[selection]),
					modifier = Modifier.clickable(onClick = {
						expanded = true
					})
				)
				IconToggleButton(
					onCheckedChange = {
						expanded = it
					},
					checked = expanded,
					modifier = Modifier.wrapContentWidth()
				) {
					if (expanded)
						Icon(Icons.Outlined.ExpandLess, "")
					else
						Icon(Icons.Outlined.ExpandMore, "")
				}
				DropdownMenu(
					expanded = expanded,
					onDismissRequest = { expanded = false },
				) {
					choices.forEachIndexed { index, s ->
						DropdownMenuItem(
							onClick = {
								onSelection(index)
								expanded = false
							},
							text = {
								Text(text = AnnotatedString(s))
							}
						)
					}
				}
			}
		},
		iconDescription = null
	)
}
