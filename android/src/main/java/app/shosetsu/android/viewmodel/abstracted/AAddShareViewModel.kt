package app.shosetsu.android.viewmodel.abstracted

import app.shosetsu.android.domain.model.local.NovelEntity
import app.shosetsu.android.viewmodel.base.ShosetsuViewModel
import app.shosetsu.lib.share.ExtensionLink
import app.shosetsu.lib.share.NovelLink
import app.shosetsu.lib.share.RepositoryLink
import kotlinx.coroutines.flow.StateFlow
import javax.security.auth.Destroyable

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
 * 01 / 10 / 2020
 */
abstract class AAddShareViewModel : ShosetsuViewModel(), Destroyable {

	abstract val isAdding: StateFlow<Boolean>
	abstract val isComplete: StateFlow<Boolean>

	/**
	 * Prompts UI to allow user to open novel
	 */
	abstract val isNovelOpenable: StateFlow<Boolean>

	abstract val isNovelAlreadyPresent: StateFlow<Boolean>
	abstract val isStyleAlreadyPresent: StateFlow<Boolean>
	abstract val isExtAlreadyPresent: StateFlow<Boolean>
	abstract val isRepoAlreadyPresent: StateFlow<Boolean>

	abstract val isProcessing: StateFlow<Boolean>

	abstract val isURLValid: StateFlow<Boolean>

	abstract val novelLink: StateFlow<NovelLink?>
	abstract val extLink: StateFlow<ExtensionLink?>
	abstract val repoLink: StateFlow<RepositoryLink?>

	abstract val showURLInput: StateFlow<Boolean>
	abstract val url: StateFlow<String>
	abstract fun setURL(url: String)
	abstract fun applyURL()

	abstract val exception: StateFlow<Exception?>

	/**
	 * Take the data from a correct QR code
	 */
	abstract fun takeData(url: String)

	/**
	 * Set that the QR code scanned is invalid
	 */
	abstract fun setInvalidURL()

	abstract fun setUserCancelled()

	abstract fun add()

	abstract fun retry()

	abstract fun getNovel(): NovelEntity?
}