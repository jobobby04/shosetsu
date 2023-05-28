package app.shosetsu.android.ui.reader

import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import app.shosetsu.android.common.ext.logE
import app.shosetsu.android.common.ext.logI
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

/**
 * Shosetsu
 *
 * @since 28 / 05 / 2023
 * @author Doomsdayrs
 */


val validBreaks = listOf(".\n\n", ".\n", "\n\n", ",\n", ". ", ", ", " ")

/**
 * A [UtteranceProgressListener]
 */
class ShosetsuUtteranceProgressListener(
	private val setIsTTSPlaying: (Boolean) -> Unit
) : UtteranceProgressListener() {
	override fun onStart(p0: String?) {
		setIsTTSPlaying(true)
	}

	override fun onDone(p0: String?) {
		setIsTTSPlaying(false)
	}

	@Deprecated(
		"Required to implement UtteranceProgressListener but deprecated in Java",
		ReplaceWith("onError")
	)
	override fun onError(p0: String?) {
		setIsTTSPlaying(false)
	}

	override fun onStop(utteranceId: String?, interrupted: Boolean) {
		setIsTTSPlaying(false)
	}

	override fun onError(utteranceId: String?, errorCode: Int) {
		logI("Error: $utteranceId ($errorCode)")
		setIsTTSPlaying(false)
	}
}

fun customSpeak(tts: TextToSpeech, text: String, utteranceId: Int, flush: Boolean = true) {
	val trimmed = text.replace("\r\n", "\n")
		.replace("\t", " ")
		.trim()
	val max = TextToSpeech.getMaxSpeechInputLength()
	if (trimmed.length <= max) {
		tts.speak(
			trimmed,
			if (flush) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD,
			null,
			utteranceId.toString()
		)
	} else {
		var ind = -1
		for (br in validBreaks) {
			if (ind == -1) ind = trimmed.substring(0, max + 1).lastIndexOf(br)
		}
		if (ind == -1) ind = max
		customSpeak(tts, trimmed.substring(0, ind + 1), utteranceId, flush)
		customSpeak(tts, trimmed.substring(ind + 1), utteranceId + 1, false)
	}
}

/**
 * A [TextToSpeech.OnInitListener]
 */
class ShosetsuTextToSpeechInitListener(
	private val getTTS: () -> TextToSpeech,
	private val setIsTTSCapable: (Boolean) -> Unit
) : TextToSpeech.OnInitListener {
	override fun onInit(it: Int) {
		when (it) {
			TextToSpeech.SUCCESS -> {
				val result = getTTS().setLanguage(Locale.getDefault())

				if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
					logE("Language not supported for TTS")
					setIsTTSCapable(false)
				} else {
					setIsTTSCapable(true)
				}
			}

			else -> {
				logE("TTS Initialization failed: $it")
				setIsTTSCapable(false)
			}
		}
	}
}