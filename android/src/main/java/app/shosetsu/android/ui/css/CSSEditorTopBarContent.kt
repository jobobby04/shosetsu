package app.shosetsu.android.ui.css

import androidx.appcompat.R
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CSSEditorTopBarContent(
	cssTitle: String,
	onBack: () -> Unit,
	onHelp: () -> Unit
) {
	Column {
		TopAppBar(
			title = {
				Text(cssTitle)
			},
			scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
			navigationIcon = {
				IconButton(
					onClick = onBack
				) {
					Icon(
						Icons.Filled.ArrowBack,
						stringResource(R.string.abc_action_bar_up_description),
					)
				}
			},
			actions = {
				IconButton(
					onClick = onHelp
				) {
					Icon(
						painterResource(app.shosetsu.android.R.drawable.help_outline_24),
						stringResource(app.shosetsu.android.R.string.help),
					)
				}
			}
		)
	}
}