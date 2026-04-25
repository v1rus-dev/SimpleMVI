import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.vanniktech.maven.publish)
}

group = "yegor.cheprasov.simplemvi"
version = "1.0.0"

kotlin {
    androidLibrary {
        namespace = "yegor.cheprasov.simplemvi.compose"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        withJava()

        compilations.configureEach {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_11)
                }
            }
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            api(project(":simple-mvi-core"))

            implementation(libs.compose.runtime)
            implementation(libs.org.jetbrains.androidx.lifecycle.viewmodelCompose)
            implementation(libs.org.jetbrains.androidx.lifecycle.runtimeCompose)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates(
        groupId = group.toString(),
        artifactId = "simple-mvi-compose",
        version = version.toString()
    )

    pom {
        name = "SimpleMVI Compose"
        description = "Compose Multiplatform integration for SimpleMVI"
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
                id = "yegor-cheprasov"
                name = "Yegor Cheprasov"
                url = "https://github.com/yegor-cheprasov"
            }
        }

        scm {
            url = "https://github.com/v1rus-dev/SimpleMVI"
            connection = "scm:git:git://github.com/yegor-cheprasov/simple-mvi.git"
            developerConnection = "scm:git:ssh://git@github.com/yegor-cheprasov/simple-mvi.git"
        }
    }
}
