package app.shosetsu.android.domain.repository.impl

import app.shosetsu.android.common.EmptyResponseBodyException
import app.shosetsu.android.common.FileNotFoundException
import app.shosetsu.android.common.FilePermissionException
import app.shosetsu.android.common.MissingFeatureException
import app.shosetsu.android.common.ext.onIO
import app.shosetsu.android.datasource.local.file.base.IFileCachedAppUpdateDataSource
import app.shosetsu.android.datasource.remote.base.IRemoteAppUpdateDataSource
import app.shosetsu.android.domain.model.local.AppUpdateEntity
import app.shosetsu.android.domain.repository.base.IAppUpdatesRepository
import app.shosetsu.lib.exceptions.HTTPException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
class FakeAppUpdatesRepository(
	private val iFileAppUpdateDataSource: IFileCachedAppUpdateDataSource,
	private val iRemoteAppUpdateDataSource: IRemoteAppUpdateDataSource,
) : IAppUpdatesRepository {
	private val _appUpdateFlow = MutableStateFlow<AppUpdateEntity?>(null)

	override fun loadAppUpdateFlow(): StateFlow<AppUpdateEntity?> =
		_appUpdateFlow

	override suspend fun loadRemoteUpdate(): AppUpdateEntity =
		onIO { loadAppUpdate() }

	override suspend fun loadAppUpdate(): AppUpdateEntity = onIO {
		val entity = AppUpdateEntity(
			"v3.0.0",
			999,
			9999,
			"https://github.com/shosetsuorg/shosetsu-preview/releases/download/r1136/shosetsu-r1136.apk",
			notes = listOf("This is a fake update")
		)

		_appUpdateFlow.emit(entity)

		entity
	}

	override fun canSelfUpdate(): Boolean =
		true

	@Throws(
		EmptyResponseBodyException::class,
		HTTPException::class,
		IOException::class,
		FilePermissionException::class,
		FileNotFoundException::class,
		MissingFeatureException::class
	)
	override suspend fun downloadAppUpdate(appUpdateEntity: AppUpdateEntity): String = onIO {
		if (iRemoteAppUpdateDataSource is IRemoteAppUpdateDataSource.Downloadable)
			iRemoteAppUpdateDataSource.downloadAppUpdate(appUpdateEntity).let { response ->
				iFileAppUpdateDataSource.saveAPK(appUpdateEntity, response).also {
					// Call GC to clean up the chunky objects
					System.gc()
				}
			}
		else throw MissingFeatureException("self update")
	}
}