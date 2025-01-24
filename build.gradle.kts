import org.codehaus.groovy.runtime.IOGroovyMethods
import org.codehaus.groovy.runtime.ProcessGroovyMethods.closeStreams
import java.io.BufferedReader
import java.io.IOException

plugins {
	alias(libs.plugins.google.ksp)
	alias(libs.plugins.kotlin.compose) apply false
}

buildscript {
	repositories {
		google()
		mavenCentral()
	}

	dependencies {
		classpath(libs.gradle)
		classpath(libs.kotlin.gradle.plugin)
		classpath(libs.kotlin.serialization)
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

/** Creates an update XML to be used by the application */
open class WriteDebugUpdate : DefaultTask() {
	companion object {
		@Throws(IOException::class)
		private fun String.execute(): Process = Runtime.getRuntime().exec(this)

		@Throws(IOException::class)
		private fun Process.getText(): String =
			IOGroovyMethods.getText(BufferedReader(java.io.InputStreamReader(inputStream))).also {
				closeStreams(this)
			}

		@Throws(IOException::class)
		private fun getCommitCount(): String =
			"git rev-list --count HEAD".execute().getText().trim()

		@Throws(IOException::class)
		private fun getLatestCommitMsg(current: Int, since: Int): String =
			"git log -${current - since} --pretty=%B".execute().getText().trim()
	}


	/** Task of this task */
	@Throws(IOException::class)
	@TaskAction
	fun main() {
		val file = File("android/src/debug/assets/update.json")
		// up the commit by one for when shosetsu-preview builds
		val commitCount = getCommitCount().toInt()
		// the last file contains the commit count since the last generation
		val lastFile = File("android/src/debug/assets/last")
		// get the previous commit count
		val prevCommitCount = lastFile.readText().toInt()
		// save the new commit count
		lastFile.writeText(commitCount.toString())

		val releaseNotes = getLatestCommitMsg(current = commitCount, since = prevCommitCount)
			// Format it so it goes well into the json
			//.replace("\n", "\",\n\t\t\t\t\"-")
			.split("\n")
			.map { it.trim() }
			.filter { it.isNotBlank() }
			.map { it.replace("\"", "'") }
			.joinToString("\n\t\t\t\t") { "\"- $it\"," }
			.removeSuffix(",")

		file.writeText(
			"""
		{
			"latestVersion":"$commitCount",
			"url":"https://cdn.shosetsu.app/debug/r$commitCount/shosetsu-r$commitCount.apk",
			"releaseNotes":[
				$releaseNotes
			]
		}
		""".trimIndent()
		)
	}
}