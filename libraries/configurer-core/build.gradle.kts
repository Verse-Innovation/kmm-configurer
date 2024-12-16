plugins {
    id("io.verse.kmm.library")
}

apply("${project.rootProject.file("gradle/github_repo_access.gradle")}")

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.tagd.arch)
                api(libs.verse.latch)
                api(libs.verse.storage)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val androidMain by getting {
            dependencies {
                api(libs.gson)
            }
        }
    }
}

android {
    namespace = "com.verse.configure.core"
}

pomBuilder {
    description.set("Configuration's core library")
}