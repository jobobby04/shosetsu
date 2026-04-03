plugins {
	`kotlin-dsl`
	alias(libs.plugins.kotlin.serialization) version libs.versions.kotlin
}

repositories {
	google()
	mavenCentral()
}

dependencies {
	implementation(libs.jgit)
	implementation(libs.google.guava) // temporarily here - remove after merge
	implementation(libs.gradle)
	implementation(libs.kotlin.gradle.plugin)
	implementation(libs.kotlin.serialization)
	implementation(libs.kotlinx.serialization.json)
}
