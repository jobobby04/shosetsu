package app.shosetsu.android.viewmodel.abstracted.settings

import app.shosetsu.android.domain.repository.base.ISettingsRepository
import kotlinx.coroutines.flow.StateFlow

abstract class ABrowseSettingsViewModel(iSettingsRepository: ISettingsRepository) :
	ASubSettingsViewModel(iSettingsRepository) {
	abstract val repoCount: StateFlow<Int>
}