package app.shosetsu.android.ui.about

import android.content.ClipData
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.shosetsu.android.BuildConfig
import app.shosetsu.android.R
import app.shosetsu.android.common.consts.SUB_TEXT_SIZE
import app.shosetsu.android.common.consts.URL_APP_REPO
import app.shosetsu.android.common.consts.URL_DISCLAIMER
import app.shosetsu.android.common.consts.URL_DISCORD
import app.shosetsu.android.common.consts.URL_EXTENSIONS_REPO
import app.shosetsu.android.common.consts.URL_KOFI
import app.shosetsu.android.common.consts.URL_MATRIX
import app.shosetsu.android.common.consts.URL_PATREON
import app.shosetsu.android.common.consts.URL_PRIVACY
import app.shosetsu.android.common.consts.URL_WEBSITE
import app.shosetsu.android.common.enums.AppThemes
import app.shosetsu.android.common.ext.viewModelDi
import app.shosetsu.android.domain.model.local.Contributor
import app.shosetsu.android.ui.theme.ShosetsuTheme
import app.shosetsu.android.view.compose.NavigateBackButton
import app.shosetsu.android.view.compose.NovelCardCozyContent
import app.shosetsu.android.viewmodel.abstracted.AAboutViewModel
import kotlinx.coroutines.launch
import org.acra.util.Installation

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
 * @since 21 / 10 / 2021
 * @author Doomsdayrs
 */

@Composable
fun AboutView(
	onOpenLicense: () -> Unit,
	onBack: () -> Unit
) {
	val viewModel: AAboutViewModel = viewModelDi()

	val uriHandler = LocalUriHandler.current

	fun onClickDisclaimer() {
		uriHandler.openUri(URL_DISCLAIMER)
	}

	fun openWebsite() =
		uriHandler.openUri(URL_WEBSITE)

	fun openExtensions() =
		uriHandler.openUri(URL_EXTENSIONS_REPO)

	fun openDiscord() =
		uriHandler.openUri(URL_DISCORD)

	fun openMatrix() =
		uriHandler.openUri(URL_MATRIX)

	fun openPatreon() =
		uriHandler.openUri(URL_PATREON)

	fun openGithub() =
		uriHandler.openUri(URL_APP_REPO)

	fun openPrivacy() =
		uriHandler.openUri(URL_PRIVACY)

	AboutContent(
		currentVersion = BuildConfig.VERSION_NAME,
		onCheckForAppUpdate = viewModel::appUpdateCheck,
		onOpenWebsite = ::openWebsite,
		onOpenSource = ::openGithub,
		onOpenExtensions = ::openExtensions,
		onOpenDiscord = ::openDiscord,
		onOpenPatreon = ::openPatreon,
		onOpenLicense = onOpenLicense,
		onOpenDisclaimer = ::onClickDisclaimer,
		onOpenMatrix = ::openMatrix,
		onOpenPrivacy = ::openPrivacy,
		onOpenKofi = {
			uriHandler.openUri(URL_KOFI)
		},
		onBack = onBack,
		contributors = viewModel.contributors
	)
}

@ExperimentalMaterial3Api
@Preview
@Composable
fun PreviewAboutContent() = ShosetsuTheme(AppThemes.LIGHT) {
	AboutContent(
		currentVersion = BuildConfig.VERSION_NAME,
		onCheckForAppUpdate = {},
		onOpenWebsite = {},
		onOpenSource = {},
		onOpenExtensions = {},
		onOpenDiscord = {},
		onOpenPatreon = {},
		onOpenLicense = {},
		onOpenDisclaimer = {},
		onOpenMatrix = {},
		onOpenPrivacy = {},
		onOpenKofi = {
		},
		onBack = {},
		contributors = listOf(
			Contributor(
				"Clocks",
				"doomsdayrs.page",
				0,
				null,
				null,
			)
		)
	)

}

@Composable
fun ContributorItem(
	contributor: Contributor
) {
	val uriHandler = LocalUriHandler.current

	Box(Modifier.requiredWidthIn(max = 60.dp)) {
		// TODO, Maybe a custom contributor image shape would be nice.
		NovelCardCozyContent(
			contributor.name,
			contributor.image ?: "",
			onClick = {
				if (!contributor.website.isNullOrBlank())
					uriHandler.openUri(contributor.website)
			},
			onLongClick = {},
		)
	}
}

