package app.shosetsu.android.ui.settings.util

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha

const val SECONDARY_ALPHA = .78f

fun Modifier.secondaryItemAlpha(): Modifier = this.alpha(SECONDARY_ALPHA)