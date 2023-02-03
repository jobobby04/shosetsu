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
 *
 */

package app.shosetsu.android.view.compose

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshState
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@ExperimentalMaterialApi
@Composable
fun rememberFakePullRefreshState(onRefresh: () -> Unit): Pair<Boolean, PullRefreshState> {
	val scope = rememberCoroutineScope()
	val fakePullRefreshState = remember(scope) { FakePullRefreshState(scope) }
	return fakePullRefreshState.isRefreshing to rememberPullRefreshState(
		refreshing = fakePullRefreshState.isRefreshing,
		onRefresh = {
			fakePullRefreshState.animateRefresh()
			onRefresh()
		}
	)
}

class FakePullRefreshState(private val scope: CoroutineScope) {
	var isRefreshing by mutableStateOf(false)
	fun animateRefresh() {
		scope.launch {
			// Fake refresh status but hide it after a second as it's a long running task
			isRefreshing = true
			delay(1000)
			isRefreshing = false
		}
	}
}
