package app.shosetsu.android.viewmodel.impl

import app.shosetsu.android.backend.workers.onetime.AppUpdateCheckWorker
import app.shosetsu.android.common.ext.launchIO
import app.shosetsu.android.domain.model.local.Contributor
import app.shosetsu.android.domain.repository.base.ContributorsRepository
import app.shosetsu.android.viewmodel.abstracted.AAboutViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

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
class AboutViewModel(
	private val manager: AppUpdateCheckWorker.Manager,
	private val contributorRepo: ContributorsRepository
) : AAboutViewModel() {

	override fun appUpdateCheck() {
		launchIO {
			if (!manager.isRunning())
				manager.start()
		}
	}

	override val contributors: ImmutableList<Contributor> =
		contributorRepo.getAll().toImmutableList()
}