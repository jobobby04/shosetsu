package app.shosetsu.android.ui.novel

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import app.shosetsu.android.R
import app.shosetsu.android.ui.theme.ShosetsuTheme
import app.shosetsu.android.view.compose.StandardDialog
import com.chargemap.compose.numberpicker.NumberPicker

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
 * @since 22 / 12 / 2023
 * @author Doomsdayrs
 */
@Composable
fun NovelCustomDownloadDialog(
	onDismissRequest: () -> Unit,
	chapterCount: Int,
	onDownload: (Int) -> Unit
) {
	var value by remember { mutableIntStateOf(0) }
	StandardDialog(
		onDismissRequest = onDismissRequest,
		title = {
			Text(stringResource(R.string.download_custom_chapters))
		},
		onConfirm = {
			onDownload(value)
		}
	) {
		Box {
			NumberPicker(
				value = value,
				onValueChange = {
					value = it
				},
				range = 0 until chapterCount,
			)
		}
	}
}

@Preview
@Composable
fun PreviewNovelCustomDownloadDialog() {
	ShosetsuTheme {
		NovelCustomDownloadDialog(
			onDismissRequest = {},
			chapterCount = 10,
			onDownload = {}
		)
	}
}