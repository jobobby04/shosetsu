package app.shosetsu.android.domain.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import app.shosetsu.android.common.enums.DownloadStatus
import app.shosetsu.android.domain.model.local.DownloadEntity
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
	tableName = "downloads",
	foreignKeys = [
		ForeignKey(
			entity = DBChapterEntity::class,
			parentColumns = ["id"],
			childColumns = ["chapterID"],
			onDelete = ForeignKey.CASCADE
		),
		ForeignKey(
			entity = DBNovelEntity::class,
			parentColumns = ["id"],
			childColumns = ["novelID"],
			onDelete = ForeignKey.CASCADE
		)
	],
	indices = [
		Index("chapterID"),
		Index("novelID"),
	]
)
data class DBDownloadEntity(
	@PrimaryKey
	val chapterID: Int,
	val novelID: Int,
	val chapterURL: String,
	val chapterName: String,
	val novelName: String,
	@ColumnInfo(name = "formatterID")
	val extensionID: Int,
	var status: DownloadStatus = DownloadStatus.PENDING,
) : Convertible<DownloadEntity> {
	override fun convertTo(): DownloadEntity =
		DownloadEntity(
			chapterID,
			novelID,
			chapterURL,
			chapterName,
			novelName,
			extensionID,
			status
		)
}