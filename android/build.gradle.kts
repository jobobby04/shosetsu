import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

plugins {
	id("com.android.application")
	kotlin("android")
	kotlin("plugin.serialization")
	id("com.google.devtools.ksp")
}

@Throws(IOException::class)
fun String.execute(): Process = Runtime.getRuntime().exec(this)

@Throws(IOException::class)
fun Process.getText(): String =
	org.codehaus.groovy.runtime.IOGroovyMethods.getText(
		BufferedReader(
			InputStreamReader(
				inputStream
			)
		)
	).also {
		org.codehaus.groovy.runtime.ProcessGroovyMethods.closeStreams(this)
	}

val versionMajor = 2
val versionMinor = 4
val versionPatch = 4
val versionBuild = System.getenv("CI_PIPELINE_IID")?.toIntOrNull() ?: 0

val computedVersionName by lazy { "$versionMajor.$versionMinor.$versionPatch" + if (versionBuild > 0) "+$versionBuild" else "" }

// Version code: S VVVVV MMMMMMM PPPPP IIIIIIIIIIIIII (32-bit integer)
// S (x1):  Sign bit. Must always be 0 for an android version code
// V (x5):  Major version. Up to 32, which should be enough (especially since we are still on 0)
//          This might not work for apps that follow proper semantic versioning, but who does that?
// M (x7):  Minor version. Up to 128, which should be enough
// P (x5):  Patch version. Up to 32, which should be enough for these
// I (x14): Pipeline ID bits. Allows a total of 16384 pipeline runs.
//          I'm simply guessing that that'll be enough
//
// This implementation assumes that these maximum numbers will never be reached.
// If they are reached, the version codes "bleed over" into the next range,
// so this should technically still produce valid, higher versions, but the format will be broken.
val computedVersionCode by lazy {
	var bits = 0
	bits = (bits shl 5) or versionMajor
	bits = (bits shl 7) or versionMinor
	bits = (bits shl 5) or versionPatch
	bits = (bits shl 14) or versionBuild
	bits
}

@Throws(IOException::class)
fun getCommitCount(): String = "git rev-list --count HEAD".execute().getText().trim()

