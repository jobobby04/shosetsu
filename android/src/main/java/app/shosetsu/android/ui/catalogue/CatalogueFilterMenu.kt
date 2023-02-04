@file:Suppress("UNCHECKED_CAST")

package app.shosetsu.android.ui.catalogue

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.shosetsu.android.R
import app.shosetsu.android.view.compose.ShosetsuCompose
import app.shosetsu.android.view.uimodels.StableHolder
import app.shosetsu.lib.Filter
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

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
 *
 * @since 02 / 08 / 2021
 */

@Preview
@Composable
fun CatalogFilterMenuPreview() = ShosetsuCompose {
	CatalogFilterMenu(
		listOf(
			Filter.Header("This is a header"),
			Filter.Separator,
			Filter.Text(1, "Text input"),
			Filter.Switch(2, "Switch"),
			Filter.Checkbox(3, "Checkbox"),
			Filter.TriState(4, "Tri state"),
			Filter.Dropdown(5, "Drop down", listOf("A", "B", "C")),
			Filter.RadioGroup(6, "Radio group", listOf("A", "B", "C")),
			Filter.FList(
				"List", listOf(
					Filter.Switch(7, "Switch"),
					Filter.Checkbox(8, "Checkbox"),
					Filter.TriState(9, "Tri state"),
				)
			),
			Filter.Group(
				"Group", listOf(
					Filter.Switch(10, "Switch"),
					Filter.Switch(11, "Switch"),
					Filter.Switch(12, "Switch"),
				)
			)
		).map { StableHolder(it) }.toImmutableList() as ImmutableList<StableHolder<Filter<*>>>,
		getBoolean = { MutableStateFlow(false) },
		setBoolean = { _, _ -> },
		getInt = { MutableStateFlow(1) },
		setInt = { _, _ -> },
		getString = { MutableStateFlow("") },
		setString = { _, _ -> },
		applyFilter = {},
		resetFilter = {}
	)
}

@Composable
fun CatalogFilterMenu(
	items: ImmutableList<StableHolder<Filter<*>>>,
	getBoolean: (Filter<Boolean>) -> Flow<Boolean>,
	setBoolean: (Filter<Boolean>, Boolean) -> Unit,
	getInt: (Filter<Int>) -> Flow<Int>,
	setInt: (Filter<Int>, Int) -> Unit,
	getString: (Filter<String>) -> Flow<String>,
	setString: (Filter<String>, String) -> Unit,
	applyFilter: () -> Unit,
	resetFilter: () -> Unit
) {
	Column(
		modifier = Modifier,
		verticalArrangement = Arrangement.Bottom
	) {
		CatalogFilterMenuControlContent(resetFilter, applyFilter)

		CatalogFilterMenuFilterListContent(
			items,
			getBoolean,
			setBoolean,
			getInt,
			setInt,
			getString,
			setString
		)

	}
}

@Composable
fun CatalogFilterMenuFilterListContent(
	list: ImmutableList<StableHolder<Filter<*>>>,
	getBoolean: (Filter<Boolean>) -> Flow<Boolean>,
	setBoolean: (Filter<Boolean>, Boolean) -> Unit,
	getInt: (Filter<Int>) -> Flow<Int>,
	setInt: (Filter<Int>, Int) -> Unit,
	getString: (Filter<String>) -> Flow<String>,
	setString: (Filter<String>, String) -> Unit
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.verticalScroll(rememberScrollState()),
		verticalArrangement = Arrangement.Bottom
	) {
		Spacer(Modifier.height(16.dp))
		list.forEach { filter ->
			when (filter.item) {
				is Filter.Header -> Column {
					Divider()
				}
				is Filter.Separator -> Divider()
				is Filter.Password -> CatalogFilterMenuTextContent(
					filter as StableHolder<Filter.Text>,
					getString,
					setString
				)
				is Filter.Text -> CatalogFilterMenuTextContent(
					filter as StableHolder<Filter.Text>,
					getString,
					setString
				)
				is Filter.Switch -> CatalogFilterMenuSwitchContent(
					filter as StableHolder<Filter.Switch>,
					getBoolean,
					setBoolean
				)
				is Filter.Checkbox ->
					CatalogFilterMenuCheckboxContent(
						filter as StableHolder<Filter.Checkbox>,
						getBoolean,
						setBoolean
					)
				is Filter.TriState -> CatalogFilterMenuTriStateContent(
					filter as StableHolder<Filter.TriState>,
					getInt,
					setInt
				)
				is Filter.Dropdown -> CatalogFilterMenuDropDownContent(
					filter as StableHolder<Filter.Dropdown>,
					getInt,
					setInt
				)
				is Filter.RadioGroup -> CatalogFilterMenuRadioGroupContent(
					filter as StableHolder<Filter.RadioGroup>,
					getInt,
					setInt
				)
				is Filter.FList -> {
					CatalogFilterMenuFilterListContent(
						remember {
							filter.item.filters.toList().map { StableHolder(it) }
								.toImmutableList()
						},
						filter.item.name,
						getBoolean, setBoolean, getInt, setInt, getString, setString
					)
				}
				is Filter.Group<*> -> {
					CatalogFilterMenuFilterListContent(
						remember {
							filter.item.filters.toList().map { StableHolder(it) }
								.toImmutableList()
						},
						filter.item.name,
						getBoolean, setBoolean, getInt, setInt, getString, setString
					)
				}
			}
		}
		Spacer(Modifier.height(16.dp))
	}
}

