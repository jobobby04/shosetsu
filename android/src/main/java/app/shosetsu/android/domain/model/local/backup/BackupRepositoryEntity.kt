package app.shosetsu.android.domain.model.local.backup

import kotlinx.serialization.Serializable

@Serializable
data class BackupRepositoryEntity(
	val id: Int? = null,
	val url: String,
	val name: String,
)