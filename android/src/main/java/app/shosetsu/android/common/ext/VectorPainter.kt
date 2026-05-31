package app.shosetsu.android.common.ext

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.graphics.drawable.IconCompat
import app.shosetsu.android.common.utils.VectorPainterUtil
import kotlin.math.roundToInt

fun ImageVector.toIcon(
	density: Density = Density(1.0f),
	layoutDirection: LayoutDirection = LayoutDirection.Ltr,
	size: Size? = null,
	config: ImageBitmapConfig = ImageBitmapConfig.Argb8888,
): IconCompat = IconCompat.createWithBitmap(
	toImageBitmap(
		density = density,
		layoutDirection = layoutDirection,
		size = size,
		config = config,
	).asAndroidBitmap()
)

fun ImageVector.toImageBitmap(
	density: Density,
	layoutDirection: LayoutDirection,
	size: Size? = null,
	config: ImageBitmapConfig = ImageBitmapConfig.Argb8888,
): ImageBitmap {
	val painter = createVectorPainter(this, density)
	return painter.toImageBitmap(
		density = density,
		layoutDirection = layoutDirection,
		size = size,
		config = config,
	)
}

/**
 * @see androidx.compose.ui.graphics.vector.rememberVectorPainter(ImageVector)
 */
private fun createVectorPainter(image: ImageVector, density: Density): VectorPainter =
	VectorPainterUtil.createVectorPainterFromImageVector(
		density,
		image,
		VectorPainterUtil.createGroupComponent(VectorPainterUtil.createGroupComponent(), image.root)
	)

fun Painter.toImageBitmap(
	density: Density,
	layoutDirection: LayoutDirection,
	size: Size? = null,
	config: ImageBitmapConfig = ImageBitmapConfig.Argb8888,
): ImageBitmap {
	val size = (size ?: (intrinsicSize * 2f))
		.let { if (it == Size.Unspecified) Size(16f, 16f) else it }
	val image = ImageBitmap(width = size.width.roundToInt(), height = size.height.roundToInt(), config = config)
	val canvas = Canvas(image)
	CanvasDrawScope().draw(density = density, layoutDirection = layoutDirection, canvas = canvas, size = size) {
		draw(size = this.size)
	}
	return image
}
