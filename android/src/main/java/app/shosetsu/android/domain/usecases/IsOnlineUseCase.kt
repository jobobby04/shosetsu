package app.shosetsu.android.domain.usecases

import android.app.Application
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkCapabilities.*
import android.os.Build
import androidx.core.content.getSystemService
import androidx.work.impl.utils.registerDefaultNetworkCallbackCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

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
 * shosetsu
 * 04 / 09 / 2020
 */
class IsOnlineUseCase(
	private val application: Application,
) {
	private val connectivityManager by lazy {
		application.getSystemService<ConnectivityManager>()!!
	}

	operator fun invoke(): Boolean {
		return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			val networkCapabilities = connectivityManager.activeNetwork ?: return false
			val actNw = connectivityManager.getNetworkCapabilities(networkCapabilities)
				?: return false
			when {
				actNw.hasTransport(TRANSPORT_BLUETOOTH) -> true
				actNw.hasTransport(TRANSPORT_CELLULAR) -> true
				actNw.hasTransport(TRANSPORT_ETHERNET) -> true
				actNw.hasTransport(TRANSPORT_VPN) -> true
				actNw.hasTransport(TRANSPORT_WIFI) -> true
				else -> false
			}
		} else {
			// Suppressing warnings since this is old API usage
			@Suppress("DEPRECATION")
			val type = connectivityManager.activeNetworkInfo ?: return false
			@Suppress("DEPRECATION")
			when (type.type) {
				ConnectivityManager.TYPE_WIFI -> true
				ConnectivityManager.TYPE_MOBILE -> true
				ConnectivityManager.TYPE_ETHERNET -> true
				ConnectivityManager.TYPE_VPN -> true
				ConnectivityManager.TYPE_BLUETOOTH -> true
				else -> false
			}
		}
	}

	fun getFlow(): Flow<Boolean> = callbackFlow {
		val callback = object : ConnectivityManager.NetworkCallback() {
			override fun onAvailable(network: Network) {
				super.onAvailable(network)
				trySend(invoke())
			}

			override fun onLosing(network: Network, maxMsToLive: Int) {
				super.onLosing(network, maxMsToLive)
				trySend(invoke())
			}

			override fun onCapabilitiesChanged(
				network: Network,
				networkCapabilities: NetworkCapabilities
			) {
				super.onCapabilitiesChanged(network, networkCapabilities)
				trySend(invoke())
			}

			override fun onLost(network: Network) {
				super.onLost(network)
				trySend(invoke())
			}

			override fun onUnavailable() {
				super.onUnavailable()
				trySend(invoke())
			}
		}

		connectivityManager.registerDefaultNetworkCallbackCompat(callback)

		awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
	}
}