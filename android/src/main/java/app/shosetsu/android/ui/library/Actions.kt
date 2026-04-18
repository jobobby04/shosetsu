package app.shosetsu.android.ui.library

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FlipToBack
import androidx.compose.material.icons.outlined.SelectAll
import androidx.compose.material.icons.outlined.UnfoldLess
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import app.shosetsu.android.R
import app.shosetsu.android.common.enums.NovelCardType
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

/*
 * Shosetsu
 *
 * @since 19 / 12 / 2023
 * @author Doomsdayrs
 */

@Composable
fun InverseSelectionButton(
	onClick: () -> Unit
) {
	SimpleIconButton(
		Icons.Outlined.FlipToBack,
		stringResource(R.string.inverse_selection),
		onClick = onClick
	)
}

@Composable
fun SelectAllButton(
	onClick: () -> Unit
) {
	SimpleIconButton(
		Icons.Outlined.SelectAll,
		stringResource(R.string.select_all),
		onClick = onClick
	)
}

@Composable
fun RemoveAllButton(
	onClick: () -> Unit
) {
	SimpleIconButton(
		Icons.Default.Delete,
		stringResource(R.string.remove),
		onClick = onClick
	)
}

// Migrate is in more
// Toggle pin is in more
// Set categories is in more

@Composable
fun DeselectAllButton(
	onClick: () -> Unit
) {
	SimpleIconButton(
		Icons.Outlined.Close,
		stringResource(R.string.deselect_all),
		onClick = onClick
	)
}

@Composable
fun SelectBetweenButton(
	onClick: () -> Unit
) {
	SimpleIconButton(
		Icons.Outlined.UnfoldLess,
		stringResource(R.string.select_between),
		onClick = onClick
	)
}

@Composable
fun LibrarySelectedMoreButton(
	onMigrate: () -> Unit,
	onTogglePin: () -> Unit,
	onSetCategories: () -> Unit
) {
	Box {
		var showDropDown by remember { mutableStateOf(false) }
		SimpleIconButton(
			Icons.Default.MoreVert,
			stringResource(R.string.more),
			onClick = {
				showDropDown = !showDropDown
			}
		)
		DropdownMenu(
			showDropDown,
			onDismissRequest = {
				showDropDown = false
			}
		) {
			DropdownMenuItem(
				text = {
					Text(stringResource(R.string.migrate_sources))
				},
				onClick = onMigrate
			)

			DropdownMenuItem(
				text = {
					Text(stringResource(R.string.toggle_pin))
				},
				onClick = onTogglePin
			)
			DropdownMenuItem(
				text = {
					Text(stringResource(R.string.set_categories))
				},
				onClick = onSetCategories
			)
		}
	}
}

@Preview
@Composable
fun PreviewLibrarySearchAction() {
	Surface(
		Modifier.fillMaxSize()
	) {
		var query by remember { mutableStateOf("") }

		Row {
			SearchAction(
				query,
				onSearch = {
					query = it
				}
			)
		}
	}
}

// Normal View

@Composable
fun SearchAction(
	query: String,
	onSearch: (String) -> Unit,
	immediateSearch: Boolean = false,
	onSetExpanded: (Boolean) -> Unit = {},
	icon: ImageVector = Icons.Default.Search,
) {
	var expanded by remember { mutableStateOf(query.isNotEmpty()) }
	var searchQuery by remember { mutableStateOf(query) }
	val focusManager = LocalFocusManager.current
	val focusRequester = remember { FocusRequester() }
	LaunchedEffect(query) {
		if (query.isNotEmpty() && !expanded)
			expanded = true
	}

	BackHandler(enabled = expanded) {
		searchQuery = ""
		onSearch("")
		expanded = false
	}

	DisposableEffect(expanded) {
		onSetExpanded(expanded)
		onDispose { }
	}

	Row(
		verticalAlignment = Alignment.CenterVertically
	) {
		SimpleIconButton(
			if (expanded) Icons.Default.ArrowBack else icon,
			if (expanded) stringResource(android.R.string.cancel) else stringResource(android.R.string.search_go),
			onClick = {
				if (expanded) {
					searchQuery = ""
					onSearch("")
				}
				expanded = !expanded
			}
		)

		if (expanded) {
			LaunchedEffect(focusManager) {
				focusRequester.requestFocus()
			}
			BasicTextField(
				searchQuery,
				onValueChange = {
					searchQuery = it
					if (immediateSearch) {
						onSearch(it)
					}
				},
				modifier = Modifier
					.fillMaxWidth()
					.focusRequester(focusRequester),
				keyboardActions = KeyboardActions {
					onSearch(searchQuery)
					focusManager.clearFocus()
				},
				singleLine = true,
				textStyle = MaterialTheme.typography.bodyLarge.copy(
					color = MaterialTheme.colorScheme.onSurface
				),
				cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
			)
		}
	}
}

@Composable
fun ViewTypeItem(
	text: String,
	type: NovelCardType,
	selectedType: NovelCardType,
	onSetType: (NovelCardType) -> Unit
) {
	DropdownMenuItem(
		text = {
			Text(text)
		},
		onClick = {
			onSetType(type)
		},
		trailingIcon = {
			RadioButton(
				selectedType == type,
				onClick = {
					onSetType(type)
				}
			)
		}
	)
}

@Composable
fun ViewTypeButton(
	selectedType: NovelCardType,
	onSetType: (NovelCardType) -> Unit,
) {
	var showDropDown by remember { mutableStateOf(false) }
	SimpleIconButton(
		Icons.Default.ViewModule,
		stringResource(R.string.novel_card_type_selector_title),
		onClick = {
			showDropDown = !showDropDown
		}
	)
	DropdownMenu(
		showDropDown,
		onDismissRequest = {
			showDropDown = false
		}
	) {
		ViewTypeItem(
			stringResource(R.string.normal),
			NovelCardType.NORMAL,
			selectedType,
			onSetType
		)

		ViewTypeItem(
			stringResource(R.string.compressed),
			NovelCardType.COMPRESSED,
			selectedType,
			onSetType
		)

		ViewTypeItem(
			stringResource(R.string.cozy),
			NovelCardType.COZY,
			selectedType,
			onSetType
		)
	}
}

@Composable
fun RefreshButton(
	onRefresh: () -> Unit
) {
	SimpleIconButton(
		Icons.Default.Refresh,
		stringResource(R.string.update_now),
		onClick = onRefresh
	)
}