package app.shosetsu.android.ui.settings.sub

import android.content.Context
import android.os.Build
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.shosetsu.android.R
import app.shosetsu.android.common.SettingKey
import app.shosetsu.android.common.StringSetKey
import app.shosetsu.android.common.ext.launchIO
import app.shosetsu.android.common.ext.viewModelDi
import app.shosetsu.android.view.compose.NavigateBackButton
import app.shosetsu.android.view.compose.setting.RestrictionSelectPreferenceWidget
import app.shosetsu.android.view.compose.setting.SliderSettingContent
import app.shosetsu.android.view.compose.setting.SwitchSettingContent
import app.shosetsu.android.view.compose.setting.TriStateListPreferenceWidget
import app.shosetsu.android.view.compose.setting.widget.TextPreferenceWidget
import app.shosetsu.android.view.uimodels.StableHolder
import app.shosetsu.android.view.uimodels.model.CategoryUI
import app.shosetsu.android.viewmodel.abstracted.settings.ALibrarySettingsViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.map

@Composable
fun LibrarySettingsView(
	onNavToCategories: () -> Unit,
	onBack: () -> Unit
) {
	val viewModel: ALibrarySettingsViewModel = viewModelDi()

	LibrarySettingsContent(
		viewModel = viewModel,
		onNavToCategories = onNavToCategories,
		onBack = onBack
	)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibrarySettingsContent(
	viewModel: ALibrarySettingsViewModel,
	onNavToCategories: () -> Unit,
	onBack: () -> Unit
) {
	Scaffold(
		topBar = {
			TopAppBar(
				title = {
					Text(stringResource(R.string.library))
				},
				navigationIcon = {
					NavigateBackButton(onBack)
				}
			)
		}
	) { paddingValues ->
		LazyColumn(
			contentPadding = PaddingValues(bottom = 64.dp, top = 16.dp),
			modifier = Modifier.padding(paddingValues)
		) {
			item {
				val categories by viewModel.categories.collectAsState()
				TextPreferenceWidget(
					title = stringResource(R.string.settings_library_categories_title),
					subtitle = stringResource(R.string.settings_library_categories_desc, categories.size),
					onPreferenceClick = onNavToCategories
				)
			}

			item {
				SwitchSettingContent(
					stringResource(R.string.settings_update_novel_on_boot_title),
					stringResource(R.string.settings_update_novel_on_boot_desc),
					viewModel.settingsRepo,
					SettingKey.UpdateNovelsOnStartup,
					modifier = Modifier
						.fillMaxWidth()
				)
			}

			item {
				SliderSettingContent(
					title = stringResource(R.string.settings_update_novel_frequency_title),
					description = stringResource(R.string.settings_update_novel_frequency_desc),
					valueRange = remember { StableHolder(0..168) },
					parseValue = {
						when (it) {
							12 -> stringResource(R.string.cycle_bidaily)
							24 -> stringResource(R.string.cycle_daily)
							48 -> stringResource(R.string.cycle_2_days)
							72 -> stringResource(R.string.cycle_3_days)
							96 -> stringResource(R.string.cycle_4_days)
							120 -> stringResource(R.string.cycle_5_days)
							144 -> stringResource(R.string.cycle_6_days)
							168 -> stringResource(R.string.cycle_weekly)
							else -> if (it == 0) {
								stringResource(R.string.cycle_disabled)
							} else {
								pluralStringResource(R.plurals.cycle_hourly, it, it)
							}
						}
					},
					repo = viewModel.settingsRepo,
					key = SettingKey.NovelUpdateCycle,
					haveSteps = false,
					manipulateUpdate = {
						when (it) {
							in 24..35 -> 24
							in 36..48 -> 48
							in 48..59 -> 48
							in 60..72 -> 72
							in 72..83 -> 72
							in 84..96 -> 96
							in 96..107 -> 96
							in 108..120 -> 120
							in 120..131 -> 120
							in 132..144 -> 144
							in 144..156 -> 144
							in 157..168 -> 168
							else -> it
						}
					},
					maxHeaderSize = 80.dp
				)
			}

			item {
				viewModel.LibraryUpdateCategories(
					stringResource(R.string.settings_update_novel_categories_update),
					SettingKey.IncludeCategoriesInUpdate,
					SettingKey.ExcludedCategoriesInUpdate
				)
			}

			item {
				SwitchSettingContent(
					stringResource(R.string.settings_update_novel_on_update_title),
					stringResource(R.string.settings_update_novel_on_update_desc),
					viewModel.settingsRepo,
					SettingKey.DownloadNewNovelChapters,
					modifier = Modifier
						.fillMaxWidth()
				)
			}

			item {
				viewModel.LibraryUpdateCategories(
					stringResource(R.string.settings_update_novel_categories_download),
					SettingKey.IncludeCategoriesToDownload,
					SettingKey.ExcludedCategoriesToDownload
				)
			}

			item {
				SwitchSettingContent(
					stringResource(R.string.settings_update_novel_only_ongoing_title),
					stringResource(R.string.settings_update_novel_only_ongoing_desc),
					viewModel.settingsRepo,
					SettingKey.OnlyUpdateOngoingNovels,
					modifier = Modifier
						.fillMaxWidth()
				)
			}

			item {
				RestrictionSelectPreferenceWidget(
					title = stringResource(R.string.settings_library_restrictions_title),
					subtitle = R.string.settings_library_restrictions_desc,
					restrictions = mapOf(
						R.string.settings_update_novel_on_metered_title to SettingKey.NovelUpdateOnMeteredConnection,
						R.string.settings_update_novel_on_low_bat_title to SettingKey.NovelUpdateOnLowBattery,
						R.string.settings_update_novel_on_low_sto_title to SettingKey.NovelUpdateOnLowStorage,
					) + if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
						mapOf(R.string.settings_update_novel_only_idle_title to SettingKey.NovelUpdateOnlyWhenIdle)
					} else {
						emptyMap()
					},
					repo = viewModel.settingsRepo
				)
			}

			item {
				SwitchSettingContent(
					stringResource(R.string.settings_update_novel_notification_style_title),
					stringResource(R.string.settings_update_novel_notification_style_desc),
					viewModel.settingsRepo,
					SettingKey.UpdateNotificationStyle,
					modifier = Modifier
						.fillMaxWidth()
				)
			}

			item {
				SwitchSettingContent(
					stringResource(R.string.settings_update_novel_show_progress_title),
					stringResource(R.string.settings_update_novel_show_progress_desc),
					viewModel.settingsRepo,
					SettingKey.NovelUpdateShowProgress,
					modifier = Modifier
						.fillMaxWidth()
				)
			}

			item {
				SwitchSettingContent(
					stringResource(R.string.settings_update_novel_classic_notification_title),
					stringResource(R.string.settings_update_novel_classic_notification_desc),
					viewModel.settingsRepo,
					SettingKey.NovelUpdateClassicFinish,
					modifier = Modifier
						.fillMaxWidth()
				)
			}

			item {
				SwitchSettingContent(
					stringResource(R.string.date_format_mdy),
					stringResource(R.string.date_format_mdy_desc),
					viewModel.settingsRepo,
					SettingKey.NovelUpdateDateMDY,
					modifier = Modifier
						.fillMaxWidth()
				)
			}
		}
	}
}

