package app.shosetsu.android.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults.cardElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import app.shosetsu.android.R
import app.shosetsu.android.common.ext.ComposeView
import app.shosetsu.android.common.ext.navigateSafely
import app.shosetsu.android.common.ext.setShosetsuTransition
import app.shosetsu.android.view.compose.ShosetsuCompose
import app.shosetsu.android.view.controller.ShosetsuFragment

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

/**
 * Shosetsu
 *
 * @since 06 / 10 / 2021
 * @author Doomsdayrs
 */
class SettingsFragment : ShosetsuFragment() {

	override val viewTitleRes: Int = R.string.settings

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedViewState: Bundle?
	): View {
		setViewTitle()
		return ComposeView {
			SettingsView {
				findNavController().navigateSafely(
					it,
					null,
					navOptions { setShosetsuTransition() })
			}
		}
	}
}

@Composable
fun SettingsView(
	navigate: (Int) -> Unit
) {
	ShosetsuCompose {
		SettingsContent(navigate)
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingMenuItem(@StringRes title: Int, @DrawableRes drawableRes: Int, onClick: () -> Unit) {
	Card(
		modifier = Modifier
			.clickable(onClick = onClick)
			.fillMaxWidth(),
		elevation = cardElevation(0.dp),
		colors = CardDefaults.cardColors(
			containerColor = colorResource(android.R.color.transparent),
		),
		shape = RectangleShape
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically
		) {
			Icon(
				painterResource(drawableRes),
				null,
				modifier = Modifier
					.padding(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 24.dp)
					.size(24.dp),
				tint = MaterialTheme.colorScheme.primary
			)
			Text(stringResource(title))
		}
	}
}

@Composable
fun SettingsContent(navigate: (Int) -> Unit) {
	Column {
		SettingMenuItem(R.string.view, R.drawable.view_module) {
			navigate(R.id.action_settingsController_to_viewSettings)
		}

		SettingMenuItem(R.string.reader, R.drawable.book) {
			navigate(R.id.action_settingsController_to_readerSettings)
		}

		SettingMenuItem(R.string.download, R.drawable.download) {
			navigate(R.id.action_settingsController_to_downloadSettings)
		}

		SettingMenuItem(R.string.update, R.drawable.update) {
			navigate(R.id.action_settingsController_to_updateSettings)
		}

		SettingMenuItem(R.string.advanced, R.drawable.settings) {
			navigate(R.id.action_settingsController_to_advancedSettings)
		}
	}
}