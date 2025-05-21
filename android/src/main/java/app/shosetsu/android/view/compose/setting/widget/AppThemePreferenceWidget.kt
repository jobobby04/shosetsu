package app.shosetsu.android.view.compose.setting.widget

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import app.shosetsu.android.R
import app.shosetsu.android.common.enums.AppThemes
import kotlinx.collections.immutable.toImmutableList

@Composable
internal fun AppThemeModePreferenceWidget(
	value: AppThemes,
	onItemClick: (AppThemes) -> Unit,
) {
	BasePreferenceWidget(
		subcomponent = {
			MultiChoiceSegmentedButtonRow(
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = PrefsHorizontalPadding),
			) {
				val names = stringArrayResource(R.array.application_themes)
					.toList()
					.toImmutableList()
				val options = AppThemes.entries.associateWith { names[it.key] }
				options.onEachIndexed { index, (mode, labelRes) ->
					SegmentedButton(
						checked = mode == value,
						onCheckedChange = { onItemClick(mode) },
						shape = SegmentedButtonDefaults.itemShape(
							index,
							options.size,
						),
					) {
						Text(labelRes)
					}
				}
			}
		},
	)
}
