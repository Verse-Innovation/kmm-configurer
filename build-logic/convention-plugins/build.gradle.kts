plugins {
    id("groovy-gradle-plugin")
    `kotlin-dsl`
}

//java {
//    sourceCompatibility = JavaVersion.VERSION_11
//    targetCompatibility = JavaVersion.VERSION_11
//}

repositories {
    gradlePluginPortal()
    google()
}

dependencies {
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.ksp.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("kmmLibrary") {
            id = "io.verse.kmm.library"
            implementationClass = "KmmLibraryConventionPlugin"
        }
        register("androidLibrary") {
            id = "io.verse.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidApplication") {
            id = "io.verse.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
    }
}