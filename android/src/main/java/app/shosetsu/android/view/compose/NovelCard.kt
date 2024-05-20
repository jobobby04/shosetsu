package app.shosetsu.android.view.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.shosetsu.android.R
import app.shosetsu.android.common.ext.onIO
import app.shosetsu.android.ui.theme.ShosetsuTheme
import app.shosetsu.android.view.uimodels.model.catlog.ACatalogNovelUI
import app.shosetsu.lib.Novel
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import kotlinx.collections.immutable.persistentListOf
import java.text.NumberFormat
import java.util.Locale

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
 * @since 14 / 05 / 2022
 * @author Doomsdayrs
 */

const val coverRatio = 0.7F

@Composable
fun PlaceholderNovelCardNormalContent() {
	NovelCardNormalContent(
		"",
		"",
		onClick = {},
		onLongClick = {},
		isPlaceholder = true
	)
}

@Preview
@Composable
fun PreviewNovelCardNormalContent() {
	ShosetsuTheme {
		NovelCardNormalContent(
			"Test",
			"",
			onClick = {},
			onLongClick = {}
		)
	}
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NovelCardNormalContent(
	title: String,
	imageURL: String,
	onClick: () -> Unit,
	onLongClick: () -> Unit,
	overlay: @Composable (BoxScope.() -> Unit)? = null,
	isPlaceholder: Boolean = false,
	isSelected: Boolean = false,
	isBookmarked: Boolean = false
) {
	Box(
		modifier = Modifier
			.selectedOutline(isSelected)
			.alpha(if (isBookmarked) .5f else 1f)
			.clip(MaterialTheme.shapes.extraSmall)
			.combinedClickable(
				onClick = onClick,
				onLongClick = onLongClick
			),
	) {
		Box {
			SubcomposeAsyncImage(
				ImageRequest.Builder(LocalContext.current)
					.data(imageURL)
					.crossfade(true)
					.build(),
				stringResource(R.string.fragment_novel_info_image),
				modifier = Modifier
					.fillMaxSize()
					.aspectRatio(coverRatio)
					.placeholder(visible = isPlaceholder)
					.clip(MaterialTheme.shapes.small),
				contentScale = ContentScale.Crop,
				error = {
					ImageLoadingError()
				}
			)

			Box(
				modifier = Modifier
					.aspectRatio(coverRatio)
					.fillMaxSize()
					.drawWithCache {
						onDrawWithContent {

							drawRect(
								brush = Brush.linearGradient(
									listOf(
										Color.Transparent,
										Color.Black.copy(alpha = .75f),
									),
									Offset(0.0f, 0.0f),
									Offset(0.0f, Float.POSITIVE_INFINITY),
									TileMode.Clamp
								)
							)
						}
					}
					.alpha(if (isPlaceholder) 0.0f else 1.0f)
			)
			Box(
				Modifier
					.align(Alignment.BottomCenter)
					.fillMaxWidth()
					.padding(4.dp),
				contentAlignment = Alignment.Center
			) {
				Text(
					title,
					modifier = Modifier
						.placeholder(visible = isPlaceholder),
					textAlign = TextAlign.Center,
					color = Color.White,
					overflow = TextOverflow.Ellipsis,
					maxLines = 3,
					fontSize = 14.sp
				)
			}
			if (overlay != null)
				overlay()
		}
	}
}

@Composable
fun PlaceholderNovelCardCozyContent() {
	NovelCardCozyContent(
		"",
		"",
		onClick = {},
		onLongClick = {},
		isPlaceholder = true
	)
}

@Preview
@Composable
fun PreviewNovelCardCozyContent() {
	ShosetsuTheme {
		NovelCardCozyContent(
			"Test",
			"",
			onClick = {},
			onLongClick = {}
		)
	}
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NovelCardCozyContent(
	title: String,
	imageURL: String,
	onClick: () -> Unit,
	onLongClick: () -> Unit,
	overlay: @Composable (BoxScope.() -> Unit)? = null,
	isPlaceholder: Boolean = false,
	isSelected: Boolean = false,
	isBookmarked: Boolean = false
) {
	Column(
		modifier = Modifier
			.selectedOutline(isSelected)
			.alpha(if (isBookmarked) .5f else 1f)
			.clip(MaterialTheme.shapes.extraSmall)
			.combinedClickable(
				onClick = onClick,
				onLongClick = onLongClick
			)
	) {
		Box {
			SubcomposeAsyncImage(
				ImageRequest.Builder(LocalContext.current)
					.data(imageURL)
					.crossfade(true)
					.build(),
				stringResource(R.string.fragment_novel_info_image),
				modifier = Modifier
					.fillMaxSize()
					.aspectRatio(coverRatio)
					.placeholder(visible = isPlaceholder)
					.clip(MaterialTheme.shapes.small),
				contentScale = ContentScale.Crop,
				error = {
					ImageLoadingError()
				},
				loading = {
					Box(Modifier.placeholder(true))
				}
			)

			if (overlay != null)
				overlay()
		}

		Box(
			Modifier
				.fillMaxWidth()
				.padding(4.dp),
			contentAlignment = Alignment.Center
		) {
			Text(
				title,
				modifier = Modifier
					.placeholder(visible = isPlaceholder),
				textAlign = TextAlign.Center,
				overflow = TextOverflow.Ellipsis,
				maxLines = 3,
				fontSize = 14.sp
			)
		}
	}
}

@Preview
@Composable
fun PreviewNovelCardExtendedContent() {
	MaterialTheme {
		NovelCardExtendedContent(
			ACatalogNovelUI(
				0,
				bookmarked = false,
				title = "Celestial Squadron ZodiaMaiden",
				imageURL = "",
				language = "English",
				description = "Following the invasion of Planet Zodia by the evil Malefic Empire, Princess Andromeda seeks out four Earthling girls who will become ZodiaMaidens like her, and help her in liberating Zodia from the Empire's evil reign...",
				status = Novel.Status.PUBLISHING,
				tags = persistentListOf(),
				genres = persistentListOf(
					"General Audiences",
					"Gen",
					"Original Work",
					"Super Sentai Series",
					"No Archive Warnings Apply",
					"Original Characters",
					"Magical Girls",
					"Constellations",
					"Action",
					"Comedy",
					"Drama",
					"High School",
					"Slice of Life",
					"Armoured heroines"
				),
				authors = persistentListOf("PrincessWink"),
				artists = persistentListOf(),
				chapters = persistentListOf(),
				chapterCount = 26,
				wordCount = 32087,
				commentCount = 1,
				viewCount = 139,
				favoriteCount = 2
			),
			onClick = {},
			onLongClick = {}
		)
	}
}

@Composable
private fun formattedNumber(number: Int): String {
	return produceState("", number) {
		value = onIO {
			NumberFormat.getNumberInstance(Locale.getDefault()).apply {
				maximumFractionDigits = 0
			}.format(number)
		}
	}.value
}

@Composable
fun TextItemRow(title: String, item: String) {
	Row {
		Text(
			text = "$title:",
			style = MaterialTheme.typography.bodySmall,
			color = LocalContentColor.current.copy(alpha = 0.8f),
			lineHeight = MaterialTheme.typography.bodySmall.fontSize * 1.5
		)
		Spacer(Modifier.width(4.dp))
		Text(
			text = item,
			style = MaterialTheme.typography.bodySmall,
			color = LocalContentColor.current.copy(alpha = 0.8f),
			lineHeight = MaterialTheme.typography.bodySmall.fontSize * 1.5
		)
	}
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun NovelCardExtendedContent(
	novelUI: ACatalogNovelUI,
	onClick: () -> Unit,
	onLongClick: () -> Unit,
	isPlaceholder: Boolean = false,
	isSelected: Boolean = false,
) {
	Row(
		Modifier
			.fillMaxWidth()
			.selectedOutline(isSelected)
			.alpha(if (novelUI.bookmarked) .5f else 1f)
			.combinedClickable(
				onClick = onClick,
				onLongClick = onLongClick
			)
			.padding(10.dp)
	) {
		if (novelUI.imageURL.isNotBlank()) {
			SubcomposeAsyncImage(
				ImageRequest.Builder(LocalContext.current)
					.data(novelUI.imageURL)
					.crossfade(true)
					.build(),
				stringResource(R.string.fragment_novel_info_image),
				modifier = Modifier
					.weight(1f)
					.aspectRatio(coverRatio)
					.placeholder(visible = isPlaceholder)
					.clip(MaterialTheme.shapes.small),
				contentScale = ContentScale.Crop,
				error = {
					ImageLoadingError()
				},
				loading = {
					Box(Modifier.placeholder(true))
				}
			)
		}
		Column(
			Modifier.weight(3f)
				.let {
					if (novelUI.imageURL.isNotBlank()) {
						it.padding(start = 10.dp)
					} else {
						it
					}
				}
		) {
			Text(
				text = novelUI.title,
				style = MaterialTheme.typography.titleSmall,
				color = MaterialTheme.colorScheme.primary
			)
			if (novelUI.authors.isNotEmpty()) {
				Text(
					text = novelUI.authors.joinToString(),
					style = MaterialTheme.typography.labelSmall,
					color = MaterialTheme.colorScheme.primary
				)
			}
			if (novelUI.tags.isNotEmpty() || novelUI.genres.isNotEmpty()) {
				Spacer(Modifier.height(8.dp))
				FlowRow {
					val tags = (novelUI.tags + novelUI.genres)
					tags.forEachIndexed { index, tag ->
						Row {
							Text(
								text = tag,
								style = MaterialTheme.typography.bodySmall,
								textDecoration = TextDecoration.Underline
							)
							if (index != tags.lastIndex) {
								Text(
									text = ",",
									style = MaterialTheme.typography.bodySmall,
								)
								Spacer(Modifier.width(4.dp))
							}
						}
					}
				}
			}
			if (novelUI.description.isNotBlank()) {
				Spacer(Modifier.height(16.dp))
				Text(
					text = novelUI.description,
					style = MaterialTheme.typography.bodySmall,
					color = LocalContentColor.current.copy(alpha = 0.8f),
					lineHeight = MaterialTheme.typography.bodySmall.fontSize * 1.5
				)
			}
			Spacer(Modifier.height(16.dp))
			FlowRow(
				horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
				modifier = Modifier.fillMaxWidth()
			) {
				if (novelUI.language.isNotBlank()) {
					TextItemRow(
						stringResource(R.string.language),
						novelUI.language
					)
				}
				if (novelUI.wordCount != null) {
					TextItemRow(
						stringResource(R.string.words),
						formattedNumber(novelUI.wordCount)
					)
				}
				if (novelUI.chapterCount != null) {
					TextItemRow(
						stringResource(R.string.chapters),
						formattedNumber(novelUI.chapterCount)
					)
				}
				if (novelUI.commentCount != null) {
					TextItemRow(
						stringResource(R.string.comments),
						formattedNumber(novelUI.commentCount)
					)
				}
				if (novelUI.favoriteCount != null) {
					TextItemRow(
						stringResource(R.string.favorites),
						formattedNumber(novelUI.favoriteCount)
					)
				}
				if (novelUI.viewCount != null) {
					TextItemRow(
						stringResource(R.string.views),
						formattedNumber(novelUI.viewCount)
					)
				}
			}
		}
	}
}


@Preview
@Composable
fun PreviewNovelCardCompressedContent() {
	ShosetsuTheme {
		NovelCardCompressedContent(
			"Test",
			"",
			onClick = {},
			onLongClick = {}
		)
	}
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NovelCardCompressedContent(
	title: String,
	imageURL: String,
	onClick: () -> Unit,
	onLongClick: () -> Unit,
	overlay: @Composable (RowScope.() -> Unit)? = null,
	isPlaceholder: Boolean = false,
	isSelected: Boolean = false,
	isBookmarked: Boolean = false
) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.SpaceBetween,
		modifier = Modifier
			.fillMaxWidth()
			.selectedOutline(isSelected)
			.alpha(if (isBookmarked) .5f else 1f)
			.clip(MaterialTheme.shapes.extraSmall)
			.combinedClickable(
				onClick = onClick,
				onLongClick = onLongClick
			)
			.padding(end = 4.dp)
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier.fillMaxSize(.70f)
		) {
			SubcomposeAsyncImage(
				ImageRequest.Builder(LocalContext.current)
					.data(imageURL)
					.crossfade(true)
					.build(),
				stringResource(R.string.fragment_novel_info_image),
				modifier = Modifier
					.width(64.dp)
					.aspectRatio(1.0f)
					.clip(MaterialTheme.shapes.small),
				contentScale = ContentScale.Crop,
				error = {
					ImageLoadingError()
				},
				loading = {
					Box(Modifier.placeholder(true))
				}
			)

			Text(
				title,
				modifier = Modifier
					.placeholder(visible = isPlaceholder)
					.padding(start = 8.dp)
					.fillMaxSize()
			)
		}

		if (overlay != null)
			Row(
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.spacedBy(4.dp),
			) {
				overlay()
			}
	}
}