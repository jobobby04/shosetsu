package app.shosetsu.android.common.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow

inline fun <T, R> Flow<T>.transformCatching(
	crossinline exceptional: suspend FlowCollector<R>.(exception: Throwable) -> Unit,
	crossinline transform: suspend FlowCollector<R>.(value: T) -> Unit
): Flow<R> = flow {
	collect { value ->
		try {
			return@collect transform(value)
		} catch (e: Throwable) {
			exceptional(e)
		}
	}
}