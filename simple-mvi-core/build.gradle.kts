import com.android.build.api.dsl.androidLibrary
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.vanniktech.maven.publish)
}

group = "yegor.cheprasov.simplemvi"
version = "1.0.0"

kotlin {
    jvm()

    androidLibrary {
        namespace = "yegor.cheprasov.simplemvi.core"
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
            api(libs.kotlinx.coroutines.core)
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
        artifactId = "simple-mvi-core",
        version = version.toString()
    )

    pom {
        name = "SimpleMVI Core"
        description = "Core primitives for SimpleMVI Kotlin Multiplatform library"
        inceptionYear = "2026"
        url = "https://github.com/v1rus-dev/SimpleMVI"

        licenses {
            license {
                name = "MIT License"
                url = "https://opensource.org/licenses/MIT"
                distribution = "repo"
            }
        }

        developers {
            developer {
                id = "yegor-cheprasov"
                name = "Yegor Cheprasov"
                url = "https://github.com/v1rus-dev"
            }
        }

        scm {
            url = "https://github.com/v1rus-dev/SimpleMVI"
            connection = "scm:git:git://github.com/yegor-cheprasov//SimpleMVI.git"
            developerConnection = "scm:git:ssh://git@github.com/yegor-cheprasov//SimpleMVI.git"
        }
    }
}
