package app.shosetsu.android.ui.novel

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import app.shosetsu.android.R
import app.shosetsu.android.common.enums.ChapterSortType.SOURCE
import app.shosetsu.android.common.enums.ChapterSortType.UPLOAD
import app.shosetsu.android.common.enums.ReadingStatus.READ
import app.shosetsu.android.common.enums.ReadingStatus.UNREAD
import app.shosetsu.android.common.enums.TriStateState.CHECKED
import app.shosetsu.android.common.enums.TriStateState.UNCHECKED
import app.shosetsu.android.common.ext.collectLatestLA
import app.shosetsu.android.common.ext.logD
import app.shosetsu.android.common.ext.logV
import app.shosetsu.android.common.ext.makeSnackBar
import app.shosetsu.android.databinding.FragmentNovelInfoBottomMenu0Binding
import app.shosetsu.android.databinding.FragmentNovelInfoBottomMenu1Binding
import app.shosetsu.android.databinding.FragmentNovelInfoBottomMenuBinding
import app.shosetsu.android.view.uimodels.NovelSettingUI
import app.shosetsu.android.viewmodel.abstracted.ANovelViewModel
import kotlinx.coroutines.flow.SharedFlow
import org.acra.ACRA

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
 * 22 / 11 / 2020
 *
 * Creates the bottom menu for Novel Controller
 */
class NovelFilterMenuBuilder(
	private val novelController: NovelFragment,
	private val inflater: LayoutInflater,
	private val viewModel: ANovelViewModel
) {
	private val novelSettingFlow: SharedFlow<NovelSettingUI?> =
		viewModel.novelSettingFlow

	private fun updateNovelSetting(novelSettingUI: NovelSettingUI) =
		viewModel.updateNovelSetting(novelSettingUI)

	fun build(): View =
		FragmentNovelInfoBottomMenuBinding.inflate(
			inflater
		).also { binding ->
			// The work is done purely on the viewPager
			binding.viewPager.apply {
				val menuAdapter = MenuAdapter(binding.root.context)
				this.adapter = menuAdapter
				var isInitialSetup = true
				menuAdapter.onMenuCreated = {
					novelSettingFlow.collectLatestLA(novelController,
						catch = {
							novelController
								.makeSnackBar(it.message ?: "Unknown error loading app theme")
								?.setAction(R.string.report) { _ ->
									ACRA.errorReporter.handleSilentException(it)
								}?.show()
						}) { settings ->
						if (settings == null) return@collectLatestLA

						this@NovelFilterMenuBuilder.logV("Settings $settings")

						// Prevents some data overflow by only running certain loads once
						if (isInitialSetup) {
							isInitialSetup = false
							this@NovelFilterMenuBuilder.logD("Initial setup")
							menuAdapter.menu0.apply {
								bookmarked.isChecked = settings.showOnlyBookmarked
								downloaded.isChecked = settings.showOnlyDownloaded
								when (settings.showOnlyReadingStatusOf) {
									UNREAD -> unreadRadioButton.isChecked = true
									READ -> readRadioButton.isChecked = true
									else -> allRadioButton.isChecked = true
								}
							}
							menuAdapter.menu1.apply {
								val reversed = settings.reverseOrder

								when (settings.sortType) {
									SOURCE -> bySource::state
									UPLOAD -> byDate::state
								}.set(if (!reversed) CHECKED else UNCHECKED)
							}
						}

						// refresh all the bindings always
						this@NovelFilterMenuBuilder.logD("Setting bindings")
						menuAdapter.menu0.apply {
							this@NovelFilterMenuBuilder.logD("Menu binding#0")
							bookmarked.setOnCheckedChangeListener { _, state ->
								updateNovelSetting(
									settings.copy(
										showOnlyBookmarked = state
									)
								)
							}

							downloaded.setOnCheckedChangeListener { _, state ->
								updateNovelSetting(
									settings.copy(
										showOnlyDownloaded = state
									)
								)
							}

							radioGroup.setOnCheckedChangeListener { _, checkedId ->
								when (checkedId) {
									R.id.all_radio_button -> updateNovelSetting(
										settings.copy(
											showOnlyReadingStatusOf = null
										)
									)

									R.id.read_radio_button -> updateNovelSetting(
										settings.copy(
											showOnlyReadingStatusOf = READ
										)
									)

									R.id.unread_radio_button -> updateNovelSetting(
										settings.copy(
											showOnlyReadingStatusOf = UNREAD
										)
									)

								}
							}

						}
						menuAdapter.menu1.apply {
							this@NovelFilterMenuBuilder.logD("Menu binding#1")
							triStateGroup.addOnStateChangeListener { id, state ->
								updateNovelSetting(
									settings.copy(
										sortType = when (id) {
											R.id.by_date -> UPLOAD
											R.id.by_source -> SOURCE
											else -> UPLOAD
										},
										reverseOrder = state != CHECKED
									)
								)
							}
						}
					}
				}
			}
		}.root

	inner class MenuAdapter(
		private val context: Context
	) : PagerAdapter() {

		override fun getCount(): Int = 2
		override fun getPageTitle(position: Int): CharSequence? = when (position) {
			0 -> context.getString(R.string.filter)
			1 -> context.getString(R.string.sort)
			else -> null
		}

		override fun isViewFromObject(view: View, obj: Any): Boolean = view == obj
		lateinit var menu0: FragmentNovelInfoBottomMenu0Binding
		lateinit var menu1: FragmentNovelInfoBottomMenu1Binding

		private var menu0Created = false
		private var menu1Created = false
		var onMenuCreated: () -> Unit = {}

		fun markCreated(i: Int) {
			if (i == 0)
				menu0Created = true
			else
				menu1Created = true

			if (menu0Created && menu1Created) onMenuCreated()
		}

		override fun instantiateItem(container: ViewGroup, position: Int): Any {
			when (position) {
				0 -> {
					val view = FragmentNovelInfoBottomMenu0Binding.inflate(
						inflater,
						container,
						false
					).also {
						menu0 = it
					}.root
					container.addView(view)
					markCreated(0)
					return view
				}
				1 -> {
					val view = FragmentNovelInfoBottomMenu1Binding.inflate(
						inflater,
						container,
						false
					).also {
						menu1 = it
					}.root
					container.addView(view)
					markCreated(1)
					return view
				}
			}
			return super.instantiateItem(container, position)
		}

		override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
			(obj as? View)?.let {
				container.removeView(it)
			}
		}
	}
}