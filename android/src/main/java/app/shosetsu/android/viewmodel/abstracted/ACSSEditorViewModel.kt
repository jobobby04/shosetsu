package app.shosetsu.android.viewmodel.abstracted

import app.shosetsu.android.viewmodel.base.ShosetsuViewModel
import kotlinx.coroutines.flow.StateFlow

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
abstract class ACSSEditorViewModel : ShosetsuViewModel() {
	abstract fun undo()
	abstract fun redo()

	/**
	 * Tell view model to save the CSS
	 */
	abstract fun saveCSS()

	abstract fun write(content: String)

	abstract fun appendText(pasteContent: String)
	abstract fun setCSSId(int: Int)

	abstract val cssContent: StateFlow<String>
	abstract val cssTitle: StateFlow<String>

	abstract val isCSSValid: StateFlow<Boolean>
	abstract val cssInvalidReason: StateFlow<String?>

	abstract val canUndo: StateFlow<Boolean>
	abstract val canRedo: StateFlow<Boolean>

}