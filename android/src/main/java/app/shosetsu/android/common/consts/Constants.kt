package app.shosetsu.android.common.consts

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import app.shosetsu.android.BuildConfig

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
 * A default user agent string, should not be used
 */
const val DEFAULT_USER_AGENT =
	"Mozilla/5.0 (Linux; Android 14; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.7727.50 Mobile Safari/537.36"

/**
 * User agent for when we want to be soft and cuddly to sites.
 */
val SHOSETSU_USER_AGENT =
	"Shosetsu/${BuildConfig.VERSION_NAME} " +
		"(Linux; Android ${Build.VERSION.RELEASE}; ${Build.MODEL})"

const val SELECTED_STROKE_WIDTH: Int = 4

/**
 * Size of sub text, used for description
 */
val SUB_TEXT_SIZE: TextStyle
	@Composable
	get() = MaterialTheme.typography.bodySmall

/**
 * File system directory for source files
 */
private const val FILE_SOURCE_DIR: String = "src/"

/**
 * File system directory for extension scripts
 */
const val FILE_SCRIPT_DIR: String = "$FILE_SOURCE_DIR/scripts/"

/**
 * File system directory for library scripts
 */
const val FILE_LIBRARY_DIR: String = "$FILE_SOURCE_DIR/libraries/"


/**
 * Directory on the repository that contains the extensions,
 * proceeding this will be the extension language
 */
const val REPO_SOURCE_DIR: String = "/src/"

const val APP_UPDATE_CACHE_FILE = "SHOSETSU_APP_UPDATE.json"

const val APK_MIME = "application/vnd.android.package-archive"


/**
 * Constant of twenty minutes
 */
const val MAX_CONTINUOUS_READING_TIME: Long = 1000L * 60 * 20

/**
 * The version of backups this build of shosetsu supports
 */
const val VERSION_BACKUP: String = "1.3.0"
const val BACKUP_FILE_EXTENSION = "sbk"

/**
 * Links
 */
const val URL_HELP_REPOSITORY = "https://shosetsu.app/help/guides/repositories.html"
const val URL_HELP_BROWSE = "https://shosetsu.app/help/guides/browse.html"
const val URL_HELP_SHARE = "https://shosetsu.app/help/guides/share.html"
const val URL_HELP_CSS = "https://shosetsu.app/help/guides/css.html"
const val URL_WEBSITE = "https://shosetsu.app"
const val URL_PRIVACY = "https://shosetsu.app/privacy"
const val URL_DISCLAIMER = "https://shosetsu.app/disclaimer"
const val URL_APP_REPO = "https://gitlab.com/shosetsuorg/shosetsu"
const val URL_EXTENSIONS_REPO = "https://gitlab.com/shosetsuorg/extensions"
const val URL_PATREON = "https://www.patreon.com/doomsdayrs"
const val URL_KOFI = "https://ko-fi.com/doomsdayrs"
const val URL_DISCORD = "https://discord.gg/ttSX7gB"
const val URL_MATRIX = "https://matrix.to/#/#shosetsu:matrix.org"
