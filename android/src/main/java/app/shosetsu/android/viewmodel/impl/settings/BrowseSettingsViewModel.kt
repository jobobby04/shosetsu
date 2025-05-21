package app.shosetsu.android.viewmodel.impl.settings

import app.shosetsu.android.domain.repository.base.ISettingsRepository
import app.shosetsu.android.domain.usecases.load.LoadRepositoriesUseCase
import app.shosetsu.android.viewmodel.abstracted.settings.ABrowseSettingsViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class BrowseSettingsViewModel(
	iSettingsRepository: ISettingsRepository,
	loadRepositoriesUseCase: LoadRepositoriesUseCase
) : ABrowseSettingsViewModel(iSettingsRepository) {
	override val repoCount: StateFlow<Int> by lazy {
		loadRepositoriesUseCase()
			.map { it.size }
			.stateIn(viewModelScopeIO, SharingStarted.Lazily, 0)
	}
}