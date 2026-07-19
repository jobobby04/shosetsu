package app.shosetsu.android.domain.usecases

import android.database.sqlite.SQLiteException
import app.shosetsu.android.common.ext.generify
import app.shosetsu.android.domain.model.local.InstalledExtensionEntity
import app.shosetsu.android.domain.repository.base.IExtensionEntitiesRepository
import app.shosetsu.android.domain.repository.base.IExtensionsRepository
import app.shosetsu.android.view.uimodels.model.BrowseExtensionUI
import app.shosetsu.android.view.uimodels.model.InstalledExtensionUI

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
 * 14 / 08 / 2020
 */
class UninstallExtensionUseCase(
	private val extensionRepository: IExtensionsRepository,
	private val extensionEntitiesRepository: IExtensionEntitiesRepository,
) {
	@Throws(SQLiteException::class)
	suspend operator fun invoke(extensionUI: InstalledExtensionUI) = invoke(extensionUI.convertTo())

	@Throws(SQLiteException::class)
	suspend operator fun invoke(ext: InstalledExtensionEntity) {
		extensionEntitiesRepository.uninstall(ext.generify())
		extensionRepository.uninstall(ext)
	}

	/**
	 * This function will perform an additional call to acquire the [InstalledExtensionEntity] of this generic entity.
	 * If none is found nothing will be done.
	 */
	@Throws(SQLiteException::class)
	suspend operator fun invoke(extensionUI: BrowseExtensionUI) {
		// Try to get the installed extension entity
		val extensionEntity = extensionRepository.getInstalledExtension(extensionUI.id)

		// Check if we got it
		if (extensionEntity != null) {
			// Run the backing logic!
			invoke(extensionEntity)
		}
	}
}