@Composable
private fun ALibrarySettingsViewModel.LibraryUpdateCategories(
	title: String,
	includeKey: StringSetKey,
	excludeKey: StringSetKey
) {
	val categories by categories.collectAsState()
	val includedCategoryIds by remember(includeKey) {
		settingsRepo.getStringSetFlow(includeKey)
			.map { it.map(String::toInt).toImmutableList() }
	}.collectAsState(persistentListOf())
	val excludedCategoryIds by remember(excludeKey) {
		settingsRepo.getStringSetFlow(excludeKey)
			.map { it.map(String::toInt).toImmutableList() }
	}.collectAsState(persistentListOf())

	val context = LocalContext.current
	val description by remember {
		derivedStateOf {
			getCategorySelectDescription(
				context,
				categories,
				includedCategoryIds,
				excludedCategoryIds
			)
		}
	}
	TriStateListPreferenceWidget(
		title = title,
		subtitle = description,
		possibleValues = categories,
		initialChecked = includedCategoryIds.mapNotNull { id -> categories.firstOrNull { it.id == id } },
		initialInversed = excludedCategoryIds.mapNotNull { id -> categories.firstOrNull { it.id == id } },
		stringify = { it.name },
		onValuesChange = { newIncluded, newExcluded ->
			launchIO {
				settingsRepo.setStringSet(
					includeKey,
					newIncluded.map { it.id.toString() }.toSet()
				)
				settingsRepo.setStringSet(
					excludeKey,
					newExcluded.map { it.id.toString() }.toSet()
				)
			}
		}
	)
}

fun getCategorySelectDescription(
	context: Context,
	categories: List<CategoryUI>,
	includedCategoryIds: List<Int>,
	excludedCategoryIds: List<Int>,
): String {
	val includedCategories = includedCategoryIds
		.mapNotNull { id -> categories.find { it.id == id } }
		.sortedBy { it.order }
	val excludedCategories = excludedCategoryIds
		.mapNotNull { id -> categories.find { it.id == id } }
		.sortedBy { it.order }

	val allExcluded = excludedCategories.size == categories.size

	val includedItemsText = when {
		// Some selected, but not all
		includedCategories.isNotEmpty() && includedCategories.size != categories.size -> includedCategories.joinToString { it.name }
		// All explicitly selected
		includedCategories.size == categories.size -> context.getString(R.string.all)
		allExcluded -> context.getString(R.string.none)
		else -> context.getString(R.string.all)
	}
	val excludedItemsText = when {
		excludedCategories.isEmpty() -> context.getString(R.string.none)
		allExcluded -> context.getString(R.string.all)
		else -> excludedCategories.joinToString { it.name }
	}
	return buildString {
		append(
			context.getString(
				R.string.settings_update_novel_include_categories,
				includedItemsText
			)
		)
		appendLine()
		append(
			context.getString(
				R.string.settings_update_novel_exclude_categories,
				excludedItemsText
			)
		)
	}
}
