package app.shosetsu.android.viewmodel.abstracted

import android.graphics.Color
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import app.shosetsu.android.common.SettingKey.ReaderDisableTextSelection
import app.shosetsu.android.common.SettingKey.ReaderIndentSize
import app.shosetsu.android.common.SettingKey.ReaderParagraphSpacing
import app.shosetsu.android.common.SettingKey.ReaderTableHack
import app.shosetsu.android.common.SettingKey.ReaderTextSize
import app.shosetsu.android.common.SettingKey.ReaderTheme
import app.shosetsu.android.common.SettingKey.ReaderUserThemes
import app.shosetsu.android.common.ext.onIO
import app.shosetsu.android.domain.model.local.ColorChoiceData
import app.shosetsu.android.domain.repository.base.ISettingsRepository
import app.shosetsu.android.ui.theme.FallbackColorScheme
import app.shosetsu.android.viewmodel.impl.ChapterReaderViewModel.Companion.HTML_SIZE_DIVISION
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
abstract class ShosetsuCssViewModelComponent {
	abstract val settingsRepo: ISettingsRepository
	abstract val viewModelScopeIO: CoroutineScope
	abstract val indentSizeFlow: Flow<Int>
	abstract val paragraphSpacingFlow: Flow<Float>
	abstract val colorSchemeFlow: Flow<ColorScheme>
	abstract val paddingValuesFlow: Flow<PaddingValues>

	val themeFlow: StateFlow<Pair<Int, Int>> by lazy {
		settingsRepo.getIntFlow(ReaderTheme).mapLatest { id: Int ->
			settingsRepo.getStringSet(ReaderUserThemes)
				.map { ColorChoiceData.fromString(it) }
				.find { it.identifier == id.toLong() }
				?.let { (_, _, textColor, backgroundColor) ->
					(textColor to backgroundColor)
				} ?: (Color.BLACK to Color.WHITE)
		}.onIO().stateIn(viewModelScopeIO, SharingStarted.Lazily, Color.BLACK to Color.WHITE)
	}

	val disableTextSelection: StateFlow<Boolean> by lazy {
		settingsRepo.getBooleanFlow(ReaderDisableTextSelection)
	}

	val liveTextSize: StateFlow<Float> by lazy {
		settingsRepo.getFloatFlow(ReaderTextSize)
	}

	val tableHackEnabledFlow: Flow<Boolean> by lazy {
		settingsRepo.getBooleanFlow(ReaderTableHack)
	}

