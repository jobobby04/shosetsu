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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.google.accompanist.swiperefresh.SwipeRefreshState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun rememberFakeSwipeRefreshState(): FakeSwipeRefreshState {
	val scope = rememberCoroutineScope()
	return remember(scope) { FakeSwipeRefreshState(scope) }
}

class FakeSwipeRefreshState(private val scope: CoroutineScope) {
	val state: SwipeRefreshState = SwipeRefreshState(false)

	fun animateRefresh() {
		scope.launch {
			// Fake refresh status but hide it after a second as it's a long running task
			state.isRefreshing = true
			delay(1000)
			state.isRefreshing = false
		}
	}
}