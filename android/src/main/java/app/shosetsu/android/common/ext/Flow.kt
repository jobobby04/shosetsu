package app.shosetsu.android.common.ext

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext

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
 *
 * @since 14 / 02 / 2022
 * @author Doomsdayrs
 */

fun <T : Any?> Flow<T>.collectLA(
	owner: LifecycleOwner,
	catch: suspend FlowCollector<T>.(Throwable) -> Unit,
	onCollect: FlowCollector<T>
) = flowWithLifecycle(owner.lifecycle)
	.catch(catch)
	.onEach(onCollect::emit)
	.launchIn(owner.lifecycleScope)

fun <T> Flow<T>.firstLa(
	owner: LifecycleOwner,
	catch: suspend FlowCollector<T>.(Throwable) -> Unit,
	onCollect: (T) -> Unit
) = flowWithLifecycle(owner.lifecycle)
	.take(1)
	.catch(catch)
	.onEach(onCollect)
	.launchIn(owner.lifecycleScope)

fun <T> Flow<T>.collectLatestLA(
	owner: LifecycleOwner,
	catch: suspend FlowCollector<T>.(Throwable) -> Unit,
	onCollect: FlowCollector<T>
) = flowWithLifecycle(owner.lifecycle)
	.catch(catch)
	.mapLatest(onCollect::emit)
	.launchIn(owner.lifecycleScope)

/**
 * Run the flow on the IO dispatcher
 */
fun <T> Flow<T>.onIO() = flowOn(Dispatchers.IO)

/**
 * Run the following suspend code on the IO dispatcher
 */
suspend fun <T> onIO(block: suspend CoroutineScope.() -> T) =
	withContext(Dispatchers.IO, block)