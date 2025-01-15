package app.shosetsu.android.view.compose

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFirstOrNull
import androidx.compose.ui.util.fastSumBy
import kotlinx.coroutines.launch
import kotlin.math.floor

@Composable
fun LazyColumnScrollbar(
	modifier: Modifier = Modifier,
	listState: LazyListState,
	thickness: Dp = 6.dp,
	padding: Dp = 8.dp,
	thumbMinHeight: Float = 0.1f,
	thumbColor: Color = MaterialTheme.colorScheme.primary,
	thumbSelectedColor: Color = MaterialTheme.colorScheme.secondary,
	thumbShape: Shape = CircleShape,
	content: @Composable () -> Unit
) {
	val direction = LocalLayoutDirection.current
	Box {
		content()
		LazyColumnScrollbar(
			modifier = modifier.align(if (direction == LayoutDirection.Ltr) Alignment.TopEnd else Alignment.TopStart),
			listState = listState,
			thickness = thickness,
			padding = padding,
			thumbMinHeight = thumbMinHeight,
			thumbColor = thumbColor,
			thumbSelectedColor = thumbSelectedColor,
			thumbShape = thumbShape,
		)
	}
}

@Composable
fun LazyColumnScrollbar(
	modifier: Modifier,
	listState: LazyListState,
	thickness: Dp = 6.dp,
	padding: Dp = 8.dp,
	thumbMinHeight: Float = 0.1f,
	thumbColor: Color = MaterialTheme.colorScheme.primary,
	thumbSelectedColor: Color = MaterialTheme.colorScheme.secondary,
	thumbShape: Shape = CircleShape,
) {
	val coroutineScope = rememberCoroutineScope()

	var isSelected by remember { mutableStateOf(false) }

	var dragOffset by remember { mutableFloatStateOf(0f) }

	fun normalizedThumbSize() = listState.layoutInfo.let {
		if (it.totalItemsCount == 0) return@let 0f
		val firstPartial = it.visibleItemsInfo.first().run { -offset.toFloat() / size.toFloat() }
		val lastPartial = it.visibleItemsInfo.last()
			.run { 1f - (it.viewportEndOffset - offset).toFloat() / size.toFloat() }
		val realVisibleSize = it.visibleItemsInfo.size.toFloat() - firstPartial - lastPartial
		realVisibleSize / it.totalItemsCount.toFloat()
	}
		.coerceAtLeast(thumbMinHeight)
		.coerceAtMost(2 * thumbMinHeight)

	fun computeStartOffset() = listState.layoutInfo.let {
		val items = it.visibleItemsInfo
		if (items.isEmpty()) return@let 0f
		val estimatedSize = items.fastSumBy { it.size }.toFloat() / items.size
		val totalSize = estimatedSize * it.totalItemsCount
		val viewportSize = it.viewportSize.height - it.beforeContentPadding - it.afterContentPadding
		items.fastFirstOrNull { (it.key as? String)?.startsWith(STICKY_HEADER_KEY_PREFIX)?.not() ?: true }
			?.run {
				it.beforeContentPadding + (estimatedSize * index - offset) / totalSize * viewportSize
			}
			?: 0f
	}

	fun setScrollOffset(newOffset: Float) {
		dragOffset = newOffset.coerceIn(0f, 1f)

		val exactIndex: Float = listState.layoutInfo.totalItemsCount.toFloat() * dragOffset
		val index: Int = floor(exactIndex).toInt()
		val remainder: Float = exactIndex - floor(exactIndex)

		coroutineScope.launch {
			listState.scrollToItem(index = index, scrollOffset = 0)
			val offset =
				listState.layoutInfo.visibleItemsInfo.firstOrNull()?.size?.let { it.toFloat() * remainder }
					?.toInt() ?: 0
			listState.scrollToItem(index = index, scrollOffset = offset)
		}
	}

	val isInAction = listState.isScrollInProgress || isSelected

	val alpha by animateFloatAsState(
		targetValue = if (isInAction) 1f else 0f,
		animationSpec = tween(
			durationMillis = if (isInAction) 75 else 500,
			delayMillis = if (isInAction) 0 else 500
		)
	)

	val displacement by animateFloatAsState(
		targetValue = if (isInAction) 0f else 14f,
		animationSpec = tween(
			durationMillis = if (isInAction) 75 else 500,
			delayMillis = if (isInAction) 0 else 500
		)
	)
	val isThumbVisible = alpha > 0f

	BoxWithConstraints(modifier.fillMaxHeight()) {
		val dragState = rememberDraggableState { delta ->
			setScrollOffset(dragOffset + delta / constraints.maxHeight.toFloat())
		}

		Box(
			Modifier
				.graphicsLayer {
					translationY = computeStartOffset()
				}
				.draggable(
					state = dragState,
					orientation = Orientation.Vertical,
					startDragImmediately = true,
					onDragStarted = {
						isSelected = true
					},
					onDragStopped = {
						isSelected = false
					},
					enabled = isThumbVisible,
				)
				.then(
					// Exclude thumb from gesture area only when needed
					if (isThumbVisible && !isSelected && !listState.isScrollInProgress) {
						Modifier.systemGestureExclusion()
					} else Modifier,
				)
				.offset(x = displacement.dp)
				.fillMaxHeight(normalizedThumbSize())
				.padding(horizontal = padding)
				.width(thickness)
				.alpha(alpha)
				.background(if (isSelected) thumbSelectedColor else thumbColor, shape = thumbShape)

		)
	}
}
