package app.shosetsu.android.view.compose

import android.view.ViewConfiguration
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFirstOrNull
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastLastOrNull
import androidx.compose.ui.util.fastMaxBy
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.sample
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun LazyColumnScrollbar(
	modifier: Modifier = Modifier,
	state: LazyListState = rememberLazyListState(),
	contentPadding: PaddingValues = PaddingValues(0.dp),
	reverseLayout: Boolean = false,
	verticalArrangement: Arrangement.Vertical =
		if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
	horizontalAlignment: Alignment.Horizontal = Alignment.Start,
	userScrollEnabled: Boolean = true,
	content: LazyListScope.() -> Unit,
) {
	VerticalFastScroller(
		listState = state,
		modifier = modifier,
		topContentPadding = contentPadding.calculateTopPadding(),
		endContentPadding = contentPadding.calculateEndPadding(LocalLayoutDirection.current),
	) {
		LazyColumn(
			state = state,
			contentPadding = contentPadding,
			reverseLayout = reverseLayout,
			verticalArrangement = verticalArrangement,
			horizontalAlignment = horizontalAlignment,
			userScrollEnabled = userScrollEnabled,
			content = content,
		)
	}
}

@Composable
fun VerticalFastScroller(
	listState: LazyListState,
	modifier: Modifier = Modifier,
	thumbAllowed: () -> Boolean = { true },
	thumbColor: Color = MaterialTheme.colorScheme.primary,
	topContentPadding: Dp = Dp.Hairline,
	bottomContentPadding: Dp = Dp.Hairline,
	endContentPadding: Dp = Dp.Hairline,
	content: @Composable () -> Unit,
) {
	SubcomposeLayout(modifier = modifier) { constraints ->
		val contentPlaceable = subcompose("content", content).map { it.measure(constraints) }
		val contentHeight = contentPlaceable.fastMaxBy { it.height }?.height ?: 0
		val contentWidth = contentPlaceable.fastMaxBy { it.width }?.width ?: 0

		val scrollerConstraints = constraints.copy(minWidth = 0, minHeight = 0)
		val scrollerPlaceable = subcompose("scroller") {
			val layoutInfo = listState.layoutInfo
			if (layoutInfo.visibleItemsInfo.isEmpty() || layoutInfo.totalItemsCount == 0) return@subcompose

			val thumbTopPadding = with(LocalDensity.current) { topContentPadding.toPx() }
			var thumbOffsetY by remember(thumbTopPadding) { mutableFloatStateOf(thumbTopPadding) }

			val dragInteractionSource = remember { MutableInteractionSource() }
			val isThumbDragged by dragInteractionSource.collectIsDraggedAsState()
			val scrolled = remember {
				MutableSharedFlow<Unit>(
					extraBufferCapacity = 1,
					onBufferOverflow = BufferOverflow.DROP_OLDEST,
				)
			}

			// listState.isScrollInProgress occasionally flickers
			val scrollStateTracker = remember { MutableData(listState.isScrollInProgress) }
			val stableScrollInProgress = scrollStateTracker.value || listState.isScrollInProgress
			scrollStateTracker.value = listState.isScrollInProgress
			val anyScrollInProgress = stableScrollInProgress || isThumbDragged

			val thumbBottomPadding = with(LocalDensity.current) { bottomContentPadding.toPx() }
			val heightPx = contentHeight.toFloat() -
					thumbTopPadding -
					thumbBottomPadding -
					listState.layoutInfo.afterContentPadding
			val thumbHeightPx = with(LocalDensity.current) { ThumbLength.toPx() }
			val trackHeightPx = heightPx - thumbHeightPx
			val scrollHeightPx = contentHeight.toFloat() -
					listState.layoutInfo.beforeContentPadding -
					listState.layoutInfo.afterContentPadding -
					thumbBottomPadding

			val visibleItems = layoutInfo.visibleItemsInfo
			val topItem = visibleItems.fastFirstOrNull {
				it.bottom >= 0 &&
						(it.key as? String)?.startsWith(STICKY_HEADER_KEY_PREFIX)?.not() ?: true
			} ?: visibleItems.first()
			val bottomItem = visibleItems.fastLastOrNull {
				it.top <= scrollHeightPx &&
						(it.key as? String)?.startsWith(STICKY_HEADER_KEY_PREFIX)?.not() ?: true
			} ?: visibleItems.last()

			val topHiddenProportion = -1f * topItem.top / topItem.size.coerceAtLeast(1)
			val bottomHiddenProportion = (bottomItem.bottom - scrollHeightPx) / bottomItem.size.coerceAtLeast(1)
			val previousSections = topHiddenProportion + topItem.index
			val remainingSections = bottomHiddenProportion + (layoutInfo.totalItemsCount - (bottomItem.index + 1))
			val scrollableSections = previousSections + remainingSections

			val layoutChangeTracker = remember { MutableData(scrollableSections) }
			val layoutChanged = !anyScrollInProgress && abs(layoutChangeTracker.value - scrollableSections) > 0.1
			layoutChangeTracker.value = scrollableSections

			val estimateConfidence = remember { MutableData(remainingSections) }
			if (layoutChanged) estimateConfidence.value = remainingSections
			val maxRemainingSections = remember(estimateConfidence.value) { scrollableSections }
			estimateConfidence.value = max(estimateConfidence.value, remainingSections)

			if (maxRemainingSections < 0.5) return@subcompose

			// When thumb dragged
			LaunchedEffect(thumbOffsetY) {
				if (layoutInfo.totalItemsCount == 0 || !isThumbDragged) return@LaunchedEffect
				val thumbProportion = (thumbOffsetY - thumbTopPadding) / trackHeightPx
				if (thumbProportion <= 0.001f) {
					estimateConfidence.value = -1f
					listState.scrollToItem(index = 0, scrollOffset = 0)
					scrolled.tryEmit(Unit)
					return@LaunchedEffect
				}
				val scrollRemainingSections = (1f - thumbProportion) * maxRemainingSections
				val currentSection = layoutInfo.totalItemsCount - scrollRemainingSections
				val scrollSectionIndex = currentSection.toInt().coerceAtMost(layoutInfo.totalItemsCount)
				val expectedScrollItem = visibleItems.find { it.index == scrollSectionIndex } ?: visibleItems.first()
				val scrollRelativeOffset = expectedScrollItem.size * (currentSection - scrollSectionIndex)
				val scrollSectionOffset = (scrollRelativeOffset - scrollHeightPx).roundToInt()
				val scrollItemIndex = scrollSectionIndex.coerceIn(0, layoutInfo.totalItemsCount - 1)
				val scrollItemOffset = scrollSectionOffset + (scrollSectionIndex - scrollItemIndex) * bottomItem.size
				listState.scrollToItem(index = scrollItemIndex, scrollOffset = scrollItemOffset)
				scrolled.tryEmit(Unit)
			}

			// When list scrolled
			if (layoutInfo.totalItemsCount != 0 && !isThumbDragged) {
				val proportion = 1f - remainingSections / maxRemainingSections
				thumbOffsetY = trackHeightPx * proportion + thumbTopPadding
				if (stableScrollInProgress) scrolled.tryEmit(Unit)
			}

			// Thumb alpha
			val alpha = remember { Animatable(0f) }
			val isThumbVisible = alpha.value > 0f
			LaunchedEffect(scrolled, alpha) {
				scrolled
					.sample(100)
					.collectLatest {
						if (thumbAllowed()) {
							alpha.snapTo(1f)
							delay(ScrollBarVisibilityDuration)
							alpha.animateTo(0f, animationSpec = ImmediateFadeOutAnimationSpec)
						} else {
							alpha.animateTo(0f, animationSpec = ImmediateFadeOutAnimationSpec)
						}
					}
			}

			Box(
				modifier = Modifier
					.offset { IntOffset(0, thumbOffsetY.roundToInt()) }
					.then(
						// Recompose opts
						if (isThumbVisible && !listState.isScrollInProgress) {
							Modifier.draggable(
								interactionSource = dragInteractionSource,
								orientation = Orientation.Vertical,
								state = rememberDraggableState { delta ->
									val newOffsetY = thumbOffsetY + delta
									thumbOffsetY = newOffsetY.coerceIn(
										thumbTopPadding,
										thumbTopPadding + trackHeightPx,
									)
								},
							)
						} else {
							Modifier
						},
					)
					.then(
						// Exclude thumb from gesture area only when needed
						if (isThumbVisible && !isThumbDragged && !listState.isScrollInProgress) {
							Modifier.systemGestureExclusion()
						} else {
							Modifier
						},
					)
					.height(ThumbLength)
					.padding(horizontal = 8.dp)
					.padding(end = endContentPadding)
					.width(ThumbThickness)
					.alpha(alpha.value)
					.background(color = thumbColor, shape = ThumbShape),
			)
		}.map { it.measure(scrollerConstraints) }
		val scrollerWidth = scrollerPlaceable.fastMaxBy { it.width }?.width ?: 0

		layout(contentWidth, contentHeight) {
			contentPlaceable.fastForEach {
				it.place(0, 0)
			}
			scrollerPlaceable.fastForEach {
				it.placeRelative(contentWidth - scrollerWidth, 0)
			}
		}
	}
}

private class MutableData<T>(var value: T)

private val ThumbLength = 48.dp
private val ThumbThickness = 12.dp
private val ThumbShape = RoundedCornerShape(ThumbThickness / 2)
private val ScrollBarVisibilityDuration = 2000L.milliseconds
private val ImmediateFadeOutAnimationSpec = tween<Float>(
	durationMillis = ViewConfiguration.getScrollBarFadeDuration(),
)

private val LazyListItemInfo.top: Int
	get() = offset

private val LazyListItemInfo.bottom: Int
	get() = offset + size