@Preview
@Composable
fun PreviewCatalogFilterMenuFilterListContent() = ShosetsuCompose {
	CatalogFilterMenuFilterListContent(
		list = listOf(
			Filter.Switch(7, "Switch"),
			Filter.Checkbox(8, "Checkbox"),
			Filter.TriState(9, "Tri state"),
		).map { StableHolder(it) }.toImmutableList() as ImmutableList<StableHolder<Filter<*>>>,
		name = "A list",
		getBoolean = { MutableStateFlow(false) },
		setBoolean = { _, _ -> },
		getInt = { MutableStateFlow(1) },
		setInt = { _, _ -> },
		getString = { MutableStateFlow("") },
		setString = { _, _ -> }
	)
}

@Composable
fun CatalogFilterMenuFilterListContent(
	list: ImmutableList<StableHolder<Filter<*>>>,
	name: String,
	getBoolean: (Filter<Boolean>) -> Flow<Boolean>,
	setBoolean: (Filter<Boolean>, Boolean) -> Unit,
	getInt: (Filter<Int>) -> Flow<Int>,
	setInt: (Filter<Int>, Int) -> Unit,
	getString: (Filter<String>) -> Flow<String>,
	setString: (Filter<String>, String) -> Unit
) {
	var collapsed by remember { mutableStateOf(true) }
	Column(
		modifier = Modifier.fillMaxWidth()
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.height(56.dp)
				.clickable(onClick = { collapsed = !collapsed })
				.padding(horizontal = 16.dp),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			Text(text = name)
			IconToggleButton(
				onCheckedChange = {
					collapsed = it
				},
				checked = collapsed
			) {
				if (collapsed)
					Icon(painterResource(R.drawable.expand_more), "")
				else
					Icon(painterResource(R.drawable.expand_less), "")
			}
		}

		AnimatedVisibility(!collapsed) {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 16.dp)
			) {
				list.forEach { filter ->
					when (filter.item) {
						is Filter.Header -> Column {
							Divider()
						}
						is Filter.Separator -> Divider()
						is Filter.Password -> CatalogFilterMenuTextContent(
							filter as StableHolder<Filter.Text>,
							getString,
							setString
						)
						is Filter.Text -> CatalogFilterMenuTextContent(
							filter as StableHolder<Filter.Text>,
							getString,
							setString
						)
						is Filter.Switch -> CatalogFilterMenuSwitchContent(
							filter as StableHolder<Filter.Switch>,
							getBoolean,
							setBoolean
						)
						is Filter.Checkbox -> CatalogFilterMenuCheckboxContent(
							filter as StableHolder<Filter.Checkbox>,
							getBoolean,
							setBoolean
						)
						is Filter.TriState -> CatalogFilterMenuTriStateContent(
							filter as StableHolder<Filter.TriState>,
							getInt,
							setInt
						)
						is Filter.Dropdown -> CatalogFilterMenuDropDownContent(
							filter as StableHolder<Filter.Dropdown>,
							getInt,
							setInt
						)
						is Filter.RadioGroup -> CatalogFilterMenuRadioGroupContent(
							filter as StableHolder<Filter.RadioGroup>,
							getInt,
							setInt
						)
						is Filter.FList -> {
							Log.e(
								"FilterListContent",
								"CatalogFilterMenuFilterListContent: Please avoid usage of lists in sub lists"
							)
							CatalogFilterMenuFilterListContent(
								remember {
									filter.item.filters.toList().map { StableHolder(it) }
										.toImmutableList()
								},
								filter.item.name,
								getBoolean,
								setBoolean,
								getInt,
								setInt,
								getString,
								setString
							)
						}
						is Filter.Group<*> -> {
							Log.e(
								"FilterListContent",
								"CatalogFilterMenuFilterListContent: Please avoid usage of lists in sub lists"
							)
							CatalogFilterMenuFilterListContent(
								remember {
									filter.item.filters.toList().map { StableHolder(it) }
										.toImmutableList()
								},
								filter.item.name,
								getBoolean,
								setBoolean,
								getInt,
								setInt,
								getString,
								setString
							)
						}
					}
				}

			}
		}
	}
}

