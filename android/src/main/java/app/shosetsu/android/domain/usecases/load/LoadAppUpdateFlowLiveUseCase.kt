package app.shosetsu.android.domain.usecases.load

import app.shosetsu.android.domain.model.local.AppUpdateEntity
import app.shosetsu.android.domain.repository.base.IAppUpdatesRepository
import kotlinx.coroutines.flow.StateFlow

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
 * 07 / 09 / 2020
 */
class LoadAppUpdateFlowLiveUseCase(
	private val iAppUpdatesRepository: IAppUpdatesRepository
) {
	operator fun invoke(): StateFlow<AppUpdateEntity?> =
		iAppUpdatesRepository.loadAppUpdateFlow()
}