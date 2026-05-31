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
		val contributors = getContributors()

		FileSpec.builder(className)
			.indent("\t")
			.addType(
				TypeSpec.classBuilder(className)
					.addSuperinterface(superinterface)
					.primaryConstructor(FunSpec.constructorBuilder().build())
					.addFunction(
						FunSpec.builder("getAll")
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

	private fun getContributors(): List<Contributor> {
		val encountered = UnionFind<String, EncounteredContributor>(merge = { a, b ->
			EncounteredContributor(
				a.email,
				a.commits + b.commits
			)
		})

		try {
			Git.open(gitDir.get().asFile).use {
				it.log().all().call().forEach { commit ->
					val name = commit.authorIdent.name
					encountered.compute(name) { _, it ->
						it ?: EncounteredContributor(
							// Get the authors preferred email
							Contributors.preferredEmails.getOrDefault(
								commit.authorIdent.emailAddress,
								commit.authorIdent.emailAddress
							),
							0
						)
					}!!.commits++
					encountered.union(canonical = name, alternative = name.lowercase())
				}
			}
		} catch (exception: java.io.IOException) {
			logger.error("Something failed!", exception)
		} catch (exception: Throwable) {
			logger.error("Something worse failed!", exception)
		}

		Contributors.preferredNames.forEach { (oldName, preferredName) ->
			encountered.union(canonical = preferredName, alternative = oldName)
		}

		return encountered.asSequence()
			.mapNotNull { (name, eContributor) -> name to (eContributor ?: return@mapNotNull null) }
			.filter { (_, eContributor) -> eContributor.commits > 0 }
			.map { (name, eContributor) ->
				Contributor(
					name = name,
					email = eContributor.email,
					commits = eContributor.commits,
					website = Contributors.websites[name.lowercase()],
					image = Contributors.images[name.lowercase()]
				)
			}
			.sortedByDescending { (_, _, commits, _, _) -> commits }
			// Group by email for merging
			.groupBy { (_, email, _, _, _) -> email }
			// Merge emails that match! The one with the most commits gets the priority.
			.map { (_, contributors) ->
				val first = contributors.first()
				if (contributors.size > 1) {
					contributors.subList(1, contributors.size).forEach {
						first.commits += it.commits
					}
				}
				first
			}
			.toList()
	}

	private data class EncounteredContributor(val email: String, var commits: Int)

	private fun CodeBlock.Builder.addNullableString(value: String?) =
		if (value == null) add("null") else add("%S", value)
}
