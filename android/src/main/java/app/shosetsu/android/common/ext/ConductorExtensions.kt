package app.shosetsu.android.common.ext

import android.provider.Settings
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import app.shosetsu.android.R
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar


@Deprecated("Compose")
fun Fragment.makeSnackBar(
	@StringRes stringRes: Int,
	@BaseTransientBottomBar.Duration length: Int = Snackbar.LENGTH_SHORT,
): Snackbar? = null

@Deprecated("Compose")
fun Fragment.displayOfflineSnackBar(@StringRes res: Int = R.string.you_not_online) {
	makeSnackBar(
		res,
		Snackbar.LENGTH_LONG
	)?.setAction(R.string.generic_wifi_settings) {
		startActivity(android.content.Intent(Settings.ACTION_WIFI_SETTINGS))
	}?.show()
}

@Deprecated("Compose")
fun Fragment.makeSnackBar(
	string: String,
	@BaseTransientBottomBar.Duration length: Int = Snackbar.LENGTH_SHORT,
): Snackbar? = null