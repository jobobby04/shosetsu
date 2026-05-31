package app.shosetsu.android.common.utils;

import androidx.compose.ui.graphics.vector.GroupComponent;
import androidx.compose.ui.graphics.vector.ImageVector;
import androidx.compose.ui.graphics.vector.VectorGroup;
import androidx.compose.ui.graphics.vector.VectorPainter;
import androidx.compose.ui.graphics.vector.VectorPainterKt;
import androidx.compose.ui.unit.Density;

/**
 * Wraps internal methods from VectorPainterKt to make them accessible.
 * This is significantly easier doing the grunt work for a composition to use rememberVectorPainter.
 */
public class VectorPainterUtil {
	public static VectorPainter createVectorPainterFromImageVector(Density density, ImageVector imageVector, GroupComponent root) {
		return VectorPainterKt.createVectorPainterFromImageVector(
			density,
			imageVector,
			root
		);
	}

	public static GroupComponent createGroupComponent(GroupComponent self, VectorGroup currentGroup) {
		return VectorPainterKt.createGroupComponent(self, currentGroup);
	}

	public static GroupComponent createGroupComponent() {
		return new GroupComponent();
	}
}
