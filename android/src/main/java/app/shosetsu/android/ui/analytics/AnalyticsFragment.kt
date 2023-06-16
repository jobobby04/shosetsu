package app.shosetsu.android.ui.analytics

import android.os.Bundle
import android.view.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.shosetsu.android.R
import app.shosetsu.android.common.ext.ComposeView
import app.shosetsu.android.common.ext.viewModelDi
import app.shosetsu.android.view.compose.NovelCardNormalContent
import app.shosetsu.android.view.compose.ShosetsuCompose
import app.shosetsu.android.view.controller.ShosetsuFragment
import app.shosetsu.android.view.uimodels.model.AnalyticsNovelUI
import app.shosetsu.android.viewmodel.abstracted.AnalyticsViewModel

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
 * @since 27 / 03 / 2023
 * @author Doomsdayrs
 */
class AnalyticsFragment : ShosetsuFragment() {
	override val viewTitleRes: Int = R.string.analytics

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
	): View {
		setViewTitle()
		return ComposeView {
			ShosetsuCompose {
				AnalyticsView()
			}
		}
	}
}

/**
 * UI of [AnalyticsFragment]
 */
@Composable
fun AnalyticsView() {
	val viewModel = viewModelDi<AnalyticsViewModel>()

	val novels by viewModel.novels.collectAsState(emptyList())
	val days by viewModel.days.collectAsState(0)
	val hours by viewModel.hours.collectAsState(0)
	val minutes by viewModel.minutes.collectAsState(0)

	val totalLibraryNovelCount by viewModel.totalLibraryNovelCount.collectAsState(0)
	val totalUnreadNovelCount by viewModel.totalUnreadNovelCount.collectAsState(0)
	val totalReadingNovelCount by viewModel.totalReadingNovelCount.collectAsState(0)
	val totalReadNovelCount by viewModel.totalReadNovelCount.collectAsState(0)

	val totalChapterCount by viewModel.totalChapterCount.collectAsState(0)
	val totalUnreadChapterCount by viewModel.totalUnreadChapterCount.collectAsState(0)
	val totalReadingChapterCount by viewModel.totalReadingChapterCount.collectAsState(0)
	val totalReadChapterCount by viewModel.totalReadChapterCount.collectAsState(0)

	val topGenres by viewModel.topGenres.collectAsState(emptyList())
	val topExtensions by viewModel.topExtensions.collectAsState(emptyList())

	AnalyticsContent(
		days,
		hours,
		minutes,

		totalLibraryNovelCount,
		totalUnreadNovelCount,
		totalReadingNovelCount,
		totalReadNovelCount,

		totalChapterCount,
		totalUnreadChapterCount,
		totalReadingChapterCount,
		totalReadChapterCount,

		topGenres,
		topExtensions,
		novels
	)
}

/**
 * Preview of [AnalyticsContent]
 */
@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun PreviewAnalyticsContent() {
	Scaffold {
		Box(Modifier.padding(it)) {
			AnalyticsContent(
				10,
				5,
				30,
				10,
				10,
				10,
				10,
				10,
				10,
				10,
				10,
				listOf("Fantasy", "Sci-Fi", "History"),
				listOf("MyExt", "YourExt"),
				listOf(
					AnalyticsNovelUI(
						0,
						"Novel",
						"",
						20,
						10,
						5,
						10,
						5,
						3,
						2
					)
				)
			)
		}
	}
}

