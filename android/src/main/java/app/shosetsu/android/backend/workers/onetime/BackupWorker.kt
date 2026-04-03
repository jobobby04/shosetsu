package app.shosetsu.android.backend.workers.onetime

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteException
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import androidx.core.provider.DocumentsContractCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.Operation
import androidx.work.WorkInfo
import androidx.work.WorkerParameters
import app.shosetsu.android.R
import app.shosetsu.android.activity.MainActivity
import app.shosetsu.android.backend.workers.CoroutineWorkerManager
import app.shosetsu.android.backend.workers.NotificationCapable
import app.shosetsu.android.common.FilePermissionException
import app.shosetsu.android.common.NullContentResolverException
import app.shosetsu.android.common.SettingKey.BackupOnLowBattery
import app.shosetsu.android.common.SettingKey.BackupOnLowStorage
import app.shosetsu.android.common.SettingKey.BackupOnlyWhenIdle
import app.shosetsu.android.common.SettingKey.BackupStorageLocation
import app.shosetsu.android.common.SettingKey.ShouldBackupChapters
import app.shosetsu.android.common.SettingKey.ShouldBackupSettings
import app.shosetsu.android.common.consts.ACTION_VIEW_SETTING_BACKUP_SELECT_FOLDER
import app.shosetsu.android.common.consts.LogConstants
import app.shosetsu.android.common.consts.Notifications
import app.shosetsu.android.common.consts.Notifications.CHANNEL_BACKUP
import app.shosetsu.android.common.consts.WorkerTags.BACKUP_WORK_ID
import app.shosetsu.android.common.ext.actionBuilder
import app.shosetsu.android.common.ext.addReportErrorAction
import app.shosetsu.android.common.ext.getString
import app.shosetsu.android.common.ext.launchIO
import app.shosetsu.android.common.ext.logE
import app.shosetsu.android.common.ext.logI
import app.shosetsu.android.common.ext.logV
import app.shosetsu.android.common.ext.notificationBuilder
import app.shosetsu.android.common.ext.notificationManager
import app.shosetsu.android.common.ext.setNotOngoing
import app.shosetsu.android.common.ext.setSmallIcon
import app.shosetsu.android.common.utils.await
import app.shosetsu.android.common.utils.backupJSON
import app.shosetsu.android.domain.model.local.BackupEntity
import app.shosetsu.android.domain.model.local.InstalledExtensionEntity
import app.shosetsu.android.domain.model.local.NovelEntity
import app.shosetsu.android.domain.model.local.backup.BackupCategoryEntity
import app.shosetsu.android.domain.model.local.backup.BackupChapterEntity
import app.shosetsu.android.domain.model.local.backup.BackupExtensionEntity
import app.shosetsu.android.domain.model.local.backup.BackupNovelEntity
import app.shosetsu.android.domain.model.local.backup.BackupNovelSettingEntity
import app.shosetsu.android.domain.model.local.backup.BackupRepositoryEntity
import app.shosetsu.android.domain.model.local.backup.FleshedBackupEntity
import app.shosetsu.android.domain.repository.base.ChapterHistoryRepository
import app.shosetsu.android.domain.repository.base.IBackupRepository
import app.shosetsu.android.domain.repository.base.IBackupRepository.BackupProgress
import app.shosetsu.android.domain.repository.base.ICategoryRepository
import app.shosetsu.android.domain.repository.base.IChaptersRepository
import app.shosetsu.android.domain.repository.base.IExtensionRepoRepository
import app.shosetsu.android.domain.repository.base.IExtensionsRepository
import app.shosetsu.android.domain.repository.base.INovelCategoryRepository
import app.shosetsu.android.domain.repository.base.INovelPinsRepository
import app.shosetsu.android.domain.repository.base.INovelSettingsRepository
import app.shosetsu.android.domain.repository.base.INovelsRepository
import app.shosetsu.android.domain.repository.base.ISettingsRepository
import kotlinx.coroutines.delay
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.encodeToStream
import org.acra.ACRA
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.instance
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
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

	private val openAppToSetBackupDirectory: Intent
		get() = Intent(applicationContext, MainActivity::class.java).apply {
			action = ACTION_VIEW_SETTING_BACKUP_SELECT_FOLDER
		}

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
			.setSmallIcon(Icons.Default.Backup)
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

	private suspend fun backupStorageLocation() =
		iSettingsRepository.getString(BackupStorageLocation).takeIf { it.isNotEmpty() }?.toUri()

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

	/**
	 * Loads a backup via the [Uri] provided by Androids file selection
	 */
	@Throws(
		FileNotFoundException::class,
		FilePermissionException::class,
		NullContentResolverException::class
	)
	private fun writeToUri(uri: Uri, backupEntity: BackupEntity) {
		val contentResolver = applicationContext.contentResolver
			?: throw NullContentResolverException()

		contentResolver.openFileDescriptor(uri, "w")?.use { descriptor ->
			FileOutputStream(descriptor.fileDescriptor).use {
				it.write(backupEntity.content)
			}
		} ?: throw FilePermissionException(
			uri.path ?: "",
			FilePermissionException.PermissionType.WRITE
		)
	}

	@OptIn(ExperimentalSerializationApi::class)
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
					}.map { (id, url, name) ->
						BackupRepositoryEntity(id, url, name)
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
							extensionEntity.repoID,
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
										it.showOnlyDownloaded,
										it.showOnlyString,
										it.reverseOrder,
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
			val backupEntity = BackupEntity(zippedBytes)

			try {
				fun missing(): Result {
					logE("Failed to create document")
					notify(R.string.export_backup_notification_missing_uri) {
						setNotOngoing()
						addAction(
							actionBuilder(
								Icons.Default.Settings,
								getString(R.string.worker_backup_set_folder),
								PendingIntent.getActivity(
									applicationContext,
									0,
									openAppToSetBackupDirectory,
									if (SDK_INT >= VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
								)
							).build()
						)
					}
					backupRepository.updateProgress(BackupProgress.FAILURE)
					return Result.failure()
				}

				val directoryUri = backupStorageLocation() ?: return missing()
				// If the directory URI is not provided, we can
				val docId =
					DocumentsContractCompat.getTreeDocumentId(directoryUri) ?: return missing()
				val parentDocumentUri =
					DocumentsContractCompat.buildDocumentUriUsingTree(directoryUri, docId)
						?: return missing()
				val uri = DocumentsContractCompat.createDocument(
					applicationContext.contentResolver,
					parentDocumentUri,
					"application/octet-stream",
					backupEntity.fileName
				) ?: return missing()
				writeToUri(uri, backupEntity)
			} catch (e: NullContentResolverException) {
				logE("Failed to write to URI", e)
				notify(R.string.worker_export_backup_null_resolver) {
					setNotOngoing()
					addReportErrorAction(applicationContext, defaultNotificationID, e)
				}
				backupRepository.updateProgress(BackupProgress.FAILURE)
				return Result.failure()
			} catch (e: FileNotFoundException) {
				logE("URI is invalid file", e)
				notify(R.string.worker_export_backup_file_missing) {
					setNotOngoing()
					addReportErrorAction(applicationContext, defaultNotificationID, e)
				}
				backupRepository.updateProgress(BackupProgress.FAILURE)
				return Result.failure()
			} catch (e: FilePermissionException) {
				logE("Invalid permission to file", e)
				notify(R.string.worker_export_backup_missing_perm) {
					setNotOngoing()
					addReportErrorAction(applicationContext, defaultNotificationID, e)
				}
				backupRepository.updateProgress(BackupProgress.FAILURE)
				return Result.failure()
			}

			notify(R.string.worker_backup_complete) {
				setOngoing(false)
			}

			// Call GC to clean up the bulky resources
			System.gc()
			delay(500)
			backupRepository.updateProgress(BackupProgress.COMPLETE)
			return Result.success()
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

		override suspend fun getWorkerState(index: Int) =
			getWorkerInfoList().getOrNull(index)?.state

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