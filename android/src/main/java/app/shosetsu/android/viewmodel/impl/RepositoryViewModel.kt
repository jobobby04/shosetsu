package app.shosetsu.android.viewmodel.impl

import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.viewModelScope
import app.shosetsu.android.R
import app.shosetsu.android.common.OfflineException
import app.shosetsu.android.common.ext.get
import app.shosetsu.android.common.ext.launchIO
import app.shosetsu.android.common.ext.set
import app.shosetsu.android.common.utils.share.toURL
import app.shosetsu.android.domain.usecases.AddRepositoryUseCase
import app.shosetsu.android.domain.usecases.ForceInsertRepositoryUseCase
import app.shosetsu.android.domain.usecases.IsOnlineUseCase
import app.shosetsu.android.domain.usecases.StartRepositoryUpdateManagerUseCase
import app.shosetsu.android.domain.usecases.delete.DeleteRepositoryUseCase
import app.shosetsu.android.domain.usecases.load.LoadRepositoriesUseCase
import app.shosetsu.android.domain.usecases.update.UpdateRepositoryUseCase
import app.shosetsu.android.view.uimodels.model.QRCodeData
import app.shosetsu.android.view.uimodels.model.RepositoryUI
import app.shosetsu.android.viewmodel.abstracted.ARepositoryViewModel
import app.shosetsu.lib.share.RepositoryLink
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import io.github.g0dkar.qrcode.QRCode
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

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
 * shosetsu
 * 16 / 09 / 2020
 */
class RepositoryViewModel(
	private val loadRepositoriesUseCase: LoadRepositoriesUseCase,
	private val addRepositoryUseCase: AddRepositoryUseCase,
	private val deleteRepositoryUseCase: DeleteRepositoryUseCase,
	private val updateRepositoryUseCase: UpdateRepositoryUseCase,
	private val startRepositoryUpdateManagerUseCase: StartRepositoryUpdateManagerUseCase,
	private val forceInsertRepositoryUseCase: ForceInsertRepositoryUseCase,
	private val isOnlineUseCase: IsOnlineUseCase
) : ARepositoryViewModel() {

	override val liveData: StateFlow<ImmutableList<RepositoryUI>> by lazy {
		loadRepositoriesUseCase().map { it.toImmutableList() }
			.stateIn(viewModelScopeIO, SharingStarted.Lazily, persistentListOf())
	}
	override val error = MutableSharedFlow<Throwable>()
	override val isAddDialogVisible = MutableStateFlow(false)
	override val addState = MutableSharedFlow<AddRepoState>()
	override val removeState = MutableSharedFlow<RemoveRepoState>()
	override val toggleIsEnabledState =
		MutableSharedFlow<ToggleRepoIsEnabledState>()
	override val undoRemoveState =
		MutableSharedFlow<UndoRepoRemoveState>()

	override fun addRepository(name: String, url: String) {
		launchIO {
			try {
				addRepositoryUseCase(url = url, name = name)
				addState.emit(AddRepoState.Success)
			} catch (e: Exception) {
				addState.emit(AddRepoState.Failure(e, name, url))
			}
		}
	}

	override fun undoRemove(repo: RepositoryUI) {
		launchIO {
			try {
				forceInsertRepositoryUseCase(repo)
				undoRemoveState.emit(UndoRepoRemoveState.Success)
			} catch (e: Exception) {
				undoRemoveState.emit(UndoRepoRemoveState.Failure(repo, e))
			}
		}
	}

	override fun showAddDialog() {
		isAddDialogVisible.value = true
	}

	override fun hideAddDialog() {
		isAddDialogVisible.value = false
	}

	private val qrCodeMap: Cache<Int, QRCodeData?> =
		CacheBuilder
			.newBuilder()
			.expireAfterAccess(1, TimeUnit.MINUTES)
			.build()

	override val currentShare = MutableStateFlow<RepositoryUI?>(null)

	override val qrCode: Flow<QRCodeData?> =
		currentShare.map { repo ->
			if (repo != null) {
				val value = qrCodeMap[repo.id]
				if (value != null) {
					value
				} else {
					val url = RepositoryLink(
						repo.name,
						repo.url
					).toURL()

					val code = QRCode(url)

					val bytes = code.render(
						brightColor = Color.WHITE,
						darkColor = Color.BLACK,
						marginColor = Color.WHITE
					).getBytes()

					val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
						.asImageBitmap()

					qrCodeMap[repo.id] = QRCodeData(bitmap, url)
					qrCodeMap[repo.id]
				}
			} else {
				null
			}
		}

	override fun showShare(repositoryUI: RepositoryUI) {
		currentShare.value = repositoryUI
	}

	override fun hideShare() {
		currentShare.value = null
	}

	override fun isURL(string: String): Boolean {
		return false
	}

	override fun remove(repo: RepositoryUI) {
		launchIO {
			try {
				deleteRepositoryUseCase(repo)
				removeState.emit(RemoveRepoState.Success(repo))
			} catch (e: Exception) {
				removeState.emit(RemoveRepoState.Failure(e, repo))
			}
		}
	}

	override fun toggleIsEnabled(repo: RepositoryUI) {
		launchIO {
			val newState = !repo.isRepoEnabled
			try {
				updateRepositoryUseCase(repo.copy(isRepoEnabled = newState))
				toggleIsEnabledState.emit(ToggleRepoIsEnabledState.Success(repo, newState))
			} catch (e: Exception) {
				toggleIsEnabledState.emit(ToggleRepoIsEnabledState.Failure(repo, e))
			}
		}
	}

	override fun updateRepositories() {
		viewModelScope.launch {
			if (isOnline.value) {
				startRepositoryUpdateManagerUseCase()
			} else {
				error.emit(OfflineException(R.string.fragment_repositories_snackbar_offline_no_update))
			}
		}
	}

	private val isOnline: StateFlow<Boolean> = isOnlineUseCase.getFlow()
		.stateIn(viewModelScopeIO, SharingStarted.Eagerly, false)
}