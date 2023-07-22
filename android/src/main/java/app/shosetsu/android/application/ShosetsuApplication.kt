package app.shosetsu.android.application

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.database.sqlite.SQLiteException
import android.util.Log
import android.widget.Toast
import androidx.core.content.getSystemService
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.work.Configuration
import app.shosetsu.android.BuildConfig
import app.shosetsu.android.R
import app.shosetsu.android.common.FLAG_CONCURRENT_MEMORY
import app.shosetsu.android.common.SettingKey
import app.shosetsu.android.common.consts.Notifications
import app.shosetsu.android.common.consts.ShortCuts
import app.shosetsu.android.common.ext.fileOut
import app.shosetsu.android.common.ext.launchIO
import app.shosetsu.android.common.ext.logE
import app.shosetsu.android.common.ext.toast
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
import com.google.android.material.color.DynamicColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.acra.ACRA
import org.acra.config.dialog
import org.acra.config.httpSender
import org.acra.data.StringFormat
import org.acra.ktx.initAcra
import org.acra.sender.HttpSender.Method
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.PrintStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

	override fun attachBaseContext(base: Context?) {
		super.attachBaseContext(base)
		Notifications.createChannels(this)
		ShortCuts.createShortcuts(this)

		runBlocking {
			// Enable ACRA if allowed or if it is the first run
			// We want it enabled for first run to catch critical app errors
			if (settingsRepo.getBoolean(SettingKey.ACRAEnabled) ||
				settingsRepo.getBoolean(SettingKey.FirstTime)
			) {
				setupACRA()
			}
		}
	}

	private fun setupDualOutput() {
		val dir = getExternalFilesDir(null)

		// Ensure log file directory exists
		val loggingDir = File(dir, "logs").also { loggingDir ->
			// Ensure file "logs" exists and is a directory
			if (loggingDir.exists()) {
				if (!loggingDir.isDirectory) {
					loggingDir.delete()
					loggingDir.mkdirs()
				} else {
					// Launch to let app boot faster
					launchIO {
						// Ensure only that only 5 are kept to keep file usage down
						loggingDir.listFiles { it: File -> it.isFile }
							?.sortedBy { it.lastModified() }
							?.takeIf { it.size > 5 }
							?.let {
								val length = it.size - 5
								for (index in 0..length)
									it[index].delete()
							}
					}
				}
			} else {
				loggingDir.mkdirs()
			}
		}


		val fileDate = SimpleDateFormat("yyyy-MM-dd-hh-mm-ss", Locale.ROOT).format(Date())
		val logFile = File(loggingDir, "shosetsu-log-$fileDate.txt")

		try {
			logFile.createNewFile()
		} catch (e: IOException) {
			toast(R.string.toast_error_log_failed, Toast.LENGTH_LONG)
			logE("Failed to create logfile", e)
			return
		}

		val logOS = FileOutputStream(logFile)

		fileOut = PrintStream(logOS)

		System.setOut(
			PrintStream(
				MultipleOutputStream(
					System.out,
					logOS
				)
			)
		)

		System.setErr(
			PrintStream(
				MultipleOutputStream(
					System.err,
					logOS
				)
			)
		)
	}

	override fun onCreate() {

		runBlocking {
			if (settingsRepo.getBoolean(SettingKey.LogToFile))
				setupDualOutput()

			FLAG_CONCURRENT_MEMORY = settingsRepo.getBoolean(SettingKey.ConcurrentMemoryExperiment)
		}

		setupCoreLib()

		launchIO {
			try {
				if (extensionsRepo.loadRepositoryExtensions().isEmpty())
					startRepositoryUpdateManagerUseCase()
			} catch (e: SQLiteException) {
				ACRA.errorReporter.handleException(e)
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
		DynamicColors.applyToActivitiesIfAvailable(this)
	}

	/**
	 * Setup required methods from the core lib
	 */
	private fun setupCoreLib() {
		ShosetsuSharedLib.httpClient = okHttpClient

		ShosetsuSharedLib.logger = { ext, arg ->
			Log.i(ext, arg)
		}

		ShosetsuLuaLib.libLoader = libLoader@{ name ->
			Log.i("LuaLibLoader", "Loading ($name)")
			try {
				val result = runBlocking { extLibRepository.loadExtLibrary(name) }
				val l =
					shosetsuGlobals().load(result, "lib($name)")
				l.call()
			} catch (e: Throwable) {
				logE("${e.message}", e)
				null
			}
		}

		ShosetsuSharedLib.shosetsuHeaders = arrayOf(
			"User-Agent" to runBlocking { getUserAgent() }
		)
	}

	private fun setupACRA() {
		initAcra {
			buildConfigClass = BuildConfig::class.java
			reportFormat = StringFormat.JSON
			dialog {
				commentPrompt = getString(R.string.crashCommentPromt)
				text = getString(R.string.crashDialogText)
				resTheme = R.style.AppTheme_CrashReport
			}
			httpSender {
				uri = "https://acra.shosetsu.app/report" /*best guess, you may need to adjust this*/
				basicAuthLogin = BuildConfig.acraUsername
				basicAuthPassword = BuildConfig.acraPassword
				httpMethod = Method.POST
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
			okHttpClient(okHttpClient)
			diskCache {
				DiskCache.Builder().apply {
					directory(cacheDir.resolve("image_cache"))

				}.build()
			}

			allowRgb565(getSystemService<ActivityManager>()!!.isLowRamDevice)

			// Coil spawns a new thread for every image load by default
			fetcherDispatcher(Dispatchers.IO.limitedParallelism(8))
			decoderDispatcher(Dispatchers.IO.limitedParallelism(2))
			transformationDispatcher(Dispatchers.IO.limitedParallelism(2))
		}.build()
}