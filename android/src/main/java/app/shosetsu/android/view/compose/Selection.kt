package app.shosetsu.android.view.compose

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.shosetsu.android.ui.library.DeselectAllButton
import app.shosetsu.android.ui.library.InverseSelectionButton
import app.shosetsu.android.ui.library.SelectAllButton
import app.shosetsu.android.ui.library.SelectBetweenButton

@Composable
fun BoxScope.SelectionBar(content: @Composable RowScope.() -> Unit) {
	Card(
		modifier = Modifier
			.align(BiasAlignment(0f, 0.7f))
	) {
		Row {
			content()
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionTopAppBar(
	scrollBehavior: TopAppBarScrollBehavior,
	selectedCount: Int,
	onSelectAll: () -> Unit,
	onInverseSelection: () -> Unit,
	onSelectBetween: () -> Unit,
	onDeselectAll: () -> Unit,
) = TopAppBar(
	title = { Text("$selectedCount") },
	scrollBehavior = scrollBehavior,
	actions = {
		SelectAllButton(onSelectAll)
		InverseSelectionButton(onInverseSelection)
		SelectBetweenButton(onSelectBetween)
	},
	colors = TopAppBarDefaults.topAppBarColors(
		containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
	),
	navigationIcon = {
		DeselectAllButton(onDeselectAll)
	},
)
