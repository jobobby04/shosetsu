package app.shosetsu.android.viewmodel.abstracted

import app.shosetsu.android.common.enums.NavigationStyle
import app.shosetsu.android.domain.repository.base.IBackupRepository.BackupProgress
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
	 * Whether a backup is currently ongoing.
	 *
	 * Will always be [BackupProgress.NOT_STARTED] if [app.shosetsu.android.common.SettingKey.BackupIndicator] is false.
	 */
	abstract val backupProgressState: StateFlow<BackupProgress>
}
