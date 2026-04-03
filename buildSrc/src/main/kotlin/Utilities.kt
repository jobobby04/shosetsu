import org.eclipse.jgit.api.Git
import org.gradle.api.Project
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.internal.extensions.core.extra
import java.io.FileInputStream
import java.io.IOException
import java.util.Properties

@Throws(IOException::class)
fun Git.getCommitCount(): Int {
	// "git rev-list --count HEAD".execute().getText().trim()
	return log().all().call().count()
}

fun Project.loadSProperties(name: String): Properties {
	var properties = try {
		extra.get(name) as? Properties
	} catch (e: ExtraPropertiesExtension.UnknownPropertyException) {
		null
	}

	if (properties != null)
		return properties

	val acraPropertiesFile = rootProject.file("$name.properties")
	properties = Properties()

	if (acraPropertiesFile.exists())
		properties.load(FileInputStream(acraPropertiesFile))

	extra.set(name, properties)

	return properties
}
