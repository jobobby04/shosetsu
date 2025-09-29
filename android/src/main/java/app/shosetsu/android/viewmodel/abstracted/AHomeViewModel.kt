package app.shosetsu.android.viewmodel.abstracted

import app.shosetsu.android.common.enums.NavigationStyle
import app.shosetsu.android.domain.repository.base.IBackupRepository
import app.shosetsu.android.viewmodel.base.ShosetsuViewModel
import kotlinx.coroutines.flow.StateFlow

abstract class AHomeViewModel : ShosetsuViewModel() {

	/**
	 * If 0, Bottom
	 * If 1, Drawer
	 */
	abstract val navigationStyle: StateFlow<NavigationStyle>

	/**
	 * The app needs two presses to exit
	 */
	abstract val requireDoubleBackToExit: StateFlow<Boolean>

	/**
	 * Whether a backup is currently ongoing
	 */
	abstract val backupProgressState: StateFlow<IBackupRepository.BackupProgress>
}