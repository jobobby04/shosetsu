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
import app.shosetsu.android.viewmodel.abstracted.AHomeViewModel
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
import app.shosetsu.android.viewmodel.abstracted.settings.AAppearanceSettingsViewModel
import app.shosetsu.android.viewmodel.abstracted.settings.ABackupSettingsViewModel
import app.shosetsu.android.viewmodel.abstracted.settings.ABrowseSettingsViewModel
import app.shosetsu.android.viewmodel.abstracted.settings.ADownloadSettingsViewModel
import app.shosetsu.android.viewmodel.abstracted.settings.ALibrarySettingsViewModel
import app.shosetsu.android.viewmodel.abstracted.settings.AReaderSettingsViewModel
import app.shosetsu.android.viewmodel.impl.AboutViewModel
import app.shosetsu.android.viewmodel.impl.AddShareViewModel
import app.shosetsu.android.viewmodel.impl.AnalyticsViewModelImpl
import app.shosetsu.android.viewmodel.impl.CSSEditorViewModel
import app.shosetsu.android.viewmodel.impl.CatalogViewModel
import app.shosetsu.android.viewmodel.impl.CategoriesViewModel
import app.shosetsu.android.viewmodel.impl.ChapterReaderViewModel
import app.shosetsu.android.viewmodel.impl.DownloadsViewModel
import app.shosetsu.android.viewmodel.impl.HistoryViewModelImpl
import app.shosetsu.android.viewmodel.impl.HomeViewModel
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
import app.shosetsu.android.viewmodel.impl.settings.AppearanceSettingsViewModel
import app.shosetsu.android.viewmodel.impl.settings.BackupSettingsViewModel
import app.shosetsu.android.viewmodel.impl.settings.BrowseSettingsViewModel
import app.shosetsu.android.viewmodel.impl.settings.DownloadSettingsViewModel
import app.shosetsu.android.viewmodel.impl.settings.LibrarySettingsViewModel
import app.shosetsu.android.viewmodel.impl.settings.ReaderSettingsViewModel
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.new
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
	bind<AMainViewModel>() with provider { new(::MainViewModel) }

	// Home
	bind<AHomeViewModel>() with provider { new(::HomeViewModel) }

	// Library
	bind<ALibraryViewModel>() with provider { new(::LibraryViewModel) }

	// Other
	bind<ADownloadsViewModel>() with provider { new(::DownloadsViewModel) }
	bind<ASearchViewModel>() with provider { new(::SearchViewModel) }
	bind<AUpdatesViewModel>() with provider { new(::UpdatesViewModel) }
	bind<AAboutViewModel>() with provider { new(::AboutViewModel) }
	bind<AAddShareViewModel>() with provider { new(::AddShareViewModel) }

	// Catalog(s)
	bind<ACatalogViewModel>() with provider { new(::CatalogViewModel) }

	// Catalog(s)
	bind<ACategoriesViewModel>() with provider { new(::CategoriesViewModel) }

	// Extensions
	bind<ABrowseViewModel>() with provider { new(::ExtensionsViewModel) }
	bind<AExtensionConfigureViewModel>() with provider { new(::ExtensionConfigureViewModel) }

	// Novel View
	bind<ANovelViewModel>() with provider { new(::NovelViewModel) }

	// Chapter
	bind<AChapterReaderViewModel>() with provider { new(::ChapterReaderViewModel) }
	bind<ARepositoryViewModel>() with provider { new(::RepositoryViewModel) }

	// Settings
	bind<AAdvancedSettingsViewModel>() with provider { new(::AdvancedSettingsViewModel) }
	bind<ABackupSettingsViewModel>() with provider { new(::BackupSettingsViewModel) }
	bind<ADownloadSettingsViewModel>() with provider { new(::DownloadSettingsViewModel) }
	bind<AReaderSettingsViewModel>() with provider { new(::ReaderSettingsViewModel) }
	bind<ALibrarySettingsViewModel>() with provider { new(::LibrarySettingsViewModel) }
	bind<AAppearanceSettingsViewModel>() with provider { new(::AppearanceSettingsViewModel) }
	bind<ABrowseSettingsViewModel>() with provider { new(::BrowseSettingsViewModel) }
	bind<ATextAssetReaderViewModel>() with provider { new(::TextAssetReaderViewModel) }

	// Other
	bind<AMigrationViewModel>() with provider { new(::MigrationViewModel) }
	bind<ACSSEditorViewModel>() with provider { new(::CSSEditorViewModel) }
	bind<AIntroViewModel>() with provider { new(::IntroViewModel) }
	bind<HistoryViewModel>() with provider { new(::HistoryViewModelImpl) }
	bind<AnalyticsViewModel>() with provider { new(::AnalyticsViewModelImpl) }
	bind<WebViewViewModel>() with provider { new(::WebViewViewModelImpl) }
}
