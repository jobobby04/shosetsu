package app.shosetsu.android.common.ext

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

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.direct
import org.kodein.di.instance

/*
 * KODEIN EXT
 */

/**
 * shosetsu
 * 24 / 04 / 2020
 *
 * @author github.com/doomsdayrs
 */
inline fun <reified VM : ViewModel, T> T.viewModel()
		: Lazy<VM> where T : DIAware, T : ViewModelStoreOwner =
	lazy(LazyThreadSafetyMode.NONE) {
		ViewModelProvider(
			this,
			direct.instance()
		)[VM::class.java]
	}

/**
 * Shosetsu compose DI injection for viewModels, it surprisingly works
 */
@Suppress("MissingJvmstatic")
@Composable
public inline fun <reified VM : ViewModel> viewModelDi(
	viewModelStoreOwner: ViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
		"No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
	},
	key: String? = null,
	extras: CreationExtras = if (viewModelStoreOwner is HasDefaultViewModelProviderFactory) {
		viewModelStoreOwner.defaultViewModelCreationExtras
	} else {
		CreationExtras.Empty
	}
): VM {
	val di by closestDI(LocalContext.current)
	val factory: ViewModelProvider.Factory? by di.instance()
	return androidx.lifecycle.viewmodel.compose.viewModel(
		VM::class.java,
		viewModelStoreOwner,
		key,
		factory,
		extras
	)
}