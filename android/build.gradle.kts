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



android {
	compileSdk = 33
	defaultConfig {
		applicationId = "app.shosetsu.android"
		minSdk = 22
		targetSdk = 33
		versionCode = 40
		versionName = "2.2.3"
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
		kotlinCompilerExtensionVersion = "1.3.2"
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
			isMinifyEnabled = true
			isShrinkResources = true
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
		sourceCompatibility = JavaVersion.VERSION_11
		targetCompatibility = JavaVersion.VERSION_11
	}
	kotlinOptions {
		jvmTarget = JavaVersion.VERSION_11.toString()
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
	implementation("com.google.android.material:material:1.7.0")

	// Androidx
	implementation("androidx.work:work-runtime:2.7.1")
	implementation("androidx.work:work-runtime-ktx:2.7.1")
	implementation("androidx.appcompat:appcompat:1.5.1")
	implementation("androidx.annotation:annotation:1.5.0")
	implementation("androidx.core:core-ktx:1.9.0")
	implementation("androidx.collection:collection-ktx:1.2.0")
	implementation("androidx.core:core-splashscreen:1.0.0")
	implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
	implementation("androidx.window:window:1.0.0")

	// - Life Cycle
	implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1")
	implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.5.1")
	implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
	implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.5.1")
	implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")

	// Test classes
	testImplementation("junit:junit:4.13.2")
	testImplementation("androidx.test.ext:junit:1.1.4")
	androidTestImplementation("androidx.test:runner:1.5.1")
	androidTestImplementation("androidx.test.espresso:espresso-core:3.5.0")

	// Annotations
	implementation("org.jetbrains:annotations:23.0.0")

	// Core libraries
	implementation("org.luaj:luaj-jse:3.0.1")
	implementation("com.github.shosetsuorg:kotlin-lib:1.0.0")
	implementation("org.jsoup:jsoup:1.15.3")

	// Image loading
	implementation("io.coil-kt:coil-compose:2.2.2")

	// Time control
	implementation("joda-time:joda-time:2.12.2")

	// Cloud flare calculator
	//implementation("com.zhkrb.cloudflare-scrape-android:scrape-webview:0.0.3")

	// Network
	implementation("com.squareup.okhttp3:okhttp:4.10.0")

	// Kotlin libraries
	implementation(kotlin("stdlib-jdk8"))
	//implementation(kotlin("reflect"))

	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")

	implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.5")

	// Error logging
	val acraVersion = "5.9.6"
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
	val roomVersion = "2.4.3"
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
	val kodeinVersion = "7.14.0"
	fun kodein(module: String, version: String = kodeinVersion) =
		"org.kodein.di:$module:$version"

	implementation(kodein("kodein-di"))
	implementation(kodein("kodein-di-jvm"))
	implementation(kodein("kodein-di-framework-android-core"))
	implementation(kodein("kodein-di-framework-android-support"))
	implementation(kodein("kodein-di-framework-android-x"))

	// KTX

	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.6.4")

	// KTX - Serialization
	implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")

	// Roomigrant
	/*val enableRoomigrant = false

	val roomigrantVersion = "0.3.4"
	implementation("com.github.MatrixDev.Roomigrant:RoomigrantLib:$roomigrantVersion")
	if (enableRoomigrant) {
		kapt("com.github.MatrixDev.Roomigrant:RoomigrantCompiler:$roomigrantVersion")
	}*/

	// Compose
	val androidxCompose = "1.3.0"
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
	implementation(androidxCompose("material3", version = "1.0.0"))

	// - accompanist
	val accompanistVersion = "0.27.0"
	fun accompanist(module: String, version: String = accompanistVersion) =
		"com.google.accompanist:$module:$version"

	implementation(accompanist("accompanist-appcompat-theme"))
	implementation(accompanist("accompanist-pager"))
	implementation(accompanist("accompanist-swiperefresh"))
	implementation(accompanist("accompanist-webview"))
	implementation(accompanist("accompanist-placeholder-material"))
	implementation(accompanist("accompanist-pager-indicators"))
	implementation(accompanist("accompanist-permissions"))
	implementation(accompanist("accompanist-flowlayout"))

	//- Integration with observables
	implementation("androidx.compose.runtime:runtime-livedata:1.3.1")

	// MDC Adapter
	implementation("com.google.android.material:compose-theme-adapter:1.2.1")
	implementation("com.google.android.material:compose-theme-adapter-3:1.1.1")

	val androidxActivity = "1.6.1"
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