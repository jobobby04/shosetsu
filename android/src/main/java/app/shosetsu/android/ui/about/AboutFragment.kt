package app.shosetsu.android.ui.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import app.shosetsu.android.BuildConfig
import app.shosetsu.android.R
import app.shosetsu.android.common.consts.*
import app.shosetsu.android.common.enums.TextAsset
import app.shosetsu.android.common.ext.ComposeView
import app.shosetsu.android.common.ext.navigateSafely
import app.shosetsu.android.common.ext.setShosetsuTransition
import app.shosetsu.android.common.ext.viewModelDi
import app.shosetsu.android.ui.settings.sub.TextAssetReaderFragment.Companion.bundle
import app.shosetsu.android.view.compose.ShosetsuCompose
import app.shosetsu.android.view.controller.ShosetsuFragment
import app.shosetsu.android.viewmodel.abstracted.AAboutViewModel
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
class AboutFragment : ShosetsuFragment() {

	override val viewTitleRes: Int = R.string.about

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedViewState: Bundle?
	): View {
		setViewTitle()
		return ComposeView {
			AboutView(
				onNavigateSafely = { id, bundle, options ->
					findNavController().navigateSafely(id, bundle, options)
				}
			)
		}

	}
}

@Composable
fun AboutView(
	onNavigateSafely: (Int, Bundle, NavOptions) -> Unit
) {
	val viewModel: AAboutViewModel = viewModelDi()
	val uriHandler = LocalUriHandler.current

	fun onClickLicense() {
		onNavigateSafely(
			R.id.action_aboutController_to_textAssetReader,
			TextAsset.LICENSE.bundle,
			navOptions {
				launchSingleTop = true
				setShosetsuTransition()
			}
		)
	}

	fun onClickDisclaimer() {
		uriHandler.openUri(URL_DISCLAIMER)
	}

	fun openWebsite() =
		uriHandler.openUri(URL_WEBSITE)

	fun openExtensions() =
		uriHandler.openUri(URL_GITHUB_EXTENSIONS)

	fun openDiscord() =
		uriHandler.openUri(URL_DISCORD)

	fun openMatrix() =
		uriHandler.openUri(URL_MATRIX)

	fun openPatreon() =
		uriHandler.openUri(URL_PATREON)

	fun openGithub() =
		uriHandler.openUri(URL_GITHUB_APP)

	fun openPrivacy() =
		uriHandler.openUri(URL_PRIVACY)

	ShosetsuCompose {
		AboutContent(
			currentVersion = BuildConfig.VERSION_NAME,
			onCheckForAppUpdate = viewModel::appUpdateCheck,
			onOpenWebsite = ::openWebsite,
			onOpenSource = ::openGithub,
			onOpenExtensions = ::openExtensions,
			onOpenDiscord = ::openDiscord,
			onOpenPatreon = ::openPatreon,
			onOpenLicense = ::onClickLicense,
			onOpenDisclaimer = ::onClickDisclaimer,
			onOpenMatrix = ::openMatrix,
			onOpenPrivacy = ::openPrivacy,
			onOpenKofi = {
				uriHandler.openUri(URL_KOFI)
			}
		)
	}
}

@ExperimentalMaterial3Api
@Preview
@Composable
fun PreviewAboutContent() {
	ShosetsuCompose {
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
			}
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
	onOpenPrivacy: () -> Unit
) {
	LazyColumn(
		modifier = Modifier.fillMaxSize(),
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
			val clipboard = LocalClipboardManager.current

			val id = remember { Installation.id(context) }

			AboutItem(
				R.string.fragment_about_acra_id,
				description = id,
				onClick = {
					clipboard.setText(AnnotatedString(id))
				}
			)
		}
		item {
			Divider()
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
				URL_GITHUB_APP,
				onClick = onOpenSource
			)
		}
		item {
			AboutItem(
				R.string.extensions,
				URL_GITHUB_EXTENSIONS,
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
