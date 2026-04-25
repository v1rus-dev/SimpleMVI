pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "simple-mvi"
include(":library")
include(":simple-mvi-core")
include(":simple-mvi-compose")
include(":simple-mvi-compose-android")