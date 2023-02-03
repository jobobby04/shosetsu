package app.shosetsu.android.ui.more

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.enterAlwaysScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.shosetsu.android.R
import app.shosetsu.android.common.ext.ComposeView
import app.shosetsu.android.ui.theme.ShosetsuTheme
import app.shosetsu.android.view.controller.ShosetsuFragment
import app.shosetsu.android.view.controller.base.CollapsedToolBarController
import app.shosetsu.android.view.controller.base.HomeFragment
import kotlinx.coroutines.launch

/*
 * This file is part of Shosetsu.
 *
 * Shosetsu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Shosetsu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Shosetsu.  If not, see <https://www.gnu.org/licenses/>.
 */

/**
 * shosetsu
 * 12 / 09 / 2020
 *
 * Option for download queue
 */
@Deprecated("Compose")
class MoreFragment
	: ShosetsuFragment(), CollapsedToolBarController, HomeFragment {

	override val viewTitleRes: Int = R.string.more

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedViewState: Bundle?
	): View {
		return ComposeView {
		}
	}
}

@Composable
fun MoreView(
	onNavToDownloads: () -> Unit = {},
	onNavToBackup: () -> Unit = {},
	onNavToRepositories: () -> Unit = {},
	onNavToCategories: () -> Unit = {},
	onNavToStyles: () -> Unit = {},
	onNavToAddShare: () -> Unit = {},
	onNavToAnalytics: () -> Unit = {},
	onNavToHistory: () -> Unit = {},
	onNavToSettings: () -> Unit = {},
	onNavToAbout: () -> Unit = {},
	drawerIcon: @Composable () -> Unit
) {
	ShosetsuTheme {
		val hostState = remember { SnackbarHostState() }
		val scope = rememberCoroutineScope()
		val context = LocalContext.current

		MoreContent(
			hostState,
			showStyleBar = {
				scope.launch {
					hostState.showSnackbar(context.getString(R.string.style_wait))
				}
			},
			onNavToDownloads,
			onNavToBackup,
			onNavToRepositories,
			onNavToCategories,
			onNavToStyles,
			onNavToAddShare,
			onNavToAnalytics,
			onNavToHistory,
			onNavToSettings,
			onNavToAbout,
			drawerIcon = drawerIcon
		)
	}
}

@Composable
fun MoreItemContent(
	@StringRes title: Int,
	@DrawableRes drawableRes: Int,
	onClick: () -> Unit
) {
	Box(
		modifier = Modifier
			.clickable(onClick = onClick)
			.fillMaxWidth(),
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

@Preview
@Composable
fun PreviewMoreContent() {
	MoreContent(
		hostState = remember { SnackbarHostState() },
		drawerIcon = { }
	)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreContent(
	hostState: SnackbarHostState,
	showStyleBar: () -> Unit = {},
	onNavToDownloads: () -> Unit = {},
	onNavToBackup: () -> Unit = {},
	onNavToRepositories: () -> Unit = {},
	onNavToCategories: () -> Unit = {},
	onNavToStyles: () -> Unit = {},
	onNavToAddShare: () -> Unit = {},
	onNavToAnalytics: () -> Unit = {},
	onNavToHistory: () -> Unit = {},
	onNavToSettings: () -> Unit = {},
	onNavToAbout: () -> Unit = {},
	drawerIcon: @Composable () -> Unit
) {
	Scaffold(
		topBar = {
			TopAppBar(
				title = {
					Text(stringResource(R.string.more))
				},
				scrollBehavior = enterAlwaysScrollBehavior(),
				navigationIcon = drawerIcon
			)
		},
		snackbarHost = {
			SnackbarHost(hostState)
		},
	) { padding ->
		LazyColumn(
			modifier = Modifier
				.fillMaxSize()
				.padding(padding),
			contentPadding = PaddingValues(bottom = 80.dp)
		) {
			item {
				Box(
					modifier = Modifier.fillMaxWidth()
				) {
					Image(
						painterResource(R.drawable.shou_icon_thick),
						stringResource(R.string.app_name),
						modifier = Modifier
							.height(120.dp)
							.align(Alignment.Center),
					)
				}
			}
			item {
				Divider()
			}
			item {
				MoreItemContent(R.string.downloads, R.drawable.download, onNavToDownloads)
			}

			item {
				MoreItemContent(R.string.backup, R.drawable.restore, onNavToBackup)
			}

			item {
				MoreItemContent(
					R.string.repositories,
					R.drawable.add_shopping_cart,
					onNavToRepositories
				)
			}

			item {
				MoreItemContent(
					R.string.categories,
					R.drawable.ic_baseline_label_24,
					onNavToCategories
				)
			}

			/*item {
				MoreItemContent(R.string.styles, R.drawable.ic_baseline_style_24, showStyleBar)
			}*/

			item {
				MoreItemContent(
					R.string.qr_code_scan,
					R.drawable.ic_baseline_link_24,
					onNavToAddShare
				)
			}


			item {
				MoreItemContent(
					R.string.fragment_more_dest_analytics,
					R.drawable.baseline_analytics_24,
					onNavToAnalytics
				)
			}

			item {
				MoreItemContent(
					R.string.fragment_more_dest_history,
					R.drawable.baseline_history_edu_24,
					onNavToHistory
				)
			}

			item {
				MoreItemContent(R.string.settings, R.drawable.settings, onNavToSettings)
			}

			item {
				MoreItemContent(R.string.about, R.drawable.info_outline, onNavToAbout)
			}
		}
	}
}
