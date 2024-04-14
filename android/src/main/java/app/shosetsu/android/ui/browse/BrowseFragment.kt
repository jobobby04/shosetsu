package app.shosetsu.android.ui.browse

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

import android.content.Intent
import android.provider.Settings
import androidx.annotation.StringRes
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.enterAlwaysScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import app.shosetsu.android.R
import app.shosetsu.android.common.OfflineException
import app.shosetsu.android.common.consts.BROWSE_HELP_URL
import app.shosetsu.android.common.ext.viewModelDi
import app.shosetsu.android.domain.model.local.ExtensionInstallOptionEntity
import app.shosetsu.android.ui.library.SearchAction
import app.shosetsu.android.ui.theme.ShosetsuTheme
import app.shosetsu.android.view.BottomSheetDialog
import app.shosetsu.android.view.compose.ErrorAction
import app.shosetsu.android.view.compose.ErrorContent
import app.shosetsu.android.view.compose.HelpButton
import app.shosetsu.android.view.compose.ImageLoadingError
import app.shosetsu.android.view.compose.rememberFakePullRefreshState
import app.shosetsu.android.view.uimodels.model.BrowseExtensionUI
import app.shosetsu.android.viewmodel.abstracted.ABrowseViewModel
import app.shosetsu.lib.Version
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.google.accompanist.placeholder.material.placeholder
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch

/**
 * shosetsu
 * 18 / 01 / 2020
 *
 * @author github.com/doomsdayrs
 */

@Composable
fun BrowseView(
	openCatalogue: (extensionId: Int) -> Unit,
	openSettings: (extensionId: Int) -> Unit,
	openRepositories: () -> Unit,
	openSearch: () -> Unit,
	drawerIcon: @Composable () -> Unit
) {
	val viewModel: ABrowseViewModel = viewModelDi()

	ShosetsuTheme {
		val query by viewModel.searchTermLive.collectAsState()
		val entities by viewModel.liveData.collectAsState()
		val isOnline by viewModel.isOnline.collectAsState(false)
		val error by viewModel.error.collectAsState(null)
		val isFilterMenuVisible by viewModel.isFilterMenuVisible.collectAsState()

		val hostState = remember { SnackbarHostState() }
		val context = LocalContext.current
		val scope = rememberCoroutineScope()

		suspend fun offlineMessage(@StringRes message: Int) {
			val result = hostState.showSnackbar(
				context.getString(message),
				duration = SnackbarDuration.Long,
				actionLabel = context.getString(R.string.generic_wifi_settings)
			)
			if (result == SnackbarResult.ActionPerformed) {
				context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
			}
		}

		LaunchedEffect(error) {
			if (error != null) {
				when (error) {
					is OfflineException -> {
						offlineMessage((error as OfflineException).messageRes)
					}

					else -> {
						hostState.showSnackbar(
							error?.message ?: context.getString(R.string.error)
						)
					}
				}
			}
		}

		BrowseContent(
			entities = entities,
			refresh = viewModel::refresh,
			openRepositories = openRepositories,
			installExtension = { extension, option ->
				viewModel.installExtension(extension, option)
			},
			update = viewModel::updateExtension,
			openCatalogue = {
				if (isOnline) {
					if (it.isInstalled) {
						viewModel.resetSearch()
						openCatalogue(it.id)
					} else {
						scope.launch {
							hostState.showSnackbar(
								context.getString(R.string.fragment_browse_snackbar_not_installed)
							)
						}
					}
				} else {
					scope.launch {
						offlineMessage(R.string.fragment_browse_snackbar_offline_no_extension)
					}
				}
			},
			openSettings = {
				viewModel.resetSearch()
				openSettings(it.id)
			},
			cancelInstall = viewModel::cancelInstall,
			hostState = hostState,
			onOpenFilter = viewModel::showFilterMenu,
			onOpenSearch = openSearch,
			query = query,
			onSetQuery = viewModel::setSearch,
			drawerIcon = drawerIcon
		)

		if (isFilterMenuVisible) {
			BottomSheetDialog(viewModel::hideFilterMenu) {
				BrowseControllerFilterMenu(viewModel)
			}
		}
	}
}

