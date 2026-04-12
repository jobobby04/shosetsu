package app.shosetsu.android.view.compose

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.shosetsu.android.R

@Composable
fun AnimatedRefresh(
	modifier: Modifier = Modifier,
	tint: Color = LocalContentColor.current,
	size: Dp = 24.dp,
	rotationDurationMs: Int = 1000
) {
	val infiniteTransition = rememberInfiniteTransition()
	val angle by infiniteTransition.animateFloat(
		initialValue = 0f,
		targetValue = 360f,
		animationSpec = infiniteRepeatable(
			animation = tween(rotationDurationMs, easing = LinearEasing)
		)
	)
	Icon(
		imageVector = Icons.Default.Refresh,
		contentDescription = stringResource(R.string.loading),
		modifier = modifier
			.size(size)
			.graphicsLayer { rotationZ = angle },
		tint = tint
	)
}