android {
	compileSdk = 34
	defaultConfig {
		applicationId = "app.shosetsu.android.sy"
		minSdk = 22
		targetSdk = 34
		versionCode = computedVersionCode
		versionName = computedVersionName
		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
		multiDexEnabled = true

		setProperty("archivesBaseName", rootProject.name)
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
			isMinifyEnabled = true
			isShrinkResources = true
			proguardFiles(
				getDefaultProguardFile("proguard-android-optimize.txt"),
				"proguard-rules.pro"
			)
			versionNameSuffix = ""
			multiDexEnabled = true
		}
		named("debug") {
			versionNameSuffix = "-${getCommitCount()}"
			applicationIdSuffix = ".debug"
			isDebuggable = true
			isMinifyEnabled = false
			isShrinkResources = false
			proguardFiles(
				getDefaultProguardFile("proguard-android-optimize.txt"),
				"proguard-rules.pro"
			)
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

	// Google view things
	implementation("com.google.android.material:material:1.10.0")

	// Androidx
	implementation("androidx.work:work-runtime:2.9.0")
	implementation("androidx.work:work-runtime-ktx:2.9.0")
	implementation("androidx.appcompat:appcompat:1.6.1")
	implementation("androidx.annotation:annotation:1.7.1")
	implementation("androidx.core:core-ktx:1.12.0")
	implementation("androidx.collection:collection-ktx:1.4.0")
	implementation("androidx.core:core-splashscreen:1.0.1")
	implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
	implementation("androidx.window:window:1.2.0")
	implementation("androidx.compose.material3:material3-window-size-class:1.2.1")
	implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
	implementation("androidx.activity:activity-compose:1.8.2")

	implementation(platform("androidx.compose:compose-bom:2024.02.02"))
	androidTestImplementation(platform("androidx.compose:compose-bom:2024.02.02"))

	androidTestImplementation("androidx.compose.ui:ui-test-junit4")
	androidTestImplementation(platform("androidx.compose:compose-bom:2024.02.02"))

	debugImplementation("androidx.compose.ui:ui-tooling")
	debugImplementation("androidx.compose.ui:ui-test-manifest")

	// - Life Cycle

	val lifecycleVersion = "2.6.2"
	fun lifecycle(module: String, version: String = lifecycleVersion) =
		"androidx.lifecycle:lifecycle-$module:$version"
	implementation(lifecycle("viewmodel-ktx"))
	implementation(lifecycle("viewmodel-compose"))
	implementation(lifecycle("viewmodel-savedstate"))
	implementation(lifecycle("runtime-ktx"))


	// Test classes
	testImplementation("junit:junit:4.13.2")
	testImplementation("androidx.test.ext:junit:1.1.5")
	androidTestImplementation("androidx.test:runner:1.5.2")
	androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")


	// Core libraries
	implementation("org.luaj:luaj-jse:3.0.1")
	implementation("com.gitlab.jobobby04:kotlin-lib:5c04c7d99addc238600976fd5500c3ee67e72a6b")
	implementation("org.jsoup:jsoup:1.17.2")

	// Image loading
	implementation("io.coil-kt:coil-compose:2.6.0")

	// Time control
	implementation("joda-time:joda-time:2.12.7")

	// Cloud flare calculator
	//implementation("com.zhkrb.cloudflare-scrape-android:scrape-webview:0.0.3")

	// Network
	implementation("com.squareup.okhttp3:okhttp:4.12.0")

	// Kotlin libraries
	implementation(kotlin("stdlib-jdk8"))
	//implementation(kotlin("reflect"))

	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")

	implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.7")

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
	val roomVersion = "2.5.2"
	fun room(module: String, version: String = roomVersion) =
		"androidx.room:$module:$version"

	implementation(room("room-runtime"))
	annotationProcessor(room("room-compiler"))
	ksp(room("room-compiler"))
	implementation(room("room-ktx"))
	implementation(room("room-paging"))

	// Guava cache
	implementation("com.google.guava:guava:33.0.0-android")

	// kode-in
	val kodeinVersion = "7.20.2"
	fun kodein(module: String, version: String = kodeinVersion) =
		"org.kodein.di:kodein-di$module:$version"

	implementation(kodein(""))
	implementation(kodein("-jvm"))
	implementation(kodein("-framework-android-core"))
	implementation(kodein("-framework-android-support"))
	implementation(kodein("-framework-android-x"))
	implementation(kodein("-framework-android-x-viewmodel"))
	implementation(kodein("-framework-android-x-viewmodel-savedstate"))

	// KTX

	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.8.0")

	// KTX - Serialization
	implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

	// Roomigrant
	/*val enableRoomigrant = false

	val roomigrantVersion = "0.3.4"
	implementation("com.github.MatrixDev.Roomigrant:RoomigrantLib:$roomigrantVersion")
	if (enableRoomigrant) {
		kapt("com.github.MatrixDev.Roomigrant:RoomigrantCompiler:$roomigrantVersion")
	}*/

	// Compose
	implementation(platform("androidx.compose:compose-bom:2024.12.01"))
	implementation("androidx.compose.runtime:runtime")
	implementation("androidx.compose.ui:ui")
	implementation("androidx.compose.ui:ui-tooling")
	implementation("androidx.compose.ui:ui-graphics")
	implementation("androidx.compose.ui:ui-tooling-preview")
	implementation("androidx.compose.material3:material3")
	implementation("androidx.compose.material:material-icons-extended")
	implementation("androidx.compose.foundation:foundation")
	implementation("androidx.compose.animation:animation")
	implementation("androidx.compose.animation:animation-graphics")
	implementation("androidx.compose.animation:animation-core")

	// - accompanist
	val accompanistVersion = "0.36.0"
	fun accompanist(module: String, version: String = accompanistVersion) =
		"com.google.accompanist:$module:$version"

	implementation(accompanist("accompanist-appcompat-theme"))
	implementation(accompanist("accompanist-webview"))
	implementation(accompanist("accompanist-placeholder-material"))
	implementation(accompanist("accompanist-pager-indicators"))
	implementation(accompanist("accompanist-permissions"))
	implementation(accompanist("accompanist-systemuicontroller"))

	//- Integration with observables
	implementation("androidx.compose.runtime:runtime-livedata")

	// MDC Adapter
	implementation(accompanist("accompanist-themeadapter-material"))
	implementation(accompanist("accompanist-themeadapter-material3"))

	val androidxActivity = "1.7.2"
	fun androidxActivity(module: String, version: String = androidxActivity) =
		"androidx.activity:$module:$version"
	implementation(androidxActivity("activity"))
	implementation(androidxActivity("activity-ktx"))
	implementation(androidxActivity("activity-compose"))

	implementation("com.chargemap.compose:numberpicker:1.0.3")

	// QR Code
	implementation("io.github.g0dkar:qrcode-kotlin-android:4.1.1")

	// - paging
	val pagingVersion = "3.2.1"
	fun paging(module: String, version: String = pagingVersion) =
		"androidx.paging:$module:$version"

	implementation(paging("paging-runtime"))
	implementation(paging("paging-compose"))
	implementation(kotlin("reflect"))

	val navVersion = "2.8.5"
	fun navigation(module: String, version: String = navVersion) =
		"androidx.navigation:navigation-$module:$version"

	implementation(navigation("compose"))

	coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.4")

	implementation("com.holix.android:bottomsheetdialog-compose:1.5.0")
}