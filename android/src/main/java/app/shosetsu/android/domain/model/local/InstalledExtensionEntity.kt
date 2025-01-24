package app.shosetsu.android.domain.model.local

import app.shosetsu.android.domain.model.database.DBInstalledExtensionEntity
import app.shosetsu.lib.ExtensionType
import app.shosetsu.lib.Novel
import app.shosetsu.lib.Version

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
 * Shosetsu
 *
 * @since 10 / 02 / 2022
 * @author Doomsdayrs
 *
 * @param id [DBInstalledExtensionEntity.id]
 * @param repoID [DBInstalledExtensionEntity.repoID]
 * @param name [DBInstalledExtensionEntity.name]
 * @param fileName [DBInstalledExtensionEntity.fileName]
 * @param imageURL [DBInstalledExtensionEntity.imageURL]
 * @param lang [DBInstalledExtensionEntity.lang]
 * @param version [DBInstalledExtensionEntity.version]
 * @param md5 [DBInstalledExtensionEntity.md5]
 * @param type [DBInstalledExtensionEntity.type]
 * @param enabled [DBInstalledExtensionEntity.enabled]
 * @param chapterType [DBInstalledExtensionEntity.chapterType]
 */
data class InstalledExtensionEntity(
	val id: Int,

	val repoID: Int,

	val name: String,

	val fileName: String,

	val imageURL: String,

	val lang: String,

	val version: Version,

	val md5: String,

	val type: ExtensionType,

	val enabled: Boolean,

	val chapterType: Novel.ChapterType,
)
