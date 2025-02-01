package app.shosetsu.android.ui.novel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import app.shosetsu.android.ui.library.InverseSelectionButton
import app.shosetsu.android.ui.library.SelectAllButton
import app.shosetsu.android.ui.library.SelectBetweenButton
import app.shosetsu.android.view.compose.NavigateBackButton

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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovelAppBar(
	onBack: () -> Unit,
	hasSelected: Boolean,

	onSelectAll: () -> Unit,
	onSelectBetween: () -> Unit,
	onInverseSelection: () -> Unit,

	showTrueDelete: Boolean,
	onTrueDelete: () -> Unit,

	canMigrate: Boolean,
	onMigrate: () -> Unit,
	onJump: () -> Unit,

	hasCategories: Boolean,
	onSetCategories: () -> Unit,


	onDownloadNext: () -> Unit,
	onDownloadNext5: () -> Unit,
	onDownloadNext10: () -> Unit,
	onDownloadCustom: () -> Unit,
	onDownloadUnread: () -> Unit,
	onDownloadAll: () -> Unit,
	onOpenShareMenu: () -> Unit
) {

	val behavior = TopAppBarDefaults.enterAlwaysScrollBehavior()


	TopAppBar(
		title = {},
		scrollBehavior = behavior,
		navigationIcon = {
			NavigateBackButton(onBack)
		},
		actions = {
			if (hasSelected) {
				SelectAllButton(onSelectAll)
				SelectBetweenButton(onSelectBetween)
				InverseSelectionButton(onInverseSelection)

				AnimatedVisibility(showTrueDelete) {
					NovelSelectedMoreButton(
						true,
						onTrueDelete
					)
				}
			} else {
				NovelShareButton(onOpenShareMenu)
				NovelDownloadButton(
					onDownloadNext = onDownloadNext,
					onDownloadNext5 = onDownloadNext5,
					onDownloadNext10 = onDownloadNext10,
					onDownloadCustom = onDownloadCustom,
					onDownloadUnread = onDownloadUnread,
					onDownloadAll = onDownloadAll
				)
				NovelMoreButton(
					canMigrate = canMigrate,
					onMigrate = onMigrate,
					onJump = onJump,
					hasCategories = hasCategories,
					onSetCategories = onSetCategories
				)
			}
		},
	)
}
