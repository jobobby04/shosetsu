package app.shosetsu.android.domain.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import app.shosetsu.android.common.enums.ReadingStatus
import app.shosetsu.android.domain.model.local.ChapterEntity
import app.shosetsu.android.dto.Convertible

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
 * 05 / 12 / 2020
 */
@Entity(
	tableName = "chapters",
	foreignKeys = [
		ForeignKey(
			entity = DBNovelEntity::class,
			parentColumns = ["id"],
			childColumns = ["novelID"],
			onDelete = ForeignKey.CASCADE
		)
	],
	indices = [
		Index("novelID"),
		Index(value = ["url", "formatterID"], unique = true),
	]
)
data class DBChapterEntity(
	@PrimaryKey(autoGenerate = true)
	var id: Int? = 0,

	var url: String,

	val novelID: Int,

	@ColumnInfo(name = "formatterID")
	val extensionID: Int,

	var title: String,

	var releaseDate: String,

	var order: Double,

	var readingPosition: Double = 0.0,

	var readingStatus: ReadingStatus = ReadingStatus.UNREAD,

	var bookmarked: Boolean = false,

	var isSaved: Boolean = false,
) : Convertible<ChapterEntity> {
	override fun convertTo(): ChapterEntity = ChapterEntity(
		id ?: 0,
		url,
		novelID,
		extensionID,
		title,
		releaseDate,
		order,
		readingPosition,
		readingStatus,
		bookmarked,
		isSaved
	)
}
