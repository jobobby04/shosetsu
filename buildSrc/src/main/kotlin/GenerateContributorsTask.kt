import com.google.gson.stream.JsonWriter
import org.eclipse.jgit.api.Git
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

abstract class GenerateContributorsTask : DefaultTask() {
    private data class Contributor(
        val name: String,
        val email: String,
        var commits: Int,
        val website: String? = Contributors.websites[name.lowercase()],
        val image: String? = Contributors.knownImages[name.lowercase()]
    )

    @get:InputDirectory
    abstract val gitDir: DirectoryProperty

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    init {
        gitDir.convention(project.rootProject.layout.projectDirectory.dir(".git"))
        outputFile.convention(project.layout.projectDirectory.file("build/generated/assets/contributors.json"))
    }

    @TaskAction
    fun main() {
        val encountered = mutableMapOf<String, Contributor>()
        Git.open(gitDir.get().asFile).use {
            it.log().all().call().forEach { commit ->
                val possibleAuthors = commit.authorIdent.name.let { Contributors.knownLinks[it] + it }
                val name = possibleAuthors.firstOrNull { Contributors.preferredNames.containsKey(it) }?.let { Contributors.preferredNames[it] }
                    ?: possibleAuthors.firstOrNull { encountered.containsKey(it) }
                    ?: possibleAuthors.first()
                val contributor = encountered.getOrPut(name) { Contributor(name, commit.authorIdent.emailAddress, 0) }
                contributor.commits++
            }
        }
        val contributors = encountered.values.sortedByDescending { it.commits }
        val file = outputFile.get().asFile
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