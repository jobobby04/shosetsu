package app.shosetsu.android.domain.repository.impl

import app.shosetsu.android.common.ext.onIO
import app.shosetsu.android.datasource.local.file.base.IFileCachedAppUpdateDataSource
import app.shosetsu.android.datasource.remote.base.IRemoteAppUpdateDataSource
import app.shosetsu.android.domain.model.local.AppUpdateEntity
import app.shosetsu.android.domain.repository.base.IAppUpdatesRepository
import kotlinx.coroutines.flow.MutableStateFlow

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

	override val appUpdate: MutableStateFlow<AppUpdateEntity?> = MutableStateFlow(null)

	override suspend fun fetch(): AppUpdateEntity = onIO {
		val entity = AppUpdateEntity(
			"v3.0.0",
			999,
			9999,
			"https://github.com/shosetsuorg/shosetsu-preview/releases/download/r1136/shosetsu-r1136.apk",
			notes = listOf("This is a fake update")
		)

		appUpdate.emit(entity)

		entity
	}

	override val canSelfUpdate: Boolean = false

	override suspend fun downloadAppUpdate(): String = throw Exception("Stub")
}