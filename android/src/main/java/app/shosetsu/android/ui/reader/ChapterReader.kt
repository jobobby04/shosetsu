package app.shosetsu.android.ui.reader

import android.content.ComponentCallbacks2
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import app.shosetsu.android.common.consts.BundleKeys.BUNDLE_CHAPTER_ID
import app.shosetsu.android.common.consts.BundleKeys.BUNDLE_NOVEL_ID
import app.shosetsu.android.common.ext.collectLA
import app.shosetsu.android.common.ext.logV
import app.shosetsu.android.common.ext.setTheme
import app.shosetsu.android.common.ext.viewModel
import app.shosetsu.android.viewmodel.abstracted.AChapterReaderViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
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
 * 13 / 12 / 2019
 */
class ChapterReader
	: AppCompatActivity(), DIAware {
	override val di: DI by closestDI()
	internal val viewModel: AChapterReaderViewModel by viewModel()

	override fun onTrimMemory(level: Int) {
		super.onTrimMemory(level)
		// Determine which lifecycle or system event was raised.
		when (level) {
			ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> {
				/*
				   Release any UI objects that currently hold memory.

				   The user interface has moved to the background.
				*/
			}

			ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE,
			ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW,
			ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL -> {
				/*
				   Release any memory that your app doesn't need to run.

				   The device is running low on memory while the app is running.
				   The event raised indicates the severity of the memory-related event.
				   If the event is TRIM_MEMORY_RUNNING_CRITICAL, then the system will
				   begin killing background processes.
				*/
			}

			ComponentCallbacks2.TRIM_MEMORY_BACKGROUND,
			ComponentCallbacks2.TRIM_MEMORY_MODERATE,
			ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> {
				/*
				   Release as much memory as the process can.

				   The app is on the LRU list and the system is running low on memory.
				   The event raised indicates where the app sits within the LRU list.
				   If the event is TRIM_MEMORY_COMPLETE, the process will be one of
				   the first to be terminated.
				*/
			}

			else -> {
				/*
				  Release any non-critical data structures.

				  The app received an unrecognized memory level value
				  from the system. Treat this as a generic low-memory message.
				*/
				viewModel.clearMemory()
			}
		}

	}

	/** On Create */
	public override fun onCreate(savedInstanceState: Bundle?) {
		logV("")
		viewModel.apply {
			setNovelID(intent.getIntExtra(BUNDLE_NOVEL_ID, -1))
			viewModel.setCurrentChapterID(intent.getIntExtra(BUNDLE_CHAPTER_ID, -1), true)
		}
		runBlocking {
			setTheme(viewModel.appThemeLiveData.first())
		}
		viewModel.appThemeLiveData.collectLA(this, catch = {}) {
			setTheme(it)
		}
		super.onCreate(savedInstanceState)

		setContent {
			ChapterReaderView(
				viewModel,
				onExit = { finish() }
			)
		}

		viewModel.liveIsScreenRotationLocked.collectLA(this, catch = {}) {
			if (it)
				lockRotation()
			else unlockRotation()
		}

		viewModel.liveKeepScreenOn.collectLA(this, catch = {}) {
			if (it) {
				window.addFlags(FLAG_KEEP_SCREEN_ON)
			} else {
				window.clearFlags(FLAG_KEEP_SCREEN_ON)
			}
		}
	}

	/**
	 * Adds the
	 */
	override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
		return if (viewModel.isVolumeScrollEnabled.value)
			when (keyCode) {
				KeyEvent.KEYCODE_VOLUME_DOWN -> {
					viewModel.incrementProgress()
					true
				}

				KeyEvent.KEYCODE_VOLUME_UP -> {
					viewModel.depleteProgress()
					true
				}

				else -> super.onKeyDown(keyCode, event)
			}
		else super.onKeyDown(keyCode, event)
	}

	private fun lockRotation() {
		val currentOrientation = resources.configuration.orientation
		requestedOrientation = if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
			ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
		} else {
			ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
		}
	}

	private fun unlockRotation() {
		//window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
		requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_USER
	}
}