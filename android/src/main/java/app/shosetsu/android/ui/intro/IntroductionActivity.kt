package app.shosetsu.android.ui.intro

import android.os.Build
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.shosetsu.android.R
import app.shosetsu.android.common.consts.URL_KOFI
import app.shosetsu.android.common.consts.URL_PATREON
import app.shosetsu.android.common.ext.readAsset
import app.shosetsu.android.common.ext.viewModel
import app.shosetsu.android.common.ext.viewModelDi
import app.shosetsu.android.view.compose.ScrollStateBar
import app.shosetsu.android.view.compose.ShosetsuCompose
import app.shosetsu.android.viewmodel.abstracted.AIntroViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI

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
 * shosetsu
 * 15 / 03 / 2020
 */
class IntroductionActivity : AppCompatActivity(), DIAware {

	override val di: DI by closestDI()
	private val viewModel: AIntroViewModel by viewModel()

	/***/
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setContent {
			ShosetsuCompose {
				IntroView(viewModel) {
					finish()
				}
			}
		}
	}
}

/**
 * Introduction view in compose
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun IntroView(
	viewModel: AIntroViewModel = viewModelDi(),
	exit: () -> Unit
) {
	val state = rememberPagerState()
	val scope = rememberCoroutineScope()
	val isLicenseRead by viewModel.isLicenseRead.collectAsState()
	val shouldSupportShowNext by viewModel.shouldSupportShowNext.collectAsState()

	BackHandler {
		if (viewModel.isFinished) {
			exit()
		}
	}

	LaunchedEffect(state.currentPage) {
		if (state.currentPage == IntroPages.End.ordinal)
			viewModel.setFinished()
	}

	fun nextPage() {
		if (state.currentPage != IntroPages.End.ordinal)
			scope.launch {
				state.scrollToPage(state.currentPage + 1)
			}
		else {
			exit()
		}
	}

	Scaffold(
		bottomBar = {
			BottomAppBar {
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.SpaceBetween,
					verticalAlignment = Alignment.CenterVertically
				) {
					Box {
						if (state.currentPage > 0) {
							IconButton(
								onClick = {
									scope.launch {
										state.scrollToPage(state.currentPage - 1)
									}
								}
							) {
								Icon(
									Icons.Default.ArrowBack,
									stringResource(androidx.navigation.ui.R.string.nav_app_bar_navigate_up_description)
								)
							}
						}
					}
					Box {
						if (
							state.currentPage != IntroPages.Support.ordinal ||
							shouldSupportShowNext
						) {
							IconButton(
								onClick = {
									nextPage()
								}
							) {
								Icon(
									if (state.currentPage != IntroPages.End.ordinal)
										Icons.Default.ArrowForward
									else Icons.Default.Close,
									stringResource(
										if (state.currentPage != IntroPages.End.ordinal)
											R.string.intro_page_next else R.string.intro_close
									)
								)
							}
						}

					}
				}
			}
		}
	) {
		HorizontalPager(
			IntroPages.values().size,
			state = state,
			modifier = Modifier.padding(it),
			userScrollEnabled = state.currentPage != IntroPages.Support.ordinal
		) { page ->
			when (page) {
				IntroPages.Title.ordinal -> IntroTitlePage()
				IntroPages.Explanation.ordinal -> IntroExplanationPage()
				IntroPages.License.ordinal -> {
					IntroLicensePage(isLicenseRead) {
						viewModel.setLicenseRead()
					}
				}

				IntroPages.ACRA.ordinal -> {
					val isACRA by viewModel.isACRAEnabled.collectAsState()
					IntroACRAPage(
						isACRA
					) {
						viewModel.setACRAEnabled(it)
					}
				}

				IntroPages.Support.ordinal -> IntroSupportPage(
					{
						viewModel.supportShowNext()
					},
					::nextPage
				)

				IntroPages.Permissions.ordinal -> IntroPermissionPage()
				IntroPages.End.ordinal -> IntroEndPage()
			}
		}
	}
}

enum class IntroPages {
	Title,
	Explanation,
	License,
	ACRA,
	Permissions,
	Support,
	End
}

@Preview
@Composable
fun PreviewIntroTitlePage() {
	IntroTitlePage()
}

@Composable
fun IntroTitlePage() {
	Column(
		modifier = Modifier
			.fillMaxSize()
			.padding(16.dp),
		verticalArrangement = Arrangement.Center,
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Icon(painterResource(R.drawable.shou_icon), stringResource(R.string.app_name))
		Text(
			stringResource(R.string.intro_title_greet),
			style = MaterialTheme.typography.headlineMedium,
			textAlign = TextAlign.Center
		)
	}
}

@Preview
@Composable
fun PreviewIntroExplanationPage() {
	IntroExplanationPage()
}

@Composable
fun IntroExplanationPage() {

	Column(
		modifier = Modifier
			.fillMaxSize()
			.padding(16.dp),
		verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
		horizontalAlignment = Alignment.CenterHorizontally,
	) {
		Icon(Icons.Default.Info, null, modifier = Modifier.size(64.dp))
		Text(
			stringResource(R.string.intro_what_is_app),
			style = MaterialTheme.typography.headlineSmall
		)
		Text(
			stringResource(R.string.intro_what_is_app_desc_new),
			style = MaterialTheme.typography.bodyLarge,
			textAlign = TextAlign.Center
		)
	}

}

@Preview
@Composable
fun PreviewIntroLicensePage() {
	var isLicenseRead by remember { mutableStateOf(false) }
	IntroLicensePage(
		isLicenseRead = isLicenseRead,
		onLicenseRead = {
			isLicenseRead = true
		}
	)
}

@Composable
fun IntroLicensePage(
	isLicenseRead: Boolean,
	onLicenseRead: () -> Unit
) {

	Column(
		modifier = Modifier
			.fillMaxSize(),
		verticalArrangement = Arrangement.Center,
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Card(
			shape = RectangleShape
		) {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.padding(16.dp),
				verticalArrangement = Arrangement.Center,
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				Text(
					stringResource(R.string.license),
					style = MaterialTheme.typography.headlineSmall
				)
				Text(
					stringResource(R.string.intro_license_desc_new),
					style = MaterialTheme.typography.bodyLarge,
					textAlign = TextAlign.Center
				)
			}
		}
		val scrollState = rememberScrollState()
		LaunchedEffect(scrollState.value) {
			if (!isLicenseRead) // Only run if the license is not read to save on performance
				if (scrollState.maxValue != 0 && scrollState.value != 0) // prevent db0
					if (scrollState.value / scrollState.maxValue >= .9) {
						onLicenseRead()
					}
		}
		ScrollStateBar(scrollState) {
			Text(
				LocalContext.current.readAsset("license-gplv3.txt"),
				style = MaterialTheme.typography.bodyMedium,
				modifier = Modifier
					.verticalScroll(scrollState)
					.padding(16.dp)
			)
		}
	}

}

@Preview
@Composable
fun PreviewIntroACRAPage() {
	var isACRAEnabled by remember { mutableStateOf(false) }
	IntroACRAPage(
		isACRAEnabled
	) {
		isACRAEnabled = it
	}
}

@Composable
fun IntroACRAPage(
	isACRAEnabled: Boolean,
	setACRAEnabled: (Boolean) -> Unit
) {

	Column(
		modifier = Modifier
			.fillMaxSize()
			.padding(16.dp),
		verticalArrangement = Arrangement.Center,
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Text(stringResource(R.string.intro_acra), style = MaterialTheme.typography.headlineSmall)
		Text(
			stringResource(R.string.intro_acra_desc),
			style = MaterialTheme.typography.bodyLarge,
			textAlign = TextAlign.Center
		)
		Checkbox(isACRAEnabled, setACRAEnabled)
	}

}

@Preview
@Composable
fun PreviewIntroAdsPage() {
	IntroAdsPage()
}

@Composable
fun IntroAdsPage() {
	// TODO("Ask the users if they want to enable ads to make the developer money")
}

@Preview
@Composable
fun PreviewIntroPermissionPage() {
	IntroPermissionPage()
}

@Composable
fun IntroPermissionPage() {

	Column(
		modifier = Modifier
			.fillMaxSize()
			.padding(16.dp),
		verticalArrangement = Arrangement.Center,
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Text(
			stringResource(R.string.intro_perm_title),
			style = MaterialTheme.typography.headlineSmall
		)
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			IntroPermissionRow(
				android.Manifest.permission.POST_NOTIFICATIONS,
				stringResource(R.string.intro_perm_notif_desc)
			)
		} else {
			Text(
				stringResource(R.string.intro_perm_none),
				style = MaterialTheme.typography.bodyLarge,
				textAlign = TextAlign.Center
			)
		}
	}

}

@Composable
fun IntroSupportPage(
	showNext: () -> Unit,
	next: () -> Unit,
) {
	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
	) {
		// Header
		Card(
			shape = RectangleShape
		) {
			Text(
				stringResource(R.string.intro_support_title),
				style = MaterialTheme.typography.headlineSmall,
				modifier = Modifier
					.padding(16.dp)
					.fillMaxWidth(),
				textAlign = TextAlign.Center
			)
		}

		// Body
		Column(
			modifier = Modifier
				.fillMaxSize()
				.verticalScroll(rememberScrollState()),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.spacedBy(8.dp)
		) {
			Text(
				stringResource(R.string.intro_support_desc),
				style = MaterialTheme.typography.bodyMedium,
				modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp)
			)
			val uriHandler = LocalUriHandler.current

			Column(
				horizontalAlignment = Alignment.CenterHorizontally,
				verticalArrangement = Arrangement.spacedBy(4.dp),
				modifier = Modifier.padding(start = 16.dp, end = 16.dp)
			) {
				IntroSupportItem(R.string.patreon, URL_PATREON) {
					showNext()
					uriHandler.openUri(URL_PATREON)
				}

				IntroSupportItem(R.string.kofi, URL_KOFI) {
					showNext()
					uriHandler.openUri(URL_KOFI)
				}
			}

			val disagree by remember {
				derivedStateOf {
					listOf(
						R.string.support_disagree_1,
						R.string.support_disagree_2,
						R.string.support_disagree_3
					).random()
				}
			}

			TextButton(
				{
					showNext()
					next()
				},
				modifier = Modifier
					.fillMaxWidth()
					.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
			) {
				Text(stringResource(disagree))
			}
		}
	}
}

@Preview
@Composable
fun PreviewIntroSupportItem() {
	IntroSupportItem(
		R.string.pause,
		"link"
	) {
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntroSupportItem(textId: Int, link: String, onClick: () -> Unit) {
	Card(
		onClick = onClick,
		modifier = Modifier
			.fillMaxWidth()
	) {
		Column(
			modifier = Modifier
				.padding(8.dp)
				.fillMaxWidth()
		) {
			Text(stringResource(textId), style = MaterialTheme.typography.titleSmall)
			Text(link, style = MaterialTheme.typography.bodySmall)
		}
	}
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun IntroPermissionRow(
	permission: String,
	description: String
) {
	val permissionState = rememberPermissionState(permission)

	Row(
		modifier = Modifier.fillMaxWidth(),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.SpaceBetween
	) {
		Text(description, modifier = Modifier.fillMaxWidth(.7f))

		Checkbox(
			permissionState.status.isGranted,
			onCheckedChange = {
				permissionState.launchPermissionRequest()
			},
			modifier = Modifier.fillMaxWidth(.2f)
		)
	}
}

@Preview
@Composable
fun PreviewIntroEndPage() {
	IntroEndPage()
}

@Composable
fun IntroEndPage() {
	Column(
		modifier = Modifier
			.fillMaxSize()
			.padding(16.dp),
		verticalArrangement = Arrangement.Center,
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Text(
			stringResource(R.string.intro_happy_end),
			style = MaterialTheme.typography.headlineSmall
		)
		Text(
			stringResource(R.string.intro_happy_end_desc),
			style = MaterialTheme.typography.bodyLarge,
			textAlign = TextAlign.Center
		)
	}

}
