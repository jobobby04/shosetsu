package app.shosetsu.android.viewmodel.abstracted

import app.shosetsu.android.common.enums.AppThemes
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
 * View model for History view
 *
 * @since 12 / 02 / 2023
 * @author Doomsdayrs
 */
abstract class WebViewViewModel : ShosetsuViewModel() {
	abstract val userAgent: StateFlow<String>
	abstract val appTheme: StateFlow<AppThemes>
}