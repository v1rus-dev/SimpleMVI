import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.vanniktech.maven.publish)
}

group = "io.github.v1rus-dev"
version = "0.1.0"

android {
    namespace = "yegor.cheprasov.simplemvi.compose.android"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {
    api(project(":simple-mvi-core"))
    api(project(":simple-mvi-compose"))
    api(libs.androidx.lifecycle.viewmodel.savedstate)

    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates(
        groupId = group.toString(),
        artifactId = "simple-mvi-compose-android",
        version = version.toString()
    )

    pom {
        name = "SimpleMVI Compose Android"
        description = "Android lifecycle and ViewModel integration for SimpleMVI"
        inceptionYear = "2026"
        url = "https://github.com/v1rus-dev/SimpleMVI"

        licenses {
            license {
                name = "Apache License 2.0"
                url = "https://opensource.org/licenses/Apache-2.0"
                distribution = "repo"
            }
        }

        developers {
            developer {
                id = "v1rus-dev"
                name = "Yegor Cheprasov"
                url = "https://github.com/v1rus-dev"
            }
        }

        scm {
            url = "https://github.com/v1rus-dev/SimpleMVI"
            connection = "scm:git:git://github.com/v1rus-dev/SimpleMVI.git"
            developerConnection = "scm:git:ssh://git@github.com/v1rus-dev/SimpleMVI.git"
        }
    }
}
