package app.shosetsu.android.view.uimodels.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.ImageBitmap

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
 * @since 16 / 09 / 2023
 * @author Doomsdayrs
 *
 * @param imageBitmap QR image of the data
 * @param data Content encoded in the image
 */
@Immutable
data class QRCodeData(
	val imageBitmap: ImageBitmap,
	val data: String
)