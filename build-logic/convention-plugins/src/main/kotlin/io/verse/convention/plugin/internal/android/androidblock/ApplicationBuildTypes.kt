package io.verse.convention.plugin.internal.android.androidblock

import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra

internal fun Project.configureBuildTypesInAndroidApplicationBlock(
    appModuleExtension: BaseAppModuleExtension
) {

    val project = this
    project.extra["commitId"] = "todo"

    appModuleExtension.apply {
        buildTypes {
            getByName("debug") {
                isDebuggable = true
                isMinifyEnabled = false
                multiDexEnabled = true
                isShrinkResources = false
                matchingFallbacks.add("develop")
                applicationIdSuffix = ".develop"
            }
            getByName("release") {
                isMinifyEnabled = true
                multiDexEnabled = true
                isShrinkResources = true
                matchingFallbacks.add("release")
                lint.abortOnError = false
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
            }
            create("nightly") {
                // a fresh feature build, which might be buggy, the targets are devs, QAs
                // and stakeholders
                initWith(getByName("debug"))

                isDebuggable = true
                applicationIdSuffix = ".develop.nightly"
                versionNameSuffix = "-nightly-${project.extra["commitId"]}"
            }
            create("staging") {
                // a closed beta users, hence a release build with debugging on
                initWith(getByName("release"))

                isDebuggable = true
                signingConfig = signingConfigs.getByName("debug")
                applicationIdSuffix = ".staging"
                versionNameSuffix = "-staging-${project.extra["commitId"]}"
            }
            create("alpha") {
                // a public alpha users, typically published at firebase distribution or in house
                initWith(getByName("release"))

                applicationIdSuffix = ".release.alpha"
                versionNameSuffix = ".alpha-${project.extra["commitId"]}"
            }
            create("beta") {
                // a public alpha users, typically published at firebase distribution or in house
                initWith(getByName("release"))

                applicationIdSuffix = ".release.beta"
                versionNameSuffix = ".beta-${project.extra["commitId"]}"
            }
        }
    }
}

