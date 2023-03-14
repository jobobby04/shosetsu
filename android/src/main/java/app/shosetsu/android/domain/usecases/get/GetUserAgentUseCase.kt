package app.shosetsu.android.domain.usecases.get

import app.shosetsu.android.common.SettingKey
import app.shosetsu.android.common.consts.SHOSETSU_USER_AGENT
import app.shosetsu.android.domain.repository.base.ISettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.transform

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
 * @since 14 / 03 / 2023
 * @author Doomsdayrs
 */
class GetUserAgentUseCase(
	private val settingsRepo: ISettingsRepository
) {
	suspend operator fun invoke(): String =
		if (settingsRepo.getBoolean(SettingKey.UseShosetsuAgent)) {
			SHOSETSU_USER_AGENT
		} else {
			settingsRepo.getString(SettingKey.UserAgent)
		}

	fun flow(): Flow<String> =
		settingsRepo.getBooleanFlow(SettingKey.UseShosetsuAgent).transform { useShosetsu ->
			if (useShosetsu) {
				emit(SHOSETSU_USER_AGENT)
			} else {
				emitAll(settingsRepo.getStringFlow(SettingKey.UserAgent))
			}
		}
}