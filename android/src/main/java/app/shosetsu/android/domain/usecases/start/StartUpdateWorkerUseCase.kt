package app.shosetsu.android.domain.usecases.start

import androidx.work.Data
import androidx.work.await
import app.shosetsu.android.backend.workers.onetime.NovelUpdateWorker
import app.shosetsu.android.common.ext.launchIO

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
 * shosetsu
 * 23 / 06 / 2020
 */
class StartUpdateWorkerUseCase(
	private val manager: NovelUpdateWorker.Manager
) {
	/**
	 * Starts the update worker
	 *
	 * @param categoryID The category to update, if -1 will update all
	 * @param override if true then will override the current update loop
	 */
	operator fun invoke(categoryID: Int, override: Boolean = false) {
		launchIO {
			// Check if the update worker is running
			if (manager.isRunning())
			// Check if we can override it
				if (override)
				// Kill the old one since we can
					manager.stop().await()
				else
					return@launchIO

			// Was a category given?
			if (categoryID >= 0) {
				// Pass category over
				manager.start(
					Data.Builder()
						.putInt(NovelUpdateWorker.KEY_CATEGORY, categoryID)
						.build()
				)
			} else {
				// Update all categories
				manager.start()
			}
		}
	}
}