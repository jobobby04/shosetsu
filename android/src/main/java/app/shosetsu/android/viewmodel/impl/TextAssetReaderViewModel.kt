package app.shosetsu.android.viewmodel.impl

import android.app.Application
import app.shosetsu.android.common.enums.TextAsset
import app.shosetsu.android.common.ext.logI
import app.shosetsu.android.common.ext.readAsset
import app.shosetsu.android.viewmodel.abstracted.ATextAssetReaderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

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
 * @since 29 / 06 / 2021
 * @author Doomsdayrs
 */
class TextAssetReaderViewModel(val application: Application) : ATextAssetReaderViewModel() {

	override val targetLiveData = MutableStateFlow<TextAsset?>(null)

	@OptIn(ExperimentalCoroutinesApi::class)
	override val liveData: StateFlow<String?> by lazy {
		targetLiveData.mapLatest {
			if (it != null) {
				application.readAsset(it.assetName + ".txt")
			} else {
				null
			}
		}.onIO().stateIn(viewModelScopeIO, SharingStarted.Lazily, null)
	}

	override fun setTarget(targetOrdinal: Int) {
		logI("Opening up asset via ordinal $targetOrdinal")
		// If target is empty, emit
		if (targetLiveData.value != null) {
			// If the targets are the same, ignore and return
			if (targetLiveData.value!!.ordinal == targetOrdinal)
				return
		}
		targetLiveData.value = null
		targetLiveData.value = TextAsset.values()[targetOrdinal]
	}
}