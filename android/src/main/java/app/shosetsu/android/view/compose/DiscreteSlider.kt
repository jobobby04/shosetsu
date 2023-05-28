package app.shosetsu.android.view.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import app.shosetsu.android.R
import app.shosetsu.android.view.uimodels.StableHolder
import kotlin.math.roundToInt

@Preview
@Composable
fun PreviewSeekBar() {
	var value by remember { mutableStateOf(1) }
	ShosetsuCompose {
		Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(16.dp)
		) {
			DiscreteSlider(
				value,
				"${value}h",
				{ it, _ ->
					value = it
				},
				StableHolder(0..10),
			)
		}
	}
}

/**
 * This creates a sudo discrete slider
 *
 * @param value Value to set [Slider] to
 * @param parsedValue [value] parsed to be displayed as a string
 * @param updateValue Called when [Slider] updates its value, is fed a rounded float
 * @param valueRange An integer range of possible values
 */
@Composable
fun DiscreteSlider(
	value: Int,
	parsedValue: String,
	updateValue: (Int, fromDialog: Boolean) -> Unit,
	valueRange: StableHolder<IntRange>,
	haveSteps: Boolean = true,
	maxHeaderSize: Dp? = null,
) {
	Row(
		verticalAlignment = Alignment.CenterVertically
	) {
		var showDialog by remember { mutableStateOf(false) }

		if (showDialog) {
			DiscreteSliderDialog(value, valueRange, { showDialog = false }, updateValue)
		}

		TextButton(onClick = {
			showDialog = true
		}) {
			Text(
				text = parsedValue,
				modifier = Modifier.let {
					if (maxHeaderSize != null)
						it.width(maxHeaderSize)
					else it
				}
			)
		}
		Slider(
			value.toFloat(),
			{
				updateValue(it.roundToInt(), false)
			},
			valueRange = valueRange.item.first.toFloat()..valueRange.item.last.toFloat(),
			steps = if (haveSteps) valueRange.item.count() - 2 else 0
		)
	}
}

/**
 * This creates a sudo discrete slider
 *
 * @param value Value to set [Slider] to
 * @param parsedValue [value] parsed to be displayed as a string
 * @param updateValue Called when [Slider] updates its value
 * @param valueRange An integer range of possible values
 */
@Composable
fun DiscreteSlider(
	value: Float,
	parsedValue: String,
	updateValue: (Float, fromDialog: Boolean) -> Unit,
	valueRange: StableHolder<IntRange>,
	haveSteps: Boolean = true,
	maxHeaderSize: Dp? = null
) {
	Row(
		verticalAlignment = Alignment.CenterVertically
	) {
		var showDialog by remember { mutableStateOf(false) }

		if (showDialog)
			DiscreteSliderDialog(value, valueRange, { showDialog = false }, updateValue)


		TextButton(onClick = {
			showDialog = true
		}) {
			Text(
				text = parsedValue,
				modifier = Modifier.let {
					if (maxHeaderSize != null)
						it.width(maxHeaderSize)
					else it
				}
			)
		}
		Slider(
			value,
			{
				updateValue(it, false)
			},
			valueRange = valueRange.item.first.toFloat()..valueRange.item.last.toFloat(),
			steps = if (haveSteps) valueRange.item.count() - 2 else 0
		)
	}
}

@Composable
fun DiscreteSliderDialog(
	value: Int,
	valueRange: StableHolder<IntRange>,
	onDismissRequest: () -> Unit,
	updateValue: (Int, fromDialog: Boolean) -> Unit,
) {
	DiscreteSliderDialog(
		title = stringResource(R.string.input_int),
		description = stringResource(R.string.input_int_range_desc),
		value,
		valueRange,
		onDismissRequest,
		castInput = {
			it.toIntOrNull() ?: valueRange.item.first
		},
		validateInput = {
			it.isDigitsOnly() && it.isNotEmpty() && it.toInt() in valueRange.item
		},
		updateValue
	)
}

@Composable
fun DiscreteSliderDialog(
	value: Float,
	valueRange: StableHolder<IntRange>,
	onDismissRequest: () -> Unit,
	updateValue: (Float, fromDialog: Boolean) -> Unit,
) {
	DiscreteSliderDialog(
		title = stringResource(R.string.input_float),
		description = stringResource(R.string.input_float_range_desc),
		value,
		valueRange,
		onDismissRequest,
		castInput = {
			it.toFloatOrNull() ?: valueRange.item.first.toFloat()
		},
		validateInput = { newValue ->
			newValue.matches(Regex("(^[0-9]+$)|(^[0-9]+\\.[0-9]*$)")) &&
					newValue.toFloat()
						.let { valueRange.item.first <= it && it <= valueRange.item.last }
		},
		updateValue
	)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> DiscreteSliderDialog(
	title: String,
	description: String,
	value: T,
	valueRange: StableHolder<IntRange>,
	onDismissRequest: () -> Unit,
	castInput: (String) -> T,
	validateInput: (String) -> Boolean,
	updateValue: (T, fromDialog: Boolean) -> Unit,
) {
	var fieldContent: String by remember { mutableStateOf("$value") }
	var isTextValid by remember { mutableStateOf(true) }

	androidx.compose.material.AlertDialog(
		onDismissRequest,
		title = {
			Text(
				title,
				style = MaterialTheme.typography.titleLarge,
				modifier = Modifier.padding(
					bottom = 16.dp,
					top = 8.dp,
					start = 24.dp,
					end = 24.dp
				)
			)
		},
		text = {
			Column {
				Text(
					description.format(
						valueRange.item.first,
						valueRange.item.last
					),
					style = MaterialTheme.typography.bodyLarge,
					modifier = Modifier.padding(
						bottom = 16.dp,
						start = 24.dp,
						end = 24.dp
					)
				)
				TextField(
					value = fieldContent,
					onValueChange = { newString ->
						// Set text as error if the value is invalid
						isTextValid = validateInput(newString)

						fieldContent = newString
					},
					singleLine = true,
					keyboardOptions = KeyboardOptions(
						keyboardType = KeyboardType.Number
					),
					modifier = Modifier
						.padding(bottom = 8.dp, start = 24.dp, end = 24.dp)
						.fillMaxWidth()
				)
			}
		},
		dismissButton = {
			androidx.compose.material3.TextButton(
				onClick = onDismissRequest,
			) {
				Text(stringResource(android.R.string.cancel))
			}
		},
		confirmButton = {
			androidx.compose.material3.TextButton(
				onClick = {
					updateValue(
						castInput(fieldContent),
						true
					)
					onDismissRequest()
				},
				enabled = isTextValid
			) {
				Text(stringResource(R.string.apply))
			}
		}
	)
}
