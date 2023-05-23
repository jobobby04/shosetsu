package app.shosetsu.android.ui.reader.page

import android.annotation.SuppressLint
import android.content.pm.ApplicationInfo
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import app.shosetsu.android.BuildConfig
import app.shosetsu.android.common.ShosetsuAccompanistWebChromeClient
import app.shosetsu.android.view.compose.ScrollStateBar
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewStateWithHTMLData
import kotlinx.coroutines.launch

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
	val scrollState = rememberScrollState()
	val state = rememberWebViewStateWithHTMLData(html)
	var first by remember { mutableStateOf(true) }

	if (scrollState.isScrollInProgress)
		DisposableEffect(Unit) {
			onDispose {
				if (scrollState.value != 0)
					onScroll((scrollState.value.toDouble() / scrollState.maxValue))
				else onScroll(0.0)
			}
		}

	val backgroundColor = MaterialTheme.colors.background
	ScrollStateBar(scrollState) {
		WebView(
			state,
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
				.verticalScroll(scrollState),
			client = ShosetsuAccompanistWebViewClient,
			chromeClient = ShosetsuAccompanistWebChromeClient
		)
	}

	// Avoid scrolling when the state has not fully loaded
	if (scrollState.maxValue != 0 && scrollState.maxValue != Int.MAX_VALUE && !state.isLoading) {
		if (first) {
			LaunchedEffect(progress) {
				launch {
					val result = (scrollState.maxValue * progress).toInt()
					scrollState.scrollTo(result)
					first = false
				}
			}
		}
	}
}