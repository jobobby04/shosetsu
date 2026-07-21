package app.shosetsu.android.backend.workers.onetime

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.Operation
import androidx.work.WorkInfo
import androidx.work.WorkerParameters
import app.shosetsu.android.backend.workers.CoroutineWorkerManager
import app.shosetsu.android.backend.workers.NotificationCapable
import app.shosetsu.android.common.SettingKey
import app.shosetsu.android.common.consts.LogConstants
import app.shosetsu.android.common.consts.Notifications.CHANNEL_REPOSITORY_UPDATE
import app.shosetsu.android.common.consts.Notifications.ID_REPOSITORY_UPDATE
import app.shosetsu.android.common.consts.WorkerTags.REPOSITORY_UPDATE_TAG
import app.shosetsu.android.common.ext.addReportErrorAction
import app.shosetsu.android.common.ext.launchIO
import app.shosetsu.android.common.ext.logE
import app.shosetsu.android.common.ext.logI
import app.shosetsu.android.common.ext.notificationBuilder
import app.shosetsu.android.common.ext.notificationManager
import app.shosetsu.android.common.ext.removeProgress
import app.shosetsu.android.common.ext.setNotOngoing
import app.shosetsu.android.common.ext.setOngoing
import app.shosetsu.android.common.ext.setSmallIcon
import app.shosetsu.android.common.utils.await
import app.shosetsu.android.domain.model.local.ExtLibEntity
import app.shosetsu.android.domain.model.local.GenericExtensionEntity
import app.shosetsu.android.domain.model.local.RepositoryEntity
import app.shosetsu.android.domain.repository.base.IExtensionLibrariesRepository
import app.shosetsu.android.domain.repository.base.IExtensionRepoRepository
import app.shosetsu.android.domain.repository.base.IExtensionsRepository
import app.shosetsu.android.domain.repository.base.ISettingsRepository
import app.shosetsu.android.domain.usecases.RemoveExtensionEntityUseCase
import app.shosetsu.lib.Version
import app.shosetsu.lib.exceptions.HTTPException
import app.shosetsu.lib.json.RepoExtension
import app.shosetsu.lib.json.RepoIndex
import app.shosetsu.lib.json.RepoLibrary
import java.io.IOException
import kotlinx.coroutines.delay
import org.acra.ACRA
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.instance

/*
 * This file is part of Shosetsu.
 *
 * Shosetsu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Shosetsu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Shosetsu.  If not, see <https://www.gnu.org/licenses/>.
 */

/**
 * @since 2021-05-18
 *
 * This worker handles updating the repositories for shosetsu.
 *
 * The logic is details with the following.
 * 1. Load all repositories
 * 2. Loop over each repository
 * 3. If the repository is enabled, perform branch A, else branch B
 *
 * Branch A:
 * 1. Fetch the [RepoIndex] from the repository URL
 * 2. Download/update each extension library if its version is mismatched, or it is not installed.
 * 3. Update each db-repo-extension with its data from the respective [RepoExtension]
 * 4. Mark each db-repo-extension that is no longer present in the repository, but is installed, to -9.-9.-9
 * 5. Remove each db-repo-extension that is no longer present in the repository
 *
 * Branch B:
 * 1. De-prioritize each extension library that is installed by setting its version 0.0.0.
 * 3. Update each db-repo-extension that is installed to version -9.-9.-9
 * 2. Remove each db-repo-extension that is not installed.
 */
