package io.verse.convention.plugin.internal.android.androidblock

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project

internal fun Project.configureBuildFeaturesInAndroidBlock(
    commonExtension: CommonExtension<*, *, *, *, *>
) {

    commonExtension.apply {
        buildFeatures {
            buildConfig = true
            resValues = true
        }
    }
}