package app.shosetsu.android.ui.reader.page

import android.annotation.SuppressLint
import android.content.pm.ApplicationInfo
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import app.shosetsu.android.BuildConfig
import app.shosetsu.android.common.ShosetsuAccompanistWebChromeClient
import app.shosetsu.android.common.utils.ProgressiveDelayer
import app.shosetsu.android.view.compose.ScrollStateBar
import com.google.accompanist.web.LoadingState
import com.google.accompanist.web.WebView
import com.google.accompanist.web.WebViewState
import com.google.accompanist.web.rememberWebViewNavigator
import com.google.accompanist.web.rememberWebViewStateWithHTMLData

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
 *
 * @since 18 / 03 / 2022
 * @author Doomsdayrs
 */

@Composable
fun HTMLPage(
	html: String,
	progress: Double,
	onScroll: (perc: Double) -> Unit,
	onClick: () -> Unit,
	onDoubleClick: () -> Unit
) {
	val scope = rememberCoroutineScope()
	val scrollState = rememberScrollState()
	val state = rememberWebViewStateWithHTMLData(html)
	val navigator = rememberWebViewNavigator(scope)

	/*
	LaunchedEffect(navigator) {
		val bundle = state.viewState
		if (bundle == null) {
			navigator.loadHtml(html)
		}
	}
	 */

	var first by remember { mutableStateOf(true) }

	if (scrollState.isScrollInProgress)
		DisposableEffect(Unit) {
			onDispose {
				println("Scrolling: ${scrollState.value} out of ${scrollState.maxValue}")
				if (scrollState.value != 0)
					onScroll((scrollState.value.toDouble() / scrollState.maxValue))
				else onScroll(0.0)
			}
		}

	val backgroundColor = MaterialTheme.colors.background
	ScrollStateBar(scrollState) {
		WebView(
			state = state,
			captureBackPresses = false,
			onCreated = { webView ->
				webView.setBackgroundColor(backgroundColor.toArgb())
				webView.settings.apply {
					@SuppressLint("SetJavaScriptEnabled")
					javaScriptEnabled = true
					blockNetworkLoads = false // enable content loading
					blockNetworkImage = false // enable image loading
					loadsImagesAutomatically = true // ensure images are loaded
					allowFileAccess = true // allow to cache content
					// try to use loaded cache
					cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
				}

				val inter = ShosetsuScript(
					onClickMethod = onClick,
					onDClickMethod = onDoubleClick
				)

				webView.addJavascriptInterface(inter, "shosetsuScript")
				webView.isScrollContainer = false

				// Debug mode (chrome://inspect/#devices)
				if (BuildConfig.DEBUG &&
					0 != webView.context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE
				) {
					WebView.setWebContentsDebuggingEnabled(true)
				}
			},
			modifier = Modifier
				.fillMaxWidth()
				.heightIn(min = 1.dp)
				.verticalScroll(scrollState),
			client = ChapterReaderAccompanistWebViewClient(),
			chromeClient = ShosetsuAccompanistWebChromeClient(),
			navigator = navigator,
		)
	}

	val delayer = remember { ProgressiveDelayer(100) }
	LaunchedEffect(scrollState.maxValue, state.loadingState) {
		// Ensure this only occurs on the first time
		if (first) {
			// We can tell the view is not loaded properly by the scroll state
			if (scrollState.maxValue != 0 && scrollState.maxValue != Int.MAX_VALUE) {
				// Ensure state is loading
				if (!state.sIsLoading) {
					delayer.delay() // each call makes the delay longer
					println("I am scrolling!: ${scrollState.maxValue} by $progress")
					val result = (scrollState.maxValue * progress).toInt()
					println("Scrolling to $result from ${scrollState.value}")
					scrollState.scrollTo(result)
					first = false
					delayer.reset()
				}
			}
		}
	}
}

val WebViewState.sIsLoading: Boolean
	get() = (loadingState is LoadingState.Loading &&
			(loadingState as LoadingState.Loading).progress != 1f) ||
			loadingState is LoadingState.Initializing