import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	alias(libs.plugins.google.ksp)
	alias(libs.plugins.kotlin.compose) apply false
}

allprojects {
	repositories {
		maven("https://gitlab.com/api/v4/projects/61884451/packages/maven") {
			name = "stringly fork"
			content {
				includeGroupAndSubgroups("app.shosetsu")
			}
		}
		google()
		mavenCentral()
	}
}

val ktlint by configurations.registering

dependencies {
	ktlint(libs.ktlint) {
		attributes {
			attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
		}
	}
	ktlint(libs.ktlint.gitlab.reporter)
}

val outputDir = project.layout.buildDirectory.dir("reports/ktlint/")
val inputFiles = fileTree("buildSrc/src") { include("**/*.kt") } +
	fileTree("android/src") { include("**/*.kt") }
val editorconfig = rootProject.file(".editorconfig").absolutePath

tasks {
	val clean by registering(Delete::class) {
		delete(rootProject.layout.buildDirectory)
	}

	val androidDebugUpdateXML by registering(WriteDebugUpdate::class)
	val ktlintRun by registering(JavaExec::class) {
		group = "verification"
		inputs.files(inputFiles)
		outputs.dir(outputDir)
		mainClass = "com.pinterest.ktlint.Main"
		classpath(ktlint)
		args = listOf("--editorconfig=$editorconfig", "buildSrc/src/**/*.kt", "android/src/**/*.kt", "--reporter=plain?group_by_file", "--reporter=gitlab,output=${outputDir.get().asFile.absolutePath}/ktlint.json")
		jvmArgs = listOf("--add-opens", "java.base/java.lang=ALL-UNNAMED")
	}

	val ktlintFormat by registering(JavaExec::class) {
		group = "verification"
		inputs.files(inputFiles)
		outputs.dir(outputDir)
		mainClass = "com.pinterest.ktlint.Main"
		classpath(ktlint)
		args = listOf("--editorconfig=$editorconfig", "-F", "buildSrc/src/**/*.kt", "android/src/**/*.kt")
		jvmArgs = listOf("--add-opens", "java.base/java.lang=ALL-UNNAMED")
	}

	val lint by registering {
		group = "verification"
		dependsOn(ktlintRun)
	}

	register("check") { dependsOn(lint) }
	project(":android").afterEvaluate {
		tasks.withType(KotlinCompile::class).configureEach { dependsOn(ktlintFormat) }
	}
}
