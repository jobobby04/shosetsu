package app.shosetsu.android.viewmodel.impl

import app.shosetsu.android.common.enums.NavigationStyle
import app.shosetsu.android.domain.repository.base.IBackupRepository
import app.shosetsu.android.domain.usecases.settings.LoadNavigationStyleUseCase
import app.shosetsu.android.domain.usecases.settings.LoadRequireDoubleBackUseCase
import app.shosetsu.android.viewmodel.abstracted.AHomeViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
	loadNavigationStyleUseCase: LoadNavigationStyleUseCase,
	private val loadRequireDoubleBackUseCase: LoadRequireDoubleBackUseCase,
	backupRepo: IBackupRepository,
) : AHomeViewModel() {

	override val requireDoubleBackToExit: StateFlow<Boolean> by lazy {
		loadRequireDoubleBackUseCase()
	}

	override val navigationStyle: StateFlow<NavigationStyle> =
		loadNavigationStyleUseCase().map {
			if (it) {
				NavigationStyle.LEGACY
			} else {
				NavigationStyle.MATERIAL
			}
		}
			.stateIn(viewModelScopeIO, SharingStarted.Eagerly, NavigationStyle.MATERIAL)

	override val backupProgressState: StateFlow<IBackupRepository.BackupProgress> =
		backupRepo.backupProgress
}