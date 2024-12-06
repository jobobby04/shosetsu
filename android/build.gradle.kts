import com.google.common.collect.ImmutableMultimap
import com.google.common.collect.Multimap
import org.eclipse.jgit.api.Git
import org.jetbrains.kotlin.com.google.gson.stream.JsonWriter
import org.jetbrains.kotlin.konan.properties.Properties
import java.io.FileInputStream
import java.io.IOException

plugins {
	id("com.android.application")
	kotlin("android")
	kotlin("plugin.serialization")
	id("com.google.devtools.ksp")
}

/**
 * Associations between different usernames.
 */
private val knownLinks = bidiMultimapOf(
	"clocks" to "doomsdayrs"
)

/**
 * Association between a name and an image url.
 *
 * Name can be preferred name.
 */
private val knownImages = mapOf(
	"clocks" to "https://gitlab.com/uploads/-/system/user/avatar/3931112/avatar.png?width=256"
)

/**
 * Association between preferred names.
 *
 * For example, "doomsdayrs" should be mapped to "Clocks".
 */
private val preferredNames = mapOf(
	"doomsdayrs" to "Clocks"
)

/**
 * Association between a name and a website.
 *
 * Name can be preferred name.
 */
private val websites = mapOf(
	"clocks" to "https://doomsdayrs.page"
)

@Throws(IOException::class)
fun getCommitCount(): Int = Git.open(rootProject.projectDir).use { it.log().all().call().count() }

tasks {
	data class Contributor(
		val name: String,
		val email: String,
		var commits: Int,
		val website: String? = websites[name.lowercase()],
		val image: String? = knownImages[name.lowercase()]
	)

	val generateContributors by registering {
		doLast {
			val encountered = mutableMapOf<String, Contributor>()
			Git.open(rootProject.projectDir).use {
				it.log().all().call().forEach { commit ->
					val possibleAuthors = commit.authorIdent.name.let { knownLinks[it].plus(it) }
					val name = possibleAuthors.firstOrNull { preferredNames.containsKey(it) }?.let { preferredNames[it] }
						?: possibleAuthors.firstOrNull { encountered.containsKey(it) }
						?: possibleAuthors.first()
					val contributor = encountered.getOrPut(name) { Contributor(name, commit.authorIdent.emailAddress, 0) }
					contributor.commits++
				}
			}
			val contributors = encountered.values.sortedByDescending { it.commits }
			val file = project.file("build/generated/assets/contributors.json")
			file.parentFile.mkdirs()
			JsonWriter(file.writer()).use { it.run {
				beginArray()
				contributors.forEach {
					beginObject()
					name("name").value(it.name)
					name("email").value(it.email)
					name("commits").value(it.commits)
					it.website?.let { name("website").value(it) }
					it.image?.let { name("image").value(it) }
					endObject()
				}
				endArray()
			} }
		}
	}
	preBuild { dependsOn(generateContributors) }
}

fun <K> bidiMultimapOf(vararg pairs: Pair<K & Any, K & Any>): Multimap<K, K> {
	val builder = ImmutableMultimap.builder<K, K>()
	pairs.forEach {
		builder.put(it.first, it.second)
		builder.put(it.second, it.first)
	}
	return builder.build()
}

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

val CI_MODE = System.getenv("CI_MODE") == "true" || true

android {
	compileSdk = 34
	defaultConfig {
		applicationId = "app.shosetsu.android"
		minSdk = 22
		targetSdk = 34
		versionCode = 45
		versionName = "2.4.4"
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
			assets.srcDir("build/generated/assets")
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
	implementation("com.gitlab.shosetsuorg:kotlin-lib:9591ef55b2761cca78d3b32175326990d30d7167")
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

	// Error logging
	val acraVersion = "5.11.2"
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
	val androidxCompose = "1.5.4"
	fun androidxCompose(
		module: String,
		submodule: String = module,
		version: String = androidxCompose
	) = "androidx.compose.$submodule:$module:$version"

	implementation(platform("androidx.compose:compose-bom:2024.02.02"))
	implementation("androidx.compose.runtime:runtime")
	implementation("androidx.compose.ui:ui")
	implementation("androidx.compose.ui:ui-tooling")
	implementation("androidx.compose.ui:ui-graphics")
	implementation("androidx.compose.ui:ui-tooling-preview")
	implementation("androidx.compose.material3:material3")
	implementation("androidx.compose.foundation:foundation")
	implementation("androidx.compose.animation:animation")
	implementation("androidx.compose.animation:animation-graphics")
	implementation("androidx.compose.animation:animation-core")

	// - accompanist
	val accompanistVersion = "0.32.0"
	fun accompanist(module: String, version: String = accompanistVersion) =
		"com.google.accompanist:$module:$version"

	implementation(accompanist("accompanist-appcompat-theme"))
	implementation(accompanist("accompanist-webview"))
	implementation(accompanist("accompanist-placeholder-material"))
	implementation(accompanist("accompanist-pager-indicators"))
	implementation(accompanist("accompanist-permissions"))
	implementation(accompanist("accompanist-systemuicontroller"))

	//- Integration with observables
	implementation(androidxCompose("runtime-livedata", "runtime"))

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

	val navVersion = "2.7.2"
	fun navigation(module: String, version: String = navVersion) =
		"androidx.navigation:navigation-$module:$version"

	implementation(navigation("compose"))

	coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

	implementation("com.holix.android:bottomsheetdialog-compose:1.5.0")
}