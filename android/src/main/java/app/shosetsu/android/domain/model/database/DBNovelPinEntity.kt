package app.shosetsu.android.domain.model.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import app.shosetsu.android.domain.model.local.NovelPinEntity
import app.shosetsu.android.dto.Convertible

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
 * @since 30 / 10 / 2022
 * @author Doomsdayrs
 *
 * @param novelId id of the novel
 * @param pinned if the novel specified by [novelId] is pinned or not
 */
@Entity(
	tableName = "novel_pins",
	foreignKeys = [
		ForeignKey(
			entity = DBNovelEntity::class,
			parentColumns = ["id"],
			childColumns = ["novelId"],
			onDelete = ForeignKey.CASCADE
		)
	]
)
data class DBNovelPinEntity(
	@PrimaryKey
	val novelId: Int,
	val pinned: Boolean
) : Convertible<NovelPinEntity> {
	override fun convertTo(): NovelPinEntity = NovelPinEntity(novelId, pinned)
}
