package app.shosetsu.android.viewmodel.base

import app.shosetsu.android.common.enums.AppThemes
import kotlinx.coroutines.flow.StateFlow

interface ThemedViewModel {
	/**
	 * Theme to use
	 */
	val appTheme: StateFlow<AppThemes>
}