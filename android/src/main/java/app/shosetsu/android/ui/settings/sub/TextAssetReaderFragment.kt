package app.shosetsu.android.ui.settings.sub

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.shosetsu.android.R
import app.shosetsu.android.common.ext.ComposeView
import app.shosetsu.android.common.ext.viewModelDi
import app.shosetsu.android.view.compose.NavigateBackButton
import app.shosetsu.android.view.controller.ShosetsuFragment
import app.shosetsu.android.viewmodel.abstracted.ATextAssetReaderViewModel

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
 */


/**
 * Shosetsu
 * 9 / June / 2019
 */
@Deprecated("Composed")
class TextAssetReaderFragment : ShosetsuFragment() {

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedViewState: Bundle?
	): View = ComposeView {
	}
}

@Composable
fun TextAssetReaderView(
	bundleKey: Int,
	onBack: () -> Unit,
) {
	val viewModel: ATextAssetReaderViewModel = viewModelDi()

	LaunchedEffect(bundleKey) {
		viewModel.setTarget(bundleKey)
	}

	val target by viewModel.targetLiveData.collectAsState()
	val content by viewModel.liveData.collectAsState()

	TextAssetReaderContent(
		text = content,
		title = stringResource(
			target?.titleRes ?: R.string.loading
		),
		onBack = onBack
	)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextAssetReaderContent(text: String, title: String, onBack: () -> Unit) {
	Scaffold(
		topBar = {
			TopAppBar(
				title = {
					Text(title)
				},
				navigationIcon = {
					NavigateBackButton(onBack)
				}
			)
		}
	) { paddingValues ->
		Text(
			text = text, modifier = Modifier
				.padding(paddingValues)
				.verticalScroll(
					state = rememberScrollState(),
				)
		)
	}
}