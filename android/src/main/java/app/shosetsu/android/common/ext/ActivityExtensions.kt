package app.shosetsu.android.common.ext

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.bundleOf
import app.shosetsu.android.common.consts.BundleKeys
import app.shosetsu.android.common.consts.BundleKeys.BUNDLE_CHAPTER_ID
import app.shosetsu.android.common.consts.BundleKeys.BUNDLE_NOVEL_ID
import app.shosetsu.android.common.enums.AppThemes
import app.shosetsu.android.ui.reader.ChapterReader
import app.shosetsu.android.ui.webView.WebViewApp
import app.shosetsu.android.view.uimodels.model.ChapterUI
import java.io.IOException

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
 * 06 / 05 / 2020
 */

fun Context.openInBrowser(url: Uri, pkg: String? = null) {
	try {
		startActivity(
			Intent(Intent.ACTION_VIEW, url).apply {
				setPackage(pkg)
			}
		)
	} catch (e: ActivityNotFoundException) {
		// if pkg is not found, retry with pkg null
		openInBrowser(url)
	}
}

fun Context.openInBrowser(url: String, pkg: String? = null): Unit =
	openInBrowser(Uri.parse(url), pkg)

fun Context.openInWebView(url: String) {
	logI("Opening in web view: $url")
	startActivity(intent(this, WebViewApp::class.java) {
		bundleOf(
			BundleKeys.BUNDLE_URL to url,
		)
	})
}

fun Activity.openShare(url: String, title: String) {
	logI("Sharing URL ($title):($url)")
	application.startActivity(
		Intent.createChooser(Intent().apply {
			action = Intent.ACTION_SEND
			putExtra(Intent.EXTRA_TEXT, url)
			putExtra(Intent.EXTRA_TITLE, title)
			type = "text/plain"
		}, null).apply {
			addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
		}
	)
}


/**
 * shosetsu
 * 04 / 03 / 2020
 *
 * @author github.com/doomsdayrs
 * Pre resquite requires chapter to already have been added to library
 *
 * @param cUI novel chapter
 */

fun Activity.openChapter(cUI: ChapterUI): Unit = openChapter(cUI.id, cUI.novelID)

fun Activity.openChapter(chapterID: Int, novelID: Int) {
	startActivity(intent(this, ChapterReader::class.java) {
		bundleOf(
			BUNDLE_CHAPTER_ID to chapterID,
			BUNDLE_NOVEL_ID to novelID
		)
	})
}

fun Context.readAsset(name: String): String {
	val string = StringBuilder()
	try {
		assets.open(name).reader().buffered().use { reader ->
			// do reading, usually loop until end of file reading
			var mLine: String? = reader.readLine()
			while (mLine != null) {
				string.append("\n").append(mLine)
				mLine = reader.readLine()
			}
		}
	} catch (e: IOException) {
		Log.e(javaClass.name, "Failed to read asset of $name", e)
	}
	return string.toString()
}

fun AppCompatActivity.setTheme(theme: AppThemes) {
	logI("Setting theme to $theme")
	when (theme) {
		AppThemes.FOLLOW_SYSTEM -> {
			delegate.localNightMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
		}
		AppThemes.LIGHT -> {
			delegate.localNightMode = AppCompatDelegate.MODE_NIGHT_NO
		}
		AppThemes.DARK -> {
			delegate.localNightMode = AppCompatDelegate.MODE_NIGHT_YES
		}
		AppThemes.AMOLED -> {
			// TODO Implement amoled mode
			delegate.localNightMode = AppCompatDelegate.MODE_NIGHT_YES
		}
	}
}