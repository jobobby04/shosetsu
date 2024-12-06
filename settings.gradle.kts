buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.eclipse.jgit:org.eclipse.jgit:6.8.0.202311291450-r")
        classpath("com.google.code.gson:gson:2.11.0")
    }
}

include(":android")