@Preview
@Composable
fun PreviewBrowseContent() {
	BrowseContent(
		query = "",
		onSetQuery = {},
		entities =
		List(10) {
			BrowseExtensionUI(
				it,
				"Fake a b c",
				"",
				"en",
				installOptions = null,
				isInstalled = true,
				installedVersion = Version(1, 1, 1),
				installedRepo = 1,
				isUpdateAvailable = false,
				updateVersion = Version(1, 2, 1),
				isInstalling = false
			)
		}.toImmutableList(),
		{},
		{},
		{ _, _ -> },
		{},
		{},
		{},
		{},
		hostState = remember { SnackbarHostState() },
		onOpenFilter = {},
		onOpenSearch = {},
		drawerIcon = {}
	)
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BrowseContent(
	query: String,
	onSetQuery: (String) -> Unit,
	entities: ImmutableList<BrowseExtensionUI>?,
	refresh: () -> Unit,
	openRepositories: () -> Unit,
	installExtension: (BrowseExtensionUI, ExtensionInstallOptionEntity) -> Unit,
	update: (BrowseExtensionUI) -> Unit,
	openCatalogue: (BrowseExtensionUI) -> Unit,
	openSettings: (BrowseExtensionUI) -> Unit,
	cancelInstall: (BrowseExtensionUI) -> Unit,
	hostState: SnackbarHostState,
	onOpenFilter: () -> Unit,
	onOpenSearch: () -> Unit,
	drawerIcon: @Composable () -> Unit
) {
	val (isRefreshing, pullRefreshState) = rememberFakePullRefreshState(refresh)

	Scaffold(
		topBar = {
			TopAppBar(
				title = {
					Text(stringResource(R.string.browse))
				},
				scrollBehavior = enterAlwaysScrollBehavior(),
				actions = {
					SearchAction(
						query = query,
						onSearch = onSetQuery,
						icon = {
							Icon(
								painterResource(R.drawable.baseline_manage_search_24),
								stringResource(R.string.search)
							)
						}
					)
					IconButton(onOpenSearch) {
						Icon(Icons.Default.Search, stringResource(R.string.global_search))
					}
					HelpButton(BROWSE_HELP_URL)
				},
				navigationIcon = drawerIcon
			)
		},
		snackbarHost = {
			SnackbarHost(hostState)
		},
		floatingActionButton = {
			ExtendedFloatingActionButton(
				text = {
					Text(stringResource(R.string.filter))
				},
				icon = {
					Icon(painterResource(R.drawable.filter), stringResource(R.string.filter))
				},
				onClick = onOpenFilter
			)
		},
	) { padding ->
		Box(
			Modifier
				.pullRefresh(pullRefreshState)
				.padding(padding)
		) {
			if (!entities.isNullOrEmpty()) {
				val state = rememberLazyListState()
				LazyColumn(
					modifier = Modifier.fillMaxSize(),
					contentPadding = PaddingValues(
						bottom = 198.dp,
						top = 4.dp,
						start = 8.dp,
						end = 8.dp
					),
					state = state,
					verticalArrangement = Arrangement.spacedBy(4.dp)
				) {
					items(entities) { entity ->
						BrowseExtensionContent(
							entity,
							install = {
								installExtension(entity, it)
							},
							update = {
								update(entity)
							},
							openCatalogue = {
								openCatalogue(entity)
							},
							openSettings = {
								openSettings(entity)
							},
							cancelInstall = {
								cancelInstall(entity)
							}
						)
					}
				}
			} else {
				ErrorContent(
					R.string.empty_browse_message,
					actions = arrayOf(
						ErrorAction(R.string.empty_browse_refresh_action) {
							refresh()
						},
						ErrorAction(R.string.repositories) {
							openRepositories()
						}
					)
				)
			}

			PullRefreshIndicator(
				isRefreshing,
				pullRefreshState,
				Modifier.align(Alignment.TopCenter)
			)
		}
	}
}

@Preview
@Composable
fun PreviewBrowseExtensionContent() {
	BrowseExtensionContent(
		BrowseExtensionUI(
			1,
			"Fake a  aaaaaaaaaaaaaaaaa",
			"",
			"en",
			installOptions = listOf(
				ExtensionInstallOptionEntity(1, "Wowa", Version(1, 1, 1))
			),
			isInstalled = true,
			installedVersion = Version(1, 1, 1),
			installedRepo = 1,
			isUpdateAvailable = true,
			updateVersion = Version(1, 2, 1),
			isInstalling = false
		),
		{},
		{},
		{},
		{},
		{}
	)
}

@OptIn(
	ExperimentalMaterial3Api::class,
	androidx.compose.foundation.ExperimentalFoundationApi::class,
	androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi::class,
	androidx.compose.ui.unit.ExperimentalUnitApi::class
)
@Composable
fun BrowseExtensionContent(
	item: BrowseExtensionUI,
	install: (ExtensionInstallOptionEntity) -> Unit,
	update: () -> Unit,
	openCatalogue: () -> Unit,
	openSettings: () -> Unit,
	cancelInstall: () -> Unit
) {
	Card(
		onClick = openCatalogue,
		shape = RoundedCornerShape(16.dp)
	) {
		Column {
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(end = 8.dp),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically
			) {
				Row(
					verticalAlignment = Alignment.CenterVertically,
				) {
					if (item.imageURL.isNotEmpty()) {
						SubcomposeAsyncImage(
							ImageRequest.Builder(LocalContext.current)
								.data(item.imageURL)
								.crossfade(true)
								.build(),
							contentDescription = stringResource(R.string.fragment_browse_ext_icon_desc),
							modifier = Modifier.size(64.dp),
							error = {
								Box(Modifier.size(64.dp), contentAlignment = Alignment.Center) {
									ImageLoadingError(
										Modifier
											.size(52.dp)
											.clip(MaterialTheme.shapes.extraSmall)
									)
								}
							},
							loading = {
								Box(Modifier.placeholder(true))
							}
						)
					} else {
						Box(Modifier.size(64.dp), contentAlignment = Alignment.Center) {
							ImageLoadingError(
								Modifier
									.size(52.dp)
									.clip(MaterialTheme.shapes.extraSmall)
							)
						}
					}
					Column(
						modifier = Modifier.padding(start = 8.dp)
					) {
						Text(item.name)
						Row {
							Text(item.displayLang, fontSize = TextUnit(14f, TextUnitType.Sp))

							if (item.isInstalled && item.installedVersion != null)
								Text(
									item.installedVersion.toString(),
									modifier = Modifier.padding(start = 8.dp),
									fontSize = TextUnit(14f, TextUnitType.Sp)
								)

							if (item.isUpdateAvailable && item.updateVersion != null) {
								if (item.updateVersion != Version(-9, -9, -9))
									Text(
										stringResource(
											R.string.update_to,
											item.updateVersion.toString()
										),
										modifier = Modifier.padding(start = 8.dp),
										fontSize = TextUnit(14f, TextUnitType.Sp),
										color = MaterialTheme.colorScheme.tertiary
									)
							}
						}
					}
				}
				Row(
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.End
				) {
					if (!item.isInstalled && !item.isInstalling && !item.installOptions.isNullOrEmpty()) {
						var isDropdownVisible by remember { mutableStateOf(false) }
						IconButton(
							onClick = {
								// We can skip to dropdown if there is only 1 install option
								if (item.installOptions.size != 1)
									isDropdownVisible = true
								else install(item.installOptions[0])
							}
						) {
							Icon(painterResource(R.drawable.download), null)
						}
						DropdownMenu(
							expanded = isDropdownVisible,
							onDismissRequest = { isDropdownVisible = false },
						) {
							item.installOptions.forEach { s ->
								DropdownMenuItem(
									onClick = {
										install(s)
										isDropdownVisible = false
									},
									text = {
										Column {
											Text(
												text = AnnotatedString(s.repoName)
											)
											Text(
												text = AnnotatedString(s.version.toString()),
												modifier = Modifier.padding(start = 8.dp)
											)
										}
									}
								)
							}
						}
					}

					if (item.isUpdateAvailable) {
						IconButton(
							onClick = update
						) {
							Icon(
								painterResource(R.drawable.download),
								stringResource(R.string.update),
								modifier = Modifier.rotate(180f),
								tint = MaterialTheme.colorScheme.tertiary
							)
						}
					}

					if (item.isInstalled) {
						IconButton(
							onClick = openSettings
						) {
							Icon(
								painterResource(R.drawable.settings),
								stringResource(R.string.settings)
							)
						}
					}

					if (item.isInstalling) {
						IconButton(
							onClick = {},
							modifier = Modifier.combinedClickable(
								onClick = {},
								onLongClick = cancelInstall,
							)
						) {
							val image =
								AnimatedImageVector.animatedVectorResource(R.drawable.animated_refresh)

							Icon(
								rememberAnimatedVectorPainter(image, false),
								stringResource(R.string.installing)
							)
						}
					}
				}

			}

			if (item.isUpdateAvailable && item.updateVersion != null) {
				if (item.updateVersion == Version(-9, -9, -9)) {
					Box(
						modifier = Modifier
							.background(MaterialTheme.colorScheme.tertiary)
							.fillMaxWidth()
					) {
						Text(
							stringResource(R.string.obsolete_extension),
							color = colorResource(com.google.android.material.R.color.design_default_color_on_primary),
							modifier = Modifier
								.padding(8.dp)
								.align(Alignment.Center)
						)
					}
				}
			}
		}
	}
}
