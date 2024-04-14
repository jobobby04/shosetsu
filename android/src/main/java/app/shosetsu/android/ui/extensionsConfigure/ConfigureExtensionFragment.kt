package app.shosetsu.android.ui.extensionsConfigure

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.enterAlwaysScrollBehavior
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.shosetsu.android.R
import app.shosetsu.android.common.enums.TriStateState
import app.shosetsu.android.common.ext.viewModelDi
import app.shosetsu.android.domain.model.local.FilterEntity
import app.shosetsu.android.ui.theme.ShosetsuTheme
import app.shosetsu.android.view.compose.ImageLoadingError
import app.shosetsu.android.view.compose.NavigateBackButton
import app.shosetsu.android.view.compose.setting.DropdownSettingContent
import app.shosetsu.android.view.compose.setting.StringSettingContent
import app.shosetsu.android.view.compose.setting.SwitchSettingContent
import app.shosetsu.android.view.uimodels.model.InstalledExtensionUI
import app.shosetsu.android.viewmodel.abstracted.AExtensionConfigureViewModel
import app.shosetsu.lib.ExtensionType
import app.shosetsu.lib.Novel
import app.shosetsu.lib.Version
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.google.accompanist.placeholder.material.placeholder
import kotlinx.collections.immutable.toImmutableList
import kotlin.random.Random

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
 * shosetsu
 * 21 / 01 / 2020
 *
 * Opens up detailed view of an extension, allows modifications
 */

@Composable
fun ConfigureExtensionView(
	extensionId: Int,
	viewModel: AExtensionConfigureViewModel = viewModelDi(),
	onExit: () -> Unit
) {
	LaunchedEffect(extensionId) {
		viewModel.setExtensionID(extensionId)
	}

	ShosetsuTheme {
		ConfigureExtensionContent(
			viewModel,
			onExit
		)
	}
}


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ConfigureExtensionContent(
	viewModel: AExtensionConfigureViewModel,
	onBack: () -> Unit
) {
	val extensionUIResult by viewModel.liveData.collectAsState()
	val extensionListingResult by viewModel.extensionListing.collectAsState()
	val extensionSettingsResult by viewModel.extensionSettings.collectAsState()

	Scaffold(
		topBar = {
			TopAppBar(
				title = {
					Text(stringResource(R.string.view_title_configure))
				},
				navigationIcon = {
					NavigateBackButton(onBack)
				},
				scrollBehavior = enterAlwaysScrollBehavior()
			)
		}
	) { paddingValues ->
		LazyColumn(
			verticalArrangement = Arrangement.spacedBy(8.dp),
			state = rememberLazyListState(),
			contentPadding = PaddingValues(bottom = 8.dp),
			modifier = Modifier.padding(paddingValues)
		) {
			stickyHeader(1000000) {
				if (extensionUIResult != null) {
					ConfigureExtensionHeaderContent(extensionUIResult!!) {
						viewModel.uninstall(extensionUIResult!!)
						onBack()
					}
				}
			}

			if (extensionListingResult != null && extensionListingResult!!.choices.size > 1) {
				item {
					DropdownSettingContent(
						title = stringResource(R.string.listings),
						description = stringResource(R.string.fragment_configure_extension_listing_desc),
						choices = extensionListingResult!!.choices,
						selection = extensionListingResult!!.selection.takeIf { it != -1 } ?: 0,
						onSelection = { index ->
							viewModel.setSelectedListing(index)
						},
						modifier = Modifier
							.fillMaxWidth()
							.padding(top = 8.dp, start = 16.dp, end = 16.dp)
					)
				}
			}

			SettingsItemAsCompose(this, viewModel, extensionSettingsResult)
		}
	}
}

