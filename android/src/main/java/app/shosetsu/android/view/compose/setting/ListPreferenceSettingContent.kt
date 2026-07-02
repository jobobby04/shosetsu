package app.shosetsu.android.view.compose.setting

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import app.shosetsu.android.common.SettingKey
import app.shosetsu.android.common.ext.launchIO
import app.shosetsu.android.domain.repository.base.ISettingsRepository
import app.shosetsu.android.view.compose.setting.widget.ListPreferenceWidget

@Composable
fun <T> ListPreferenceSettingContent(
	title: String,
	description: String? = null,
	choices: List<T>,
	stringify: (T) -> String,
	toKey: (T) -> String = { stringify(it) },
	fromKey: (String) -> T,
	icon: ImageVector? = null,
	repo: ISettingsRepository,
	key: SettingKey<String>
) {
	val choice by repo.getStringFlow(key).collectAsState()

	ListPreferenceWidget(
		title = title,
		subtitle = description ?: stringify(fromKey(choice)),
		icon = icon,
		value = fromKey(choice),
		entries = choices.associateWith { stringify(it) },
		onValueChange = {
			launchIO { repo.setString(key, toKey(it)) }
		},
		iconDescription = null
	)
}

@Composable
fun StringListPreferenceSettingContent(
	title: String,
	choices: List<String>,
	icon: ImageVector? = null,
	iconDescription: String?,
	repo: ISettingsRepository,
	key: SettingKey<Int>,
) {
	val choice by repo.getIntFlow(key).collectAsState()

	ListPreferenceWidget(
		value = choices[choice],
		title = title,
		subtitle = choices[choice],
		icon = icon,
		entries = choices.associateWith { it },
		onValueChange = {
			launchIO { repo.setInt(key, choices.indexOf(it)) }
		},
		iconDescription = iconDescription
	)
}
