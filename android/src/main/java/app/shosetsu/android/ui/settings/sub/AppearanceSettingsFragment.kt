package app.shosetsu.android.ui.settings.sub

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import app.shosetsu.android.R
import app.shosetsu.android.common.SettingKey.AppTheme
import app.shosetsu.android.common.SettingKey.ChapterColumnsInLandscape
import app.shosetsu.android.common.SettingKey.ChapterColumnsInPortait
import app.shosetsu.android.common.SettingKey.NavStyle
import app.shosetsu.android.common.SettingKey.NovelBadgeToast
import app.shosetsu.android.common.SettingKey.SelectedNovelCardType
import app.shosetsu.android.common.ext.launchIO
import app.shosetsu.android.common.ext.viewModelDi
import app.shosetsu.android.view.compose.NavigateBackButton
import app.shosetsu.android.view.compose.setting.NumberPickerSettingContent
import app.shosetsu.android.view.compose.setting.StringListPreferenceSettingContent
import app.shosetsu.android.view.compose.setting.SwitchSettingContent
import app.shosetsu.android.view.compose.setting.widget.AppThemeModePreferenceWidget
import app.shosetsu.android.view.compose.setting.widget.ListPreferenceWidget
import app.shosetsu.android.view.compose.setting.widget.PreferenceGroupHeader
import app.shosetsu.android.view.uimodels.StableHolder
import app.shosetsu.android.viewmodel.abstracted.settings.AAppearanceSettingsViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.xmlpull.v1.XmlPullParser
import java.util.Locale

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

@Composable
fun AppearanceSettingsView(
	onBack: () -> Unit
) {
	val viewModel: AAppearanceSettingsViewModel = viewModelDi()

	AppearanceSettingsContent(
		viewModel,
		onBack = onBack
	)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettingsContent(
	viewModel: AAppearanceSettingsViewModel,
	onBack: () -> Unit
) {
	Scaffold(
		topBar = {
			TopAppBar(
				title = {
					Text(stringResource(R.string.appearance))
				},
				navigationIcon = {
					NavigateBackButton(onBack)
				}
			)
		}
	) { paddingValues ->
		LazyColumn(
			contentPadding = PaddingValues(
				top = 16.dp,
				bottom = 64.dp
			),
			modifier = Modifier.padding(paddingValues)
		) {
			item {
				PreferenceGroupHeader(stringResource(R.string.theme))
			}

			item {
				val choice by viewModel.appTheme.collectAsState()

				AppThemeModePreferenceWidget(
					value = choice,
					onItemClick = {
						launchIO {
							viewModel.settingsRepo.setInt(AppTheme, it.key)
						}
						it.setAppCompatDelegateThemeMode()
					}
				)
			}

			item { Spacer(modifier = Modifier.height(12.dp)) }

			item {
				PreferenceGroupHeader(stringResource(R.string.display))
			}

			item {
				val context = LocalContext.current
				val langs = remember { getLangs(context) }
				var currentLanguage by remember {
					mutableStateOf(
						AppCompatDelegate.getApplicationLocales().get(0)?.toLanguage() ?: context.defaultLanguage
					)
				}

				LaunchedEffect(currentLanguage) {
					val locale = if (currentLanguage.langTag.isEmpty()) {
						LocaleListCompat.getEmptyLocaleList()
					} else {
						LocaleListCompat.forLanguageTags(currentLanguage.langTag)
					}
					AppCompatDelegate.setApplicationLocales(locale)
				}

				ListPreferenceWidget(
					title = stringResource(R.string.app_language),
					subtitle = currentLanguage.localizedDisplayName ?: currentLanguage.displayName,
					icon = null,
					value = currentLanguage,
					entries = langs.associateWith { it.localizedDisplayName ?: it.displayName },
					onValueChange = { currentLanguage = it },
				)
			}

			item {
				NumberPickerSettingContent(
					title = stringResource(R.string.columns_of_novel_listing_p),
					description = stringResource(R.string.columns_zero_automatic),
					range = remember { StableHolder(0..10) },
					repo = viewModel.settingsRepo,
					key = ChapterColumnsInPortait,
					modifier = Modifier
						.fillMaxWidth()
				)
			}

			item {
				NumberPickerSettingContent(
					title = stringResource(R.string.columns_of_novel_listing_h),
					description = stringResource(R.string.columns_zero_automatic),
					range = remember { StableHolder(0..10) },
					repo = viewModel.settingsRepo,
					key = ChapterColumnsInLandscape,
					modifier = Modifier
						.fillMaxWidth()
				)
			}

			item {
				StringListPreferenceSettingContent(
					title = stringResource(R.string.novel_card_type_selector_title),
					choices = stringArrayResource(R.array.novel_card_types).toList(),
					repo = viewModel.settingsRepo,
					key = SelectedNovelCardType
				)
			}

			item {
				SwitchSettingContent(
					title = stringResource(R.string.novel_badge_toast_title),
					description = stringResource(R.string.novel_badge_toast_desc),
					repo = viewModel.settingsRepo,
					key = NovelBadgeToast,
					modifier = Modifier.fillMaxWidth()
				)
			}

			item {
				SwitchSettingContent(
					title = stringResource(R.string.settings_view_legacy_nav_title),
					description = stringResource(R.string.settings_view_legacy_nav_desc),
					modifier = Modifier.fillMaxWidth(),
					repo = viewModel.settingsRepo,
					key = NavStyle
				)
			}
		}
	}
}

private fun getLangs(context: Context): ImmutableList<Language> {
	val langs = mutableListOf<Language>()
	val parser = context.resources.getXml(R.xml.locales_config)
	var eventType = parser.eventType
	while (eventType != XmlPullParser.END_DOCUMENT) {
		if (eventType == XmlPullParser.START_TAG && parser.name == "locale") {
			for (i in 0..<parser.attributeCount) {
				if (parser.getAttributeName(i) == "name") {
					val langTag = parser.getAttributeValue(i)
					val language = Locale.forLanguageTag(langTag).toLanguage()
					if (language.displayName.isNotEmpty()) {
						langs.add(language)
					}
				}
			}
		}
		eventType = parser.next()
	}

	langs.sortBy { it.displayName }
	langs.add(0, context.defaultLanguage)

	return langs.toImmutableList()
}

private fun Locale.toLanguage(): Language =
	Language(toLanguageTag(), displayName, getDisplayName(this))

private val Context.defaultLanguage: Language
	get() =
		Language("", getString(R.string.app_language_default), null)

private data class Language(
	val langTag: String,
	val displayName: String,
	val localizedDisplayName: String?,
)
