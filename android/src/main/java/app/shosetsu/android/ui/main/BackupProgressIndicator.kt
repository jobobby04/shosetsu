package app.shosetsu.android.ui.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.shosetsu.android.R
import app.shosetsu.android.ui.theme.ShosetsuTheme

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
 * @since 25 / 12 / 2023
 * @author Doomsdayrs
 */

@Composable
fun BackupProgressIndicator() {
	Card(
		colors = CardDefaults.elevatedCardColors(
			containerColor = Color.Red
		),
		modifier = Modifier.padding(8.dp)
	) {
		Column(
			Modifier
				.padding(16.dp)
				.fillMaxWidth(),
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			Text(
				stringResource(R.string.activity_main_backup_in_progress),
				style = MaterialTheme.typography.titleMedium
			)
			Text(
				stringResource(R.string.activity_main_backup_in_progress_warning),
				style = MaterialTheme.typography.titleSmall
			)
		}
	}
}

@Preview
@Composable
fun PreviewBackupProgressIndicator() {
	ShosetsuTheme {
		BackupProgressIndicator()
	}
}