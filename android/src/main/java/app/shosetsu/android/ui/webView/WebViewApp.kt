package app.shosetsu.android.ui.webView

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebView
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import app.shosetsu.android.BuildConfig
import app.shosetsu.android.R
import app.shosetsu.android.common.consts.BundleKeys.BUNDLE_URL
import app.shosetsu.android.common.consts.USER_AGENT
import app.shosetsu.android.common.ext.logI
import app.shosetsu.android.common.ext.logV
import app.shosetsu.android.common.ext.openInBrowser
import app.shosetsu.android.common.ext.toast
import app.shosetsu.android.common.utils.CookieJarSync
import com.google.accompanist.web.*
import com.google.android.material.composethemeadapter.MdcTheme
import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrl
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

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val url = intent.getStringExtra(BUNDLE_URL) ?: kotlin.run {
			toast(R.string.activity_webview_null_url)
			finish()
			return
		}

		setContent {
			MdcTheme {
				WebViewScreen(
					onUp = ::finish,
					url = url,
					onShare = ::shareWebpage,
					onOpenInBrowser = ::openInBrowser
				)
			}
		}
	}
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewScreen(
	onUp: () -> Unit,
	url: String,
	onShare: (String) -> Unit,
	onOpenInBrowser: (String) -> Unit,
) {
	val state = rememberWebViewState(url = url)
	val navigator = rememberWebViewNavigator()
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
							Icon(imageVector = Icons.Default.ArrowBack, contentDescription = stringResource(R.string.action_webview_back))
						}
						IconButton(
							onClick = {
								if (navigator.canGoForward) {
									navigator.navigateForward()
								}
							},
							enabled = navigator.canGoForward,
						) {
							Icon(imageVector = Icons.Default.ArrowForward, contentDescription = stringResource(R.string.action_webview_forward))
						}
						var overflow by remember { mutableStateOf(false) }
						IconButton(onClick = { overflow = !overflow }) {
							Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.more))
						}
						DropdownMenu(expanded = overflow, onDismissRequest = { overflow = false }) {
							DropdownMenuItem(onClick = { navigator.reload(); overflow = false }) {
								Text(text = stringResource(R.string.action_webview_refresh))
							}
							DropdownMenuItem(onClick = { onShare(state.content.getCurrentUrl()!!); overflow = false }) {
								Text(text = stringResource(R.string.share))
							}
							DropdownMenuItem(onClick = { onOpenInBrowser(state.content.getCurrentUrl()!!); overflow = false }) {
								Text(text = stringResource(R.string.open_in_browser))
							}
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
				override fun onPageFinished(view: WebView?, url: String?) {
					super.onPageFinished(view, url)
					logI(url)
					if (view == null || url == null) return
					view.evaluateJavascript("document.cookie") { cookies ->
						val httpUrl = url.toHttpUrl()
						CookieJarSync.saveFromResponse(
							httpUrl,
							cookies.split("; ").mapNotNull { Cookie.parse(httpUrl, it) }
						)
						logV("Cookies: $cookies")
					}
				}
			}
		}

		WebView(
			state = state,
			modifier = Modifier.padding(contentPadding),
			navigator = navigator,
			onCreated = { webView ->
				webView.settings.apply {
					userAgentString = USER_AGENT
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
		)
	}
}