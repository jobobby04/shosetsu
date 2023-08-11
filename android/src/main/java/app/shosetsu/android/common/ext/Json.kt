package app.shosetsu.android.common.ext

import android.os.Build
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.serializer
import java.io.InputStream

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
 * @since 11 / 08 / 2023
 * @author Doomsdayrs
 */

/**
 * Deals with Android 6 and lower issue with kotlinx.serialization
 *
 * @see <a href="https://github.com/Kotlin/kotlinx.serialization/issues/2231">Issue 2231</a>
 */
@ExperimentalSerializationApi
public inline fun <reified T> Json.decodeSafeFromStream(stream: InputStream): T {
	return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
		decodeFromStream(serializersModule.serializer(), stream)
	} else {
		decodeFromString(stream.bufferedReader().readText())
	}
}
