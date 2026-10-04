package app.shosetsu.android.di

import app.shosetsu.android.domain.repository.base.ChapterHistoryRepository
import app.shosetsu.android.domain.repository.base.ContributorsRepository
import app.shosetsu.android.domain.repository.base.IAppUpdatesRepository
import app.shosetsu.android.domain.repository.base.IBackupRepository
import app.shosetsu.android.domain.repository.base.ICategoryRepository
import app.shosetsu.android.domain.repository.base.IChaptersRepository
import app.shosetsu.android.domain.repository.base.IDownloadsRepository
import app.shosetsu.android.domain.repository.base.IExtensionDownloadRepository
import app.shosetsu.android.domain.repository.base.IExtensionEntitiesRepository
import app.shosetsu.android.domain.repository.base.IExtensionLibrariesRepository
import app.shosetsu.android.domain.repository.base.IExtensionRepoRepository
import app.shosetsu.android.domain.repository.base.IExtensionSettingsRepository
import app.shosetsu.android.domain.repository.base.IExtensionsRepository
import app.shosetsu.android.domain.repository.base.INovelCategoryRepository
import app.shosetsu.android.domain.repository.base.INovelPinsRepository
import app.shosetsu.android.domain.repository.base.INovelReaderSettingsRepository
import app.shosetsu.android.domain.repository.base.INovelSettingsRepository
import app.shosetsu.android.domain.repository.base.INovelsRepository
import app.shosetsu.android.domain.repository.base.ISettingsRepository
import app.shosetsu.android.domain.repository.base.IUpdatesRepository
import app.shosetsu.android.domain.repository.impl.AppUpdatesRepository
import app.shosetsu.android.domain.repository.impl.BackupRepository
import app.shosetsu.android.domain.repository.impl.CategoryRepository
import app.shosetsu.android.domain.repository.impl.ChapterHistoryRepositoryImpl
import app.shosetsu.android.domain.repository.impl.ChaptersRepository
import app.shosetsu.android.domain.repository.impl.ContributorsRepositoryImpl
import app.shosetsu.android.domain.repository.impl.DownloadsRepository
import app.shosetsu.android.domain.repository.impl.ExtRepoRepository
import app.shosetsu.android.domain.repository.impl.ExtensionDownloadRepository
import app.shosetsu.android.domain.repository.impl.ExtensionEntitiesRepository
import app.shosetsu.android.domain.repository.impl.ExtensionLibrariesRepository
import app.shosetsu.android.domain.repository.impl.ExtensionSettingsRepository
import app.shosetsu.android.domain.repository.impl.ExtensionsRepository
import app.shosetsu.android.domain.repository.impl.NovelCategoryRepository
import app.shosetsu.android.domain.repository.impl.NovelPinsRepository
import app.shosetsu.android.domain.repository.impl.NovelReaderSettingsRepository
import app.shosetsu.android.domain.repository.impl.NovelSettingsRepository
import app.shosetsu.android.domain.repository.impl.NovelsRepository
import app.shosetsu.android.domain.repository.impl.SettingsRepository
import app.shosetsu.android.domain.repository.impl.UpdatesRepository
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.new
import org.kodein.di.singleton

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
 * 25 / 04 / 2020
 *
 * @author github.com/doomsdayrs
 */

val repositoryModule: DI.Module = DI.Module("repository_module") {
	bind<ICategoryRepository>() with singleton { new(::CategoryRepository) }

	bind<IChaptersRepository>() with singleton { new(::ChaptersRepository) }

	bind<IDownloadsRepository>() with singleton { new(::DownloadsRepository) }

	bind<IExtensionsRepository>() with singleton { new(::ExtensionsRepository) }

	bind<IExtensionLibrariesRepository>() with singleton { new(::ExtensionLibrariesRepository) }

	bind<IExtensionRepoRepository>() with singleton { new(::ExtRepoRepository) }

	bind<INovelCategoryRepository>() with singleton { new(::NovelCategoryRepository) }

	bind<INovelsRepository>() with singleton { new(::NovelsRepository) }

	bind<INovelPinsRepository>() with singleton { new(::NovelPinsRepository) }

	bind<IUpdatesRepository>() with singleton { new(::UpdatesRepository) }

	bind<IAppUpdatesRepository>() with singleton {
		new(::AppUpdatesRepository)
		// new(::FakeAppUpdatesRepository)
	}

	bind<ISettingsRepository>() with singleton { new(::SettingsRepository) }

	bind<IBackupRepository>() with singleton { new(::BackupRepository) }

	bind<INovelSettingsRepository>() with singleton { new(::NovelSettingsRepository) }

	bind<INovelReaderSettingsRepository>() with singleton { new(::NovelReaderSettingsRepository) }

	bind<IExtensionSettingsRepository>() with singleton { new(::ExtensionSettingsRepository) }

	bind<IExtensionDownloadRepository>() with singleton { new(::ExtensionDownloadRepository) }

	bind<IExtensionEntitiesRepository>() with singleton { new(::ExtensionEntitiesRepository) }

	bind<ChapterHistoryRepository>() with singleton { new(::ChapterHistoryRepositoryImpl) }

	bind<ContributorsRepository>() with singleton { new(::ContributorsRepositoryImpl) }
}
