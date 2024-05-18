package app.shosetsu.android.common.enums

/*
 * This file is part of Shosetsu.
 *
 * Shosetsu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Shosetsu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Shosetsu.  If not, see <https://www.gnu.org/licenses/>.
 */

/**
 * shosetsu
 * 08 / 12 / 2020
 */
enum class NovelCardType(
	private val code: Int
) {
	NORMAL(0),
	COMPRESSED(1),
	COZY(2),
	EXTENDED(3), ;

	fun toInt(): Int = code

	companion object {
		fun valueOf(code: Int): NovelCardType = values().find { it.code == code } ?: NORMAL
	}
}