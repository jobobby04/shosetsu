package app.shosetsu.android.datasource.remote.base

import app.shosetsu.android.domain.model.remote.AppUpdateDTO
import app.shosetsu.common.domain.model.local.AppUpdateEntity
import app.shosetsu.common.dto.HResult
import okhttp3.Response

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
interface IRemoteAppUpdateDataSource {
	suspend fun loadGitAppUpdate(): HResult<AppUpdateDTO>

	suspend fun downloadGitUpdate(update: AppUpdateEntity): HResult<Response>
}