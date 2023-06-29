package app.shosetsu.android.viewmodel.abstracted

import app.shosetsu.android.view.uimodels.model.RepositoryUI
import app.shosetsu.android.viewmodel.base.IsOnlineCheckViewModel
import app.shosetsu.android.viewmodel.base.ShosetsuViewModel
import app.shosetsu.android.viewmodel.base.SubscribeViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.StateFlow

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
abstract class ARepositoryViewModel
	: SubscribeViewModel<ImmutableList<RepositoryUI>>, ShosetsuViewModel(),
	IsOnlineCheckViewModel {
	/**
	 * Show add dialog or not
	 */
	abstract val isAddDialogVisible: StateFlow<Boolean>

	/**
	 * Add repo state
	 */
	abstract val addState: StateFlow<AddRepoState>

	/**
	 * Remove repo state
	 */
	abstract val removeState: StateFlow<RemoveRepoState>

	/**
	 * Toggle repo is enabled state
	 */
	abstract val toggleIsEnabledState: StateFlow<ToggleRepoIsEnabledState>

	/**
	 * Undo repo remove state
	 */
	abstract val undoRemoveState: StateFlow<UndoRepoRemoveState>

	/**
	 * Adds a URL via a string the user provides
	 *
	 * @param url THe URL of the repository
	 */
	abstract fun addRepository(name: String, url: String)

	/**
	 * Checks if the string provided is a valid URL
	 */
	abstract fun isURL(string: String): Boolean


	/**
	 * Remove the repo from the app
	 */
	abstract fun remove(repo: RepositoryUI)


	/**
	 * Toggles the state of [RepositoryUI.isRepoEnabled], returns the new state
	 */
	abstract fun toggleIsEnabled(repo: RepositoryUI)

	/**
	 * Start the repository updater
	 */
	abstract fun updateRepositories()

	/**
	 * Try to restore a repository
	 */
	abstract fun undoRemove(repo: RepositoryUI)

	/**
	 * Show the add repo dialog
	 */
	abstract fun showAddDialog()

	/**
	 * Hide the add repo dialog
	 */
	abstract fun hideAddDialog()

	sealed interface AddRepoState {
		object Unknown : AddRepoState
		data class Failure(
			val exception: Exception,
			val name: String,
			val url: String
		) : AddRepoState

		object Success : AddRepoState
	}

	sealed interface UndoRepoRemoveState {
		object Unknown : UndoRepoRemoveState
		data class Failure(
			val repo: RepositoryUI,
			val exception: Exception
		) : UndoRepoRemoveState

		object Success : UndoRepoRemoveState
	}

	sealed interface ToggleRepoIsEnabledState {
		object Unknown : ToggleRepoIsEnabledState
		data class Failure(
			val repo: RepositoryUI,
			val exception: Exception
		) : ToggleRepoIsEnabledState

		data class Success(
			val repo: RepositoryUI,
			val newState: Boolean
		) : ToggleRepoIsEnabledState
	}

	sealed interface RemoveRepoState {
		object Unknown : RemoveRepoState
		data class Failure(
			val exception: Exception,
			val repo: RepositoryUI
		) : RemoveRepoState

		data class Success(val repo: RepositoryUI) : RemoveRepoState
	}
}