package app.shosetsu.android.domain.model.database

import androidx.room.Entity
import app.shosetsu.android.domain.model.local.ExtLibEntity
import app.shosetsu.android.dto.Convertible
import app.shosetsu.lib.Version
import app.shosetsu.lib.json.RepoLibrary

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
 * @param scriptName name of the extension library. [RepoLibrary.name]
 * @param version version of this library. [RepoLibrary.version]
 * @param repoID repository this extension library is associated with.
 */
@Entity(
	tableName = "libs",
	// An extension library entity is identified by its name & repoID combo
	primaryKeys = ["scriptName", "repoID"]
)
data class DBExtLibEntity(
	val scriptName: String,
	var version: Version,
	var repoID: Int,
) : Convertible<ExtLibEntity> {
	override fun convertTo(): ExtLibEntity = ExtLibEntity(scriptName, version, repoID)
}
