package app.shosetsu.android.viewmodel.abstracted

import app.shosetsu.android.common.enums.ReadingStatus
import app.shosetsu.android.view.uimodels.model.UpdatesUI
import app.shosetsu.android.viewmodel.base.IsOnlineCheckViewModel
import app.shosetsu.android.viewmodel.base.ShosetsuViewModel
import app.shosetsu.android.viewmodel.base.StartUpdateManagerViewModel
import app.shosetsu.android.viewmodel.base.SubscribeViewModel
import kotlinx.coroutines.flow.Flow
import org.joda.time.DateTime

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
 * 29 / 04 / 2020
 *
 * @author github.com/doomsdayrs
 */
abstract class AUpdatesViewModel
	: ShosetsuViewModel(),
	SubscribeViewModel<Map<DateTime, List<UpdatesUI>>>,
	StartUpdateManagerViewModel, IsOnlineCheckViewModel {

	abstract val isRefreshing: Flow<Boolean>

	abstract suspend fun updateChapter(updateUI: UpdatesUI, readingStatus: ReadingStatus)
}