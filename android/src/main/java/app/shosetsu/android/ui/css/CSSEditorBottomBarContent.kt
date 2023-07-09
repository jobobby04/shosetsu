package app.shosetsu.android.ui.css

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.shosetsu.android.R
import app.shosetsu.android.common.consts.SUB_TEXT_SIZE

@Preview
@Composable
fun PreviewCSSEditorBottomBarContent() {
	Box(
		Modifier.fillMaxSize(),
		contentAlignment = Alignment.BottomCenter
	) {
		CSSEditorBottomBarContent(
			false,
			"Fuck you",
			{},
			true,
			{},
			true,
			{},
			{},
			{},
			true
		)
	}
}

@Composable
fun CSSEditorBottomBarContent(
	isCSSValid: Boolean,
	cssInvalidReason: String?,
	onUndo: () -> Unit,
	canUndo: Boolean,
	onPaste: () -> Unit,
	hasPaste: Boolean,
	onSave: () -> Unit,
	onExport: () -> Unit,
	onRedo: () -> Unit,
	canRedo: Boolean
) {
	Column {
		if (!isCSSValid && cssInvalidReason != null)
			Card(
				border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
				modifier = Modifier
					.align(Alignment.CenterHorizontally)
					.fillMaxWidth()
					.padding(16.dp)
			) {
				Column(
					modifier = Modifier.padding(8.dp)
				) {
					Text(
						"Invalid CSS"
					)
					Text(
						cssInvalidReason,
						style = SUB_TEXT_SIZE,
						modifier = Modifier
							.alpha(0.7f)
					)
				}
			}
		BottomAppBar {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.SpaceBetween,
				modifier = Modifier.fillMaxWidth()
			) {
				Row(
					verticalAlignment = Alignment.CenterVertically,
				) {
					IconButton(onClick = onUndo, enabled = canUndo) {
						Icon(
							painterResource(R.drawable.ic_baseline_undo_24),
							stringResource(R.string.activity_css_undo)
						)
					}

					IconButton(
						onClick = onPaste, enabled = hasPaste,
						modifier = Modifier.padding(start = 8.dp)
					) {
						Icon(
							painterResource(androidx.appcompat.R.drawable.abc_ic_menu_paste_mtrl_am_alpha),
							stringResource(R.string.activity_css_paste)
						)
					}
				}

				val shapes = MaterialTheme.shapes
				val fabShape = remember { shapes.small.copy(CornerSize(percent = 50)) }

				FloatingActionButton(
					onClick = onSave,
					shape = fabShape,
					containerColor = MaterialTheme.colorScheme.primary,
					contentColor = colorResource(android.R.color.white)
				) {
					Icon(
						painterResource(R.drawable.ic_baseline_save_24),
						stringResource(R.string.activity_css_save)
					)
				}

				Row(
					verticalAlignment = Alignment.CenterVertically,
				) {
					IconButton(
						onClick = onExport, enabled = false,
						modifier = Modifier.padding(end = 8.dp)
					) {
						Icon(
							painterResource(R.drawable.ic_baseline_save_alt_24),
							stringResource(R.string.activity_css_export)
						)
					}
					IconButton(onClick = onRedo, enabled = canRedo) {
						Icon(
							painterResource(R.drawable.ic_baseline_redo_24),
							stringResource(R.string.activity_css_redo)
						)
					}
				}
			}
		}
	}
}