class RepositoryUpdateWorker(appContext: Context, params: WorkerParameters) :
	CoroutineWorker(appContext, params),
	DIAware,
	NotificationCapable {

	private val extRepo: IExtensionsRepository by instance()
	private val removeExtension: RemoveExtensionEntityUseCase by instance()
	private val extRepoRepo: IExtensionRepoRepository by instance()
	private val extensionLibrariesRepo: IExtensionLibrariesRepository by instance()

	private val iSettingsRepository by instance<ISettingsRepository>()

	private suspend fun disableOnFail(): Boolean =
		iSettingsRepository.getBoolean(SettingKey.RepoUpdateDisableOnFail)

	/**
	 * Updates the libraries, or deprioritizes them
	 *
	 * @param repoExtLibList of the application
	 * @param repository Repo of the index
	 */
	private suspend fun updateLibraries(
		repoExtLibList: List<RepoLibrary>,
		repository: RepositoryEntity,
	) {
		// the extension libs currently present in the database matching the repo
		val dbExtLibs = try {
			extensionLibrariesRepo.loadExtLibByRepo(repository.id)
		} catch (e: Exception) {
			// uh oh
			notify("Failed to load ext libs of repo: : ${e.message}")
			logE("Failed to load ext libs of repo: ${e.message}", e)
			// cant do anything else now, skip!
			return
		}

		// Max size for notification progress bar
		val repoExtLibListSize = repoExtLibList.size

		// Loops through the libraries from the remote repository
		for ((index, repoExtLib) in repoExtLibList.withIndex()) {
			// Notify the user of our progress
			notify("Checking ${repoExtLib.name} from ${repository.name}") {
				setSilent(true)
				setProgress(repoExtLibListSize, index + 1, false)
			}

			// Find the ext lib from our db list, or not
			val dbExtLib = dbExtLibs.find { dbExtLib ->
				dbExtLib.scriptName == repoExtLib.name && dbExtLib.repoID == repository.id
			}

			// Check if the repository is enabled or not

			// The ext lib to install, if any
			var extLibToInstall: ExtLibEntity? = null

			/*
			Check if we have to install the ext.
			Either if it is not present in the database, or the version is a mismatch.
			 */
			if (dbExtLib == null || repoExtLib.version > dbExtLib.version) {
				logI("${repoExtLib.name} version ${repoExtLib.version} is available, installing")

				// Set the ext to install as the repo version
				extLibToInstall = ExtLibEntity(
					scriptName = repoExtLib.name,
					version = repoExtLib.version,
					repoID = repository.id,
				)
			}

			// If install is true, then it adds it to the list for later
			if (extLibToInstall != null) {
				notify("Installing ${extLibToInstall.scriptName} from ${repository.name}") {
					setSilent(true)
					setProgress(1, 0, true)
				}
				// We handle the installation automatically, without user approval
				try {
					extensionLibrariesRepo.installExtLibrary(repository.url, extLibToInstall)
				} catch (e: Exception) {
					logE(
						"Failed to install extension library ${extLibToInstall.scriptName} from ${repository.name}",
						e,
					)
					ACRA.errorReporter.handleSilentException(e)
				}
			}
		}

		notify("Completed extension library update for ${repository.name}") {
			setSilent(true)
			removeProgress()
		}
	}

	/**
	 * Deprioritize installed extension libraries for a given repository
	 */
	private suspend fun deprioritizeLibraries(repository: RepositoryEntity) {
		// the extension libs currently present in the database matching the repo
		val dbExtLibs = try {
			extensionLibrariesRepo.loadExtLibByRepo(repository.id)
		} catch (e: Exception) {
			// uh oh
			notify("Failed to load ext libs of repo: : ${e.message}")
			logE("Failed to load ext libs of repo: ${e.message}", e)
			// cant do anything else now, skip!
			return
		}

		@Suppress("Destructure")
		for (dbExtLib in dbExtLibs) {
			/*
			Check if the extension library has not already been deprioritized
			 */
			if (dbExtLib.version != Version(0, 0, 0)) {
				// Deprioritize this extension
				extensionLibrariesRepo.update(dbExtLib.copy(version = Version(0, 0, 0)))
			}
		}
	}

	/**
	 * Loops over the given parameter [extensionsToRemove] to process its removal from the database.
	 *
	 * Most of the time it is just an outright goodbye, for ones that are installed, they get set to "-9.-9.-9".l
	 *
	 * @param extensionsToRemove Extensions to remove.
	 */
	private suspend inline fun handleExtensionRemoval(
		extensionsToRemove: List<GenericExtensionEntity>,
	) {
		// Loop over the extensions to remove
		for (extension in extensionsToRemove) {
			// Check if it is installed
			if (extRepo.isExtensionInstalled(extension)) {
				// By setting the repo version of the ext, the extension gets marked for removal
				extRepo.updateRepositoryExtension(
					extension.copy(
						version = Version(-9, -9, -9),
					),
				)
			} else {
				// Outright goodbye
				logI("Removing Extension: $extension")
				removeExtension(extension)
			}
		}
	}

	/**
	 * Handle updating an extension
	 *
	 * @param repo The repository
	 * @param repoExt The extension found in the repository
	 */
	private suspend fun handleRepoExtension(
		repo: RepositoryEntity,
		repoExt: RepoExtension,
		dbExtensions: List<GenericExtensionEntity>,
	) {
		// Get the extension matching this repo
		@Suppress("Destructure")
		val dbExtension = dbExtensions.find { dbExt ->
			dbExt.id == repoExt.id && dbExt.repoID == repo.id
		}

		// Check if the extension is present in shosetsu or not
		if (dbExtension == null) {
			// If the extension is not present, add it!
			val newEntity = GenericExtensionEntity(
				id = repoExt.id,
				repoID = repo.id,
				name = repoExt.name,
				fileName = repoExt.fileName,
				imageURL = repoExt.imageURL,
				lang = repoExt.lang,
				version = repoExt.version,
				md5 = repoExt.md5,
				type = repoExt.type,
			)
			logI("Inserting new extension, ${newEntity.name} #${newEntity.id}")
			extRepo.insert(newEntity)
		} else {
			// The extension is present, update it!
			extRepo.updateRepositoryExtension(
				dbExtension.copy(
					name = repoExt.name,
					fileName = repoExt.fileName,
					imageURL = repoExt.imageURL,
					lang = repoExt.lang,
					version = repoExt.version,
					md5 = repoExt.md5,
					type = repoExt.type,
				),
			)
		}
	}

	/**
	 * Updates database with [repoExtList]
	 *
	 * @param repo The repository being worked on
	 * @param repoExtList The extensions found in the repository
	 * @return list of extension ids that are present in this repository
	 */
	private suspend fun updateExtensions(
		repoExtList: List<RepoExtension>,
		repo: RepositoryEntity,
	): List<Int> {
		// Get the extensions in the database
		val dbExtensions = extRepo.getRepositoryExtensions(repo.id)

		// Loop over each repoExt in the repoExtList
		for (repoExtension in repoExtList) {
			// Handle the update of	 each extension
			handleRepoExtension(repo, repoExtension, dbExtensions)
		}

		// Filter to only have db entries that are not present in the repo list
		val extensionsToRemove = dbExtensions.filterNot { dbExt ->
			repoExtList.any { repoExt -> repoExt.id == dbExt.id }
		}

		// Handle the extension removal
		handleExtensionRemoval(extensionsToRemove)

		return repoExtList.map { it.id }
	}

	/**
	 * Handle a repository that was disabled.
	 */
	private suspend fun disableRepository(repository: RepositoryEntity) {
		// Deprioritize libraries that are installed
		deprioritizeLibraries(repository)

		// Remove all extensions from this repository
		handleExtensionRemoval(extRepo.getRepositoryExtensions(repository.id))
	}

	/**
	 * Handle updating the repository with its data from its index
	 */
	private suspend fun updateRepository(repo: RepositoryEntity) {
		logI("Updating $repo")

		// The repository index
		val repoIndex: RepoIndex

		// try to get the index for the repo
		try {
			repoIndex = extRepoRepo.getRepoData(repo)
		} catch (e: IllegalArgumentException) {
			e.printStackTrace()
			notify(
				"${e.message}",
				notificationId = ID_REPOSITORY_UPDATE + 1 + repo.id,
			) {
				removeProgress()
				setContentTitle("${repo.name} failed to load")
				setNotOngoing()
				addReportErrorAction(
					applicationContext,
					ID_REPOSITORY_UPDATE + 1 + repo.id,
					e,
				)
			}
			return
		} catch (e: IOException) {
			notify(
				"${e.message}",
				notificationId = ID_REPOSITORY_UPDATE + 1 + repo.id,
			) {
				removeProgress()
				setContentTitle("${repo.name} failed to load")
				setNotOngoing()
			}
			return
		} catch (e: HTTPException) {
			notify(
				"${e.code}",
				notificationId = ID_REPOSITORY_UPDATE + 1 + repo.id,
			) {
				removeProgress()
				setContentTitle("${repo.name} failed to load")
				setNotOngoing()
			}
			return
		} catch (e: Exception) {
			notify(
				"${e.message}",
				notificationId = ID_REPOSITORY_UPDATE + 1 + repo.id,
			) {
				removeProgress()
				setContentTitle("${repo.name} failed to load")
				setNotOngoing()
				addReportErrorAction(
					applicationContext,
					ID_REPOSITORY_UPDATE + 1 + repo.id,
					e,
				)
			}
			logE(
				"${repo.name} failed to load : ${e.message}",
				e,
			)
			if (disableOnFail()) {
				logI("Disabling repository: $repo")
				extRepoRepo.update(repo.copy(isEnabled = false))
			}
			return
		}

		// update the libraries first
		updateLibraries(repoIndex.libraries, repo)

		// update the extensions
		updateExtensions(repoIndex.extensions, repo)
	}

	/**
	 * @see [CoroutineWorker.doWork]
	 */
	override suspend fun doWork(): Result {
		logI("Starting Update")
		notify("Starting Repository Update") { setOngoing() }

		// Load all repositories
		val repos = extRepoRepo.loadRepositories()

		// Loop over each repository
		for (repo in repos) {
			// Handle each repository

			// Check if the repository is enabled
			if (repo.isEnabled) {
				// Enabled repositories get updated
				updateRepository(repo)
			} else {
				// Disable the repository fully
				disableRepository(repo)
			}
		}

		notify("Completed") { setNotOngoing() }
		delay(1000)
		notificationManager.cancel(defaultNotificationID)
		logI("Completed Repository Update")
		return Result.success()
	}

	override val di: DI by closestDI(appContext)

	override val baseNotificationBuilder: NotificationCompat.Builder
		get() = notificationBuilder(applicationContext, CHANNEL_REPOSITORY_UPDATE)
			.setSmallIcon(Icons.Default.Download)
			.setContentTitle("Repository Update")
			.setPriority(NotificationCompat.PRIORITY_DEFAULT)
			.setOngoing(true)

	override val notificationManager: NotificationManagerCompat by notificationManager()

	override val notifyContext: Context
		get() = applicationContext

	override val defaultNotificationID: Int
		get() = ID_REPOSITORY_UPDATE

	class Manager(context: Context) : CoroutineWorkerManager(context) {
		private val iSettingsRepository by instance<ISettingsRepository>()

		private suspend fun updateOnMetered(): Boolean =
			iSettingsRepository.getBoolean(SettingKey.RepoUpdateOnMeteredConnection)

		private suspend fun updateOnLowStorage(): Boolean =
			iSettingsRepository.getBoolean(SettingKey.RepoUpdateOnLowStorage)

		private suspend fun updateOnLowBattery(): Boolean =
			iSettingsRepository.getBoolean(SettingKey.RepoUpdateOnLowBattery)

		/**
		 * Returns the status of the service.
		 *
		 * @return true if the service is running, false otherwise.
		 */
		override suspend fun isRunning(): Boolean = try {
			getWorkerState() == WorkInfo.State.RUNNING
		} catch (e: Exception) {
			false
		}

		override suspend fun getWorkerState(index: Int) = getWorkerInfoList().getOrNull(index)?.state

		override suspend fun getWorkerInfoList(): List<WorkInfo> =
			workerManager.getWorkInfosForUniqueWork(REPOSITORY_UPDATE_TAG).await()

		override suspend fun getCount(): Int = getWorkerInfoList().size

		fun start(data: Data = Data.EMPTY, force: Boolean) {
			launchIO {
				logI(LogConstants.SERVICE_NEW)
				workerManager.enqueueUniqueWork(
					REPOSITORY_UPDATE_TAG,
					ExistingWorkPolicy.REPLACE,
					OneTimeWorkRequestBuilder<RepositoryUpdateWorker>().setInputData(data)
						.setConstraints(
							Constraints.Builder().apply {
								if (!force) {
									setRequiredNetworkType(
										if (updateOnMetered()) {
											NetworkType.CONNECTED
										} else {
											NetworkType.UNMETERED
										},
									)
									setRequiresStorageNotLow(!updateOnLowStorage())
									setRequiresBatteryNotLow(!updateOnLowBattery())
								}
							}.build(),
						).build(),
				)
				logI(
					"Worker State ${
						workerManager.getWorkInfosForUniqueWork(REPOSITORY_UPDATE_TAG)
							.await()[0].state
					}",
				)
			}
		}

		/**
		 * Starts the service. It will be started only if there isn't another instance already
		 * running.
		 */
		override fun start(data: Data) {
			start(data, false)
		}

		/**
		 * Stops the service.
		 */
		override fun stop(): Operation = workerManager.cancelUniqueWork(REPOSITORY_UPDATE_TAG)
	}
}
