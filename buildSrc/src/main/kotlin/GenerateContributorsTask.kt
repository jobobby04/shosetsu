import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import org.eclipse.jgit.api.Git
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

abstract class GenerateContributorsTask : DefaultTask() {
    @Serializable
    private data class Contributor(
        val name: String,
        val email: String,
        var commits: Int,
        val website: String?,
        val image: String?,
    )

    @get:InputDirectory
    abstract val gitDir: DirectoryProperty

    @get:OutputDirectory
    abstract val generatedKotlinDir: DirectoryProperty

    init {
        gitDir.convention(project.rootProject.layout.projectDirectory.dir(".git"))
        generatedKotlinDir.convention(project.layout.buildDirectory.dir("generated/contributors"))
    }

    private val superinterface = ClassName("app.shosetsu.android.domain.repository.base", "ContributorsRepository")
    private val className = ClassName("app.shosetsu.android.domain.repository.impl", "ContributorsRepositoryImpl")
    private val contributorClass = ClassName("app.shosetsu.android.domain.model.local", "Contributor")

    @OptIn(ExperimentalSerializationApi::class)
    @TaskAction
    fun main() {
        val encountered = mutableMapOf<String, Contributor>()
        Git.open(gitDir.get().asFile).use {
            it.log().all().call().forEach { commit ->
                val possibleAuthors = commit.authorIdent.name.let { Contributors.knownLinks[it.lowercase()] + it }
                val name = possibleAuthors.firstNotNullOfOrNull { Contributors.preferredNames[it.lowercase()] }
                    ?: possibleAuthors.firstOrNull { encountered.containsKey(it) }
                    ?: possibleAuthors.first()
                val contributor = encountered.getOrPut(name) { Contributor(
                    name,
                    commit.authorIdent.emailAddress,
                    0,
                    Contributors.websites[name.lowercase()],
                    Contributors.knownImages[name.lowercase()],
                ) }
                contributor.commits++
            }
        }
        val contributors = encountered.values.sortedByDescending { it.commits }

        FileSpec.builder(className)
            .indent("\t")
            .addType(TypeSpec.classBuilder(className)
                .addSuperinterface(superinterface)
                .primaryConstructor(FunSpec.constructorBuilder().build())
                .addFunction(FunSpec.builder("getAll")
                    .addModifiers(KModifier.OVERRIDE)
                    .returns(List::class.asTypeName().parameterizedBy(contributorClass))
                    .addCode(CodeBlock.builder().apply {
                        add("return listOf(\n")
                        indent()
                        var first = true
                        for (contributor in contributors) {
                            if (first) first = false else add(",\n")
                            add("Contributor(\n")
                            indent()
                            add("name = %S,\n", contributor.name)
                            add("email = ").addNullableString(contributor.email).add(",\n")
                            add("commits = %L,\n", contributor.commits)
                            add("website = ").addNullableString(contributor.website).add(",\n")
                            add("image = ").addNullableString(contributor.image).add(",\n")
                            unindent()
                            add(")")
                        }
                        unindent()
                        add("\n)\n")
                    }.build()).build()
                ).build()
            ).build()
            .writeTo(generatedKotlinDir.get().asFile)
    }

    private fun CodeBlock.Builder.addNullableString(value: String?) =
        if (value == null) add("null") else add("%S", value)
}
