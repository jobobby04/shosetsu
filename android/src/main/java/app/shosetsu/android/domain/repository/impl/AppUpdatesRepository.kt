package app.shosetsu.android.domain.repository.impl

import app.shosetsu.android.BuildConfig
import app.shosetsu.android.common.EmptyResponseBodyException
import app.shosetsu.android.common.FileNotFoundException
import app.shosetsu.android.common.FilePermissionException
import app.shosetsu.android.common.MissingFeatureException
import app.shosetsu.android.common.enums.ProductFlavors
import app.shosetsu.android.common.ext.launchIO
import app.shosetsu.android.common.ext.logE
import app.shosetsu.android.common.ext.logV
import app.shosetsu.android.common.ext.onIO
import app.shosetsu.android.common.utils.flavor
import app.shosetsu.android.datasource.local.file.base.IFileCachedAppUpdateDataSource
import app.shosetsu.android.datasource.remote.base.IRemoteAppUpdateDataSource
import app.shosetsu.android.domain.model.local.AppUpdateEntity
import app.shosetsu.android.domain.repository.base.IAppUpdatesRepository
import app.shosetsu.lib.Version
import app.shosetsu.lib.exceptions.HTTPException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.firstOrNull
import java.io.IOException

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
 * 07 / 09 / 2020
 */
class AppUpdatesRepository(
	private val iRemoteAppUpdateDataSource: IRemoteAppUpdateDataSource,
	private val iFileAppUpdateDataSource: IFileCachedAppUpdateDataSource,
) : IAppUpdatesRepository {

	/**
	 * SharedFlow so that the UI can always get whatever is last pushed.
	 *
	 * A StateFlow would not update the UI if the user clicks the update button twice
	 */
	override val appUpdate: MutableSharedFlow<AppUpdateEntity> = MutableSharedFlow(1)

	init {
		launchIO {
			try {
				appUpdate.emit(iFileAppUpdateDataSource.load())
			} catch (ignore: Exception) {
				// If it doesn't work, meh!
			}
		}
	}

	private fun compareVersion(newVersion: AppUpdateEntity): Int {
		when (flavor()) {
			ProductFlavors.UP_TO_DOWN -> {
				val currentVersion = Version(BuildConfig.VERSION_NAME.substringBefore("-"))
				val remoteVersion = Version(
					newVersion.version.substringBefore("-").substringAfter("v")
				)

				return remoteVersion.compareTo(currentVersion)
			}

			else -> {
				val currentV: Int
				val remoteV: Int

				// Assuming update will return a dev update for debug, only on standard
				if (flavor() == ProductFlavors.STANDARD && BuildConfig.DEBUG) {
					currentV = BuildConfig.VERSION_NAME.substringAfter("-").toInt()
					remoteV = newVersion.commit.takeIf { it != -1 } ?: newVersion.version.toInt()
				} else {
					currentV = BuildConfig.VERSION_CODE
					remoteV = newVersion.versionCode
				}

				return when {
					remoteV < currentV -> {
						//println("This a future release compared to $newVersion")
						-1
					}

					remoteV > currentV -> {
						//println("Update found compared to $newVersion")
						1
					}

					else -> {
						//println("This the current release compared to $newVersion")
						0
					}
				}
			}
		}
	}

	@Throws(FilePermissionException::class, IOException::class, HTTPException::class)
	override suspend fun fetch(): AppUpdateEntity? = onIO {
		return@onIO null // no updating for my fork
		// Ignore any attempt to run updater on non-standard debug versions
		if (flavor() != ProductFlavors.STANDARD && BuildConfig.DEBUG) return@onIO null

		val appUpdateEntity = try {
			iRemoteAppUpdateDataSource.loadAppUpdate()
		} catch (e: EmptyResponseBodyException) {
			logE(e.message!!, e)
			return@onIO null
		}

		val compared = compareVersion(appUpdateEntity)
		logV("Compared value $compared")
		if (compared > 0) {
			iFileAppUpdateDataSource.save(appUpdateEntity)
			appUpdate.emit(appUpdateEntity)
			return@onIO appUpdateEntity
		}

		return@onIO null
	}

	override val canSelfUpdate: Boolean =
		iRemoteAppUpdateDataSource is IRemoteAppUpdateDataSource.Downloadable

	@Throws(
		IOException::class,
		FilePermissionException::class,
		FileNotFoundException::class,
		MissingFeatureException::class,
		EmptyResponseBodyException::class,
		HTTPException::class,
		NoSuchElementException::class
	)
	override suspend fun downloadAppUpdate(): String = onIO {
		if (iRemoteAppUpdateDataSource is IRemoteAppUpdateDataSource.Downloadable) {
			val update = appUpdate.firstOrNull()

			if (update != null) {
				// Download
				val response = iRemoteAppUpdateDataSource.downloadAppUpdate(update)

				// Write
				iFileAppUpdateDataSource.writeAPK(update, response)
			} else throw NoSuchElementException("No update")
		} else throw MissingFeatureException("self update")
	}
}