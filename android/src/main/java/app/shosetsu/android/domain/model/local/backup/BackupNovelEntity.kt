package app.shosetsu.android.domain.model.local.backup

import app.shosetsu.lib.Novel
import kotlinx.serialization.Serializable

@Serializable
data class BackupNovelEntity(
	val url: String,
	val bookmarked: Boolean = true,
	val loaded: Boolean = false,
	val name: String,
	val imageURL: String = "",
	val description: String = "",
	val language: String = "",
	val genres: List<String> = emptyList(),
	val authors: List<String> = emptyList(),
	val artists: List<String> = emptyList(),
	val tags: List<String> = emptyList(),
	val status: Novel.Status = Novel.Status.UNKNOWN,
	val chapters: List<BackupChapterEntity> = emptyList(),
	val settings: BackupNovelSettingEntity = BackupNovelSettingEntity(),
	val categories: List<Int> = emptyList(),
	val pinned: Boolean = false
)