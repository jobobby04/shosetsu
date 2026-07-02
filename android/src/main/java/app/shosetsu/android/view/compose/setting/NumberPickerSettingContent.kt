package app.shosetsu.android.view.compose.setting

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.shosetsu.android.common.SettingKey
import app.shosetsu.android.common.ext.launchIO
import app.shosetsu.android.domain.repository.base.ISettingsRepository
import app.shosetsu.android.view.compose.StandardDialog
import app.shosetsu.android.view.compose.setting.widget.TextPreferenceWidget
import app.shosetsu.android.view.uimodels.StableHolder
import com.chargemap.compose.numberpicker.NumberPicker

@Composable
fun NumberPickerSettingContent(
	title: String,
	description: String,
	range: StableHolder<IntRange>,
	repo: ISettingsRepository,
	key: SettingKey<Int>,
	modifier: Modifier = Modifier,
) {
	val selection by repo.getIntFlow(key).collectAsState()

	NumberPickerSettingContent(
		title, description, selection, range, modifier
	) {
		launchIO { repo.setInt(key, it) }
	}
}

@Composable
fun NumberPickerSettingContent(
	title: String,
	description: String,
	value: Int,
	range: StableHolder<IntRange>,
	modifier: Modifier = Modifier,
	onValueChanged: (newValue: Int) -> Unit
) {
	var openDialog by remember { mutableStateOf(false) }

	TextPreferenceWidget(
		modifier = modifier,
		title = title,
		subtitle = description,
		widget = {
			Text("$value", color = MaterialTheme.colorScheme.tertiary)
		},
		onPreferenceClick = { openDialog = true },
		iconDescription = null
	)

	if (openDialog) {
		var currentValue by remember { mutableIntStateOf(value) }
		StandardDialog(
			onDismissRequest = { openDialog = false },
			title = { Text(title) },
			onConfirm = {
				openDialog = false
				if (currentValue != value) {
					onValueChanged(currentValue)
				}
			},
		) {
			NumberPicker(
				value = currentValue,
				onValueChange = { currentValue = it },
				range = range.item,
				dividersColor = MaterialTheme.colorScheme.tertiary,
				textStyle = MaterialTheme.typography.bodyMedium
					.copy(color = MaterialTheme.colorScheme.onSurface),
			)
		}
	}
}

@Preview
@Composable
fun PreviewPickerSettingContent() {
	NumberPickerSettingContent(
		"A Number picker",
		"This is a number picker",
		2,
		range = remember { StableHolder(0..10) }
	) {

	}
}
