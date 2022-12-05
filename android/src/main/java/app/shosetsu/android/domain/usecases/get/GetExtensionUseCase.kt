package app.shosetsu.android.domain.usecases.get

import android.database.sqlite.SQLiteException
import app.shosetsu.android.common.IncompatibleExtensionException
import app.shosetsu.android.common.ext.generify
import app.shosetsu.android.domain.repository.base.IExtensionEntitiesRepository
import app.shosetsu.android.domain.repository.base.IExtensionsRepository
import app.shosetsu.lib.IExtension
import kotlinx.serialization.SerializationException

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
 * 15 / 05 / 2020
 *
 * This use case makes sure that all gets for an [IExtension] will always have
 * the [IExtension] be fully updated (settings wise).
 *
 * This ensures that the [IExtension] will always be set with the latest settings,
 * at the trade off of loading time.
 *
 * A possible
 * TODO Have IExtensionsRepository call a new settings data source and do this massive load call when only pulling from the filesystem
 *
 */
class GetExtensionUseCase(
	private val extRepo: IExtensionsRepository,
	private val extEntitiesRepo: IExtensionEntitiesRepository
) {
	@Throws(
		SQLiteException::class,
		IncompatibleExtensionException::class,
		SerializationException::class
	)
	suspend operator fun invoke(extensionId: Int): IExtension? {
		if (extensionId == -1)
			return null

		return extRepo.getInstalledExtension(extensionId)?.let {
			extEntitiesRepo.get(it.generify())
		}
	}
}