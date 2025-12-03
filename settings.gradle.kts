// settings.gradle.kts
pluginManagement {
    repositories {
        gradlePluginPortal() // <- obligatorio para resolver plugins como com.google.devtools.ksp
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "pokegame"
include(":app")
