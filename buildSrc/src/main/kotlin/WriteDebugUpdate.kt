import org.codehaus.groovy.runtime.IOGroovyMethods
import org.codehaus.groovy.runtime.ProcessGroovyMethods.closeStreams
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.BufferedReader
import java.io.File
import java.io.IOException

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
		private fun getLatestCommitMsg(): String =
			"git log -1 --pretty=%B".execute().getText().trim()
	}


	/** Task of this task */
	@Throws(IOException::class)
	@TaskAction
	fun main() {
		val file = File("android/src/debug/assets/update.json")
		// up the commit by one for when shosetsu-preview builds
		val commitCount = getCommitCount().toInt()
		file.writeText(
			"""
		{
		  "latestVersion":"$commitCount",
		  "url":"https://github.com/shosetsuorg/shosetsu-preview/releases/download/r$commitCount/shosetsu-r$commitCount.apk",
		  "releaseNotes":[
		    "${getLatestCommitMsg().replace("\n", "\",\n\t\t\t\t\"")}"
		  ]
		}
		""".trimIndent()
		)
	}
}