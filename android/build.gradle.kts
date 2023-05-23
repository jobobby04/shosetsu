import org.jetbrains.kotlin.konan.properties.Properties
import java.io.BufferedReader
import java.io.FileInputStream
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

@Throws(IOException::class)
fun getCommitCount(): String = "git rev-list --count HEAD".execute().getText().trim()

fun loadSProperties(name: String): Properties {
	var properties = try {
		extra.get(name) as? Properties
	} catch (e: ExtraPropertiesExtension.UnknownPropertyException) {
		null
	}

	if (properties != null)
		return properties

	val acraPropertiesFile = rootProject.file("$name.properties")
	properties = Properties()

	if (acraPropertiesFile.exists())
		properties.load(FileInputStream(acraPropertiesFile))

	ext.set(name, properties)

	return properties
}

val CI_MODE = System.getenv("CI_MODE") == "true"

android {
	compileSdk = 33
	defaultConfig {
		applicationId = "app.shosetsu.android"
		minSdk = 22
		targetSdk = 33
		versionCode = 41
		versionName = "2.4.0"
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

		setProperty("archivesBaseName", rootProject.name)
	}

	buildFeatures {
		viewBinding = true
		compose = true
	}

	composeOptions {
		kotlinCompilerExtensionVersion = "1.4.5"
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
			versionNameSuffix = "-${getCommitCount()}"
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
	implementation("com.google.android.material:material:1.9.0")

	// Androidx
	implementation("androidx.work:work-runtime:2.8.1")
	implementation("androidx.work:work-runtime-ktx:2.8.1")
	implementation("androidx.appcompat:appcompat:1.6.1")
	implementation("androidx.annotation:annotation:1.6.0")
	implementation("androidx.core:core-ktx:1.10.1")
	implementation("androidx.collection:collection-ktx:1.2.0")
	implementation("androidx.core:core-splashscreen:1.0.1")
	implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
	implementation("androidx.window:window:1.0.0")

	// - Life Cycle

	val lifecycleVersion = "2.6.1"
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

	// Annotations
	implementation("org.jetbrains:annotations:24.0.1")

	// Core libraries
	implementation("org.luaj:luaj-jse:3.0.1")
	implementation("com.gitlab.shosetsuorg:kotlin-lib:1.2.0")
	implementation("org.jsoup:jsoup:1.15.4")

	// Image loading
	implementation("io.coil-kt:coil-compose:2.3.0")

	// Time control
	implementation("joda-time:joda-time:2.12.5")

	// Cloud flare calculator
	//implementation("com.zhkrb.cloudflare-scrape-android:scrape-webview:0.0.3")

	// Network
	implementation("com.squareup.okhttp3:okhttp:4.10.0")

	// Kotlin libraries
	implementation(kotlin("stdlib-jdk8"))
	//implementation(kotlin("reflect"))

	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.0")

	implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.5")

	// Error logging
	val acraVersion = "5.9.7"
	fun acra(module: String, version: String = acraVersion) =
		"ch.acra:$module:$version"

	implementation(acra("acra-http"))
	implementation(acra("acra-dialog"))

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
	val roomVersion = "2.5.1"
	fun room(module: String, version: String = roomVersion) =
		"androidx.room:$module:$version"

	implementation(room("room-runtime"))
	annotationProcessor(room("room-compiler"))
	ksp(room("room-compiler"))
	implementation(room("room-ktx"))
	implementation(room("room-paging"))

	// Guava cache
	implementation("com.google.guava:guava:31.1-android")

	// kode-in
	val kodeinVersion = "7.20.1"
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

	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.7.0")

	// KTX - Serialization
	implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")

	// Roomigrant
	/*val enableRoomigrant = false

	val roomigrantVersion = "0.3.4"
	implementation("com.github.MatrixDev.Roomigrant:RoomigrantLib:$roomigrantVersion")
	if (enableRoomigrant) {
		kapt("com.github.MatrixDev.Roomigrant:RoomigrantCompiler:$roomigrantVersion")
	}*/

	// Compose
	val androidxCompose = "1.4.1"
	fun androidxCompose(
		module: String,
		submodule: String = module,
		version: String = androidxCompose
	) = "androidx.compose.$submodule:$module:$version"

	implementation(androidxCompose("ui"))

	//- Tooling support (Previews, etc.)
	implementation(androidxCompose("ui-tooling", "ui"))

	//- Foundation (Border, Background, Box, Image, Scroll, shapes, animations, etc.)
	implementation(androidxCompose("foundation"))
	implementation(androidxCompose("animation"))
	implementation(androidxCompose("animation-graphics", "animation"))
	implementation(androidxCompose("animation-core", "animation"))

	// - Material
	implementation(androidxCompose("material3", version = "1.0.1"))

	// - accompanist
	val accompanistVersion = "0.30.1"
	fun accompanist(module: String, version: String = accompanistVersion) =
		"com.google.accompanist:$module:$version"

	implementation(accompanist("accompanist-appcompat-theme"))
	implementation(accompanist("accompanist-webview"))
	implementation(accompanist("accompanist-placeholder-material"))
	implementation(accompanist("accompanist-pager-indicators"))
	implementation(accompanist("accompanist-permissions"))
	implementation(accompanist("accompanist-flowlayout"))
	implementation(accompanist("accompanist-systemuicontroller"))

	//- Integration with observables
	implementation("androidx.compose.runtime:runtime-livedata:1.4.3")

	// MDC Adapter
	implementation(accompanist("accompanist-themeadapter-material"))
	implementation(accompanist("accompanist-themeadapter-material3"))

	val androidxActivity = "1.7.0"
	fun androidxActivity(module: String, version: String = androidxActivity) =
		"androidx.activity:$module:$version"
	implementation(androidxActivity("activity"))
	implementation(androidxActivity("activity-ktx"))
	implementation(androidxActivity("activity-compose"))

	implementation("com.chargemap.compose:numberpicker:1.0.3")

	// QR Code
	implementation("com.github.doomsdayrs:qrcode-kotlin:513d290b")

	// - paging
	val pagingVersion = "3.1.1"
	fun paging(module: String, version: String = pagingVersion) =
		"androidx.paging:$module:$version"

	implementation(paging("paging-runtime"))
	implementation(paging("paging-compose", "1.0.0-alpha14"))
	implementation(kotlin("reflect"))

	val navVersion = "2.5.3"
	fun navigation(module: String, version: String = navVersion) =
		"androidx.navigation:navigation-$module:$version"

	implementation(navigation("fragment-ktx"))
	implementation(navigation("ui-ktx"))

}