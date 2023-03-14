package app.shosetsu.android.view.compose.setting

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.shosetsu.android.common.SettingKey
import app.shosetsu.android.common.consts.SUB_TEXT_SIZE
import app.shosetsu.android.common.ext.launchIO
import app.shosetsu.android.domain.repository.base.ISettingsRepository

/*
 * This file is part of shosetsu.
 *
 * shosetsu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * shosetsu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with shosetsu.  If not, see <https://www.gnu.org/licenses/>.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StringSettingContent(
	title: String,
	description: String,
	value: String,
	modifier: Modifier = Modifier,
	onValueChanged: (newString: String) -> Unit
) {
	Column(
		modifier = modifier,
	) {
		OutlinedTextField(
			value = value,
			onValueChange = onValueChanged,
			label = { Text(title) },
			modifier = Modifier.fillMaxWidth()
		)
		Text(description)
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StringSettingContent(
	title: String,
	description: String,
	repo: ISettingsRepository,
	key: SettingKey<String>,
	modifier: Modifier = Modifier,
	enabled: Boolean = true,
) {
	val value by repo.getStringFlow(key).collectAsState()

	Column(
		Modifier
			.padding(horizontal = 16.dp)
	) {
		TextField(
			value = value,
			onValueChange = {
				launchIO { repo.setString(key, it) }
			},
			modifier = modifier,
			label = { Text(title) },
			enabled = enabled
		)
		Text(
			description,
			style = SUB_TEXT_SIZE,
			modifier = Modifier.alpha(0.7f),
			color = LocalContentColor.current
		)
	}
}

@Preview
@Composable
fun PreviewStringSettingContent() {
	StringSettingContent("Text Input", "This is a text input", "") {

	}
}
