package app.shosetsu.android.viewmodel.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.plus

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
 */

/**
 * shosetsu
 * 30 / 10 / 2020
 */
abstract class ShosetsuViewModel : ViewModel() {
	fun <T> Flow<T>.asIOLiveData(): LiveData<T> =
		asLiveData(viewModelScope.coroutineContext + Dispatchers.IO)

	/**
	 * TODO Figure out why this wont add with coroutine context of viewmodel
	 */
	fun <T> Flow<T>.onIO(): Flow<T> =
		flowOn(Dispatchers.IO)

	val viewModelScopeIO
		get() = viewModelScope + Dispatchers.IO
}