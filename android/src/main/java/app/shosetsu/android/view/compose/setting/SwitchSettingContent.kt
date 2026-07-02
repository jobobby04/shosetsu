package app.shosetsu.android.view.compose.setting

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import app.shosetsu.android.common.SettingKey
import app.shosetsu.android.common.ext.launchIO
import app.shosetsu.android.domain.repository.base.ISettingsRepository
import app.shosetsu.android.view.compose.setting.widget.SwitchPreferenceWidget

@Composable
fun SwitchSettingContent(
	title: String,
	description: String,
	repo: ISettingsRepository,
	key: SettingKey<Boolean>,
	modifier: Modifier = Modifier,
	enabled: Boolean = true,
	andAlso: suspend (newValue: Boolean) -> Unit = {}
) {
	val value by repo.getBooleanFlow(key).collectAsState()
	SwitchPreferenceWidget(
		title = title,
		subtitle = description,
		checked = value,
		modifier = modifier,
		enabled = enabled,
		iconDescription = null
	) { it: Boolean ->
		launchIO {
			repo.setBoolean(key, it)
			andAlso(it)
		}
	}
}
