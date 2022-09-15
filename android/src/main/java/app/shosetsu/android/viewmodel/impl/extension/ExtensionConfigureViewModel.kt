package app.shosetsu.android.viewmodel.impl.extension

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

import app.shosetsu.android.common.ext.launchIO
import app.shosetsu.android.common.ext.logI
import app.shosetsu.android.common.ext.logV
import app.shosetsu.android.domain.model.local.FilterEntity
import app.shosetsu.android.domain.usecases.UninstallExtensionUseCase
import app.shosetsu.android.domain.usecases.get.GetExtListingNamesUseCase
import app.shosetsu.android.domain.usecases.get.GetExtSelectedListingFlowUseCase
import app.shosetsu.android.domain.usecases.get.GetExtensionSettingsUseCase
import app.shosetsu.android.domain.usecases.get.GetInstalledExtensionUseCase
import app.shosetsu.android.domain.usecases.update.UpdateExtSelectedListing
import app.shosetsu.android.domain.usecases.update.UpdateExtensionSettingUseCase
import app.shosetsu.android.view.uimodels.model.InstalledExtensionUI
import app.shosetsu.android.viewmodel.abstracted.AExtensionConfigureViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

/**
 * shosetsu
 * 29 / 04 / 2020
 *
 * @author github.com/doomsdayrs
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ExtensionConfigureViewModel(
	private val loadInstalledExtension: GetInstalledExtensionUseCase,
	private val uninstallExtensionUI: UninstallExtensionUseCase,
	private val getExtensionSettings: GetExtensionSettingsUseCase,
	private val getExtListNames: GetExtListingNamesUseCase,
	private val updateExtSelectedListing: UpdateExtSelectedListing,
	private val getExtSelectedListingFlow: GetExtSelectedListingFlowUseCase,
	private val updateSetting: UpdateExtensionSettingUseCase,
) : AExtensionConfigureViewModel() {
	private val extensionIdFlow: MutableStateFlow<Int> by lazy { MutableStateFlow(-1) }

	override val liveData: StateFlow<InstalledExtensionUI?> by lazy {
		extensionIdFlow.flatMapLatest { id ->
			loadInstalledExtension(id)
		}.onIO().stateIn(viewModelScopeIO, SharingStarted.Lazily, null)
	}

	private val extListNamesFlow: Flow<ListingSelectionData> by lazy {
		extensionIdFlow.flatMapLatest { extensionID ->
			val listingNames = getExtListNames(extensionID).toImmutableList()

			getExtSelectedListingFlow(extensionID).mapLatest { selectedListing ->
				ListingSelectionData(listingNames, selectedListing)
			}
		}
	}

	private val extensionSettingsFlow: Flow<ImmutableList<FilterEntity>> by lazy {
		extensionIdFlow.flatMapLatest { extensionID ->
			getExtensionSettings(extensionID).map { it.toImmutableList() }
		}
	}

	override val extensionSettings: StateFlow<ImmutableList<FilterEntity>> by lazy {
		extensionSettingsFlow.onIO()
			.stateIn(viewModelScopeIO, SharingStarted.Lazily, persistentListOf())
	}

	override val extensionListing: StateFlow<ListingSelectionData?> by lazy {
		extListNamesFlow.onIO()
			.stateIn(viewModelScopeIO, SharingStarted.Lazily, null)
	}

	override fun setExtensionID(id: Int) {
		logV("Setting extension id = $id")
		launchIO {
			when {
				extensionIdFlow.value == id -> {
					this@ExtensionConfigureViewModel.logI("id is the same, ignoring")
					return@launchIO
				}
				extensionIdFlow.value != id -> {
					this@ExtensionConfigureViewModel.logI("id is different, resetting")
					destroy()
				}
				extensionIdFlow.value == -1 -> {
					this@ExtensionConfigureViewModel.logI("id is new, setting")
				}
			}
			extensionIdFlow.value = id
		}
	}

	override fun uninstall(extension: InstalledExtensionUI) {
		launchIO {
			uninstallExtensionUI(extension)
		}
	}

	override fun destroy() {
		extensionIdFlow.value = -1
	}

	override fun saveSetting(id: Int, value: String) {
		launchIO {
			updateSetting(extensionIdFlow.value, id, value)
		}
	}

	override fun saveSetting(id: Int, value: Boolean) {
		launchIO {
			updateSetting(extensionIdFlow.value, id, value)
		}
	}

	override fun saveSetting(id: Int, value: Int) {
		launchIO {
			updateSetting(extensionIdFlow.value, id, value)
		}
	}

	override fun setSelectedListing(value: Int) {
		launchIO {
			updateExtSelectedListing(extensionIdFlow.value, value)
		}
	}
}

