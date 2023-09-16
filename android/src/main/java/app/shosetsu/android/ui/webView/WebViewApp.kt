package app.shosetsu.android.ui.webView

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.graphics.Bitmap
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebView
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import app.shosetsu.android.BuildConfig
import app.shosetsu.android.R
import app.shosetsu.android.common.ShosetsuAccompanistWebChromeClient
import app.shosetsu.android.common.consts.BundleKeys.BUNDLE_URL
import app.shosetsu.android.common.ext.openInBrowser
import app.shosetsu.android.common.ext.toast
import app.shosetsu.android.common.ext.viewModelDi
import app.shosetsu.android.view.compose.ShosetsuCompose
import app.shosetsu.android.viewmodel.abstracted.WebViewViewModel
import com.google.accompanist.web.*
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI

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
 * 31 / 07 / 2019
 *
 * @author github.com/doomsdayrs
 *
 * Opens a URL in the apps internal webview
 * This allows cross saving cookies, allowing the app to access features such as logins
 */
class WebViewApp : AppCompatActivity(), DIAware {
	override val di: DI by closestDI()

	private fun shareWebpage(url: String) {
		try {
			val intent = Intent(Intent.ACTION_SEND).apply {
				type = "text/plain"
				putExtra(Intent.EXTRA_TEXT, url)
			}
			startActivity(Intent.createChooser(intent, getString(R.string.share)))
		} catch (e: Exception) {
			e.message?.let { toast(it) }
		}
	}

	private fun clearCookies(url: String) {
		val manager = CookieManager.getInstance()
		val cookies = manager.getCookie(url) ?: return
		cookies.split(";")
			.map { it.substringBefore("=") }
			.onEach { manager.setCookie(url, "$it=;Max-Age=-1") }
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val url = intent.getStringExtra(BUNDLE_URL) ?: kotlin.run {
			toast(R.string.activity_webview_null_url)
			finish()
			return
		}

		setContent {
			WebViewAppView(
				url,
				finish = ::finish,
				shareWebpage = ::shareWebpage,
				openInBrowser = ::openInBrowser,
				onClearCookies = ::clearCookies,
			)
		}
	}
}

@Composable
fun WebViewAppView(
	url: String,
	finish: () -> Unit,
	shareWebpage: (String) -> Unit,
	openInBrowser: (String) -> Unit,
	onClearCookies: (String) -> Unit,
	viewModel: WebViewViewModel = viewModelDi(),
) {
	ShosetsuCompose {
		val userAgent by viewModel.userAgent.collectAsState()

		WebViewScreen(
			onUp = finish,
			url = url,
			userAgent = userAgent,
			onShare = shareWebpage,
			onOpenInBrowser = openInBrowser,
			onClearCookies = onClearCookies,
		)
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewScreen(
	onUp: () -> Unit,
	url: String,
	userAgent: String,
	onShare: (String) -> Unit,
	onOpenInBrowser: (String) -> Unit,
	onClearCookies: (String) -> Unit
) {
	val state = rememberWebViewState(url = url)
	val navigator = rememberWebViewNavigator()
	var currentUrl by remember { mutableStateOf(url) }
	Scaffold(
		topBar = {
			Box {
				TopAppBar(
					title = {
						Text(
							text = state.pageTitle ?: stringResource(R.string.app_name),
							maxLines = 1,
							overflow = TextOverflow.Ellipsis
						)
					},
					navigationIcon = {
						IconButton(onClick = onUp) {
							Icon(imageVector = Icons.Default.Close, contentDescription = null)
						}
					},
					actions = {
						IconButton(
							onClick = {
								if (navigator.canGoBack) {
									navigator.navigateBack()
								}
							},
							enabled = navigator.canGoBack,
						) {
							Icon(
								imageVector = Icons.Default.ArrowBack,
								contentDescription = stringResource(R.string.action_webview_back)
							)
						}
						IconButton(
							onClick = {
								if (navigator.canGoForward) {
									navigator.navigateForward()
								}
							},
							enabled = navigator.canGoForward,
						) {
							Icon(
								imageVector = Icons.Default.ArrowForward,
								contentDescription = stringResource(R.string.action_webview_forward)
							)
						}
						var overflow by remember { mutableStateOf(false) }
						IconButton(onClick = { overflow = !overflow }) {
							Icon(
								Icons.Default.MoreVert,
								contentDescription = stringResource(R.string.more)
							)
						}
						DropdownMenu(expanded = overflow, onDismissRequest = { overflow = false }) {
							DropdownMenuItem(onClick = { navigator.reload(); overflow = false },
								text = {
									Text(text = stringResource(R.string.action_webview_refresh))
								}
							)
							DropdownMenuItem(onClick = {
								onShare(currentUrl); overflow = false
							},
								text = {
									Text(text = stringResource(R.string.share))
								}
							)
							DropdownMenuItem(onClick = {
								onOpenInBrowser(currentUrl); overflow = false
							},
								text = {
									Text(text = stringResource(R.string.open_in_browser))
								}
							)
							DropdownMenuItem(onClick = {
								onClearCookies(currentUrl); overflow = false
							},
								text = {
									Text(text = stringResource(R.string.action_webview_clear_cookies))
								}
							)
						}
					}
				)
				when (val loadingState = state.loadingState) {
					is LoadingState.Initializing -> LinearProgressIndicator(
						modifier = Modifier
							.fillMaxWidth()
							.align(Alignment.BottomCenter),
					)

					is LoadingState.Loading -> {
						val animatedProgress by animateFloatAsState(
							(loadingState as? LoadingState.Loading)?.progress ?: 1f,
							animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
						)
						LinearProgressIndicator(
							progress = animatedProgress,
							modifier = Modifier
								.fillMaxWidth()
								.align(Alignment.BottomCenter),
						)
					}

					else -> {}
				}
			}

		}
	) { contentPadding ->
		val webClient = remember {
			object : AccompanistWebViewClient() {
				override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
					super.onPageStarted(view, url, favicon)
					url?.let {
						currentUrl = it
					}
				}

				override fun doUpdateVisitedHistory(
					view: WebView,
					url: String?,
					isReload: Boolean,
				) {
					super.doUpdateVisitedHistory(view, url, isReload)
					url?.let {
						currentUrl = it
					}
				}
			}
		}

		WebView(
			state = state,
			modifier = Modifier
				.fillMaxSize()
				.padding(contentPadding),
			navigator = navigator,
			onCreated = { webView ->
				webView.settings.apply {
					userAgentString = userAgent
					javaScriptEnabled = true
				}

				CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)

				// Debug mode (chrome://inspect/#devices)
				if (BuildConfig.DEBUG &&
					0 != webView.context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE
				) {
					WebView.setWebContentsDebuggingEnabled(true)
				}
			},
			client = webClient,
			chromeClient = ShosetsuAccompanistWebChromeClient()
		)
	}
}
