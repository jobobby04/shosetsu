package app.shosetsu.android.ui.migration

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.shosetsu.android.R
import app.shosetsu.android.common.ext.viewModelDi
import app.shosetsu.android.ui.theme.ShosetsuTheme
import app.shosetsu.android.view.compose.ImageLoadingError
import app.shosetsu.android.view.compose.placeholder
import app.shosetsu.android.view.uimodels.model.MigrationExtensionUI
import app.shosetsu.android.view.uimodels.model.MigrationNovelUI
import app.shosetsu.android.viewmodel.abstracted.AMigrationViewModel
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import kotlinx.collections.immutable.ImmutableList

/*
 * This file is part of Shosetsu.
 *
 * Shosetsu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Shosetsu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Shosetsu.  If not, see <https://www.gnu.org/licenses/>.
 * ====================================================================
 */


/**
 * Shosetsu
 * 9 / June / 2019
 *
 * @author github.com/doomsdayrs
 * yes, a THIRD ONE
 */

@Composable
fun MigrationView(
	novelIds: List<Int>,
) {
	val viewModel: AMigrationViewModel = viewModelDi()
	LaunchedEffect(novelIds) {
		viewModel.setNovels(novelIds)
	}

	MigrationContent(viewModel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MigrationContent(viewModel: AMigrationViewModel) {
	val novelList by viewModel.novels.collectAsState()
	val extensionsToSelect by viewModel.extensions.collectAsState()
	val currentQuery by viewModel.currentQuery.collectAsState()

	Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
		// Novels that the user selected to transfer
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.fillMaxHeight(.25f)
		) {
			// TODO Loading via loading flow
			// MigrationNovelsLoadingContent()
			MigrationNovelsContent(list = novelList) {
				viewModel.setWorkingOn(it.id)
			}
		}


		Text(text = "With name")

		if (currentQuery != null) {
			TextField(value = currentQuery!!, onValueChange = viewModel::setQuery)
		}

		Text(text = "In")

		// Select the extension
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.fillMaxHeight(.25f)
		) {
			// TODO Loading via loading flow
			// MigrationExtensionsLoadingContent()
			MigrationExtensionsContent(
				list = extensionsToSelect,
				onClick = viewModel::setSelectedExtension
			)
		}

		// Holds an arrow indicating it will be transferred to
		Text(text = "To")

		Icon(
			painter = painterResource(id = R.drawable.expand_more),
			contentDescription = "The above will transfer to the below"
		)


		// Select novel from its results
		Box(modifier = Modifier.fillMaxWidth()) {
			Text(text = "This is under construction, Try again in another release :D")
		}
	}
}

@Composable
fun MigrationExtensionsLoadingContent() {
	LinearProgressIndicator()
}

@Composable
fun MigrationExtensionsContent(
	list: ImmutableList<MigrationExtensionUI>,
	onClick: (MigrationExtensionUI) -> Unit
) {
	LazyRow(
		horizontalArrangement = Arrangement.Center,
		modifier = Modifier.fillMaxWidth()
	) {
		items(items = list, key = { it.id }) { extensionUI ->
			MigrationExtensionItemContent(extensionUI, onClick = onClick)
		}
	}
}

@ExperimentalMaterial3Api
@Preview
@Composable
fun PreviewMigrationExtensionItemContent() {
	val item by remember {
		mutableStateOf(
			MigrationExtensionUI(
				0,
				"This is a novel",
				"",
				false
			)
		)
	}
	ShosetsuTheme {
		Box(modifier = Modifier.height(200.dp)) {
			MigrationExtensionItemContent(item = item) {
				println("Test")
			}
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MigrationExtensionItemContent(
	item: MigrationExtensionUI,
	onClick: (MigrationExtensionUI) -> Unit
) {
	Card(
		modifier = Modifier.padding(start = 8.dp, top = 8.dp, bottom = 8.dp),
		shape = RoundedCornerShape(16.dp),
		border =
		if (item.isSelected) {
			BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
		} else {
			null
		},

		onClick = { onClick(item) },
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
		) {
			if (item.imageURL.isNotEmpty()) {
				SubcomposeAsyncImage(
					ImageRequest.Builder(LocalContext.current)
						.data(item.imageURL)
						.crossfade(true)
						.build(),
					contentDescription = null,
					modifier = Modifier.size(64.dp),
					error = {
						ImageLoadingError()
					},
					loading = {
						Box(Modifier.placeholder(true))
					}
				)
			} else {
				ImageLoadingError(Modifier.size(64.dp))
			}
			Text(
				text = item.name,
				modifier = Modifier.padding(end = 16.dp)
			)
		}
	}
}

@Composable
fun MigrationNovelsLoadingContent() {
	LinearProgressIndicator()
}

@Composable
fun MigrationNovelsContent(
	list: ImmutableList<MigrationNovelUI>,
	onClick: (MigrationNovelUI) -> Unit
) {
	LazyRow(
		horizontalArrangement = Arrangement.Center,
		modifier = Modifier.fillMaxWidth()
	) {
		items(items = list, key = { it.id }) { novelUI ->
			MigrationNovelItemContent(item = novelUI, onClick = onClick)
		}
	}
}

@ExperimentalMaterial3Api
@Composable
@Preview
fun PreviewMigrationNovelItemRowContent() {
	val item by remember {
		mutableStateOf(
			MigrationNovelUI(
				0,
				"This is a novel",
				"",
				false
			)
		)
	}
	ShosetsuTheme {
		Row(
			modifier = Modifier
				.height(200.dp)
				.width(600.dp)
		) {
			MigrationNovelItemContent(item = item) {
				println("Test")
			}
			MigrationNovelItemContent(item = item) {
				println("Test")
			}
		}
	}
}

@ExperimentalMaterial3Api
@Composable
@Preview
fun PreviewMigrationNovelItemContent() {
	val item by remember {
		mutableStateOf(
			MigrationNovelUI(
				0,
				"This is a novel",
				"",
				false
			)
		)
	}
	ShosetsuTheme {
		Box(modifier = Modifier.height(200.dp)) {
			MigrationNovelItemContent(item = item) {
				println("Test")
			}
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MigrationNovelItemContent(item: MigrationNovelUI, onClick: (MigrationNovelUI) -> Unit) {
	Card(
		onClick = { onClick(item) },
		border =
		if (item.isSelected) {
			BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
		} else {
			null
		},
		modifier = Modifier.aspectRatio(.70f)
	) {
		val blackTrans = colorResource(id = R.color.black_trans)
		Box {
			val modifier = Modifier
				.fillMaxSize()
				.drawWithContent {
					drawContent()
					drawRect(
						Brush.verticalGradient(
							colors = listOf(
								Color.Transparent,
								blackTrans
							),
						)
					)
				}
			if (item.imageURL.isNotEmpty()) {
				SubcomposeAsyncImage(
					ImageRequest.Builder(LocalContext.current)
						.data(item.imageURL)
						.crossfade(true)
						.build(),
					contentDescription = null,
					modifier = modifier,
					error = {
						ImageLoadingError()
					},
					loading = {
						Box(Modifier.placeholder(true))
					}
				)
			} else {
				ImageLoadingError(modifier)
			}

			Text(
				text = item.title,
				modifier = Modifier
					.align(Alignment.BottomCenter)
					.padding(8.dp),
				fontWeight = FontWeight.Bold,
				textAlign = TextAlign.Center
			)
		}
	}
}
