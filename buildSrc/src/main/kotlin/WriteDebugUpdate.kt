import org.eclipse.jgit.api.Git
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.IOException

/** Creates an update XML to be used by the application */
abstract class WriteDebugUpdate : DefaultTask() {
	companion object {
		@Throws(IOException::class)
		private fun Git.getLatestCommitMsg(): String {
			// git log -1 --pretty=%B
			return log().setMaxCount(1).call().single().fullMessage
		}
	}

	@get:InputDirectory
	abstract val gitDir: DirectoryProperty

	@get:OutputFile
	abstract val outputFile: RegularFileProperty

	init {
		gitDir.convention(project.rootProject.layout.projectDirectory.dir(".git"))
		outputFile.convention(project.layout.projectDirectory.file("android/src/debug/assets/update.json"))
	}

	/** Task of this task */
	@Throws(IOException::class)
	@TaskAction
	fun main() {
		// up the commit by one for when shosetsu-preview builds
		val (commitCount, latestCommitMsg) = Git.open(gitDir.get().asFile).use {
			it.getCommitCount() to it.getLatestCommitMsg()
		}
		outputFile.get().asFile.writeText(
			"""
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
