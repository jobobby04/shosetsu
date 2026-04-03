package app.shosetsu.android.view.compose

import android.text.format.DateUtils
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringResource
import app.shosetsu.android.R
import kotlin.time.Duration.Companion.minutes

@Composable
@ReadOnlyComposable
fun relativeTimeSpanString(epochMillis: Long): String {
	val now = System.currentTimeMillis()
	return when {
		epochMillis <= 0L -> stringResource(R.string.relative_time_span_never)
		now - epochMillis < 1.minutes.inWholeMilliseconds -> stringResource(R.string.updates_last_update_info_just_now)
		else -> DateUtils.getRelativeTimeSpanString(epochMillis, now, 60_000).toString()
	}
}
