package app.shosetsu.android.backend.workers.onetime

import android.content.Context
import android.database.sqlite.SQLiteException
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import app.shosetsu.android.R
import app.shosetsu.android.backend.workers.CoroutineWorkerManager
import app.shosetsu.android.backend.workers.NotificationCapable
import app.shosetsu.android.common.SettingKey.*
import app.shosetsu.android.common.consts.LogConstants
import app.shosetsu.android.common.consts.Notifications
import app.shosetsu.android.common.consts.Notifications.CHANNEL_BACKUP
import app.shosetsu.android.common.consts.WorkerTags.BACKUP_WORK_ID
import app.shosetsu.android.common.ext.*
import app.shosetsu.android.common.utils.backupJSON
import app.shosetsu.android.domain.model.local.BackupEntity
import app.shosetsu.android.domain.model.local.InstalledExtensionEntity
import app.shosetsu.android.domain.model.local.NovelEntity
import app.shosetsu.android.domain.model.local.backup.*
import app.shosetsu.android.domain.repository.base.*
import app.shosetsu.android.domain.repository.base.IBackupRepository.BackupProgress
import kotlinx.coroutines.delay
import kotlinx.serialization.json.encodeToStream
import org.acra.ACRA
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.instance
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.zip.GZIPOutputStream

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
 * 18 / 01 / 2021
 */
class BackupWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(
	appContext,
	params,
), DIAware, NotificationCapable {

	override val di: DI by closestDI(appContext)
	private val novelRepository by instance<INovelsRepository>()
	private val novelPinRepository by instance<INovelPinsRepository>()
	private val iSettingsRepository by instance<ISettingsRepository>()

	/**
	 * TODO add settings backup
	 */
	private val novelSettingsRepository by instance<INovelSettingsRepository>()
	private val extensionsRepository by instance<IExtensionsRepository>()
	private val chaptersRepository by instance<IChaptersRepository>()
	private val chapterHistoryRepository by instance<ChapterHistoryRepository>()
	private val extensionRepoRepository by instance<IExtensionRepoRepository>()
	private val backupRepository by instance<IBackupRepository>()
	private val categoriesRepository by instance<ICategoryRepository>()
	private val novelCategoriesRepository by instance<INovelCategoryRepository>()

	override val notificationManager: NotificationManagerCompat by notificationManager()

	override val baseNotificationBuilder: NotificationCompat.Builder
		get() = notificationBuilder(applicationContext, CHANNEL_BACKUP)
			.setSmallIcon(R.drawable.backup_icon)
			.setSubText("Backup")
			.setOnlyAlertOnce(true)
			.setOngoing(true)

	override val notifyContext: Context
		get() = applicationContext
	override val defaultNotificationID: Int = Notifications.ID_BACKUP

	private suspend fun backupChapters() =
		iSettingsRepository.getBoolean(ShouldBackupChapters)

	private suspend fun backupSettings() =
		iSettingsRepository.getBoolean(ShouldBackupSettings)

	@Throws(IOException::class)
	inline fun gzip(block: (GZIPOutputStream) -> Unit): ByteArray {
		val bos = ByteArrayOutputStream()
		GZIPOutputStream(bos).use { block(it) }
		return bos.toByteArray()
	}

	@Suppress("Destructure")
	@Throws(SQLiteException::class)
	private suspend fun getBackupChapters(novelID: Int): List<BackupChapterEntity> {
		if (backupChapters())
			chaptersRepository.getChapters(novelID).let { list ->
				return list.map { chapterEntity ->
					val chapterHistory = try {
						chapterHistoryRepository.get(chapterEntity.id!!)
					} catch (e: SQLiteException) {
						null
					}

					BackupChapterEntity(
						url = chapterEntity.url,
						name = chapterEntity.title,
						bookmarked = chapterEntity.bookmarked,
						rS = chapterEntity.readingStatus,
						rP = chapterEntity.readingPosition,
						startedReadingAt = chapterHistory?.startedReadingAt,
						endedReadingAt = chapterHistory?.endedReadingAt,
						releaseDate = chapterEntity.releaseDate,
						order = chapterEntity.order
					)
				}
			}
		return emptyList()
	}

	private suspend fun getBackupCategories(): Map<Int, BackupCategoryEntity> {
		return categoriesRepository.getCategories().associate {
			it.id!! to BackupCategoryEntity(
				it.name,
				it.order
			)
		}
	}


	@Throws(IOException::class)
	override suspend fun doWork(): Result {
		// Load novels
		logV(LogConstants.SERVICE_EXECUTE)
		notify("Starting...")
		backupRepository.updateProgress(BackupProgress.IN_PROGRESS)
		val backupSettings = backupSettings()


		lateinit var novelsToChapters: List<Pair<NovelEntity, List<BackupChapterEntity>>>
		lateinit var extensions: List<InstalledExtensionEntity>
		lateinit var categories: Map<Int, BackupCategoryEntity>

		/*
		Run to isolate the variable 'novels' so it can be trashed, hopefully saving memory
		 */
		val success = run {
			val novels = try {
				novelRepository.loadBookmarkedNovelEntities()
			} catch (e: SQLiteException) {
				ACRA.errorReporter.handleSilentException(e)
				e.printStackTrace()
				return@run false
			}

			logI("Loaded ${novels.size} novel(s)")
			notify("Loaded ${novels.size} novel(s)")

			logI("Retrieving and mapping chapters")
			notify("Retrieving and mapping chapters")
			// Novels to their chapters
			novelsToChapters = novels.map { it to getBackupChapters(it.id!!) }

			logI("Loading extensions required")
			notify("Loading extensions required")
			// Extensions each novel requires
			// Distinct, with no duplicates
			extensions = novels.mapNotNull {
				try {
					extensionsRepository.getInstalledExtension(it.extensionID)
				} catch (e: SQLiteException) {
					ACRA.errorReporter.handleSilentException(e)
					e.printStackTrace()
					null
				}
			}.distinct()

			// Categories each novel requires
			categories = try {
				getBackupCategories()
			} catch (e: SQLiteException) {
				ACRA.errorReporter.handleSilentException(e)
				e.printStackTrace()
				emptyMap()
			}


			true
		}
		System.gc() // please clean up

		if (success) {
			logI("Loading repositories required")
			notify("Loading repositories required")
			// All the repos required for backup
			// Contains only the repos that are used
			val repositoriesRequired =
				extensionRepoRepository.loadRepositories()
					.filter { repositoryEntity ->
						extensions.any { extensionEntity ->
							extensionEntity.repoID == repositoryEntity.id
						}
					}.map { (_, url, name) ->
						BackupRepositoryEntity(url, name)
					}

			val zippedBytes = gzip { gzip ->
				logI("Creating backup entity")
				notify("Creating backup entity")
				val backup = FleshedBackupEntity(
					repos = repositoriesRequired,
					// Creates the trees
					extensions = extensions.map { extensionEntity ->
						BackupExtensionEntity(
							extensionEntity.id,
							novelsToChapters.filter { (novel, _) ->
								novel.extensionID == extensionEntity.id
							}.map { (novel, chapters) ->
								val settings =
									if (backupSettings)
										novelSettingsRepository.get(novel.id!!)
									else null

								val bSettings = settings?.let {
									BackupNovelSettingEntity(
										it.sortType,
										it.showOnlyReadingStatusOf,
										it.showOnlyBookmarked,
										it.showOnlyDownloaded
									)
								} ?: BackupNovelSettingEntity()

								val novelCategories =
									novelCategoriesRepository.getNovelCategoriesFromNovel(
										novel.id!!
									)
										.map { categories[it.categoryID]!!.order }

								BackupNovelEntity(
									url = novel.url,
									bookmarked = novel.bookmarked,
									loaded = novel.loaded,
									name = novel.title,
									imageURL = novel.imageURL,
									description = novel.description,
									language = novel.language,
									genres = novel.genres,
									authors = novel.authors,
									artists = novel.artists,
									tags = novel.tags,
									status = novel.status,
									chapters = chapters,
									settings = bSettings,
									categories = novelCategories,
									pinned = novelPinRepository.isPinned(novel.id!!)
								)
							}
						)
					},
					categories = categories.values.toList()
				)

				logI("Encoding to json")
				notify("Encoding to json")
				backupJSON.encodeToStream(backup, gzip)
			}
			System.gc() // please clean up

			logI("Saving to file")
			notify("Saving to file")
			val pathResult = backupRepository.saveBackup(
				BackupEntity(
					zippedBytes
				)
			)
			pathResult.let {
				notify(R.string.worker_backup_complete) {
					setOngoing(false)
				}

				// Call GC to clean up the bulky resources
				System.gc()
				delay(500)
				backupRepository.updateProgress(BackupProgress.COMPLETE)
				return Result.success()
			}
		}

		backupRepository.updateProgress(BackupProgress.FAILURE)
		return Result.failure()
	}

	/**
	 * Manager of [BackupWorker]
	 */
	class Manager(context: Context) : CoroutineWorkerManager(context) {
		private val iSettingsRepository: ISettingsRepository by instance()

		private suspend fun requiresBackupOnIdle(): Boolean =
			iSettingsRepository.getBoolean(BackupOnlyWhenIdle)

		private suspend fun allowsBackupOnLowStorage(): Boolean =
			iSettingsRepository.getBoolean(BackupOnLowStorage)

		private suspend fun allowsBackupOnLowBattery(): Boolean =
			iSettingsRepository.getBoolean(BackupOnLowBattery)

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

		override suspend fun getWorkerState(index: Int): WorkInfo.State =
			getWorkerInfoList()[index].state

		override suspend fun getWorkerInfoList(): List<WorkInfo> =
			workerManager.getWorkInfosForUniqueWork(BACKUP_WORK_ID).await()

		override suspend fun getCount(): Int =
			getWorkerInfoList().size

		/**
		 * Starts the service. It will be started only if there isn't another instance already
		 * running.
		 */
		override fun start(data: Data) {
			launchIO {
				logI(LogConstants.SERVICE_NEW)
				workerManager.enqueueUniqueWork(
					BACKUP_WORK_ID,
					ExistingWorkPolicy.REPLACE,
					OneTimeWorkRequestBuilder<BackupWorker>(
					).setConstraints(
						Constraints.Builder().apply {
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
								setRequiresDeviceIdle(requiresBackupOnIdle())

							setRequiresStorageNotLow(!allowsBackupOnLowStorage())
							setRequiresBatteryNotLow(!allowsBackupOnLowBattery())
						}.build()
					).build()
				)
				logI(
					"Worker State ${
						workerManager.getWorkInfosForUniqueWork(BACKUP_WORK_ID)
							.await()[0].state
					}"
				)
			}
		}

		/**
		 * Stops the service.
		 */
		override fun stop(): Operation =
			workerManager.cancelUniqueWork(BACKUP_WORK_ID)
	}
}