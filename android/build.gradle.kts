import org.eclipse.jgit.api.Git

plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.kotlin.android)
	alias(libs.plugins.kotlin.serialization)
	alias(libs.plugins.google.ksp)
	alias(libs.plugins.kotlin.compose)
}

val generateContributors by tasks.registering(GenerateContributorsTask::class)
tasks.preBuild { dependsOn(generateContributors) }

val CI_MODE = System.getenv("CI_MODE") == "true"
val USE_JENKINS = System.getenv("USE_JENKINS") == "true"

android {
	// keep these in sync with .gitlab-ci.yml
	compileSdk = 36
	buildToolsVersion = "37.0.0"
	defaultConfig {
		applicationId = "app.shosetsu.android"
		minSdk = 22
		targetSdk = 36
		versionCode = 50
		versionName = "2.5.3"
		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
		multiDexEnabled = true

		buildConfigField(
			"String",
			"acraUsername",
			loadSProperties("acra")["username"]?.toString() ?: "\"\""
		)
		buildConfigField(
			"String",
			"acraPassword",
			loadSProperties("acra")["password"]?.toString() ?: "\"\""
		)

		// Hopefully this results in the beloved shosetsu-... apks
		base.archivesName.set(rootProject.name)

		vectorDrawables {
			useSupportLibrary = true
		}
	}

	buildFeatures {
		viewBinding = true
		compose = true
		buildConfig = true
	}

	composeOptions {
		kotlinCompilerExtensionVersion = "1.5.7"
	}

	/*
	splits {
		abi {
			isEnable = true

			isUniversalApk = true
		}
	}
	 */


	buildTypes {
		named("release") {
			isMinifyEnabled = !CI_MODE
			isShrinkResources = !CI_MODE
			proguardFiles(
				getDefaultProguardFile("proguard-android-optimize.txt"),
				"proguard-rules.pro"
			)
			versionNameSuffix = ""
			multiDexEnabled = true
		}
		named("debug") {
			versionNameSuffix = Git.open(rootProject.projectDir.resolve(".git")).use { "-${it.getCommitCount()}" }
			applicationIdSuffix = ".debug"
			isDebuggable = true
			isMinifyEnabled = !CI_MODE
			isShrinkResources = !CI_MODE
			proguardFiles(
				getDefaultProguardFile("proguard-android-optimize.txt"),
				"proguard-rules.pro"
			)
			buildConfigField(
				"String",
				"acraUsername",
				loadSProperties("acra-debug")["username"]?.toString() ?: "\"\""
			)
			buildConfigField(
				"String",
				"acraPassword",
				loadSProperties("acra-debug")["password"]?.toString() ?: "\"\""
			)
		}
	}
	flavorDimensions += listOf("default")
	productFlavors {
		create("playstore") {
			// play store will be in this
			applicationId = "app.shosetsu.android"
			applicationIdSuffix = ".play"
			versionNameSuffix = "-play"
		}
		create("uptodown") {
			applicationIdSuffix = ".uptodown"
			versionNameSuffix = "-uptodown"
			buildConfigField(
				"String",
				"acraUsername",
				loadSProperties("acra-uptodown")["username"]?.toString() ?: "\"\""
			)
			buildConfigField(
				"String",
				"acraPassword",
				loadSProperties("acra-uptodown")["password"]?.toString() ?: "\"\""
			)
		}
		create("fdroid") {
			applicationIdSuffix = ".fdroid"
			versionNameSuffix = "-fdroid"
			buildConfigField(
				"String",
				"acraUsername",
				loadSProperties("acra-fdroid")["username"]?.toString() ?: "\"\""
			)
			buildConfigField(
				"String",
				"acraPassword",
				loadSProperties("acra-fdroid")["password"]?.toString() ?: "\"\""
			)

		}
		create("standard") {
			isDefault = true
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
		isCoreLibraryDesugaringEnabled = true
	}
	kotlinOptions {
		jvmTarget = JavaVersion.VERSION_17.toString()
		freeCompilerArgs = freeCompilerArgs + "-Xjvm-default=all-compatibility"
	}

	lint {
		disable.addAll(listOf("MissingTranslation", "ExtraTranslation"))
		abortOnError = false
	}
	namespace = "app.shosetsu.android"
	packaging {
		resources {
			excludes += "/META-INF/{AL2.0,LGPL2.1}"
		}
	}
	sourceSets {
		named("main") {
			kotlin.srcDir(generateContributors.map { it.generatedKotlinDir })
		}
	}
}

ksp {
	arg("room.schemaLocation", "$projectDir/schemas")
}

//TODO Fix application variant naming
/*
android.applicationVariants.forEach { variant ->
	variant.outputs.all {
		val v = this as com.android.build.gradle.internal.api.ApkVariantOutputImpl
		val appName = "shosetsu"
		val versionName = variant.versionName
		val versionCode = variant.versionCode
		val flavorName = variant.flavorName
		val buildType = variant.buildType.name
		val variantName = variant.name
		val gitCount = getCommitCount()

		outputFileName = "${appName}-" +
				if (buildType == "debug" && flavorName.toString() == "standard") {
					gitCount
				} else {
					versionName
				} + ".apk"
	}
}
 */

dependencies {
	implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

	implementation(platform(libs.kotlin.bom))

	// Androidx
	implementation(libs.androidx.work.runtime)
	implementation(libs.androidx.work.runtime.ktx)
	implementation(libs.androidx.appcompat)
	implementation(libs.androidx.annotation)
	implementation(libs.androidx.core.ktx)
	implementation(libs.androidx.collection.ktx)
	implementation(libs.androidx.core.splashscreen)
	implementation(libs.androidx.coordinatorlayout)
	implementation(libs.androidx.window)
	implementation(libs.androidx.compose.material3.wsc)
	implementation(libs.androidx.lifecycle.runtime.ktx)
	implementation(libs.androidx.activity.compose)

	implementation(platform(libs.androidx.compose.bom))
	androidTestImplementation(platform(libs.androidx.compose.bom))

	androidTestImplementation(libs.androidx.compose.ui.testjunit4)
	androidTestImplementation(platform(libs.androidx.compose.bom))

	debugImplementation(libs.androidx.compose.ui.tooling)
	debugImplementation(libs.androidx.compose.ui.testmanifest)

	// - Life Cycle

	implementation(libs.androidx.lifecycle.viewmodel.ktx)
	implementation(libs.androidx.lifecycle.viewmodel.compose)
	implementation(libs.androidx.lifecycle.viewmodel.savedstate)
	implementation(libs.androidx.lifecycle.runtime.ktx)


	// Test classes
	testImplementation(libs.junit)
	testImplementation(libs.androidx.test.ext.junit)
	androidTestImplementation(libs.androidx.test.runner)
	androidTestImplementation(libs.androidx.test.espresso.core)


	// Core libraries
	implementation(libs.luaj.jse)
	if (USE_JENKINS) {
		// F-Droid does not like gitlab maven
		implementation(libs.shosetsuorg.klib.jitpack)
	} else {
		// Use gitlab maven build
		implementation(libs.shosetsuorg.klib)
	}
	implementation(libs.jsoup)

	// Image loading
	implementation(libs.coil.compose)

	// Time control
	implementation(libs.joda.time)

	// Cloud flare calculator
	//implementation("com.zhkrb.cloudflare-scrape-android:scrape-webview:0.0.3")

	// Network
	implementation(libs.okhttp)

	// Kotlin libraries
	implementation(kotlin("stdlib-jdk8"))
	//implementation(kotlin("reflect"))

	implementation(libs.kotlinx.coroutines.android)

	implementation(libs.kotlinx.collections.immutable)

	// Error logging

	implementation(libs.acra.http)
	implementation(libs.acra.dialog)

	// Conductor
	/*
	val conductorVersion = "3.1.5"
	fun conductor(module: String, version: String = conductorVersion) =
		"com.bluelinelabs:$module:$version"

	implementation(conductor("conductor"))
	implementation(conductor("conductor-androidx-transition"))
	implementation(conductor("conductor-archlifecycle"))
	 */

	// Room

	implementation(libs.androidx.room.runtime)
	annotationProcessor(libs.androidx.room.compiler)
	ksp(libs.androidx.room.compiler)
	implementation(libs.androidx.room.ktx)
	implementation(libs.androidx.room.paging)

	// Guava cache
	implementation(libs.google.guava)

	// kode-in

	implementation(libs.kodein.di)
	implementation(libs.kodein.di.jvm)
	implementation(libs.kodein.di.framework.android.core)
	implementation(libs.kodein.di.framework.androidx)
	implementation(libs.kodein.di.framework.androidx.viewmodel)
	implementation(libs.kodein.di.framework.androidx.viewmodel.savedstate)

	// KTX

	implementation(libs.kotlinx.coroutines.jdk8)

	// KTX - Serialization
	implementation(libs.kotlinx.serialization.json)

	// Roomigrant
	/*val enableRoomigrant = false

	val roomigrantVersion = "0.3.4"
	implementation("com.github.MatrixDev.Roomigrant:RoomigrantLib:$roomigrantVersion")
	if (enableRoomigrant) {
		kapt("com.github.MatrixDev.Roomigrant:RoomigrantCompiler:$roomigrantVersion")
	}*/

	// Compose

	implementation(platform(libs.androidx.compose.bom))
	implementation(libs.androidx.compose.runtime)
	implementation(libs.androidx.compose.ui)
	implementation(libs.androidx.compose.ui.tooling)
	implementation(libs.androidx.compose.ui.graphics)
	implementation(libs.androidx.compose.ui.tooling.preview)
	implementation(libs.androidx.compose.material3)
	implementation(libs.androidx.material.icons.extended)
	implementation(libs.androidx.compose.foundation)
	implementation(libs.androidx.compose.animation)
	implementation(libs.androidx.compose.animation.graphics)
	implementation(libs.androidx.compose.animation.core)

	// - accompanist

	implementation(libs.google.accompanist.appcompat.theme)
	implementation(libs.google.accompanist.webview)
	implementation(libs.google.accompanist.placeholder.material)
	implementation(libs.google.accompanist.pager.indicators)
	implementation(libs.google.accompanist.permissions)
	implementation(libs.google.accompanist.systemuicontroller)

	//- Integration with observables
	implementation(libs.androidx.compose.runtime.livedata)

	// MDC Adapter
	implementation(libs.google.accompanist.themeadapter.material)
	implementation(libs.google.accompanist.themeadapter.material3)

	implementation(libs.androidx.activity)
	implementation(libs.androidx.activity.ktx)
	implementation(libs.androidx.activity.compose)

	implementation(libs.numberpicker)

	// QR Code
	implementation(libs.qrcode)

	// - paging

	implementation(libs.androidx.paging.runtime)
	implementation(libs.androidx.paging.compose)

	implementation(kotlin("reflect"))

	implementation(libs.androidx.navigation.compose)

	coreLibraryDesugaring(libs.desugar)

	implementation(libs.bottomsheetdialog)
}
