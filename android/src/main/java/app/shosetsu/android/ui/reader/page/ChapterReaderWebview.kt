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
 *
 */

package app.shosetsu.android.ui.reader.page

import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.util.AttributeSet
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebView
import app.shosetsu.android.R

class ChapterReaderWebview(
	context: Context,
	attrs: AttributeSet? = null,
) : WebView(context, attrs) {

	var searchInBrowser: ((String) -> Unit)? = null

	override fun startActionMode(callback: ActionMode.Callback?): ActionMode {
		return super.startActionMode(wrapCallback(callback))
	}

	override fun startActionMode(callback: ActionMode.Callback?, type: Int): ActionMode {
		return super.startActionMode(wrapCallback(callback), type)
	}

	private fun wrapCallback(callback: ActionMode.Callback?): ActionMode.Callback {
		val actionMode = object : ActionMode.Callback {

			override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
				callback?.onCreateActionMode(mode, menu)

				// Add custom "Search" item
				menu?.add(0, 1001, 0, R.string.search)
				return true
			}

			override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
				return callback?.onPrepareActionMode(mode, menu) ?: false
			}

			override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
				if (item?.itemId == 1001) {
					getSelectedText { selectedText ->
						selectedText?.let {
							searchInBrowser(it)
						}
					}
					mode?.finish()
					return true
				}
				return callback?.onActionItemClicked(mode, item) ?: false
			}

			override fun onDestroyActionMode(mode: ActionMode?) {
				callback?.onDestroyActionMode(mode)
			}
		}

		return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && callback is ActionMode.Callback2) {
			object : ActionMode.Callback2() {
				override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean =
					actionMode.onCreateActionMode(mode, menu)

				override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean =
					actionMode.onPrepareActionMode(mode, menu)

				override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean =
					actionMode.onActionItemClicked(mode, item)

				override fun onDestroyActionMode(mode: ActionMode?): Unit = actionMode.onDestroyActionMode(mode)
				override fun onGetContentRect(mode: ActionMode?, view: View?, outRect: Rect?) {
					callback.onGetContentRect(mode, view, outRect)
				}
			}
		} else {
			actionMode
		}
	}

	private fun getSelectedText(callback: (String?) -> Unit) {
		evaluateJavascript(
			"(function(){return window.getSelection().toString();})()"
		) { value ->
			callback(value?.removeSurrounding("\""))
		}
	}

	private fun searchInBrowser(query: String) {
		this.searchInBrowser?.invoke(query)
	}
}