@Preview
@Composable
fun PreviewCatalogFilterMenuTextContent() =
	ShosetsuCompose {
		CatalogFilterMenuTextContent(
			filterHolder = StableHolder(Filter.Text(0, "This is a text input")),
			{ MutableStateFlow("") },
			{ _, _ -> }
		)
	}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogFilterMenuTextContent(
	filterHolder: StableHolder<Filter.Text>,
	getString: (Filter<String>) -> Flow<String>,
	setString: (Filter<String>, String) -> Unit
) {
	val filter = filterHolder.item
	val text by getString(filter)
		.collectAsState(initial = "")

	OutlinedTextField(
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 16.dp)
			.padding(bottom = 8.dp),
		value = text,
		onValueChange = { setString(filter, it) },
		label = {
			Text(text = filter.name)
		}
	)
}

@Preview
@Composable
fun PreviewCatalogFilterMenuSwitchContent() = ShosetsuCompose {
	CatalogFilterMenuSwitchContent(
		filterHolder = StableHolder(Filter.Switch(0, "Switch")),
		{ MutableStateFlow(false) },
		{ _, _ -> }
	)
}

@Composable
fun CatalogFilterMenuSwitchContent(
	filterHolder: StableHolder<Filter.Switch>,
	getBoolean: (Filter<Boolean>) -> Flow<Boolean>,
	setBoolean: (Filter<Boolean>, Boolean) -> Unit
) {
	val filter = filterHolder.item
	val state by getBoolean(filter)
		.collectAsState(initial = false)

	Row(
		modifier = Modifier
			.fillMaxWidth()
			.height(56.dp)
			.clickable(onClick = { setBoolean(filter, !state) })
			.padding(horizontal = 16.dp),
		horizontalArrangement = Arrangement.SpaceBetween,
		verticalAlignment = Alignment.CenterVertically
	) {
		Text(text = filter.name)
		Switch(
			checked = state,
			onCheckedChange = null
		)
	}
}

@Preview
@Composable
fun PreviewCatalogFilterMenuCheckboxContent() = ShosetsuCompose {
	CatalogFilterMenuCheckboxContent(filterHolder = StableHolder(Filter.Checkbox(0, "Checkbox")),
		{ MutableStateFlow(false) },
		{ _, _ -> })
}

@Composable
fun CatalogFilterMenuCheckboxContent(
	filterHolder: StableHolder<Filter.Checkbox>,
	getBoolean: (Filter<Boolean>) -> Flow<Boolean>,
	setBoolean: (Filter<Boolean>, Boolean) -> Unit
) {
	val filter = filterHolder.item
	val state by getBoolean(filter)
		.collectAsState(initial = false)

	Row(
		modifier = Modifier
			.fillMaxWidth()
			.height(56.dp)
			.clickable(onClick = { setBoolean(filter, !state) })
			.padding(horizontal = 16.dp),
		horizontalArrangement = Arrangement.SpaceBetween,
		verticalAlignment = Alignment.CenterVertically
	) {
		Text(text = filter.name)
		Checkbox(
			checked = state,
			onCheckedChange = null
		)
	}
}

@Preview
@Composable
fun PreviewCatalogFilterMenuTriStateContent() = ShosetsuCompose {
	CatalogFilterMenuTriStateContent(filterHolder = StableHolder(Filter.TriState(0, "Tristate")),
		{ MutableStateFlow(1) },
		{ _, _ -> })
}

@Composable
fun CatalogFilterMenuTriStateContent(
	filterHolder: StableHolder<Filter.TriState>,
	getInt: (Filter<Int>) -> Flow<Int>,
	setInt: (Filter<Int>, Int) -> Unit
) {
	val filter = filterHolder.item
	val triState by getInt(filter)
		.collectAsState(initial = Filter.TriState.STATE_IGNORED)

	val convertedState = when (triState) {
		Filter.TriState.STATE_IGNORED -> ToggleableState.Off
		Filter.TriState.STATE_EXCLUDE -> ToggleableState.Indeterminate
		Filter.TriState.STATE_INCLUDE -> ToggleableState.On
		else -> ToggleableState.Off
	}

	Row(
		modifier = Modifier
			.fillMaxWidth()
			.height(56.dp)
			.clickable(onClick = {
				setInt(
					filter,
					when (triState) {
						Filter.TriState.STATE_IGNORED -> Filter.TriState.STATE_INCLUDE
						Filter.TriState.STATE_INCLUDE -> Filter.TriState.STATE_EXCLUDE
						Filter.TriState.STATE_EXCLUDE -> Filter.TriState.STATE_IGNORED
						else -> Filter.TriState.STATE_IGNORED
					}
				)
			})
			.padding(horizontal = 16.dp),
		horizontalArrangement = Arrangement.SpaceBetween,
		verticalAlignment = Alignment.CenterVertically
	) {
		Text(text = filter.name)
		TriStateCheckbox(
			state = convertedState,
			onClick = null
		)
	}
}

