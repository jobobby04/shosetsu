plugins {
	id("com.google.devtools.ksp") version "1.9.21-1.0.16"
	id("org.jetbrains.kotlin.android") version "1.9.0" apply false
}

buildscript {
	val kotlinVersion: String by extra("1.9.21")

	repositories {
		google()
		mavenCentral()
	}

	dependencies {
		classpath("com.android.tools.build:gradle:8.7.2")
		classpath(kotlin("gradle-plugin", version = kotlinVersion))
		classpath(kotlin("serialization", version = kotlinVersion))
	}
}

allprojects {
	repositories {
		google()
		mavenCentral()
		maven("https://jitpack.io")
	}
}

task("clean", Delete::class) {
	delete(rootProject.buildDir)
}

tasks.register<WriteDebugUpdate>("androidDebugUpdateXML")