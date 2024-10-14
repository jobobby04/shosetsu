package app.shosetsu.android.viewmodel.abstracted

import android.graphics.Color
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
import app.shosetsu.android.viewmodel.impl.ChapterReaderViewModel.Companion.HTML_SIZE_DIVISION
import app.shosetsu.android.viewmodel.impl.ChapterReaderViewModel.ShosetsuCSSBuilder
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

            setShosetsuStyle("body") {
                this["background-color"] = it.backgroundColor.cssColor()
                this["color"] = it.foregroundColor.cssColor()
                this["font-size"] = "${it.textSize / HTML_SIZE_DIVISION}pt"
                this["scroll-behavior"] = "smooth"
                this["text-indent"] = "${it.indentSize}em"
                this["overflow-wrap"] = "break-word"
                this["padding"] = "0.5em" // ensure everything stays away from the edge
            }

            setShosetsuStyle("p") {
                this["margin-top"] = "${it.paragraphSpacing}em"
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
        val disableTextSelection: Boolean = ReaderDisableTextSelection.default
    )
}