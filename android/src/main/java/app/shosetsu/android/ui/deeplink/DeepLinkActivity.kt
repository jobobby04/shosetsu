package app.shosetsu.android.ui.deeplink

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import app.shosetsu.android.activity.MainActivity

class DeepLinkActivity : Activity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		intent.apply {
			flags = flags or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
			setClass(applicationContext, MainActivity::class.java)
		}
		startActivity(intent)
		finish()
	}
}
