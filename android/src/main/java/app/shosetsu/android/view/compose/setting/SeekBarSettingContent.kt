package app.shosetsu.android.view.compose.setting

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import app.shosetsu.android.common.SettingKey
import app.shosetsu.android.common.ext.launchIO
import app.shosetsu.android.domain.repository.base.ISettingsRepository
import app.shosetsu.android.view.compose.DiscreteSlider
import app.shosetsu.android.view.uimodels.StableHolder
import kotlin.math.roundToInt

@Composable
fun SliderSettingContent(
	title: String,
	description: String,
	valueRange: StableHolder<IntRange>,
	parseValue: (Int) -> String,
	repo: ISettingsRepository,
	key: SettingKey<Int>,
	modifier: Modifier = Modifier,
	haveSteps: Boolean = true,
	manipulateUpdate: ((Int) -> Int)? = null,
	maxHeaderSize: Dp? = null
) {
	val choice by repo.getIntFlow(key).collectAsState()

	GenericBottomSettingLayout(
		title,
		description,
		modifier,
	) {
		DiscreteSlider(
			choice,
			parseValue(choice),
			{ it, a ->
				launchIO {
					if (manipulateUpdate != null && !a)
						repo.setInt(key, manipulateUpdate(it))
					else
						repo.setInt(key, it)
				}
			},
			valueRange,
			haveSteps = haveSteps,
			maxHeaderSize
		)
	}
}

@Composable
fun FloatSliderSettingContent(
	title: String,
	description: String,
	valueRange: StableHolder<IntRange>,
	parseValue: (Float) -> String,
	repo: ISettingsRepository,
	key: SettingKey<Float>,
	modifier: Modifier = Modifier,
	haveSteps: Boolean = true,
	flip: Boolean = false,
	maxHeaderSize: Dp? = null
) {
	val choice by repo.getFloatFlow(key).collectAsState()

	GenericBottomSettingLayout(
		title,
		description,
		modifier,
	) {
		DiscreteSlider(
			choice,
			parseValue(choice),
			{ newValue, fromDialog ->
				launchIO {
					repo.setFloat(
						key,
						if (flip && !fromDialog)
							newValue.roundToInt().toFloat()
						else newValue
					)
				}
			},
			valueRange,
			haveSteps,
			maxHeaderSize
		)
	}
}
