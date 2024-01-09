package app.shosetsu.android.domain.model.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

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
 * @since 11 / 11 / 2021
 * @author Doomsdayrs
 *
 */
@Entity(
	tableName = "chapter_history",
	foreignKeys = [
		ForeignKey(
			entity = DBNovelEntity::class,
			parentColumns = ["id"],
			childColumns = ["novelId"],
			onDelete = ForeignKey.CASCADE
		),
		ForeignKey(
			entity = DBChapterEntity::class,
			parentColumns = ["id"],
			childColumns = ["chapterId"],
			onDelete = ForeignKey.CASCADE
		)
	],
	indices = [
		Index(value = ["novelId", "chapterId"], unique = true),
		Index("chapterId")
	]
)
data class DBChapterHistoryEntity(
	@PrimaryKey(autoGenerate = true)
	val id: Int?,
	val novelId: Int,
	val chapterId: Int,
	val startedReadingAt: Long,
	val endedReadingAt: Long?
)