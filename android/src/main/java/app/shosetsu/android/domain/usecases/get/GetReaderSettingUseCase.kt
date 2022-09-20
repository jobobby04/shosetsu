package app.shosetsu.android.domain.usecases.get

import android.database.sqlite.SQLiteException
import app.shosetsu.android.common.SettingKey
import app.shosetsu.android.common.ext.logE
import app.shosetsu.android.common.utils.uifactory.NovelReaderSettingConversionFactory
import app.shosetsu.android.domain.model.local.NovelReaderSettingEntity
import app.shosetsu.android.domain.repository.base.INovelReaderSettingsRepository
import app.shosetsu.android.domain.repository.base.ISettingsRepository
import app.shosetsu.android.view.uimodels.model.NovelReaderSettingUI
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

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
 * 24 / 02 / 2021
 */
class GetReaderSettingUseCase(
	private val readerRepo: INovelReaderSettingsRepository,
	private val settingsRepo: ISettingsRepository,
) {
	@ExperimentalCoroutinesApi
	operator fun invoke(novelID: Int): Flow<NovelReaderSettingUI> = flow {
		emitAll(readerRepo.getFlow(novelID).transformLatest { result ->
			if (result != null) {
				emit(result)
			} else {
				try {
					readerRepo.insert(
						NovelReaderSettingEntity(
							novelID,
							settingsRepo.getInt(SettingKey.ReaderIndentSize),
							settingsRepo.getFloat(SettingKey.ReaderParagraphSpacing),
						)
					)
				} catch (e: SQLiteException) {
					logE("Failed to insert reader settings, already inserted?", e)
				}
			}
		})
	}.map { NovelReaderSettingConversionFactory(it).convertTo() }
}