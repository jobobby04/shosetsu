package app.shosetsu.android.viewmodel.impl

import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
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
import app.shosetsu.android.view.uimodels.model.RepositoryUI
import app.shosetsu.android.viewmodel.abstracted.ARepositoryViewModel
import app.shosetsu.lib.share.RepositoryLink
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import io.github.g0dkar.qrcode.QRCode
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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
	override val isAddDialogVisible = MutableStateFlow(false)
	override val addState = MutableStateFlow<AddRepoState>(AddRepoState.Unknown)
	override val removeState = MutableStateFlow<RemoveRepoState>(RemoveRepoState.Unknown)
	override val toggleIsEnabledState =
		MutableStateFlow<ToggleRepoIsEnabledState>(ToggleRepoIsEnabledState.Unknown)
	override val undoRemoveState =
		MutableStateFlow<UndoRepoRemoveState>(UndoRepoRemoveState.Unknown)

	override fun addRepository(name: String, url: String) {
		launchIO {
			try {
				addRepositoryUseCase(url = url, name = name)
				addState.value = AddRepoState.Success
			} catch (e: Exception) {
				addState.value = AddRepoState.Failure(e, name, url)
			} finally {
				delay(100)
				addState.value = AddRepoState.Unknown
			}
		}
	}

	override fun undoRemove(repo: RepositoryUI) {
		launchIO {
			try {
				forceInsertRepositoryUseCase(repo)
				undoRemoveState.value = UndoRepoRemoveState.Success
			} catch (e: Exception) {
				undoRemoveState.value = UndoRepoRemoveState.Failure(repo, e)
			} finally {
				delay(100)
				undoRemoveState.value = UndoRepoRemoveState.Unknown
			}
		}
	}

	override fun showAddDialog() {
		isAddDialogVisible.value = true
	}

	override fun hideAddDialog() {
		isAddDialogVisible.value = false
	}

	private val qrCodeMap: Cache<Int, ImageBitmap?> =
		CacheBuilder
			.newBuilder()
			.expireAfterAccess(1, TimeUnit.MINUTES)
			.build()

	override val currentShare = MutableStateFlow<RepositoryUI?>(null)

	override val qrCode: Flow<ImageBitmap?> =
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

					qrCodeMap[repo.id] = bitmap

					bitmap
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
				removeState.value = RemoveRepoState.Success(repo)
			} catch (e: Exception) {
				removeState.value = RemoveRepoState.Failure(e, repo)
			} finally {
				delay(100)
				removeState.value = RemoveRepoState.Unknown
			}
		}
	}

	override fun toggleIsEnabled(repo: RepositoryUI) {
		launchIO {
			val newState = !repo.isRepoEnabled
			try {
				updateRepositoryUseCase(repo.copy(isRepoEnabled = newState))
				toggleIsEnabledState.value =
					ToggleRepoIsEnabledState.Success(repo, newState)
			} catch (e: Exception) {
				toggleIsEnabledState.value = ToggleRepoIsEnabledState.Failure(repo, e)
			} finally {
				delay(100)
				toggleIsEnabledState.value = ToggleRepoIsEnabledState.Unknown
			}
		}
	}

	override fun updateRepositories() {
		startRepositoryUpdateManagerUseCase()
	}

	override fun isOnline(): Boolean = isOnlineUseCase()
}