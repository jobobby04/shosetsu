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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.floor

@Composable
fun LazyColumnScrollbar(
	modifier: Modifier = Modifier,
	listState: LazyListState,
	rightSide: Boolean = true,
	thickness: Dp = 6.dp,
	padding: Dp = 8.dp,
	thumbMinHeight: Float = 0.1f,
	thumbColor: Color = MaterialTheme.colorScheme.primary,
	thumbSelectedColor: Color = MaterialTheme.colorScheme.secondary,
	thumbShape: Shape = CircleShape,
	content: @Composable () -> Unit
) {
	Box {
		content()
		LazyColumnScrollbar(
			modifier = modifier.align(if (rightSide) Alignment.TopEnd else Alignment.TopStart),
			listState = listState,
			rightSide = rightSide,
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
	rightSide: Boolean = true,
	thickness: Dp = 6.dp,
	padding: Dp = 8.dp,
	thumbMinHeight: Float = 0.1f,
	thumbColor: Color = MaterialTheme.colorScheme.primary,
	thumbSelectedColor: Color = MaterialTheme.colorScheme.secondary,
	thumbShape: Shape = CircleShape,
) {
	val coroutineScope = rememberCoroutineScope()

	var isSelected by remember { mutableStateOf(false) }

	var dragOffset by remember { mutableStateOf(0f) }

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

	fun normalizedOffsetPosition() = listState.layoutInfo.let {
		if (it.totalItemsCount == 0 || it.visibleItemsInfo.isEmpty()) 0f
		else it.visibleItemsInfo.first()
			.run { index.toFloat() - offset.toFloat() / size.toFloat() } / it.totalItemsCount.toFloat()
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
					translationY = constraints.maxHeight.toFloat() * normalizedOffsetPosition()
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
				.absoluteOffset(x = if (rightSide) displacement.dp else -displacement.dp)
				.fillMaxHeight(normalizedThumbSize())
				.padding(horizontal = padding)
				.width(thickness)
				.alpha(alpha)
				.background(if (isSelected) thumbSelectedColor else thumbColor, shape = thumbShape)

		)
	}
}
