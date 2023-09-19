package app.shosetsu.android.view

import androidx.activity.compose.BackHandler
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.holix.android.bottomsheetdialog.compose.BottomSheetDialogProperties
import com.holix.android.bottomsheetdialog.compose.BottomSheetDialog as OBottomSheetDialog


/*
 * This file is part of shosetsu.
 *
 * shosetsu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * shosetsu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with shosetsu.  If not, see <https://www.gnu.org/licenses/>.
 */

/**
 * Shosetsu
 *
 * @since 19 / 09 / 2023
 * @author Doomsdayrs
 */

@Composable
fun BottomSheetDialog(
	onDismissRequest: () -> Unit,
	properties: BottomSheetDialogProperties = BottomSheetDialogProperties(),
	content: @Composable () -> Unit
) {
	BackHandler(onBack = onDismissRequest)
	OBottomSheetDialog(onDismissRequest, properties) {
		Surface(tonalElevation = 10.dp) {
			content()
		}
	}
}