@Preview
@Composable
fun PreviewCatalogFilterMenuDropDownContent() = ShosetsuCompose {
	CatalogFilterMenuDropDownContent(
		filterHolder = StableHolder(Filter.Dropdown(0, "Dropdown", listOf("A", "B", "C"))),
		{ MutableStateFlow(1) },
		{ _, _ -> }
	)
}

@Composable
fun CatalogFilterMenuDropDownContent(
	filterHolder: StableHolder<Filter.Dropdown>,
	getInt: (Filter<Int>) -> Flow<Int>,
	setInt: (Filter<Int>, Int) -> Unit
) {
	val filter = filterHolder.item
	val selection by getInt(filter)
		.collectAsState(initial = 0)
	var expanded by remember { mutableStateOf(false) }

	Row(
		modifier = Modifier
			.fillMaxWidth()
			.height(56.dp)
			.clickable(onClick = { expanded = true })
			.padding(horizontal = 16.dp),
		horizontalArrangement = Arrangement.SpaceBetween,
		verticalAlignment = Alignment.CenterVertically
	) {
		Text(text = filter.name)


		Row(
			modifier = Modifier.fillMaxHeight(),
			verticalAlignment = Alignment.CenterVertically,
		) {
			Text(
				text = AnnotatedString(filter.choices[selection]),
			)
			IconToggleButton(
				onCheckedChange = {
					expanded = it
				},
				checked = expanded,
				modifier = Modifier.wrapContentWidth()
			) {

				if (expanded)
					Icon(painterResource(R.drawable.expand_less), "")
				else
					Icon(painterResource(R.drawable.expand_more), "")
			}
			DropdownMenu(
				expanded = expanded,
				onDismissRequest = { expanded = false },
			) {
				filter.choices.forEachIndexed { i, s ->
					DropdownMenuItem(
						onClick = {
							setInt(filter, i)
							expanded = false
						},
						text = {
							Text(text = AnnotatedString(s))
						}
					)
				}
			}
		}
	}
}

@Preview
@Composable
fun PreviewCatalogFilterMenuRadioGroupContent() = ShosetsuCompose {
	CatalogFilterMenuRadioGroupContent(
		filterHolder = StableHolder(Filter.RadioGroup(0, "Dropdown", listOf("A", "B", "C"))),
		{ MutableStateFlow(1) },
		{ _, _ -> }
	)
}

@Composable
fun CatalogFilterMenuRadioGroupContent(
	filterHolder: StableHolder<Filter.RadioGroup>,
	getInt: (Filter<Int>) -> Flow<Int>,
	setInt: (Filter<Int>, Int) -> Unit
) {
	val filter = filterHolder.item
	val selection by getInt(filter)
		.collectAsState(initial = 0)
	var expanded by remember { mutableStateOf(true) }

	Column(
		modifier = Modifier.fillMaxWidth(),
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.height(56.dp)
				.clickable(onClick = { expanded = !expanded })
				.padding(horizontal = 16.dp),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			Text(text = filter.name)

			IconToggleButton(
				onCheckedChange = {
					expanded = it
				},
				checked = expanded
			) {
				if (expanded)
					Icon(painterResource(R.drawable.expand_less), "")
				else
					Icon(painterResource(R.drawable.expand_more), "")
			}
		}

		AnimatedVisibility(expanded) {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.padding(start = 8.dp, end = 8.dp),
			) {
				filter.choices.forEachIndexed { index, s ->
					Row(
						modifier = Modifier
							.fillMaxWidth()
							.height(56.dp)
							.clickable(onClick = { setInt(filter, index) }),
						horizontalArrangement = Arrangement.SpaceBetween,
						verticalAlignment = Alignment.CenterVertically
					) {
						Text(text = s)
						RadioButton(
							selected = index == selection,
							onClick = null
						)
					}
				}
			}
		}
	}
}

@Composable
fun CatalogFilterMenuControlContent(
	resetFilter: () -> Unit,
	applyFilter: () -> Unit
) {
	Surface(
		modifier = Modifier
			.fillMaxWidth(),
	) {
		Row(
			horizontalArrangement = Arrangement.SpaceEvenly,
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier.fillMaxWidth()
		) {
			TextButton(onClick = resetFilter, contentPadding = PaddingValues(8.dp)) {
				Text(text = stringResource(id = R.string.reset))
			}

			TextButton(onClick = applyFilter, contentPadding = PaddingValues(8.dp)) {
				Text(text = stringResource(id = R.string.apply))
			}
		}
	}
}
