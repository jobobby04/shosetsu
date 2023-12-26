package app.shosetsu.android.domain.repository.base

import app.shosetsu.android.common.EmptyResponseBodyException
import app.shosetsu.android.common.FileNotFoundException
import app.shosetsu.android.common.FilePermissionException
import app.shosetsu.android.common.MissingFeatureException
import app.shosetsu.android.domain.model.local.AppUpdateEntity
import app.shosetsu.lib.exceptions.HTTPException
import kotlinx.coroutines.flow.StateFlow
import java.io.IOException
import java.net.UnknownHostException

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
 * 06 / 09 / 2020
 *
 * Source of truth for all app updates
 */
interface IAppUpdatesRepository {
	/**
	 * Flow of app updates
	 */
	val appUpdate: StateFlow<AppUpdateEntity?>

	/**
	 * Checks for a new update
	 */
	@Throws(
		FilePermissionException::class,
		UnknownHostException::class,
		IOException::class,
		HTTPException::class
	)
	suspend fun fetch(): AppUpdateEntity?

	/**
	 * Can the app self update itself
	 */
	val canSelfUpdate: Boolean

	/**
	 * Downloads the app update specified in [appUpdate]
	 *
	 * @return Path of the apk file, this is messy but it must be done so the intent can work
	 */
	@Throws(
		IOException::class,
		FilePermissionException::class,
		FileNotFoundException::class,
		MissingFeatureException::class,
		EmptyResponseBodyException::class,
		HTTPException::class,
		NoSuchElementException::class
	)
	suspend fun downloadAppUpdate(): String
}