package app.shosetsu.android.ui.novel

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.shosetsu.android.R

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
) {
	var showDropDown by remember { mutableStateOf(false) }

	Box {
		IconButton(
			onClick = {
				showDropDown = true
			}
		) {
			Icon(
				Icons.Default.MoreVert,
				stringResource(R.string.more)
			)
		}

		if (showTrueDelete)
			DropdownMenu(
				expanded = showDropDown,
				onDismissRequest = { showDropDown = false },
			) {
				DropdownMenuItem(
					text = {
						Text(stringResource(R.string.fragment_novel_true_delete))
					},
					onClick = onTrueDelete
				)
			}
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

	Box {
		IconButton(
			onClick = {
				showDropDown = true
			}
		) {
			Icon(
				painterResource(R.drawable.download),
				stringResource(R.string.download)
			)
		}

		DropdownMenu(showDropDown,
			onDismissRequest = { showDropDown = false }) {
			DropdownMenuItem(
				text = {
					Text(stringResource(R.string.download_next_chapter))
				},
				onClick = onDownloadNext
			)

			DropdownMenuItem(
				text = {
					Text(stringResource(R.string.download_next_5_chapters))
				},
				onClick = onDownloadNext5
			)

			DropdownMenuItem(
				text = {
					Text(stringResource(R.string.download_next_10_chapters))
				},
				onClick = onDownloadNext10
			)

			DropdownMenuItem(
				text = {
					Text(stringResource(R.string.download_custom_chapters))
				},
				onClick = onDownloadCustom
			)

			DropdownMenuItem(
				text = {
					Text(stringResource(R.string.unread))
				},
				onClick = onDownloadUnread
			)

			DropdownMenuItem(
				text = {
					Text(stringResource(R.string.all))
				},
				onClick = onDownloadAll
			)
		}
	}
}

@Composable
fun NovelMoreButton(
	onMigrate: () -> Unit,
	onJump: () -> Unit,
	onSetCategories: () -> Unit,
	canMigrate: Boolean,
	hasCategories: Boolean
) {
	var showDropDown by remember { mutableStateOf(false) }

	Box {
		IconButton(
			onClick = {
				showDropDown = true
			}
		) {
			Icon(
				Icons.Default.MoreVert,
				stringResource(R.string.more)
			)
		}

		DropdownMenu(showDropDown,
			onDismissRequest = { showDropDown = false }) {
			if (canMigrate)
				DropdownMenuItem(
					text = {
						Text(stringResource(R.string.migrate_source))
					},
					onClick = onMigrate
				)

			DropdownMenuItem(
				text = {
					Text(stringResource(R.string.jump_to_chapter))
				},
				onClick = onJump
			)

			if (hasCategories)
				DropdownMenuItem(
					text = {
						Text(stringResource(R.string.set_categories))
					},
					onClick = onSetCategories
				)
		}
	}
}
