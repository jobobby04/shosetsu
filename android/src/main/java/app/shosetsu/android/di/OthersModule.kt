package app.shosetsu.android.di

import app.shosetsu.android.backend.workers.onetime.AppUpdateCheckWorker
import app.shosetsu.android.backend.workers.onetime.AppUpdateInstallWorker
import app.shosetsu.android.backend.workers.onetime.BackupWorker
import app.shosetsu.android.backend.workers.onetime.DownloadWorker
import app.shosetsu.android.backend.workers.onetime.ExtensionInstallWorker
import app.shosetsu.android.backend.workers.onetime.MigrateBackupWorker
import app.shosetsu.android.backend.workers.onetime.NovelUpdateWorker
import app.shosetsu.android.backend.workers.onetime.RepositoryUpdateWorker
import app.shosetsu.android.backend.workers.onetime.RestoreBackupWorker
import app.shosetsu.android.backend.workers.perodic.AppUpdateCheckCycleWorker
import app.shosetsu.android.backend.workers.perodic.BackupCycleWorker
import app.shosetsu.android.backend.workers.perodic.NovelUpdateCycleWorker
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
 * 30 / 07 / 2020
 */
internal val othersModule = DI.Module("others") {

	// Workers

	// - onetime
	bind<DownloadWorker.Manager>() with singleton { new(DownloadWorker::Manager) }
	bind<AppUpdateCheckWorker.Manager>() with singleton { new(AppUpdateCheckWorker::Manager) }
	bind<NovelUpdateWorker.Manager>() with singleton { new(NovelUpdateWorker::Manager) }
	bind<AppUpdateInstallWorker.Manager>() with singleton { new(AppUpdateInstallWorker::Manager) }
	bind<BackupWorker.Manager>() with singleton { new(BackupWorker::Manager) }
	bind<RestoreBackupWorker.Manager>() with singleton { new(RestoreBackupWorker::Manager) }
	bind<MigrateBackupWorker.Manager>() with singleton { new(MigrateBackupWorker::Manager) }
	bind<RepositoryUpdateWorker.Manager>() with singleton { new(RepositoryUpdateWorker::Manager) }
	bind<ExtensionInstallWorker.Manager>() with singleton { new(ExtensionInstallWorker::Manager) }

	// - perodic
	bind<AppUpdateCheckCycleWorker.Manager>() with
		singleton { new(AppUpdateCheckCycleWorker::Manager) }
	bind<NovelUpdateCycleWorker.Manager>() with singleton { new(NovelUpdateCycleWorker::Manager) }
	bind<BackupCycleWorker.Manager>() with singleton { new(BackupCycleWorker::Manager) }
}
