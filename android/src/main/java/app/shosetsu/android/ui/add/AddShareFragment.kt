package app.shosetsu.android.ui.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.shosetsu.android.R
import app.shosetsu.android.common.consts.SHARE_HELP_URL
import app.shosetsu.android.common.ext.viewModelDi
import app.shosetsu.android.domain.model.local.NovelEntity
import app.shosetsu.android.view.compose.ErrorAction
import app.shosetsu.android.view.compose.ErrorContent
import app.shosetsu.android.view.compose.HelpButton
import app.shosetsu.android.view.compose.ImageLoadingError
import app.shosetsu.android.view.compose.NavigateBackButton
import app.shosetsu.android.view.compose.coverRatio
import app.shosetsu.android.view.compose.placeholder
import app.shosetsu.android.viewmodel.abstracted.AAddShareViewModel
import app.shosetsu.lib.share.ExtensionLink
import app.shosetsu.lib.share.NovelLink
import app.shosetsu.lib.share.RepositoryLink
import app.shosetsu.lib.share.StyleLink
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest

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
 * This class represents a combination of a QR code scanner + handler.
 *
 * First, it will read the QR code.
 * Second, it will display to the user what the QR code provides.
 * Third, it allows the user to choose to add the content or not.
 *
 * @since 07 / 03 / 2022
 * @author Doomsdayrs
 */

@Composable
fun AddShareView(
	shareURL: String?,
	onBackPressed: () -> Unit,
	openNovel: (NovelEntity?) -> Unit
) {
	val viewModel: AAddShareViewModel = viewModelDi()

	LaunchedEffect(shareURL) {
		if (shareURL != null)
			viewModel.setURL(shareURL)
	}

	val url by viewModel.url.collectAsState()
	val showURLInput by viewModel.showURLInput.collectAsState()
	val isProcessing by viewModel.isProcessing.collectAsState()
	val isQRCodeValid by viewModel.isURLValid.collectAsState()
	val isAdding by viewModel.isAdding.collectAsState()
	val isComplete by viewModel.isComplete.collectAsState()
	val isNovelOpenable by viewModel.isNovelOpenable.collectAsState()

	val isNovelAlreadyPresent by viewModel.isNovelAlreadyPresent.collectAsState()
	val isStyleAlreadyPresent by viewModel.isStyleAlreadyPresent.collectAsState()
	val isExtAlreadyPresent by viewModel.isExtAlreadyPresent.collectAsState()
	val isRepoAlreadyPresent by viewModel.isRepoAlreadyPresent.collectAsState()

	val novelLink by viewModel.novelLink.collectAsState()
	val extLink by viewModel.extLink.collectAsState()
	val repoLink by viewModel.repoLink.collectAsState()

	AddShareContent(
		showURLInput = showURLInput,
		url = url,
		setURL = viewModel::setURL,
		applyURL = viewModel::applyURL,
		isProcessing = isProcessing,
		isUrlValid = isQRCodeValid,
		isAdding = isAdding,
		add = viewModel::add,
		reject = onBackPressed,
		retry = viewModel::retry,
		novelLink = novelLink,
		extensionLink = extLink,
		repositoryLink = repoLink,
		isNovelAlreadyPresent = isNovelAlreadyPresent,
		isStyleAlreadyPresent = isStyleAlreadyPresent,
		isExtAlreadyPresent = isExtAlreadyPresent,
		isRepoAlreadyPresent = isRepoAlreadyPresent,
		isComplete = isComplete,
		openNovel = {
			val entity = viewModel.getNovel()

			openNovel(entity)
		},
		isNovelOpenable = isNovelOpenable,
		onBack = onBackPressed
	)
}

