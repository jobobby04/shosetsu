package app.shosetsu.android.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import app.shosetsu.android.ui.main.MainView
import app.shosetsu.android.ui.theme.ShosetsuTheme

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContent {
			ShosetsuTheme {
				MainView()
			}
		}
	}
}