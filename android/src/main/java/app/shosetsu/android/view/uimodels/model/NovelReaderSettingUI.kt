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
 *
 */

package app.shosetsu.android.view.uimodels.model

import androidx.compose.runtime.Immutable
import app.shosetsu.android.domain.model.local.NovelReaderSettingEntity
import app.shosetsu.android.dto.Convertible

/**
 * shosetsu
 * 14 / 09 / 2022
 *
 * @see NovelReaderSettingEntity
 */
@Immutable
data class NovelReaderSettingUI(
	val novelID: Int,
	val paragraphIndentSize: Int,
	val paragraphSpacingSize: Float,
) : Convertible<NovelReaderSettingEntity> {
	override fun convertTo(): NovelReaderSettingEntity = NovelReaderSettingEntity(
		novelID,
		paragraphIndentSize,
		paragraphSpacingSize
	)
}