@Preview
@Composable
fun PreviewAboutContent() {
	val repoLink =
		RepositoryLink("Test Repo", "https://raw.githubusercontent.com/shosetsuorg/extensions/dev/")
	val extLink = ExtensionLink(
		0,
		"Test Ext",
		"https://raw.githubusercontent.com/shosetsuorg/extensions/dev/icons/AsianHobbyist.png",
		repoLink
	)
	val novelLink = NovelLink(
		"How to Get My Husband on My Side",
		"https://noveltrench.com/wp-content/uploads/2021/03/How-To-Get-My-Husband-On-My-Side-193x278.jpg",
		"https://noveltrench.com/novel/how-to-get-my-husband-on-my-side/",
		extLink
	)


	AddShareContent(
		showURLInput = true,
		isProcessing = false,
		novelLink = novelLink,
		extensionLink = extLink,
		repositoryLink = repoLink,
		add = {
		},
		reject = {
		},
		isAdding = false,
		isUrlValid = true,
		retry = {
		},
		isNovelAlreadyPresent = false,
		isStyleAlreadyPresent = true,
		isExtAlreadyPresent = true,
		isRepoAlreadyPresent = true,
		openNovel = {}
	)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddShareContent(
	showURLInput: Boolean,
	url: String = "",
	setURL: (String) -> Unit = { },
	applyURL: () -> Unit = {},

	isProcessing: Boolean,
	isUrlValid: Boolean = false,
	isAdding: Boolean = false,
	novelLink: NovelLink? = null,
	styleLink: StyleLink? = null,
	extensionLink: ExtensionLink? = null,
	repositoryLink: RepositoryLink? = null,
	add: () -> Unit,
	reject: () -> Unit,
	openNovel: () -> Unit,
	retry: () -> Unit,
	isNovelAlreadyPresent: Boolean = false,
	isStyleAlreadyPresent: Boolean = false,
	isExtAlreadyPresent: Boolean = false,
	isRepoAlreadyPresent: Boolean = false,
	isComplete: Boolean = false,
	isNovelOpenable: Boolean = false,
	onBack: () -> Unit = {}
) {
	Scaffold(
		topBar = {
			TopAppBar(
				title = {
					Text(stringResource(R.string.qr_code_scan))
				},
				navigationIcon = {
					NavigateBackButton(onBack)
				},
				actions = {
					HelpButton(SHARE_HELP_URL)
				},
				colors = TopAppBarDefaults.topAppBarColors(
					containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
					titleContentColor = MaterialTheme.colorScheme.onSurface,
				)
			)
		}
	) { padding ->
		if (isComplete) {
			Box(
				modifier = Modifier
					.fillMaxSize()
					.padding(padding),
				contentAlignment = Alignment.Center
			) {
				Column(horizontalAlignment = Alignment.CenterHorizontally) {
					Text(
						stringResource(R.string.completed),
						style = MaterialTheme.typography.bodyLarge
					)
					TextButton(onClick = reject) {
						Text(stringResource(android.R.string.ok))
					}

					if (isNovelOpenable)
						TextButton(onClick = openNovel) {
							Text(stringResource(R.string.fragment_add_open_novel))
						}
				}
			}
		} else if (isProcessing) {
			Box(
				modifier = Modifier
					.fillMaxSize()
					.padding(padding),
				contentAlignment = Alignment.Center
			) {
				CircularProgressIndicator()
			}
		} else if (showURLInput) {
			Column(
				modifier = Modifier
					.fillMaxSize()
					.padding(padding),
				horizontalAlignment = Alignment.CenterHorizontally,
				verticalArrangement = Arrangement.Center
			) {
				TextField(url, setURL, isError = isUrlValid)
				TextButton(applyURL) {
					Text(stringResource(R.string.apply))
				}
			}
		} else if (!isUrlValid) {
			ErrorContent(
				R.string.fragment_add_invalid,
				ErrorAction(
					R.string.retry,
				) {
					retry()
				},
				modifier = Modifier.padding(padding)
			)
		} else if (isNovelAlreadyPresent && novelLink != null) {
			ErrorContent(
				stringResource(R.string.fragment_add_present_novel, novelLink.name),
				actions = arrayOf(
					ErrorAction(
						android.R.string.ok,
					) {
						reject()
					},
					ErrorAction(
						R.string.fragment_add_open_novel,
					) {
						openNovel()
					},
				),
				modifier = Modifier.padding(padding)
			)
		} else if (isStyleAlreadyPresent && styleLink != null) {
			ErrorContent(
				stringResource(R.string.fragment_add_present_style, styleLink.name),
				ErrorAction(
					android.R.string.ok,
				) {
					reject()
				},
				modifier = Modifier.padding(padding)
			)
		} else if (isExtAlreadyPresent && extensionLink != null && novelLink == null && styleLink == null) {
			ErrorContent(
				stringResource(R.string.fragment_add_present_ext, extensionLink.name),
				ErrorAction(
					android.R.string.ok,
				) {
					reject()
				},
				modifier = Modifier.padding(padding)
			)
		} else if (isRepoAlreadyPresent && repositoryLink != null && novelLink == null && styleLink == null && extensionLink == null) {
			ErrorContent(
				stringResource(R.string.fragment_add_present_repo, repositoryLink.name),
				ErrorAction(
					android.R.string.ok,
				) {
					reject()
				},
				modifier = Modifier.padding(padding)
			)
		} else {
			Column(
				modifier = Modifier
					.padding(padding)
					.fillMaxSize()
					.padding(bottom = 56.dp),
				horizontalAlignment = Alignment.CenterHorizontally,
				verticalArrangement = Arrangement.SpaceBetween
			) {
				Text(
					stringResource(R.string.fragment_add_following),
					modifier = Modifier.padding(bottom = 16.dp, top = 16.dp),
					style = MaterialTheme.typography.headlineSmall
				)

				LazyColumn(
					horizontalAlignment = Alignment.CenterHorizontally,
					contentPadding = PaddingValues(16.dp),
					modifier = Modifier.fillMaxHeight(.75f)
				) {
					if (novelLink != null) {
						item {
							Column {
								Text(
									stringResource(R.string.fragment_add_novel),
									style = MaterialTheme.typography.titleLarge,
									modifier = Modifier.padding(bottom = 8.dp)
								)

								Card(
									modifier = Modifier
										.fillMaxWidth()
										.padding(bottom = 16.dp)
								) {
									Column(
										modifier = Modifier.padding(16.dp)
									) {

										Row(
											verticalAlignment = Alignment.CenterVertically
										) {

											SubcomposeAsyncImage(
												model = ImageRequest.Builder(LocalContext.current)
													.data(novelLink.imageURL)
													.crossfade(true)
													.build(),
												contentDescription = null,
												modifier = Modifier
													.heightIn(max = 128.dp)
													.aspectRatio(coverRatio),
												error = {
													ImageLoadingError()
												},
												loading = {
													Box(Modifier.placeholder(true))
												}
											)
											Column(
												modifier = Modifier.padding(start = 8.dp)
											) {
												Text(
													novelLink.name,
													style = MaterialTheme.typography.bodyLarge
												)
												Text(
													novelLink.url,
													style = MaterialTheme.typography.bodySmall
												)
											}
										}
									}
								}
							}
						}
					}
					//if (styleLink != null) {
					//	// TODO Style
					//}
					if (extensionLink != null && !isExtAlreadyPresent) {
						item {
							Column {
								Text(
									stringResource(R.string.fragment_add_extension),
									style = MaterialTheme.typography.titleLarge,
									modifier = Modifier.padding(bottom = 8.dp)
								)
								Card(
									modifier = Modifier
										.padding(bottom = 16.dp)
										.fillMaxWidth()
								) {
									Column(
										modifier = Modifier.padding(16.dp),
									) {

										Row(
											verticalAlignment = Alignment.CenterVertically
										) {
											SubcomposeAsyncImage(
												ImageRequest.Builder(LocalContext.current)
													.data(extensionLink.imageURL)
													.crossfade(true)
													.build(),
												"",
												modifier = Modifier.size(64.dp),
												error = {
													ImageLoadingError()
												},
												loading = {
													Box(Modifier.placeholder(true))
												}
											)
											Text(
												extensionLink.name,
												style = MaterialTheme.typography.bodyLarge
											)
										}
									}
								}
							}
						}
					}
					if (repositoryLink != null && !isRepoAlreadyPresent) {
						item {
							Column {
								Text(
									stringResource(
										R.string.fragment_add_repository,
									),
									style = MaterialTheme.typography.titleLarge,
									modifier = Modifier.padding(bottom = 8.dp)
								)
								Card(
									modifier = Modifier.fillMaxWidth()
								) {
									Column(
										modifier = Modifier.padding(16.dp)
									) {

										Text(
											repositoryLink.name,
											style = MaterialTheme.typography.bodyLarge
										)
										Text(
											repositoryLink.url,
											style = MaterialTheme.typography.bodySmall,

											)
									}
								}
							}
						}
					}
				}

				if (isAdding)
					CircularProgressIndicator()

				Card(
					modifier = Modifier
						.fillMaxWidth()
						.padding(16.dp)
				) {
					Row(
						horizontalArrangement = Arrangement.SpaceEvenly,
						modifier = Modifier.fillMaxWidth()
					) {
						TextButton(reject, contentPadding = PaddingValues(16.dp)) {
							Text(stringResource(android.R.string.cancel))
						}

						if (!isAdding)
							TextButton(add, contentPadding = PaddingValues(16.dp)) {
								Text(stringResource(android.R.string.ok))
							}
					}
				}
			}
		}
	}
}
