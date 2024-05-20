package app.shosetsu.android.di

import app.shosetsu.android.viewmodel.abstracted.AAboutViewModel
import app.shosetsu.android.viewmodel.abstracted.AAddShareViewModel
import app.shosetsu.android.viewmodel.abstracted.ABrowseViewModel
import app.shosetsu.android.viewmodel.abstracted.ACSSEditorViewModel
import app.shosetsu.android.viewmodel.abstracted.ACatalogViewModel
import app.shosetsu.android.viewmodel.abstracted.ACategoriesViewModel
import app.shosetsu.android.viewmodel.abstracted.AChapterReaderViewModel
import app.shosetsu.android.viewmodel.abstracted.ADownloadsViewModel
import app.shosetsu.android.viewmodel.abstracted.AExtensionConfigureViewModel
import app.shosetsu.android.viewmodel.abstracted.AIntroViewModel
import app.shosetsu.android.viewmodel.abstracted.ALibraryViewModel
import app.shosetsu.android.viewmodel.abstracted.AMainViewModel
import app.shosetsu.android.viewmodel.abstracted.AMigrationViewModel
import app.shosetsu.android.viewmodel.abstracted.ANovelViewModel
import app.shosetsu.android.viewmodel.abstracted.ARepositoryViewModel
import app.shosetsu.android.viewmodel.abstracted.ASearchViewModel
import app.shosetsu.android.viewmodel.abstracted.ATextAssetReaderViewModel
import app.shosetsu.android.viewmodel.abstracted.AUpdatesViewModel
import app.shosetsu.android.viewmodel.abstracted.AnalyticsViewModel
import app.shosetsu.android.viewmodel.abstracted.HistoryViewModel
import app.shosetsu.android.viewmodel.abstracted.WebViewViewModel
import app.shosetsu.android.viewmodel.abstracted.settings.AAdvancedSettingsViewModel
import app.shosetsu.android.viewmodel.abstracted.settings.ABackupSettingsViewModel
import app.shosetsu.android.viewmodel.abstracted.settings.ADownloadSettingsViewModel
import app.shosetsu.android.viewmodel.abstracted.settings.AReaderSettingsViewModel
import app.shosetsu.android.viewmodel.abstracted.settings.AUpdateSettingsViewModel
import app.shosetsu.android.viewmodel.abstracted.settings.AViewSettingsViewModel
import app.shosetsu.android.viewmodel.impl.AboutViewModel
import app.shosetsu.android.viewmodel.impl.AddShareViewModel
import app.shosetsu.android.viewmodel.impl.AnalyticsViewModelImpl
import app.shosetsu.android.viewmodel.impl.CSSEditorViewModel
import app.shosetsu.android.viewmodel.impl.CatalogViewModel
import app.shosetsu.android.viewmodel.impl.CategoriesViewModel
import app.shosetsu.android.viewmodel.impl.ChapterReaderViewModel
import app.shosetsu.android.viewmodel.impl.DownloadsViewModel
import app.shosetsu.android.viewmodel.impl.HistoryViewModelImpl
import app.shosetsu.android.viewmodel.impl.IntroViewModel
import app.shosetsu.android.viewmodel.impl.LibraryViewModel
import app.shosetsu.android.viewmodel.impl.MainViewModel
import app.shosetsu.android.viewmodel.impl.MigrationViewModel
import app.shosetsu.android.viewmodel.impl.NovelViewModel
import app.shosetsu.android.viewmodel.impl.RepositoryViewModel
import app.shosetsu.android.viewmodel.impl.SearchViewModel
import app.shosetsu.android.viewmodel.impl.TextAssetReaderViewModel
import app.shosetsu.android.viewmodel.impl.UpdatesViewModel
import app.shosetsu.android.viewmodel.impl.extension.ExtensionConfigureViewModel
import app.shosetsu.android.viewmodel.impl.extension.ExtensionsViewModel
import app.shosetsu.android.viewmodel.impl.extension.WebViewViewModelImpl
import app.shosetsu.android.viewmodel.impl.settings.AdvancedSettingsViewModel
import app.shosetsu.android.viewmodel.impl.settings.BackupSettingsViewModel
import app.shosetsu.android.viewmodel.impl.settings.DownloadSettingsViewModel
import app.shosetsu.android.viewmodel.impl.settings.ReaderSettingsViewModel
import app.shosetsu.android.viewmodel.impl.settings.UpdateSettingsViewModel
import app.shosetsu.android.viewmodel.impl.settings.ViewSettingsViewModel
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.provider

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
 * shosetsu
 * 01 / 05 / 2020
 */
