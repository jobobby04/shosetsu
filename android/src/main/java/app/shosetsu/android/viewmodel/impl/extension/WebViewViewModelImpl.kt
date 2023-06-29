package app.shosetsu.android.viewmodel.impl.extension

import app.shosetsu.android.domain.usecases.get.GetUserAgentUseCase
import app.shosetsu.android.viewmodel.abstracted.WebViewViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

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
 * @since 29 / 06 / 2023
 * @author Doomsdayrs
 */
class WebViewViewModelImpl(
	getUserAgent: GetUserAgentUseCase
) : WebViewViewModel() {
	override val userAgent: StateFlow<String> =
		getUserAgent.flow().stateIn(viewModelScopeIO, SharingStarted.Eagerly, "")
}