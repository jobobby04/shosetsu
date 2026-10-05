package app.shosetsu.android.di

import app.shosetsu.android.domain.usecases.AddCategoryUseCase
import app.shosetsu.android.domain.usecases.AddRepositoryUseCase
import app.shosetsu.android.domain.usecases.CancelExtensionInstallUseCase
import app.shosetsu.android.domain.usecases.DeleteCategoryUseCase
import app.shosetsu.android.domain.usecases.DownloadChapterPassageUseCase
import app.shosetsu.android.domain.usecases.ForceInsertRepositoryUseCase
import app.shosetsu.android.domain.usecases.InstallExtensionUseCase
import app.shosetsu.android.domain.usecases.IsOnlineUseCase
import app.shosetsu.android.domain.usecases.MoveCategoryUseCase
import app.shosetsu.android.domain.usecases.NovelBackgroundAddUseCase
import app.shosetsu.android.domain.usecases.PurgeNovelCacheUseCase
import app.shosetsu.android.domain.usecases.RecordChapterIsReadUseCase
import app.shosetsu.android.domain.usecases.RecordChapterIsReadingUseCase
import app.shosetsu.android.domain.usecases.RemoveExtensionEntityUseCase
import app.shosetsu.android.domain.usecases.RequestInstallExtensionUseCase
import app.shosetsu.android.domain.usecases.SearchBookMarkedNovelsUseCase
import app.shosetsu.android.domain.usecases.SetNovelCategoriesUseCase
import app.shosetsu.android.domain.usecases.SetNovelPinUseCase
import app.shosetsu.android.domain.usecases.SetNovelsCategoriesUseCase
import app.shosetsu.android.domain.usecases.StartDownloadWorkerAfterUpdateUseCase
import app.shosetsu.android.domain.usecases.StartRepositoryUpdateManagerUseCase
import app.shosetsu.android.domain.usecases.UninstallExtensionUseCase
import app.shosetsu.android.domain.usecases.delete.DeleteChapterPassageUseCase
import app.shosetsu.android.domain.usecases.delete.DeleteRepositoryUseCase
import app.shosetsu.android.domain.usecases.delete.TrueDeleteChapterUseCase
import app.shosetsu.android.domain.usecases.get.GetCatalogueListingDataUseCase
import app.shosetsu.android.domain.usecases.get.GetCatalogueQueryDataUseCase
import app.shosetsu.android.domain.usecases.get.GetCategoriesUseCase
import app.shosetsu.android.domain.usecases.get.GetChapterPassageUseCase
import app.shosetsu.android.domain.usecases.get.GetChapterUIsUseCase
import app.shosetsu.android.domain.usecases.get.GetExtensionSettingsUseCase
import app.shosetsu.android.domain.usecases.get.GetExtensionUseCase
import app.shosetsu.android.domain.usecases.get.GetInstalledExtensionUseCase
import app.shosetsu.android.domain.usecases.get.GetLastReadChapterUseCase
import app.shosetsu.android.domain.usecases.get.GetNovelCategoriesUseCase
import app.shosetsu.android.domain.usecases.get.GetNovelSettingFlowUseCase
import app.shosetsu.android.domain.usecases.get.GetNovelUIUseCase
import app.shosetsu.android.domain.usecases.get.GetReaderChaptersUseCase
import app.shosetsu.android.domain.usecases.get.GetReaderSettingUseCase
import app.shosetsu.android.domain.usecases.get.GetRemoteNovelUseCase
import app.shosetsu.android.domain.usecases.get.GetRepositoryUseCase
import app.shosetsu.android.domain.usecases.get.GetTrueDeleteChapterUseCase
import app.shosetsu.android.domain.usecases.get.GetURLUseCase
import app.shosetsu.android.domain.usecases.get.GetUserAgentUseCase
import app.shosetsu.android.domain.usecases.load.LoadBrowseExtensionsUseCase
import app.shosetsu.android.domain.usecases.load.LoadDeletePreviousChapterUseCase
import app.shosetsu.android.domain.usecases.load.LoadDownloadsUseCase
import app.shosetsu.android.domain.usecases.load.LoadLibraryFilterSettingsUseCase
import app.shosetsu.android.domain.usecases.load.LoadLibraryUseCase
import app.shosetsu.android.domain.usecases.load.LoadLiveAppThemeUseCase
import app.shosetsu.android.domain.usecases.load.LoadNovelUIBadgeToastUseCase
import app.shosetsu.android.domain.usecases.load.LoadNovelUIColumnsHUseCase
import app.shosetsu.android.domain.usecases.load.LoadNovelUIColumnsPUseCase
import app.shosetsu.android.domain.usecases.load.LoadNovelUITypeUseCase
import app.shosetsu.android.domain.usecases.load.LoadReaderThemes
import app.shosetsu.android.domain.usecases.load.LoadRepositoriesUseCase
import app.shosetsu.android.domain.usecases.load.LoadUpdatesUseCase
import app.shosetsu.android.domain.usecases.settings.LoadChaptersResumeFirstUnreadUseCase
import app.shosetsu.android.domain.usecases.settings.LoadNavigationStyleUseCase
import app.shosetsu.android.domain.usecases.settings.LoadRequireDoubleBackUseCase
import app.shosetsu.android.domain.usecases.settings.SetNovelUITypeUseCase
import app.shosetsu.android.domain.usecases.start.StartAppUpdateInstallWorkerUseCase
import app.shosetsu.android.domain.usecases.start.StartBackupMigrationWorkerUseCase
import app.shosetsu.android.domain.usecases.start.StartBackupWorkerUseCase
import app.shosetsu.android.domain.usecases.start.StartDownloadWorkerUseCase
import app.shosetsu.android.domain.usecases.start.StartRestoreWorkerUseCase
import app.shosetsu.android.domain.usecases.start.StartUpdateWorkerUseCase
import app.shosetsu.android.domain.usecases.update.UpdateBookmarkedNovelUseCase
import app.shosetsu.android.domain.usecases.update.UpdateChapterUseCase
import app.shosetsu.android.domain.usecases.update.UpdateExtensionSettingUseCase
import app.shosetsu.android.domain.usecases.update.UpdateLibraryFilterStateUseCase
import app.shosetsu.android.domain.usecases.update.UpdateNovelSettingUseCase
import app.shosetsu.android.domain.usecases.update.UpdateNovelUseCase
import app.shosetsu.android.domain.usecases.update.UpdateRepositoryUseCase
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
val useCaseModule: DI.Module = DI.Module("useCase") {
	bind<GetUserAgentUseCase>() with provider { new(::GetUserAgentUseCase) }

	bind<LoadDownloadsUseCase>() with provider { new(::LoadDownloadsUseCase) }

	bind<LoadLibraryUseCase>() with provider { new(::LoadLibraryUseCase) }

	bind<SearchBookMarkedNovelsUseCase>() with provider { new(::SearchBookMarkedNovelsUseCase) }

	bind<SetNovelPinUseCase>() with provider { new(::SetNovelPinUseCase) }

	bind<LoadBrowseExtensionsUseCase>() with provider { new(::LoadBrowseExtensionsUseCase) }

	bind<LoadUpdatesUseCase>() with provider { new(::LoadUpdatesUseCase) }

	bind<StartRepositoryUpdateManagerUseCase>() with
		provider { new(::StartRepositoryUpdateManagerUseCase) }

	bind<RequestInstallExtensionUseCase>() with provider { new(::RequestInstallExtensionUseCase) }

	bind<UpdateNovelUseCase>() with provider { new(::UpdateNovelUseCase) }

	bind<GetExtensionUseCase>() with provider { new(::GetExtensionUseCase) }

	bind<NovelBackgroundAddUseCase>() with provider { new(::NovelBackgroundAddUseCase) }

	bind<GetNovelUIUseCase>() with provider { new(::GetNovelUIUseCase) }

	bind<GetRemoteNovelUseCase>() with provider { new(::GetRemoteNovelUseCase) }

	bind<StartDownloadWorkerAfterUpdateUseCase>() with
		provider { new(::StartDownloadWorkerAfterUpdateUseCase) }

	bind<GetCatalogueListingDataUseCase>() with provider { new(::GetCatalogueListingDataUseCase) }

	bind<GetChapterUIsUseCase>() with provider { new(::GetChapterUIsUseCase) }

	bind<UpdateChapterUseCase>() with provider { new(::UpdateChapterUseCase) }

	bind<GetReaderChaptersUseCase>() with provider { new(::GetReaderChaptersUseCase) }

	bind<GetChapterPassageUseCase>() with provider { new(::GetChapterPassageUseCase) }

	bind<DownloadChapterPassageUseCase>() with provider { new(::DownloadChapterPassageUseCase) }
	bind<DeleteChapterPassageUseCase>() with provider { new(::DeleteChapterPassageUseCase) }

	bind<StartDownloadWorkerUseCase>() with provider { new(::StartDownloadWorkerUseCase) }

	bind<StartUpdateWorkerUseCase>() with provider { new(::StartUpdateWorkerUseCase) }

	bind<UpdateBookmarkedNovelUseCase>() with provider { new(::UpdateBookmarkedNovelUseCase) }

	bind<UninstallExtensionUseCase>() with provider { new(::UninstallExtensionUseCase) }

	bind<GetURLUseCase>() with provider { new(::GetURLUseCase) }

	bind<IsOnlineUseCase>() with provider { new(::IsOnlineUseCase) }

	bind<GetCatalogueQueryDataUseCase>() with provider { new(::GetCatalogueQueryDataUseCase) }

	bind<GetExtensionSettingsUseCase>() with provider { new(::GetExtensionSettingsUseCase) }

	bind<GetInstalledExtensionUseCase>() with provider { new(::GetInstalledExtensionUseCase) }

	bind<GetRepositoryUseCase>() with provider { new(::GetRepositoryUseCase) }

	bind<LoadRepositoriesUseCase>() with provider { new(::LoadRepositoriesUseCase) }

	bind<LoadReaderThemes>() with provider { new(::LoadReaderThemes) }

	bind<LoadChaptersResumeFirstUnreadUseCase>() with
		provider { new(::LoadChaptersResumeFirstUnreadUseCase) }

	bind<LoadNavigationStyleUseCase>() with provider { new(::LoadNavigationStyleUseCase) }

	bind<LoadRequireDoubleBackUseCase>() with provider { new(::LoadRequireDoubleBackUseCase) }

	bind<LoadLiveAppThemeUseCase>() with provider { new(::LoadLiveAppThemeUseCase) }

	bind<LoadNovelUIColumnsPUseCase>() with provider { new(::LoadNovelUIColumnsPUseCase) }
	bind<LoadNovelUIColumnsHUseCase>() with provider { new(::LoadNovelUIColumnsHUseCase) }
	bind<LoadNovelUIBadgeToastUseCase>() with provider { new(::LoadNovelUIBadgeToastUseCase) }
	bind<LoadNovelUITypeUseCase>() with provider { new(::LoadNovelUITypeUseCase) }

	bind<StartAppUpdateInstallWorkerUseCase>() with
		provider { new(::StartAppUpdateInstallWorkerUseCase) }

	bind<SetNovelUITypeUseCase>() with provider { new(::SetNovelUITypeUseCase) }

	bind<GetNovelSettingFlowUseCase>() with provider { new(::GetNovelSettingFlowUseCase) }

	bind<UpdateNovelSettingUseCase>() with provider { new(::UpdateNovelSettingUseCase) }

	bind<LoadDeletePreviousChapterUseCase>() with provider { new(::LoadDeletePreviousChapterUseCase) }

	bind<PurgeNovelCacheUseCase>() with provider { new(::PurgeNovelCacheUseCase) }

	bind<StartBackupWorkerUseCase>() with provider { new(::StartBackupWorkerUseCase) }

	bind<StartBackupMigrationWorkerUseCase>() with
		provider { new(::StartBackupMigrationWorkerUseCase) }

	bind<StartRestoreWorkerUseCase>() with provider { new(::StartRestoreWorkerUseCase) }

	bind<AddRepositoryUseCase>() with provider { new(::AddRepositoryUseCase) }
	bind<DeleteRepositoryUseCase>() with provider { new(::DeleteRepositoryUseCase) }
	bind<UpdateRepositoryUseCase>() with provider { new(::UpdateRepositoryUseCase) }

	bind<GetReaderSettingUseCase>() with provider { new(::GetReaderSettingUseCase) }

	bind<LoadLibraryFilterSettingsUseCase>() with provider { new(::LoadLibraryFilterSettingsUseCase) }

	bind<GetCategoriesUseCase>() with provider { new(::GetCategoriesUseCase) }
	bind<AddCategoryUseCase>() with provider { new(::AddCategoryUseCase) }
	bind<DeleteCategoryUseCase>() with provider { new(::DeleteCategoryUseCase) }
	bind<MoveCategoryUseCase>() with provider { new(::MoveCategoryUseCase) }

	bind<GetNovelCategoriesUseCase>() with provider { new(::GetNovelCategoriesUseCase) }
	bind<SetNovelCategoriesUseCase>() with provider { new(::SetNovelCategoriesUseCase) }
	bind<SetNovelsCategoriesUseCase>() with provider { new(::SetNovelsCategoriesUseCase) }

	bind<UpdateLibraryFilterStateUseCase>() with provider { new(::UpdateLibraryFilterStateUseCase) }

	bind<UpdateExtensionSettingUseCase>() with provider { new(::UpdateExtensionSettingUseCase) }

	bind<ForceInsertRepositoryUseCase>() with provider { new(::ForceInsertRepositoryUseCase) }

	bind<RemoveExtensionEntityUseCase>() with provider { new(::RemoveExtensionEntityUseCase) }

	bind<InstallExtensionUseCase>() with provider { new(::InstallExtensionUseCase) }
	bind<CancelExtensionInstallUseCase>() with provider { new(::CancelExtensionInstallUseCase) }

	bind<RecordChapterIsReadingUseCase>() with provider { new(::RecordChapterIsReadingUseCase) }
	bind<RecordChapterIsReadUseCase>() with provider { new(::RecordChapterIsReadUseCase) }

	bind<GetLastReadChapterUseCase>() with provider { new(::GetLastReadChapterUseCase) }

	bind<TrueDeleteChapterUseCase>() with provider { new(::TrueDeleteChapterUseCase) }
	bind<GetTrueDeleteChapterUseCase>() with provider { new(::GetTrueDeleteChapterUseCase) }
}
