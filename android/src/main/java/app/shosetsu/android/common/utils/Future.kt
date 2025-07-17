package app.shosetsu.android.common.utils

import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * @see androidx.work.await
 */
suspend inline fun <R> ListenableFuture<R>.await(): R {
	// Mirrors androidx.work.await
	if (isDone) {
		try {
			return get()
		} catch (e: ExecutionException) {
			throw e.cause ?: e
		}
	}
	return suspendCancellableCoroutine { continuation ->
		addListener(
			{
				try {
					continuation.resume(get())
				} catch (e: Throwable) {
					val cause = e.cause ?: e
					when (e) {
						is CancellationException -> continuation.cancel(cause)
						else -> continuation.resumeWithException(e)
					}
				}
			},
			{ it.run() }
		)

		continuation.invokeOnCancellation {
			cancel(false)
		}
	}
}