package app.shosetsu.android.view.compose.setting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import app.shosetsu.android.common.SettingKey
import app.shosetsu.android.common.ext.launchIO
import app.shosetsu.android.common.utils.ProxyConfig
import app.shosetsu.android.domain.repository.base.ISettingsRepository
import app.shosetsu.android.view.compose.SimpleIconButton
import app.shosetsu.android.view.compose.setting.widget.TextPreferenceWidget

@Composable
fun ProxySettingsContent(
	title: String,
	description: String,
	repo: ISettingsRepository,
	usedKey: SettingKey<Boolean>,
	settingKey: SettingKey<String>,
	modifier: Modifier = Modifier,
) {
	val isUsed by repo.getBooleanFlow(usedKey).collectAsState()
	val proxySetting by repo.getStringFlow(settingKey).collectAsState()

	ProxySettingsContent(
		title, description, isUsed, proxySetting, modifier
	) { used, settings ->
		launchIO {
			repo.setBoolean(usedKey, used)
			repo.setString(settingKey, settings)
		}
	}
}

@Composable
fun ProxySettingsContent(
	title: String,
	description: String,
	proxyEnabled: Boolean,
	proxyString: String,
	modifier: Modifier = Modifier,
	onValueChanged: (newEnabled: Boolean, newSetting: String) -> Unit
) {
	var openDialog by remember { mutableStateOf(false) }

	TextPreferenceWidget(
		title = title,
		subtitle = description,
		modifier = modifier,
		widget = {
			Text(
				color = if (proxyEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
				text = if (proxyEnabled) "On" else "Off"
			)
		},
		onPreferenceClick = { openDialog = !openDialog },
		iconDescription = null
	)
	Text(
		color = if (proxyEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
		text = if (proxyEnabled) "On" else "Off"
	)

	if (openDialog)
		Dialog({ openDialog = false }) {
			ProxySettingsDialogContent(
				title,
				"",
				proxyEnabled,
				proxyString
			) { newEnabled, newSetting ->
				onValueChanged(newEnabled, newSetting)
				openDialog = false
			}
		}

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProxySettingsDialogContent(
	title: String,
	description: String,
	proxyEnabled: Boolean,
	proxyString: String,
	onValueChanged: (newEnabled: Boolean, newSetting: String) -> Unit
) {

	var enabled by remember { mutableStateOf(proxyEnabled) }
	var config by remember { mutableStateOf(ProxyConfig.fromString(proxyString)) }
	var passwordVisible by remember { mutableStateOf(false) }

	val hostValid = !enabled or (enabled and config.hostname.isNotEmpty())
	val portValid = !enabled or (enabled and (config.port in 80..65535))
	val usernameValid = !config.authUsed or (config.authUsed and config.username.isNotEmpty())
	val saveEnabled = !enabled or (config.valid())

	Card(modifier = Modifier.fillMaxWidth()) {
		Column(
			modifier = Modifier.padding(16.dp),
			horizontalAlignment = Alignment.CenterHorizontally,
		) {
			TextPreferenceWidget(
				title = title,
				subtitle = description,
				widget = {
					Switch(enabled, null)
				},
				onPreferenceClick = { enabled = !enabled },
				iconDescription = null
			)
			Row {
				TextField(
					enabled = enabled,
					value = config.hostname,
					onValueChange = {
						config = config.copy(hostname = it)
					},
					isError = !hostValid,
					modifier = Modifier.weight(2f),
					singleLine = true,
					label = { Text(text = "Host") },
				)
				TextField(
					enabled = enabled,
					modifier = Modifier.weight(1f),
					value = if (config.port > 0) config.port.toString() else "",
					onValueChange = {
						config = config.copy(port = it.toIntOrNull() ?: -1)
					},
					isError = !portValid,
					singleLine = true,
					label = { Text(text = "Port") },
				)
			}

			Row(
				modifier = Modifier.fillMaxWidth(),
				verticalAlignment = Alignment.CenterVertically
			) {
				Checkbox(
					enabled = enabled,
					checked = config.authUsed,
					onCheckedChange = {
						config = config.copy(authUsed = it)
					},
				)
				Text(
					text = "use authentication",
					modifier = Modifier.clickable(
						enabled,
						onClick = {
							config = config.copy(authUsed = !config.authUsed)
						}
					)
				)
			}

			TextField(
				value = config.username,
				onValueChange = {
					config = config.copy(username = it)
				},
				isError = !usernameValid,
				singleLine = true,
				label = { Text(text = "Username") },
				enabled = enabled and config.authUsed
			)

			TextField(
				value = config.password,
				onValueChange = {
					config = config.copy(password = it)
				},
				singleLine = true,
				visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
				label = { Text(text = "Password") },
				enabled = enabled and config.authUsed,
				trailingIcon = {
					val icon = if (passwordVisible) Icons.Outlined.Info else Icons.Filled.Info
					SimpleIconButton(icon, description = null, onClick = { passwordVisible = !passwordVisible })
				}
			)
			Button(
				modifier = Modifier
					.fillMaxWidth()
					.padding(top = 8.dp),
				enabled = saveEnabled,
				onClick = {
					onValueChanged(enabled, config.toString())
				}
			) {
				Text("Save", textAlign = TextAlign.Center)
			}
		}
	}
}


@Preview
@Composable
fun ProxySettingsDialogFilledContent() {
	Box(modifier = Modifier.size(300.dp, 500.dp)) {
		ProxySettingsDialogContent(
			title = "use proxy",
			description = "description",
			proxyEnabled = true,
			proxyString = "user:pass@longhostnameislong:8080"
		) { _, _ -> }
	}
}

@Preview
@Composable
fun ProxySettingsDialogEmptyContent() {
	Box(modifier = Modifier.size(300.dp, 500.dp)) {
		ProxySettingsDialogContent(
			title = "use proxy",
			description = "description",
			proxyEnabled = false,
			proxyString = "ab:pwd@"
		) { _, _ -> }
	}
}

@Preview
@Composable
fun PreviewProxySettingsDisabled() {
	ProxySettingsContent(
		title = "title",
		description = "description",
		proxyEnabled = false,
		proxyString = "",
	) { _, _ -> }
}

@Preview
@Composable
fun PreviewProxySettingsEnabled() {
	ProxySettingsContent(
		title = "title",
		description = "description",
		proxyEnabled = true,
		proxyString = "",
	) { _, _ -> }
}
