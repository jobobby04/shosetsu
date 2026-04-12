package app.shosetsu.android.viewmodel.base

import app.shosetsu.android.common.enums.AppThemes
import app.shosetsu.android.domain.usecases.load.LoadLiveAppThemeUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

abstract class ShosetsuRootViewModel : ShosetsuViewModel(), ThemedViewModel {
	protected abstract val loadLiveAppThemeUseCase: LoadLiveAppThemeUseCase

	/**
	 * Theme to use
	 */
	override val appTheme: StateFlow<AppThemes> by lazy {
		loadLiveAppThemeUseCase()
			.stateIn(viewModelScopeIO, SharingStarted.Lazily, AppThemes.FOLLOW_SYSTEM)
	}
}
