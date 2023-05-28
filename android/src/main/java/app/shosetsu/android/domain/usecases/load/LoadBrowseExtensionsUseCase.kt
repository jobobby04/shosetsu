package app.shosetsu.android.domain.usecases.load

import app.shosetsu.android.common.enums.DownloadStatus
import app.shosetsu.android.common.utils.uifactory.mapLatestToResultFlowWithFactory
import app.shosetsu.android.domain.repository.base.IExtensionDownloadRepository
import app.shosetsu.android.domain.repository.base.IExtensionsRepository
import app.shosetsu.android.dto.convertList
import app.shosetsu.android.view.uimodels.model.BrowseExtensionUI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

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
 * 13 / 05 / 2020
 */
class LoadBrowseExtensionsUseCase(
	private val extensionsRepository: IExtensionsRepository,
	private val extensionDownloadRepository: IExtensionDownloadRepository
) {
	@OptIn(ExperimentalCoroutinesApi::class)
	operator fun invoke(): Flow<List<BrowseExtensionUI>> =
		extensionsRepository.loadBrowseExtensions()
			.flatMapLatest { extensionList -> // Merge with downloadStatus
				val listOfFlows =
					extensionList.map { it to extensionDownloadRepository.getStatusFlow(it.id) }
						.map { (extensionUI, statusFlow) ->
							statusFlow.map { status ->
								extensionUI.copy(
									isInstalling = status == DownloadStatus.PENDING || status == DownloadStatus.DOWNLOADING,
								)
							}
						}

				// Merge the flows
				combine(*listOfFlows.toTypedArray()) { it.toList() }
			}
			.mapLatestToResultFlowWithFactory()
			.mapLatest { it.convertList() }

}