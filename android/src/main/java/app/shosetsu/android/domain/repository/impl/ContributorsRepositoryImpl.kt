package app.shosetsu.android.domain.repository.impl

import app.shosetsu.android.common.ext.onIO
import app.shosetsu.android.datasource.remote.base.RemoteGitlabContributorsDataSource
import app.shosetsu.android.domain.model.local.Contributor
import app.shosetsu.android.domain.model.remote.GitlabContributor
import app.shosetsu.android.domain.repository.base.ContributorsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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

	override fun getAll(): Flow<List<Contributor>> = flow { }

	override suspend fun refresh() {
		onIO {
			val remoteContributors = arrayListOf<GitlabContributor>()

			remoteContributors.addAll(remote.get(SHOSETSU_ID))
			remoteContributors.sortBy { it.commits }
			filter(remoteContributors)

			remoteContributors.addAll(remote.get(EXTENSIONS_ID))
			remoteContributors.sortBy { it.commits }
			filter(remoteContributors)

			remoteContributors.addAll(remote.get(LIB_ID))
			remoteContributors.sortBy { it.commits }
			filter(remoteContributors)
		}
	}

	/**
	 * Ensure contributors are unique
	 */
	private fun filter(remoteContributors: ArrayList<GitlabContributor>) {
		for ((index, gc) in remoteContributors.withIndex()) {
			if (index + 1 != remoteContributors.size) {
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
					commits = gc.commits + newCommits,
					additions = gc.additions + newAdditions,
					deletions = gc.deletions + newDeletions
				)
			}
		}
	}

	private val knownLinks = listOf(
		"clocks" to "doomsdayrs"
	)

	private fun isKnownLink(nameA: String, nameB: String) =
		knownLinks.any {
			it.first == nameA && it.second == nameB
					|| it.first == nameB && it.second == nameA
		}

	companion object {
		private const val SHOSETSU_ID = 39099987
		private const val EXTENSIONS_ID = 41616615
		private const val LIB_ID = 41584845
	}
}