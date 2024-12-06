import org.eclipse.jgit.api.Git

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

tasks {
	val clean by registering(Delete::class) {
		delete(rootProject.layout.buildDirectory)
	}

	/** Creates an update XML to be used by the application */
	val androidDebugUpdateXML by registering {
		doLast {
			val commitCount: Int
			val latestCommitMsg: String
			Git.open(project.projectDir).use { git ->
				val log = git.log().all().call()
				// git log -1 --pretty=%B
				latestCommitMsg = log.first().fullMessage
				// git rev-list --count HEAD
				commitCount = log.count() + 1
			}

			val file = File("android/src/debug/assets/update.json")
			// up the commit by one for when shosetsu-preview builds
			file.writeText("""
				{
				  "latestVersion":"$commitCount",
				  "url":"https://github.com/shosetsuorg/shosetsu-preview/releases/download/r$commitCount/shosetsu-r$commitCount.apk",
				  "releaseNotes":[
					"${latestCommitMsg.replace("\n", "\",\n\t\t\t\t\"")}"
				  ]
				}
				""".trimIndent()
			)
		}
	}
}