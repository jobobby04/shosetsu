package app.shosetsu.android.datasource.remote.impl.update

import android.annotation.SuppressLint
import app.shosetsu.android.datasource.remote.base.IRemoteAppUpdateDataSource
import app.shosetsu.android.domain.model.local.AppUpdateEntity

class PlayAppUpdateDataSource : IRemoteAppUpdateDataSource {
	@SuppressLint("StopShip")
	override suspend fun loadAppUpdate(): AppUpdateEntity {
		@Suppress("TodoComment")
		TODO("Add play store update source")
	}
}
