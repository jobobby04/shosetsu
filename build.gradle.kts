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
		maven("https://gitlab.com/api/v4/groups/12585416/-/packages/maven") {
			content {
				includeGroupAndSubgroups("app.shosetsu")
			}
		}
		google()
		mavenCentral()
		maven("https://jitpack.io") {
			content {
				includeGroup("com.gitlab.shosetsuorg")
			}
		}
	}
}

tasks {
	val clean by registering(Delete::class) {
		delete(rootProject.layout.buildDirectory)
	}

	val androidDebugUpdateXML by registering(WriteDebugUpdate::class)
}
