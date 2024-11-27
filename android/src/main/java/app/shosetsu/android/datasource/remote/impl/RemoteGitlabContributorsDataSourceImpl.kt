package app.shosetsu.android.datasource.remote.impl

import app.shosetsu.android.common.EmptyResponseBodyException
import app.shosetsu.android.common.ext.decodeSafeFromStream
import app.shosetsu.android.common.ext.quickie
import app.shosetsu.android.datasource.remote.base.RemoteGitlabContributorsDataSource
import app.shosetsu.android.domain.model.remote.GitlabContributor
import app.shosetsu.lib.exceptions.HTTPException
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient

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
 * 12 / May / 2020
 */
class RemoteGitlabContributorsDataSourceImpl(
	private val client: OkHttpClient,
) : RemoteGitlabContributorsDataSource {

	/**
	 * @see <a href="https://docs.gitlab.com/ee/api/repositories.html#contributors">Gitlab contributors API</a>
	 */
	@OptIn(ExperimentalSerializationApi::class)
	override suspend fun get(id: Int): List<GitlabContributor> {
		val url = "https://gitlab.com/api/v4/projects/$id/repository/contributors"

		val response = client.quickie(url)

		if (response.isSuccessful) {
			return response.body?.use {
				Json.decodeSafeFromStream(it.byteStream())
			} ?: throw EmptyResponseBodyException(url)
		} else {
			throw HTTPException(response.code)
		}
	}
}