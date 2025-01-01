package app.shosetsu.android.viewmodel.abstracted.settings

import app.shosetsu.android.domain.repository.base.ISettingsRepository

abstract class ABrowseSettingsViewModel(iSettingsRepository: ISettingsRepository) :
    ASubSettingsViewModel(iSettingsRepository)