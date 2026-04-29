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
include(":simple-mvi-core")
include(":simple-mvi-compose")
include(":simple-mvi-compose-android")
include(":simple-mvi-logger")
include(":samples:compose-multiplatform-app")
include(":samples:native-android-app")