@ExperimentalMaterial3Api
@Composable
fun AboutItem(
	@StringRes titleRes: Int,
	description: String? = null,
	@StringRes descriptionRes: Int? = null,
	@DrawableRes iconRes: Int? = null,
	onClick: () -> Unit = {}
) {
	Box(
		modifier = Modifier.clickable { onClick() }
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.Start,
			modifier = Modifier.padding(16.dp)
		) {
			if (iconRes != null)
				Image(painterResource(iconRes), null, modifier = Modifier.padding(end = 8.dp))

			Column(
				verticalArrangement = Arrangement.Center,
				horizontalAlignment = Alignment.Start,
				modifier = Modifier.fillMaxWidth()
			) {
				Text(stringResource(titleRes), style = MaterialTheme.typography.bodyLarge)

				if (descriptionRes != null || description != null)
					Text(
						if (descriptionRes != null) {
							stringResource(descriptionRes)
						} else {
							description ?: return@Column
						},
						style = SUB_TEXT_SIZE,
						modifier = Modifier.alpha(0.7f)
					)
			}
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutContent(
	currentVersion: String,
	onCheckForAppUpdate: () -> Unit,
	onOpenWebsite: () -> Unit,
	onOpenSource: () -> Unit,
	onOpenExtensions: () -> Unit,
	onOpenDiscord: () -> Unit,
	onOpenPatreon: () -> Unit,
	onOpenKofi: () -> Unit,
	onOpenLicense: () -> Unit,
	onOpenDisclaimer: () -> Unit,
	onOpenMatrix: () -> Unit,
	onOpenPrivacy: () -> Unit,
	onBack: () -> Unit,
	contributors: List<Contributor>
) {
	Scaffold(
		topBar = {
			TopAppBar(
				title = {
					Text(stringResource(R.string.about))
				},
				navigationIcon = {
					NavigateBackButton(onBack)
				}
			)
		}
	) { paddingValues ->
		LazyColumn(
			modifier = Modifier
				.fillMaxSize()
				.padding(paddingValues),
			contentPadding = PaddingValues(bottom = 128.dp)
		) {
			item {
				AboutItem(
					R.string.version,
					description = currentVersion
				)
			}
			item {
				AboutItem(
					R.string.check_for_app_update,
					onClick = onCheckForAppUpdate
				)
			}
			item {
				val context = LocalContext.current
				val clipboard = LocalClipboard.current
				val scope = rememberCoroutineScope()

				val id = remember { Installation.id(context) }

				AboutItem(
					R.string.fragment_about_acra_id,
					description = id,
					onClick = {
						scope.launch { clipboard.setClipEntry(ClipData.newPlainText("text", id).toClipEntry()) }
					}
				)
			}
			item {
				HorizontalDivider()
			}

			item {
				Text(stringResource(R.string.contributors), Modifier.padding(start = 16.dp, top = 8.dp))
			}

			item {
				LazyRow(
					contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
					horizontalArrangement = Arrangement.spacedBy(4.dp)
				) {
					items(contributors) {
						ContributorItem(it)
					}
				}
			}

			item {
				HorizontalDivider()
			}
			item {
				AboutItem(
					R.string.website,
					URL_WEBSITE,
					onClick = onOpenWebsite
				)
			}
			item {
				AboutItem(
					R.string.github,
					URL_APP_REPO,
					onClick = onOpenSource
				)
			}
			item {
				AboutItem(
					R.string.extensions,
					URL_EXTENSIONS_REPO,
					onClick = onOpenExtensions
				)
			}
			item {
				AboutItem(
					R.string.matrix,
					URL_MATRIX,
					onClick = onOpenMatrix
				)
			}
			item {
				AboutItem(
					R.string.discord,
					URL_DISCORD,
					onClick = onOpenDiscord
				)
			}
			item {
				AboutItem(
					R.string.patreon_support,
					URL_PATREON,
					onClick = onOpenPatreon
				)
			}
			item {
				AboutItem(
					R.string.kofi_support,
					URL_KOFI,
					onClick = onOpenKofi
				)
			}
			item {
				AboutItem(
					R.string.source_licenses,
					onClick = onOpenLicense
				)
			}
			item {
				AboutItem(
					R.string.disclaimer,
					URL_DISCLAIMER,
					onClick = onOpenDisclaimer
				)
			}
			item {
				AboutItem(
					R.string.privacy_policy,
					onClick = onOpenPrivacy
				)
			}
		}
	}
}
