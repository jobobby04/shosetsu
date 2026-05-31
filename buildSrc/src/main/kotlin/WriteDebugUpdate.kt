import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import org.eclipse.jgit.api.Git
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.IOException

/** Creates an update XML to be used by the application */
abstract class WriteDebugUpdate : DefaultTask() {
	companion object {
		@Throws(IOException::class)
		private fun Git.getLatestCommitMsg(current: Int, since: Int): List<String> {
			// git log -${current - since} --pretty=%B
			return log().setMaxCount(current - since).call().map { it.fullMessage }
		}
	}

	@get:InputDirectory
	abstract val gitDir: DirectoryProperty

	@get:InputFile
	abstract val lastFile: RegularFileProperty

	@get:OutputFile
	abstract val outputFile: RegularFileProperty

	init {
		gitDir.convention(project.rootProject.layout.projectDirectory.dir(".git"))
		lastFile.convention(project.layout.projectDirectory.file("android/src/debug/assets/last"))
		outputFile.convention(project.layout.projectDirectory.file("android/src/debug/assets/update.json"))
	}

	@Serializable
	private data class DebugUpdate(
		val latestVersion: String,
		val url: String,
		val releaseNotes: List<String>
	)

	/** Task of this task */
	@OptIn(ExperimentalSerializationApi::class)
	@Throws(IOException::class)
	@TaskAction
	fun main() {
		val file = outputFile.get().asFile

		val update: DebugUpdate = Git.open(gitDir.get().asFile).use { git ->
			// up the commit by one for when shosetsu-preview builds
			val commitCount = git.getCommitCount()
			// the last file contains the commit count since the last generation
			val lastFile = lastFile.get().asFile
			// get the previous commit count
			val prevCommitCount = lastFile.readText().toInt()
			// save the new commit count
			lastFile.writeText(commitCount.toString())

			val releaseNotes = git.getLatestCommitMsg(current = commitCount, since = prevCommitCount)
				.map { commitMessage ->
					commitMessage
						// Format it so it goes well into the json
						//.replace("\n", "\",\n\t\t\t\t\"-")
						.split("\n")
						.map { it.trim() }
						.filter { it.isNotBlank() }
						.map { it.replace("\"", "'") }
						.joinToString("\n\t\t\t\t") { "- $it," }
						.removeSuffix(",")
				}

			DebugUpdate(
				latestVersion = commitCount.toString(),
				url = "https://cdn.shosetsu.app/debug/r$commitCount/shosetsu-r$commitCount.apk",
				releaseNotes
			)
		}

		file.outputStream().use { Json.encodeToStream(update, it) }
	}
}
