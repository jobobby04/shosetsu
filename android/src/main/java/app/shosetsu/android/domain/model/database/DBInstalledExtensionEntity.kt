package app.shosetsu.android.domain.model.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.shosetsu.android.domain.model.local.InstalledExtensionEntity
import app.shosetsu.android.dto.Convertible
import app.shosetsu.lib.ExtensionType
import app.shosetsu.lib.Novel
import app.shosetsu.lib.Version
import app.shosetsu.lib.json.RepoExtension

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
 * shosetsu
 * @since 05 / 12 / 2020
 *
 * @param id [RepoExtension.id]
 * @param repoID Repository id that the extension belongs too
 * @param name [RepoExtension.name]
 * @param fileName [RepoExtension.fileName]
 * @param imageURL [RepoExtension.imageURL]
 * @param lang [RepoExtension.lang]
 * @param version [RepoExtension.version] Version currently installed
 * @param md5 [RepoExtension.md5]
 * @param type [RepoExtension.type]
 * @param enabled If extension is enabled
 * @param chapterType The reader type of this extension
 */
@Entity(
	tableName = "installed_extension",
)
data class DBInstalledExtensionEntity(
	@PrimaryKey
	val id: Int,

	val repoID: Int,

	var name: String = "",

	val fileName: String = "",

	var imageURL: String,

	val lang: String = "",

	var version: Version,

	var md5: String = "",

	val type: ExtensionType,

	var enabled: Boolean = false,

	var chapterType: Novel.ChapterType,
) : Convertible<InstalledExtensionEntity> {
	override fun convertTo(): InstalledExtensionEntity = InstalledExtensionEntity(
		id = id,
		repoID = repoID,
		name = name,
		fileName = fileName,
		imageURL = imageURL,
		lang = lang,
		enabled = enabled,
		version = version,
		chapterType = chapterType,
		md5 = md5,
		type = type
	)
}