@Composable
fun AnalyticsContent(
	days: Int,
	hours: Int,
	minutes: Int,

	totalLibraryNovelCount: Int,
	totalUnreadNovelCount: Int,
	totalReadingNovelCount: Int,
	totalReadNovelCount: Int,

	totalChapterCount: Int,
	totalUnreadChapterCount: Int,
	totalReadingChapterCount: Int,
	totalReadChapterCount: Int,

	topGenres: List<String>,
	topExtensions: List<String>,

	novels: List<AnalyticsNovelUI>
) {
	Column(
		modifier = Modifier
			.fillMaxSize()
			.verticalScroll(rememberScrollState()),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.spacedBy(4.dp)
	) {
		// Overview
		Column(
			modifier = Modifier.padding(16.dp),
			verticalArrangement = Arrangement.spacedBy(8.dp)
		) {
			Text("Overview", style = MaterialTheme.typography.titleLarge)

			Text("Total reading time", style = MaterialTheme.typography.titleMedium)
			Row(
				horizontalArrangement = Arrangement.SpaceEvenly,
				modifier = Modifier.fillMaxWidth()
			) {
				AnalyticsUnitCard(
					"Day(s)",
					days
				)
				AnalyticsUnitCard(
					"Hours(s)",
					hours
				)
				AnalyticsUnitCard(
					"Minutes(s)",
					minutes
				)
			}

			Text("Library", style = MaterialTheme.typography.titleMedium)

			Row(
				horizontalArrangement = Arrangement.SpaceEvenly,
				modifier = Modifier.fillMaxWidth()
			) {
				AnalyticsUnitCard(
					"Novel(s)",
					totalLibraryNovelCount
				)
				AnalyticsUnitCard(
					"Unread",
					totalUnreadNovelCount
				)
				AnalyticsUnitCard(
					"Reading",
					totalReadingNovelCount
				)
				AnalyticsUnitCard(
					"Read",
					totalReadNovelCount
				)
			}

			Text("Chapters", style = MaterialTheme.typography.titleMedium)

			Row(
				horizontalArrangement = Arrangement.SpaceEvenly,
				modifier = Modifier.fillMaxWidth()
			) {
				AnalyticsUnitCard(
					"Chapter(s)",
					totalChapterCount
				)
				AnalyticsUnitCard(
					"Unread",
					totalUnreadChapterCount
				)
				AnalyticsUnitCard(
					"Reading",
					totalReadingChapterCount
				)
				AnalyticsUnitCard(
					"Read",
					totalReadChapterCount
				)
			}
			Text("Top Genre(s)", style = MaterialTheme.typography.titleMedium)

			Column(
				modifier = Modifier.fillMaxWidth(),
				horizontalAlignment = Alignment.CenterHorizontally,
				verticalArrangement = Arrangement.spacedBy(4.dp)
			) {
				for (genre in topGenres) {
					Card(modifier = Modifier.fillMaxWidth()) {
						Text(
							genre, modifier = Modifier
								.padding(8.dp)
								.fillMaxWidth(),
							textAlign = TextAlign.Center
						)
					}
				}
			}

			Text("Top Extension(s)", style = MaterialTheme.typography.titleMedium)

			Column(
				modifier = Modifier.fillMaxWidth(),
				horizontalAlignment = Alignment.CenterHorizontally,
				verticalArrangement = Arrangement.spacedBy(4.dp)
			) {
				for (extension in topExtensions) {
					Card(modifier = Modifier.fillMaxWidth()) {
						Text(
							extension, modifier = Modifier
								.padding(8.dp)
								.fillMaxWidth(),
							textAlign = TextAlign.Center
						)
					}
				}
			}
		}
		// Per novel

		LazyRow(
			horizontalArrangement = Arrangement.spacedBy(4.dp),
			contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp)
		) {
			items(novels, key = { it.id }) {
				AnalyticsNovelCard(it)
			}
		}
	}
}

/**
 * Preview of [PreviewAnalyticsUnitCard]
 */
@Preview
@Composable
fun PreviewAnalyticsUnitCard() {
	AnalyticsUnitCard(
		"Day(s)",
		10
	)
}

@Composable
fun AnalyticsUnitCard(
	description: String,
	value: Int
) {
	Box {
		Column(
			modifier = Modifier.padding(8.dp),
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			Text(value.toString(), style = MaterialTheme.typography.titleMedium)
			Text(description, style = MaterialTheme.typography.bodyMedium)
		}
	}
}

/**
 * Preview of [AnalyticsNovelCard]
 */
@Preview
@Composable
fun PreviewAnalyticsNovelCard() {
	AnalyticsNovelCard(
		AnalyticsNovelUI(
			0,
			"Novel",
			"",
			20,
			10,
			5,
			10,
			5,
			3,
			2
		)
	)
}

@Composable
fun AnalyticsNovelCard(
	entity: AnalyticsNovelUI
) {
	Column(
		modifier = Modifier
			.padding(8.dp)
			.width(144.dp),
		verticalArrangement = Arrangement.spacedBy(4.dp),
	) {
		Box(
			Modifier
				.align(Alignment.CenterHorizontally)
		) {
			NovelCardNormalContent(
				entity.title,
				entity.imageURL,
				onClick = {},
				onLongClick = {},
			)
		}

		Text("Chapter(s): %d".format(entity.chapterCount))
		Text("Unread: %d".format(entity.unreadChapterCount))
		Text("Reading: %d".format(entity.readingChapterCount))
		Text("Read: %d".format(entity.readChapterCount))
	}
}