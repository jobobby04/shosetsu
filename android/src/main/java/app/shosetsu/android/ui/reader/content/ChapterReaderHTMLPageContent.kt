package app.shosetsu.android.ui.reader.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.shosetsu.android.R
import app.shosetsu.android.ui.reader.page.HTMLPage
import app.shosetsu.android.view.compose.ErrorAction
import app.shosetsu.android.view.compose.ErrorContent
import app.shosetsu.android.view.uimodels.model.reader.ReaderUIItem
import app.shosetsu.android.viewmodel.abstracted.AChapterReaderViewModel
import kotlinx.coroutines.flow.Flow

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
 * @since 26 / 05 / 2022
 * @author Doomsdayrs
 */
/**
 * Creates the HTML page
 */
@Suppress("FunctionName")
@Composable
fun ChapterReaderHTMLContent(
	item: ReaderUIItem.ReaderChapterUI,
	progressFlow: () -> Flow<Double>,
	getHTMLContent: (item: ReaderUIItem.ReaderChapterUI) -> Flow<AChapterReaderViewModel.ChapterPassage>,
	retryChapter: (item: ReaderUIItem.ReaderChapterUI) -> Unit,
	onScroll: (item: ReaderUIItem.ReaderChapterUI, perc: Double) -> Unit,
	onClick: () -> Unit,
	onDoubleClick: () -> Unit
) {
	val html by remember(item) {
		getHTMLContent(item)
	}.collectAsState(AChapterReaderViewModel.ChapterPassage.Loading)

	when (html) {
		is AChapterReaderViewModel.ChapterPassage.Error -> {
			val throwable = (html as? AChapterReaderViewModel.ChapterPassage.Error)?.throwable
			ErrorContent(
				throwable?.message
					?: "Unknown error",
				ErrorAction(R.string.retry) {
					retryChapter(item)
				},
				stackTrace = throwable?.stackTraceToString()
			)
		}

		AChapterReaderViewModel.ChapterPassage.Loading -> {
			Box(
				Modifier
					.background(MaterialTheme.colors.background)
					.fillMaxSize()
			) {
				LinearProgressIndicator(
					modifier = Modifier
						.fillMaxWidth()
						.align(Alignment.TopCenter)
				)
			}
		}

		is AChapterReaderViewModel.ChapterPassage.Success -> {
			val progress by remember { progressFlow() }.collectAsState(0.0)
			HTMLPage(
				html = (html as AChapterReaderViewModel.ChapterPassage.Success).content,
				progress = progress,
				onScroll = {
					onScroll(item, it)
				},
				onClick = onClick,
				onDoubleClick = onDoubleClick
			)
		}
	}
}