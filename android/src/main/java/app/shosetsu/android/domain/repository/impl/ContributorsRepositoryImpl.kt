package app.shosetsu.android.domain.repository.impl

import app.shosetsu.android.common.ext.onIO
import app.shosetsu.android.datasource.remote.base.RemoteGitlabContributorsDataSource
import app.shosetsu.android.domain.model.local.Contributor
import app.shosetsu.android.domain.model.remote.GitlabContributor
import app.shosetsu.android.domain.repository.base.ContributorsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow

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
 * @since 2024/08/17
 * @author Clocks
 */
class ContributorsRepositoryImpl(
	private val remote: RemoteGitlabContributorsDataSource
) : ContributorsRepository {
	private val contributors = MutableStateFlow(emptyList<Contributor>())
	private var lastRefersh = 0;

	override fun getAll(): StateFlow<List<Contributor>> = contributors

	override suspend fun refresh() {
		// 10 minutes between checking again
		if (lastRefersh + (60 * 60 * 1000) >= System.currentTimeMillis()) return;

		onIO {
			val remoteContributors = arrayListOf<GitlabContributor>()

			remoteContributors.addAll(remote.get(SHOSETSU_ID))
			remoteContributors.sortByDescending { it.commits }
			filter(remoteContributors)

			remoteContributors.addAll(remote.get(EXTENSIONS_ID))
			remoteContributors.sortByDescending { it.commits }
			filter(remoteContributors)

			remoteContributors.addAll(remote.get(LIB_ID))
			remoteContributors.sortByDescending { it.commits }
			filter(remoteContributors)

			contributors.emit(remoteContributors.map {
				Contributor(
					it.name,
					getWebsite(it.name) ?: ("mailto:" + it.email),
					getImage(it.name)
				)
			})
		}
	}

	/**
	 * Ensure contributors are unique
	 */
	private fun filter(remoteContributors: ArrayList<GitlabContributor>) {
		var index = 0
		while (index < remoteContributors.size - 1) {
			val gc = remoteContributors[index]

			// Find duplicates
			val matches =
				remoteContributors.subList(index + 1, remoteContributors.size).filter {
					it.name.equals(gc.name, true) || isKnownLink(
						it.name,
						gc.name
					)
				}

			// Remove duplicates
			remoteContributors.removeAll(matches.toSet())

			// Add duplicate values to gc
			val newCommits = matches.sumOf { it.commits }
			val newAdditions = matches.sumOf { it.additions }
			val newDeletions = matches.sumOf { it.deletions }

			// Set new value
			remoteContributors[index] = gc.copy(
				name = getPreferredName(gc.name),
				commits = gc.commits + newCommits,
				additions = gc.additions + newAdditions,
				deletions = gc.deletions + newDeletions
			)

			index++
		}
	}

	companion object {
		private val knownLinks = listOf(
			"clocks" to "doomsdayrs"
		)

		private val knownImages = listOf(
			"Clocks" to "https://gitlab.com/uploads/-/system/user/avatar/3931112/avatar.png?width=256"
		)

		private val preferredNames = listOf(
			"doomsdayrs" to "Clocks"
		)

		private val websites = listOf(
			"clocks" to "https://doomsdayrs.page"
		)

		private fun getImage(name: String) =
			knownImages.firstOrNull { it.first.equals(name, true) }?.second

		private fun getPreferredName(name: String) =
			preferredNames.firstOrNull { it.first.equals(name, true) }?.second ?: name

		private fun isKnownLink(nameA: String, nameB: String) =
			knownLinks.any {
				it.first.equals(nameA, true) && it.second.equals(nameB, true)
						|| it.first.equals(nameB, true) && it.second.equals(nameA, true)
			}

		private fun getWebsite(name: String) =
			websites.firstOrNull { it.first.equals(name, true) }?.second


		private const val SHOSETSU_ID = 39099987
		private const val EXTENSIONS_ID = 41616615
		private const val LIB_ID = 41584845
	}
}