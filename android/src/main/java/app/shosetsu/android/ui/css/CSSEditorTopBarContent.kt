package app.shosetsu.android.ui.css

import androidx.appcompat.R
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.shosetsu.android.view.compose.SimpleIconButton
import app.shosetsu.android.view.compose.pagerTabIndicatorOffset
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CSSEditorTopBarContent(
	pagerState: PagerState,
	pages: ImmutableList<String>,
	cssTitle: String,
	onBack: () -> Unit,
	onHelp: () -> Unit
) {
	val scope = rememberCoroutineScope()
	Column {
		TopAppBar(
			title = {
				Text(cssTitle)
			},
			scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
			navigationIcon = {
				SimpleIconButton(
					Icons.AutoMirrored.Filled.ArrowBack,
					stringResource(R.string.abc_action_bar_up_description),
					onClick = onBack
				)
			},
			actions = {
				SimpleIconButton(
					Icons.AutoMirrored.Outlined.HelpOutline,
					stringResource(app.shosetsu.android.R.string.help),
					onClick = onHelp
				)
			},
			colors = TopAppBarDefaults.topAppBarColors(
				containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
				titleContentColor = MaterialTheme.colorScheme.onSurface,
			)
		)
		TabRow(
			// Our selected tab is our current page
			selectedTabIndex = pagerState.currentPage,
			// Override the indicator, using the provided pagerTabIndicatorOffset modifier
			indicator = { tabPositions ->
				TabRowDefaults.SecondaryIndicator(
					Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
				)
			},
			containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
			contentColor = MaterialTheme.colorScheme.onSurface
		) {
			// Add tabs for all of our pages
			pages.forEachIndexed { index, title ->
				Tab(
					text = { Text(title) },
					selected = pagerState.currentPage == index,
					onClick = {
						scope.launch {
							pagerState.scrollToPage(index)
						}
					},
				)
			}
		}
	}
}