	val shosetsuCss: Flow<String> by lazy {
		themeFlow.combine(liveTextSize) { (fore, back), textSize ->
			ShosetsuCSSBuilder(
				backgroundColor = back,
				foregroundColor = fore,
				textSize = textSize
			)
		}.combine(indentSizeFlow) { builder, indent ->
			builder.copy(
				indentSize = indent
			)
		}.combine(paragraphSpacingFlow) { builder, space ->
			builder.copy(
				paragraphSpacing = space
			)
		}.combine(tableHackEnabledFlow) { builder, enabled ->
			builder.copy(
				tableHackEnabled = enabled
			)
		}.combine(disableTextSelection) { builder, enabled ->
			builder.copy(
				disableTextSelection = enabled
			)
		}.combine(colorSchemeFlow) { builder, colorScheme ->
			builder.copy(
				colorScheme = colorScheme
			)
		}.combine(paddingValuesFlow) { builder, paddingValues ->
			builder.copy(
				paddingValues = paddingValues
			)
		}.map {
			val shosetsuStyle: HashMap<String, HashMap<String, String>> = hashMapOf()

			fun setShosetsuStyle(elem: String, action: HashMap<String, String>.() -> Unit) =
				shosetsuStyle.getOrPut(elem) { hashMapOf() }.apply(action)

			fun Int.cssColor(): String = "rgb($red,$green,$blue)"

			if (it.disableTextSelection) {
				setShosetsuStyle("*") {
					this["-webkit-user-select"] = "none"
					this["user-select"] = "none"
				}
			}

			setShosetsuStyle(":root") {
				// Naming is based on the Material Theme Builder's CSS output
				fun color(name: String, color: androidx.compose.ui.graphics.Color) {
					this["--md-sys-color-$name"] = color.toArgb().cssColor()
				}
				it.colorScheme.run {
					color("primary", primary)
					color("surface-tint", surfaceTint)
					color("on-primary", onPrimary)
					color("primary-container", primaryContainer)
					color("on-primary-container", onPrimaryContainer)
					color("secondary", secondary)
					color("on-secondary", onSecondary)
					color("secondary-container", secondaryContainer)
					color("on-secondary-container", onSecondaryContainer)
					color("tertiary", tertiary)
					color("on-tertiary", onTertiary)
					color("tertiary-container", tertiaryContainer)
					color("on-tertiary-container", onTertiaryContainer)
					color("error", error)
					color("on-error", onError)
					color("error-container", errorContainer)
					color("on-error-container", onErrorContainer)
					color("background", background)
					color("on-background", onBackground)
					color("surface", surface)
					color("on-surface", onSurface)
					color("surface-variant", surfaceVariant)
					color("on-surface-variant", onSurfaceVariant)
					color("outline", outline)
					color("outline-variant", outlineVariant)
					color("shadow", scrim) // doesn't exist in ColorScheme
					color("scrim", scrim)
					color("inverse-surface", inverseSurface)
					color("inverse-on-surface", inverseOnSurface)
					color("inverse-primary", inversePrimary)
					color("primary-fixed", primary) // doesn't exist in ColorScheme
					color("on-primary-fixed", onPrimary) // doesn't exist in ColorScheme
					color("primary-fixed-dim", primary) // doesn't exist in ColorScheme
					color("on-primary-fixed-variant", onPrimary) // doesn't exist in ColorScheme
					color("secondary-fixed", secondary) // doesn't exist in ColorScheme
					color("on-secondary-fixed", onSecondary) // doesn't exist in ColorScheme
					color("secondary-fixed-dim", secondary) // doesn't exist in ColorScheme
					color("on-secondary-fixed-variant", onSecondary) // doesn't exist in ColorScheme
					color("tertiary-fixed", tertiary) // doesn't exist in ColorScheme
					color("on-tertiary-fixed", onTertiary) // doesn't exist in ColorScheme
					color("tertiary-fixed-dim", tertiary) // doesn't exist in ColorScheme
					color("on-tertiary-fixed-variant", onTertiary) // doesn't exist in ColorScheme
					color("surface-dim", surfaceDim)
					color("surface-bright", surfaceBright)
					color("surface-container-lowest", surfaceContainerLowest)
					color("surface-container-low", surfaceContainerLow)
					color("surface-container", surfaceContainer)
					color("surface-container-high", surfaceContainerHigh)
					color("surface-container-highest", surfaceContainerHighest)
				}
				this["--shosetsu-indent-size"] = "${it.indentSize}em"
				this["--shosetsu-text-size"] = "${it.textSize / HTML_SIZE_DIVISION}pt"
				this["--shosetsu-paragraph-spacing"] = "${it.paragraphSpacing}em"

				this["--shosetsu-padding-top"] = "${it.paddingValues.calculateTopPadding().value.toLong()}px"
				this["--shosetsu-padding-bottom"] = "${it.paddingValues.calculateBottomPadding().value.toLong()}px"
				// at least 0.5em is needed to prevent weird scrolling behavior with some content
				// (like chapter 1 of "The Perfect Run" from "Royal Road")
				this["--shosetsu-padding-left"] =
					"max(0.5em, ${it.paddingValues.calculateLeftPadding(layoutDirection = LayoutDirection.Ltr).value.toLong()}px)"
				this["--shosetsu-padding-right"] =
					"max(0.5em, ${it.paddingValues.calculateRightPadding(layoutDirection = LayoutDirection.Ltr).value.toLong()}px)"
			}

			setShosetsuStyle("body") {
				this["background-color"] = it.backgroundColor.cssColor()
				this["color"] = it.foregroundColor.cssColor()
				this["font-size"] = "var(--shosetsu-text-size)"
				this["scroll-behavior"] = "smooth"
				this["text-indent"] = "var(--shosetsu-indent-size)"
				this["overflow-wrap"] = "break-word"
				// ensure everything stays away from the edge
				this["padding"] =
					"var(--shosetsu-padding-top) var(--shosetsu-padding-right) var(--shosetsu-padding-bottom) var(--shosetsu-padding-left)"
			}

			setShosetsuStyle("p") {
				this["margin-top"] = "var(--shosetsu-paragraph-spacing)"
			}

			setShosetsuStyle("img") {
				this["max-width"] = "100%"
				this["height"] = "initial !important"
			}

			setShosetsuStyle(".tts-border-style") {
				this["border"] = "2px solid red"
			}

			if (it.tableHackEnabled)
				setShosetsuStyle("table") {
					this["overflow-x"] = "auto"
					this["display"] = "block"
					this["white-space"] = "nowrap"
				}

			shosetsuStyle.map { elem ->
				"${elem.key} {" + elem.value.map { rule -> "${rule.key}:${rule.value}" }
					.joinToString(";", postfix = ";") + "}"
			}.joinToString("")
		}.onIO()
	}

	data class ShosetsuCSSBuilder(
		val backgroundColor: Int = Color.WHITE,
		val foregroundColor: Int = Color.BLACK,
		val textSize: Float = ReaderTextSize.default,
		val indentSize: Int = ReaderIndentSize.default,
		val paragraphSpacing: Float = ReaderParagraphSpacing.default,
		val tableHackEnabled: Boolean = ReaderTableHack.default,
		val disableTextSelection: Boolean = ReaderDisableTextSelection.default,
		val colorScheme: ColorScheme = FallbackColorScheme,
		val paddingValues: PaddingValues = PaddingValues(0.dp)
	)
}
