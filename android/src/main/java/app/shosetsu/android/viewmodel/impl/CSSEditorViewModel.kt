package app.shosetsu.android.viewmodel.impl

import android.app.Application
import app.shosetsu.android.R
import app.shosetsu.android.common.SettingKey
import app.shosetsu.android.common.ext.launchIO
import app.shosetsu.android.common.ext.logI
import app.shosetsu.android.domain.model.local.StyleEntity
import app.shosetsu.android.domain.repository.base.ISettingsRepository
import app.shosetsu.android.viewmodel.abstracted.ACSSEditorViewModel
import kotlinx.coroutines.flow.*
import java.util.*

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
 * Shosetsu
 *
 * @since 22 / 10 / 2021
 * @author Doomsdayrs
 */
class CSSEditorViewModel(
	private val app: Application,
	private val settingsRepo: ISettingsRepository
) : ACSSEditorViewModel() {
	private val undoStack by lazy { Stack<String>() }
	private val redoStack by lazy { Stack<String>() }
	private val cssIDFlow = MutableStateFlow(-2)


	override val cssContent = MutableStateFlow("")
	override val cssTitle: StateFlow<String> by lazy {
		styleFlow.map { it.title }.onIO().stateIn(
			viewModelScopeIO,
			SharingStarted.Lazily,
			app.resources.getString(R.string.loading)
		)
	}
	override val isCSSValid: MutableStateFlow<Boolean> = MutableStateFlow(true)
	override val cssInvalidReason: MutableStateFlow<String?> = MutableStateFlow(null)
	override val canRedo: MutableStateFlow<Boolean> = MutableStateFlow(false)
	override val canUndo: MutableStateFlow<Boolean> = MutableStateFlow(false)

	private val styleFlow by lazy {
		cssIDFlow.map { id ->
			StyleEntity(
				id,
				app.resources.getString(R.string.default_reader)
			)
		}
	}

	override fun undo() {
		logI("Undo")
		redoStack.add(cssContent.value) // Save currentText as a redo action
		canRedo.value = true
		cssContent.value = undoStack.pop()
		if (undoStack.size == 0) {
			canUndo.value = false
		}
	}

	override fun redo() {
		logI("Redo")
		undoStack.add(cssContent.value)
		canUndo.value = true
		cssContent.value = redoStack.pop()
		if (redoStack.size == 0) {
			canRedo.value = false
		}
	}

	override fun write(content: String) {
		launchIO {
			if (undoStack.size > 0 && undoStack.peek() == content) return@launchIO // ignore if nothing changed
			undoStack.add(cssContent.value)
			canUndo.value = true
			redoStack.clear()
			canRedo.value = false
		}
		cssContent.value = content
	}

	override fun saveCSS() {
		launchIO {
			settingsRepo.setString(SettingKey.ReaderHtmlCss, cssContent.value)
		}
	}

	override fun appendText(pasteContent: String) {
		val value = cssContent.value
		val combined = value + pasteContent
		if (value == combined) return // ignore paste if the old value equals paste
		launchIO {
			if (undoStack.size > 0 && undoStack.peek() == combined) return@launchIO // ignore if nothing changed
			undoStack.add(value)
			canUndo.value = true
			redoStack.clear()
			canRedo.value = false
		}
		cssContent.value = combined
	}

	override fun setCSSId(int: Int) {
		launchIO {
			if (int != cssIDFlow.value) {
				undoStack.clear()
				redoStack.clear()
				cssContent.value = ""
				cssIDFlow.value = int
			}
		}
	}

	init {
		launchIO {
			styleFlow.collect { result ->
				result.let {
					settingsRepo.getString(SettingKey.ReaderHtmlCss).let {
						cssContent.value = it
					}
				}
			}
		}
	}
}