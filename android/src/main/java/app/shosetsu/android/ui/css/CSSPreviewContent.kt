package app.shosetsu.android.ui.css

import android.annotation.SuppressLint
import android.webkit.WebSettings
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.shosetsu.android.R
import app.shosetsu.android.common.ShosetsuAccompanistWebChromeClient
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewStateWithHTMLData

@Composable
fun CSSPreviewContent(
	cssContent: String,
) {
	val state =
		rememberWebViewStateWithHTMLData(stringResource(R.string.activity_css_example, cssContent))
	WebView(
		state,
		modifier = Modifier
			.fillMaxSize(),
		chromeClient = ShosetsuAccompanistWebChromeClient(),
		captureBackPresses = false,
		onCreated = {
			it.settings.apply {
				@SuppressLint("SetJavaScriptEnabled")
				javaScriptEnabled = true
				cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
			}
		},
	)
}