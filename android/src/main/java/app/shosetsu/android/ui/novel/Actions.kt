package app.shosetsu.android.ui.novel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import app.shosetsu.android.R
import app.shosetsu.android.view.compose.MoreIconButton
import app.shosetsu.android.view.compose.SimpleIconButton

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
 * @since 23 / 12 / 2023
 * @author Doomsdayrs
 */

@Composable
fun NovelSelectedMoreButton(
	showTrueDelete: Boolean,
	onTrueDelete: () -> Unit
) = MoreIconButton {
	if (showTrueDelete) {
		DropdownMenuItem(
			text = {
				Text(stringResource(R.string.fragment_novel_true_delete))
			},
			onClick = onTrueDelete
		)
	}
}

@Composable
fun NovelDownloadButton(
	onDownloadNext: () -> Unit,
	onDownloadNext5: () -> Unit,
	onDownloadNext10: () -> Unit,
	onDownloadCustom: () -> Unit,
	onDownloadUnread: () -> Unit,
	onDownloadAll: () -> Unit
) {
	var showDropDown by remember { mutableStateOf(false) }
	val onDismissRequest = { showDropDown = false }

	SimpleIconButton(
		Icons.Default.Download,
		stringResource(R.string.downloads),
		onClick = {
			showDropDown = true
		}
	)

	DropdownMenu(showDropDown, onDismissRequest = onDismissRequest) {
		DropdownMenuItem(
			text = {
				Text(stringResource(R.string.download_next_chapter))
			},
			onClick = {
				onDismissRequest()
				onDownloadNext()
			}
		)

		DropdownMenuItem(
			text = {
				Text(stringResource(R.string.download_next_5_chapters))
			},
			onClick = {
				onDismissRequest()
				onDownloadNext5()
			}
		)

		DropdownMenuItem(
			text = {
				Text(stringResource(R.string.download_next_10_chapters))
			},
			onClick = {
				onDismissRequest()
				onDownloadNext10()
			}
		)

		DropdownMenuItem(
			text = {
				Text(stringResource(R.string.download_custom_chapters))
			},
			onClick = {
				onDismissRequest()
				onDownloadCustom()
			}
		)

		DropdownMenuItem(
			text = {
				Text(stringResource(R.string.unread))
			},
			onClick = {
				onDismissRequest()
				onDownloadUnread()
			}
		)

		DropdownMenuItem(
			text = {
				Text(stringResource(R.string.all))
			},
			onClick = {
				onDismissRequest()
				onDownloadAll()
			}
		)
	}
}

@Composable
fun NovelMoreButton(
	onMigrate: () -> Unit,
	onJump: () -> Unit,
	onSetCategories: () -> Unit,
	canMigrate: Boolean,
	hasCategories: Boolean
) = MoreIconButton { onDismissRequest ->
	if (canMigrate)
		DropdownMenuItem(
			text = {
				Text(stringResource(R.string.migrate_source))
			},
			onClick = {
				onDismissRequest()
				onMigrate()
			}
		)

	DropdownMenuItem(
		text = {
			Text(stringResource(R.string.jump_to_chapter))
		},
		onClick = {
			onDismissRequest()
			onJump()
		}
	)

	if (hasCategories)
		DropdownMenuItem(
			text = {
				Text(stringResource(R.string.set_categories))
			},
			onClick = {
				onDismissRequest()
				onSetCategories()
			}
		)
}


@Composable
fun NovelShareButton(
	onShare: () -> Unit,
) {
	SimpleIconButton(
		Icons.Default.Share,
		stringResource(R.string.share),
		onClick = onShare
	)
}
