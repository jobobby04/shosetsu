package app.shosetsu.android.application

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.database.sqlite.SQLiteException
import android.util.Log
import androidx.core.content.getSystemService
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.work.Configuration
import app.shosetsu.android.common.SettingKey
import app.shosetsu.android.common.consts.Notifications
import app.shosetsu.android.common.consts.ShortCuts
import app.shosetsu.android.common.ext.launchIO
import app.shosetsu.android.common.ext.logE
import app.shosetsu.android.common.ext.toast
import app.shosetsu.android.common.utils.LoggingPrintStream
import app.shosetsu.android.common.utils.SiteProtector
import app.shosetsu.android.di.dataSourceModule
import app.shosetsu.android.di.databaseModule
import app.shosetsu.android.di.networkModule
import app.shosetsu.android.di.othersModule
import app.shosetsu.android.di.providersModule
import app.shosetsu.android.di.repositoryModule
import app.shosetsu.android.di.useCaseModule
import app.shosetsu.android.di.viewModelsModule
import app.shosetsu.android.domain.repository.base.IExtensionLibrariesRepository
import app.shosetsu.android.domain.repository.base.IExtensionsRepository
import app.shosetsu.android.domain.repository.base.ISettingsRepository
import app.shosetsu.android.domain.usecases.StartRepositoryUpdateManagerUseCase
import app.shosetsu.android.domain.usecases.get.GetUserAgentUseCase
import app.shosetsu.android.viewmodel.factory.ViewModelFactory
import app.shosetsu.lib.ShosetsuSharedLib
import app.shosetsu.lib.lua.ShosetsuLuaLib
import app.shosetsu.lib.lua.shosetsuGlobals
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.OneArgFunction
import org.luaj.vm2.lib.ZeroArgFunction

typealias LibLoader = (name: String) -> LuaValue?
typealias ShosetsuLogger = (extensionName: String, log: String) -> Unit

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
 * 28 / 01 / 2020
 */
class ShosetsuApplication : Application(), LifecycleEventObserver, DIAware,
	Configuration.Provider, ImageLoaderFactory {
	private val extLibRepository by instance<IExtensionLibrariesRepository>()
	private val okHttpClient by instance<OkHttpClient>()
	private val startRepositoryUpdateManagerUseCase: StartRepositoryUpdateManagerUseCase by instance()
	private val extensionsRepo: IExtensionsRepository by instance()
	private val settingsRepo: ISettingsRepository by instance()
	private val getUserAgent: GetUserAgentUseCase by instance()

	/***/
	override val di: DI by DI.lazy {
		bind<ViewModelFactory>() with singleton { ViewModelFactory(applicationContext) }
		import(othersModule)
		import(providersModule)
		import(dataSourceModule)
		import(networkModule)
		import(databaseModule)
		import(repositoryModule)
		import(useCaseModule)
		import(viewModelsModule)
		import(androidXModule(this@ShosetsuApplication))
	}

	/**
	 * Perform setup as soon as context is available
	 */
	override fun attachBaseContext(base: Context?) {
		super.attachBaseContext(base)
		Notifications.createChannels(this)
		ShortCuts.createShortcuts(this)
	}

	/***/
	override fun onCreate() {
		runBlocking {
			System.setOut(LoggingPrintStream { Log.i("System,out", it) })
			System.setErr(LoggingPrintStream { Log.e("System,err", it) })
		}

		setupCoreLib()

		launchIO {
			try {
				if (extensionsRepo.loadRepositoryExtensions().isEmpty())
					startRepositoryUpdateManagerUseCase()
			} catch (e: SQLiteException) {
				applicationContext.toast("Could not load extension repositories: $e")
			}
		}
		launchIO {
			settingsRepo.getIntFlow(SettingKey.SiteProtectionPermits).collectLatest {
				SiteProtector.permits = it
			}
		}
		launchIO {
			settingsRepo.getIntFlow(SettingKey.SiteProtectionPeriod).collectLatest {
				SiteProtector.period = it.toLong()
			}
		}
		super.onCreate()
	}

	/**
	 * Setup required methods from the core lib
	 */
	private fun setupCoreLib() {
		ShosetsuSharedLib.httpClient = okHttpClient
		ShosetsuSharedLib.logger = SLog
		ShosetsuLuaLib.libLoader = ShosetsuLibLoader(extLibRepository)
		ShosetsuSharedLib.shosetsuHeaders = arrayOf(
			"User-Agent" to runBlocking { getUserAgent() }
		)
	}

	object SLog : ShosetsuLogger {
		override fun invoke(ext: String, log: String) {
			Log.i(ext, log)
		}
	}

	class ShosetsuLibLoader(private val extLibRepository: IExtensionLibrariesRepository) : LibLoader {
		override fun invoke(name: String): LuaValue? {
			if (name == "xx-print") return PrintLib
			if (name == "xx-time") return TimeLib
			Log.i("LuaLibLoader", "Loading ($name)")
			return try {
				val result = runBlocking { extLibRepository.loadExtLibrary(name) }
				val l =
					shosetsuGlobals().apply {
						STDOUT = LoggingPrintStream { Log.i("lib($name)", it) }
						STDERR = LoggingPrintStream { Log.e("lib($name)", it) }
					}.load(result, "lib($name)")
				l.call()
			} catch (e: Throwable) {
				logE("${e.message}", e)
				null
			}
		}

		object PrintLib : OneArgFunction() {
			override fun call(arg: LuaValue): LuaValue {
				Log.i("LuaLibLoader/print", arg.toString())
				return arg
			}
		}

        object TimeLib : ZeroArgFunction() {
            override fun call(): LuaValue {
				return valueOf(System.currentTimeMillis().toDouble()) // Y2038 bug, but we don't care
            }
        }
	}

	override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {}

	override val workManagerConfiguration: Configuration =
		Configuration.Builder().apply {
		}.build()

	@OptIn(ExperimentalCoroutinesApi::class)
	override fun newImageLoader(): ImageLoader =
		ImageLoader.Builder(this).apply {
			okHttpClient(
				okHttpClient.newBuilder()
					.apply {
						interceptors().remove(SiteProtector)
					}
					.build()
			)
			diskCache {
				DiskCache.Builder().apply {
					directory(cacheDir.resolve("image_cache"))

				}.build()
			}

			@Suppress("ReplaceNotNullAssertionWithElvisReturn")
			allowRgb565(getSystemService<ActivityManager>()!!.isLowRamDevice)

			// Coil spawns a new thread for every image load by default
			fetcherDispatcher(Dispatchers.IO.limitedParallelism(8))
			decoderDispatcher(Dispatchers.IO.limitedParallelism(2))
			transformationDispatcher(Dispatchers.IO.limitedParallelism(2))
		}.build()
}
