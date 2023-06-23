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
 *
 */

package app.shosetsu.android.viewmodel.impl

import app.shosetsu.android.common.ext.launchIO
import app.shosetsu.android.domain.usecases.AddCategoryUseCase
import app.shosetsu.android.domain.usecases.DeleteCategoryUseCase
import app.shosetsu.android.domain.usecases.MoveCategoryUseCase
import app.shosetsu.android.domain.usecases.get.GetCategoriesUseCase
import app.shosetsu.android.view.uimodels.model.CategoryUI
import app.shosetsu.android.viewmodel.abstracted.ACategoriesViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class CategoriesViewModel(
	private val getCategoriesUseCase: GetCategoriesUseCase,
	private val addCategoryUseCase: AddCategoryUseCase,
	private val deleteCategoryUseCase: DeleteCategoryUseCase,
	private val moveCategoryUseCase: MoveCategoryUseCase
) : ACategoriesViewModel() {

	override val liveData: StateFlow<ImmutableList<CategoryUI>> by lazy {
		getCategoriesUseCase().map { it.toImmutableList() }
			.stateIn(viewModelScopeIO, SharingStarted.Lazily, persistentListOf())
	}
	override val addCategoryState =
		MutableStateFlow<CategoryChangeState>(CategoryChangeState.Unknown)

	override val removeCategoryState =
		MutableStateFlow<CategoryChangeState>(CategoryChangeState.Unknown)

	override val moveUpCategoryState =
		MutableStateFlow<CategoryChangeState>(CategoryChangeState.Unknown)

	override val moveDownCategoryState =
		MutableStateFlow<CategoryChangeState>(CategoryChangeState.Unknown)

	override fun addCategory(name: String): Unit {
		launchIO {
			try {
				addCategoryUseCase(name)
				addCategoryState.emit(CategoryChangeState.Finished)
			} catch (e: Exception) {
				addCategoryState.emit(
					CategoryChangeState.Failure(
						CategoryUI(-1, name, -1),
						e
					)
				)
			} finally {
				delay(100)
				addCategoryState.emit(CategoryChangeState.Unknown)
			}
		}
	}

	override fun remove(categoryUI: CategoryUI): Unit {
		launchIO {
			try {
				deleteCategoryUseCase(categoryUI)
				removeCategoryState.emit(CategoryChangeState.Finished)
			} catch (e: Exception) {
				removeCategoryState.emit(
					CategoryChangeState.Failure(
						categoryUI,
						e
					)
				)
			} finally {
				delay(100)
				removeCategoryState.emit(CategoryChangeState.Unknown)
			}
		}
	}

	override fun moveUp(categoryUI: CategoryUI) {
		launchIO {
			try {
				moveCategoryUseCase(categoryUI, categoryUI.order + 1)
				moveUpCategoryState.emit(CategoryChangeState.Finished)
			} catch (e: Exception) {
				moveUpCategoryState.emit(
					CategoryChangeState.Failure(
						categoryUI,
						e
					)
				)
			} finally {
				delay(100)
				moveUpCategoryState.emit(CategoryChangeState.Unknown)
			}
		}
	}

	override fun moveDown(categoryUI: CategoryUI) {
		launchIO {
			try {
				moveCategoryUseCase(categoryUI, categoryUI.order - 1)
				moveDownCategoryState.emit(CategoryChangeState.Finished)
			} catch (e: Exception) {
				moveDownCategoryState.emit(
					CategoryChangeState.Failure(
						categoryUI,
						e
					)
				)
			} finally {
				delay(100)
				moveDownCategoryState.emit(CategoryChangeState.Unknown)
			}
		}
	}

	override val isAddDialogVisible = MutableStateFlow<Boolean>(false)
	override fun showAddDialog() {
		isAddDialogVisible.tryEmit(true)
	}

	override fun hideAddDialog() {
		isAddDialogVisible.tryEmit(false)
	}
}