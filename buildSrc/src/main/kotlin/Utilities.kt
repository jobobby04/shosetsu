import org.gradle.api.Project
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.internal.extensions.core.extra
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.util.Properties

@Throws(IOException::class)
fun String.execute(): Process = Runtime.getRuntime().exec(this)

@Throws(IOException::class)
fun Process.getText(): String =
	org.codehaus.groovy.runtime.IOGroovyMethods.getText(
		BufferedReader(
			InputStreamReader(
				inputStream
			)
		)
	).also {
		org.codehaus.groovy.runtime.ProcessGroovyMethods.closeStreams(this)
	}

@Throws(IOException::class)
fun getCommitCount(): String = "git rev-list --count HEAD".execute().getText().trim()

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