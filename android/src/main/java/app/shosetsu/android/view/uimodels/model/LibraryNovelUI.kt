package app.shosetsu.android.view.uimodels.model

import androidx.compose.runtime.Immutable
import app.shosetsu.android.domain.model.local.LibraryNovelEntity
import app.shosetsu.android.dto.Convertible
import app.shosetsu.lib.Novel

@Immutable
data class LibraryNovelUI(
	val id: Int,
	val title: String,
	val imageURL: String,
	val bookmarked: Boolean,
	val unread: Int,
	val downloaded: Int,
	val pinned: Boolean,
	val genres: List<String>,
	val authors: List<String>,
	val artists: List<String>,
	val tags: List<String>,
	val status: Novel.Status,
	val category: Int,
	val lastUpdate: Long,
	val isSelected: Boolean = false,
) : Convertible<LibraryNovelEntity> {
	override fun convertTo(): LibraryNovelEntity =
		LibraryNovelEntity(
			id,
			title,
			imageURL,
			bookmarked,
			unread,
			downloaded,
			pinned,
			genres,
			authors,
			artists,
			tags,
			status,
			category,
			lastUpdate
		)
}