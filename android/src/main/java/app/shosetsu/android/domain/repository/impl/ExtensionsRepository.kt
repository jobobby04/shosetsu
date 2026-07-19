package app.shosetsu.android.domain.repository.impl

import android.database.sqlite.SQLiteException
import app.shosetsu.android.common.ext.logV
import app.shosetsu.android.common.ext.onIO
import app.shosetsu.android.datasource.local.database.base.IDBExtRepoDataSource
import app.shosetsu.android.datasource.local.database.base.IDBInstalledExtensionsDataSource
import app.shosetsu.android.datasource.local.database.base.IDBRepositoryExtensionsDataSource
import app.shosetsu.android.datasource.remote.base.IRemoteExtensionDataSource
import app.shosetsu.android.domain.model.local.BrowseExtensionEntity
import app.shosetsu.android.domain.model.local.ExtensionInstallOptionEntity
import app.shosetsu.android.domain.model.local.GenericExtensionEntity
import app.shosetsu.android.domain.model.local.InstalledExtensionEntity
import app.shosetsu.android.domain.model.local.RepositoryEntity
import app.shosetsu.android.domain.repository.base.IExtensionsRepository
import app.shosetsu.lib.Version
import app.shosetsu.lib.exceptions.HTTPException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

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
 * 24 / 04 / 2020
 *
 * @author github.com/doomsdayrs
 */
class ExtensionsRepository(
	private val installedDBSource: IDBInstalledExtensionsDataSource,
	private val repoDBSource: IDBRepositoryExtensionsDataSource,
	private val remoteSource: IRemoteExtensionDataSource,
	private val _repoDBSource: IDBExtRepoDataSource,
) : IExtensionsRepository {
	@Throws(SQLiteException::class)
	@OptIn(ExperimentalCoroutinesApi::class)
	override fun loadBrowseExtensions(): Flow<List<BrowseExtensionEntity>> =
		repoDBSource.loadExtensionsFlow().flatMapLatest { list ->

			val browseExtensions = list.groupBy { it.id }.map { (extId, matchingExtensions) ->
				installedDBSource.loadExtensionLive(extId).map { installedExt ->
					val firstExt = matchingExtensions.first()

					// Variables to calculate, with their defaults
					var isUpdateAvailable = false
					var isObsolete = false

					// Is this extension installed?
					if (installedExt != null) {
						// Extension is installed

						// Try to get the repo version
						val repoVersion =
							matchingExtensions.find { it.repoID == installedExt.repoID }?.version

						// Check if the repo version is found or not
						if (repoVersion != null) {
							// We have an upstream version
							if (installedExt.version < repoVersion) {
								// Repo version is newer! Update!
								isUpdateAvailable = true
							} else {
								// Repo version is not newer...
								// Is it marked obsolete?
								if (repoVersion == Version(-9, -9, -9)) {
									// We are now obsolete!
									isObsolete = true
								}
							}
						} else {
							// The repo version is gone... obsolete likely
							isObsolete = true
						}
					}

					BrowseExtensionEntity(
						id = extId,
						name = installedExt?.name ?: firstExt.name,
						imageURL = installedExt?.imageURL ?: firstExt.imageURL,
						lang = installedExt?.lang ?: firstExt.lang,
						installOptions = if (installedExt == null) {
							matchingExtensions.mapNotNull { genericExt ->
								val repo = _repoDBSource.loadRepository(genericExt.repoID)

								if (repo != null && repo.isEnabled) {
									ExtensionInstallOptionEntity(
										genericExt.repoID,
										repo.name,
										genericExt.version,
									)
								} else {
									null
								}
							}.sortedBy { it.repoId }.sortedBy { it.version }
						} else {
							null
						},
						isInstalled = installedExt != null,
						installedVersion = installedExt?.version,
						installedRepo = installedExt?.repoID ?: -1,
						isUpdateAvailable = isUpdateAvailable,
						updateVersion = matchingExtensions.find { it.repoID == installedExt?.repoID }?.version,
						isInstalling = false, // We can ignore this, another layer will set it
						isObsolete = isObsolete,
					)
				}
			}

			combine(browseExtensions) { it.toList() }
		}.distinctUntilChanged().onIO()

	override fun loadExtensionsFLow(): Flow<List<InstalledExtensionEntity>> =
		installedDBSource.loadExtensionsFlow().onIO()

	override fun getInstalledExtensionFlow(id: Int): Flow<InstalledExtensionEntity?> =
		installedDBSource.loadExtensionLive(id).onIO()

	@Throws(SQLiteException::class)
	override suspend fun getExtension(repoId: Int, extId: Int): GenericExtensionEntity? =
		onIO { repoDBSource.loadExtension(repoId, extId) }

	@Throws(SQLiteException::class)
	override suspend fun getInstalledExtension(id: Int): InstalledExtensionEntity? =
		onIO { installedDBSource.loadExtension(id) }

	@Throws(SQLiteException::class)
	override suspend fun getRepositoryExtensions(repoID: Int): List<GenericExtensionEntity> =
		onIO { repoDBSource.getExtensions(repoID) }

	@Throws(SQLiteException::class)
	override suspend fun loadRepositoryExtensions(): List<GenericExtensionEntity> =
		onIO { repoDBSource.loadExtensions() }

	@Throws(SQLiteException::class)
	override suspend fun uninstall(extensionEntity: InstalledExtensionEntity) {
		onIO { installedDBSource.deleteExtension(extensionEntity) }
	}

	@Throws(SQLiteException::class)
	override suspend fun updateRepositoryExtension(extensionEntity: GenericExtensionEntity) {
		onIO { repoDBSource.updateExtension(extensionEntity) }
	}

	@Throws(SQLiteException::class)
	override suspend fun updateInstalledExtension(extensionEntity: InstalledExtensionEntity) {
		onIO { installedDBSource.updateExtension(extensionEntity) }
	}

	@Throws(SQLiteException::class)
	override suspend fun delete(extensionEntity: GenericExtensionEntity) {
		onIO {
			installedDBSource.loadExtension(extensionEntity.id)?.let {
				installedDBSource.deleteExtension(it)
			}

			repoDBSource.deleteExtension(extensionEntity)
		}
	}

	@Throws(SQLiteException::class)
	override suspend fun insert(extensionEntity: GenericExtensionEntity): Long {
		logV("Arguments: $extensionEntity")
		return onIO { repoDBSource.insert(extensionEntity) }
	}

	@Throws(SQLiteException::class)
	override suspend fun insert(extensionEntity: InstalledExtensionEntity): Long =
		onIO { installedDBSource.insert(extensionEntity) }

	@Throws(
		HTTPException::class,
		SocketTimeoutException::class,
		UnknownHostException::class,
	)
	override suspend fun downloadExtension(
		repositoryEntity: RepositoryEntity,
		extension: GenericExtensionEntity,
	): ByteArray = onIO { remoteSource.downloadExtension(repositoryEntity, extension) }

	@Throws(SQLiteException::class)
	override suspend fun isExtensionInstalled(extensionEntity: GenericExtensionEntity): Boolean =
		onIO { installedDBSource.loadExtension(extensionEntity.id) != null }
}