@Suppress("FunctionName")
fun SettingsItemAsCompose(
	column: LazyListScope,
	viewModel: AExtensionConfigureViewModel,
	list: List<FilterEntity>
) {
	list.forEach { data ->
		when (data) {
			is FilterEntity.Header -> {
				column.item(Random.nextInt() + 1000000) {
					Row(
						modifier = Modifier.fillMaxWidth()
					) {
						Text(data.name)
						Divider()
					}
				}
			}

			is FilterEntity.Separator -> {
				column.item(Random.nextInt() + 1000000) {
					Divider()
				}
			}

			is FilterEntity.Text -> {
				column.item(data.id) {
					StringSettingContent(
						data.name,
						"",
						data.state,
						onValueChanged = { value ->
							viewModel.saveSetting(data.id, value)
						},
						modifier = Modifier.fillMaxWidth()
					)
				}
			}

			is FilterEntity.Switch -> {
				column.item(data.id) {
					SwitchSettingContent(
						data.name,
						"",
						isChecked = data.state,
						onCheckChange = { newValue ->
							viewModel.saveSetting(data.id, newValue)
						},
						modifier = Modifier.fillMaxWidth()
					)
				}
			}

			is FilterEntity.TriState -> {
				column.item(data.id) {
					Row(
						modifier = Modifier.fillMaxWidth(),
						horizontalArrangement = Arrangement.SpaceBetween
					) {
						Text(text = data.name)
						TriStateCheckbox(
							state = when (data.state) {
								TriStateState.CHECKED -> ToggleableState.On
								TriStateState.UNCHECKED -> ToggleableState.Indeterminate
								else -> ToggleableState.Off
							},
							onClick = {
								viewModel.saveSetting(data.id, data.state.cycle(false).name)
							}
						)
					}
				}
			}

			is FilterEntity.Dropdown -> {
				column.item(data.id) {
					DropdownSettingContent(
						title = data.name,
						description = "",
						choices = data.choices.toImmutableList(),
						selection = data.selected,
						onSelection = { index ->
							viewModel.saveSetting(data.id, index)
						},
						modifier = Modifier.fillMaxWidth()
					)
				}
			}

			is FilterEntity.FList -> {
				column.item(Random.nextInt() + 1000000) {
					Row(
						modifier = Modifier
							.fillMaxWidth()
					) {
						Text(data.name)
						Divider()
					}
				}
				SettingsItemAsCompose(column, viewModel, data.filters.toList())
			}

			is FilterEntity.Group -> {
				SettingsItemAsCompose(column, viewModel, data.filters.toList())
			}

			is FilterEntity.Checkbox -> {
				column.item(data.id) {
					SwitchSettingContent(
						data.name,
						"",
						isChecked = data.state,
						onCheckChange = { newValue ->
							viewModel.saveSetting(data.id, newValue)
						},
						modifier = Modifier.fillMaxWidth()
					)
				}
			}

			is FilterEntity.RadioGroup -> {
				column.item(data.id) {
					DropdownSettingContent(
						title = data.name,
						description = "",
						choices = data.choices.toImmutableList(),
						selection = data.selected,
						onSelection = { index ->
							viewModel.saveSetting(data.id, index)
						},
						modifier = Modifier.fillMaxWidth()
					)
				}
			}
		}
	}
}

@Preview
@Composable
fun PreviewConfigureExtensionHeaderContent() {
	ConfigureExtensionHeaderContent(
		InstalledExtensionUI(
			1,
			1,
			"This is an extension",
			"fileName",
			"",
			"en",
			version = Version(1, 0, 0),
			md5 = "",
			type = ExtensionType.LuaScript,
			enabled = true,
			chapterType = Novel.ChapterType.HTML
		)
	) {

	}
}

@Composable
fun ConfigureExtensionHeaderContent(
	extension: InstalledExtensionUI,
	onUninstall: () -> Unit
) {
	Card {
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically
			) {
				if (extension.imageURL.isNotEmpty()) {
					SubcomposeAsyncImage(
						ImageRequest.Builder(LocalContext.current)
							.data(extension.imageURL)
							.crossfade(true)
							.build(),
						contentDescription = stringResource(R.string.extension_image_desc),
						modifier = Modifier.size(100.dp),
						error = {
							ImageLoadingError()
						},
						loading = {
							Box(Modifier.placeholder(true))
						}
					)
				} else {
					Box(Modifier.size(100.dp), contentAlignment = Alignment.Center) {
						ImageLoadingError(
							Modifier
								.size(80.dp)
								.clip(MaterialTheme.shapes.extraSmall)
						)
					}
				}

				Column {
					Text(extension.name)
					Row(
						horizontalArrangement = Arrangement.SpaceBetween,
					) {
						Text(extension.id.toString())
						Text(extension.fileName, modifier = Modifier.padding(start = 16.dp))
					}
					Text(extension.displayLang)
				}
			}

			IconButton(
				onClick = onUninstall,
			) {
				Icon(
					painterResource(R.drawable.trash),
					stringResource(R.string.uninstall)
				)
			}
		}
	}
}