val viewModelsModule: DI.Module = DI.Module("view_models_module") {
	// Main
	bind<AMainViewModel>() with provider {
		MainViewModel(
			isOnlineUseCase = instance(),
			loadNavigationStyleUseCase = instance(),
			loadLiveAppThemeUseCase = instance(),
			startInstallWorker = instance(),
			loadRequireDoubleBackUseCase = instance(),
			settingsRepository = instance(),
			appUpdateRepo = instance(),
			backupRepo = instance()
		)
	}

	// Library
	bind<ALibraryViewModel>() with provider {
		LibraryViewModel(
			loadLibrary = instance(),
			updateBookmarkedNovelUseCase = instance(),
			isOnlineUseCase = instance(),
			startUpdateWorkerUseCase = instance(),
			loadNovelUITypeUseCase = instance(),
			setNovelUITypeUseCase = instance(),
			setNovelsCategoriesUseCase = instance(),
			loadNovelUIColumnsH = instance(),
			loadNovelUIColumnsP = instance(),
			loadNovelUIBadgeToast = instance(),
			toggleNovelPin = instance(),
			loadLibraryFilterSettings = instance(),
			_updateLibraryFilterState = instance()
		)
	}

	// Other
	bind<ADownloadsViewModel>() with provider {
		DownloadsViewModel(
			getDownloadsUseCase = instance(),
			startDownloadWorkerUseCase = instance(),
			settings = instance(),
			isOnlineUseCase = instance(),
			downloadsRepository = instance()
		)
	}
	bind<ASearchViewModel>() with provider {
		SearchViewModel(
			searchBookMarkedNovelsUseCase = instance(),
			loadSearchRowUIUseCase = instance(),
			loadCatalogueListingDataUseCase = instance(),
			getExtensionUseCase = instance(),
			loadNovelUITypeUseCase = instance()
		)
	}
	bind<AUpdatesViewModel>() with provider {
		UpdatesViewModel(
			startUpdateWorkerUseCase = instance(),
			isOnlineUseCase = instance(),
			updatesRepository = instance(),
			settingsRepository = instance()
		)
	}

	bind<AAboutViewModel>() with provider {
		AboutViewModel(
			manager = instance()
		)
	}

	bind<AAddShareViewModel>() with provider {
		AddShareViewModel(
			instance(),
			instance(),
			instance(),
			instance(),
			instance(),
			instance(),
			instance(),
			instance(),
			instance(),
			instance()
		)
	}

	// Catalog(s)
	bind<ACatalogViewModel>() with provider {
		CatalogViewModel(
			getExtensionUseCase = instance(),
			backgroundAddUseCase = instance(),
			getCatalogueListingData = instance(),

			loadNovelUITypeUseCase = instance(),
			loadNovelUIColumnsHUseCase = instance(),
			loadNovelUIColumnsPUseCase = instance(),
			setNovelUIType = instance(),
			getCategoriesUseCase = instance(),
			setNovelCategoriesUseCase = instance()
		)
	}

	// Catalog(s)
	bind<ACategoriesViewModel>() with provider {
		CategoriesViewModel(
			instance(),
			instance(),
			instance(),
			instance()
		)
	}

	// Extensions
	bind<ABrowseViewModel>() with provider {
		ExtensionsViewModel(
			instance(),
			instance(),
			instance(),
			instance(),
			instance(),
			instance(),
		)
	}
	bind<AExtensionConfigureViewModel>() with provider {
		ExtensionConfigureViewModel(
			instance(),
			instance(),
			instance(),
			instance(),
		)
	}

	// Novel View
	bind<ANovelViewModel>() with provider {
		NovelViewModel(
			getChapterUIsUseCase = instance(),
			loadNovelUIUseCase = instance(),

			updateNovelUseCase = instance(),
			loadRemoteNovel = instance(),
			isOnlineUseCase = instance(),
			downloadChapterPassageUseCase = instance(),
			deleteChapterPassageUseCase = instance(),
			isChaptersResumeFirstUnread = instance(),
			getNovelSettingFlowUseCase = instance(),
			updateNovelSettingUseCase = instance(),
			startDownloadWorkerUseCase = instance(),
			startDownloadWorkerAfterUpdateUseCase = instance(),
			getContentURL = instance(),
			settingsRepo = instance(),
			trueDeleteChapter = instance(),
			getInstalledExtensionUseCase = instance(),
			getRepositoryUseCase = instance(),
			chapterRepo = instance(),
			getCategoriesUseCase = instance(),
			getNovelCategoriesUseCase = instance(),
			setNovelCategoriesUseCase = instance()
		)
	}

	// Chapter
	bind<AChapterReaderViewModel>() with provider {
		ChapterReaderViewModel(
			settingsRepo = instance(),
			instance(),
			instance(),
			instance(),
			instance(),
			loadReaderChaptersUseCase = instance(),
			loadChapterPassageUseCase = instance(),

			getReaderSettingsUseCase = instance(),
			instance(),
			instance(),
			instance(),
			instance(),
			instance(),
			instance(),
			instance()
		)
	}
	bind<ARepositoryViewModel>() with provider {
		RepositoryViewModel(
			loadRepositoriesUseCase = instance(),

			addRepositoryUseCase = instance(),
			deleteRepositoryUseCase = instance(),
			updateRepositoryUseCase = instance(),
			startRepositoryUpdateManagerUseCase = instance(),
			forceInsertRepositoryUseCase = instance(),
			isOnlineUseCase = instance()
		)
	}


	// Settings
	bind<AAdvancedSettingsViewModel>() with provider {
		AdvancedSettingsViewModel(
			iSettingsRepository = instance(),
			purgeNovelCacheUseCase = instance(),
			instance(),
			instance(),
			instance(),
			instance()
		)
	}
	bind<ABackupSettingsViewModel>() with provider {
		BackupSettingsViewModel(
			iSettingsRepository = instance(),

			manager = instance(),
			startBackupWorkerUseCase = instance(),
			loadInternalBackupNamesUseCase = instance(),
			instance(),
			instance()
		)
	}
	bind<ADownloadSettingsViewModel>() with provider {
		DownloadSettingsViewModel(
			iSettingsRepository = instance(),
			instance()
		)
	}
	bind<AReaderSettingsViewModel>() with provider {
		ReaderSettingsViewModel(
			iSettingsRepository = instance(),
			app = instance(),

			loadReaderThemes = instance()
		)
	}
	bind<AUpdateSettingsViewModel>() with provider {
		UpdateSettingsViewModel(
			iSettingsRepository = instance(),
			instance(),
			instance(),
			instance(),
			instance()
		)
	}
	bind<AViewSettingsViewModel>() with provider {
		ViewSettingsViewModel(
			iSettingsRepository = instance(),
		)
	}
	bind<ATextAssetReaderViewModel>() with provider {
		TextAssetReaderViewModel(instance())
	}

	bind<AMigrationViewModel>() with provider {
		MigrationViewModel(instance(), instance(), instance())
	}

	bind<ACSSEditorViewModel>() with provider {
		CSSEditorViewModel(instance(), instance())
	}

	bind<AIntroViewModel>() with provider {
		IntroViewModel(instance())
	}

	bind<HistoryViewModel>() with provider {
		HistoryViewModelImpl(instance(), instance(), instance())
	}
	bind<AnalyticsViewModel>() with provider {
		AnalyticsViewModelImpl(
			instance(),
			instance()
		)
	}
	bind<WebViewViewModel>() with provider {
		WebViewViewModelImpl(instance())
	}
}