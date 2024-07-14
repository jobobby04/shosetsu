package app.shosetsu.android.ui.reader

import android.speech.tts.TextToSpeech
import java.util.UUID

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

fun customSpeak(tts: TextToSpeech, text: String, utteranceId: String, flush: Boolean = false) {
	val trimmed = text.replace("\r\n", "\n")
		.replace("\t", " ")
		.trim()
	val max = TextToSpeech.getMaxSpeechInputLength()
	if (trimmed.length <= max) {
		tts.speak(
			trimmed,
			if (flush) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD,
			null,
			utteranceId
		)
	} else {
		var ind = -1
		for (br in validBreaks) {
			if (ind == -1) ind = trimmed.substring(0, max + 1).lastIndexOf(br)
		}
		if (ind == -1) ind = max
		customSpeak(
			tts,
			trimmed.substring(0, ind + 1),
			utteranceId,
			flush,
		)
		customSpeak(
			tts,
			trimmed.substring(ind + 1),
			utteranceId.substringBefore('|') + UUID.randomUUID(),
			false
		)
	}
}
