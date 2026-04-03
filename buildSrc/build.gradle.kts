plugins {
	`kotlin-dsl`
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.eclipse.jgit:org.eclipse.jgit:7.6.0.202603022253-r")
	implementation("com.google.code.gson:gson:2.13.2")
	implementation("com.google.guava:guava:33.5.0-jre") // temporarily here - remove after merge
}
