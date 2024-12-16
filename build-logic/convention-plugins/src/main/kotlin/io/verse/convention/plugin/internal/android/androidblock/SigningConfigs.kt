package io.verse.convention.plugin.internal.android.androidblock

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project

internal fun Project.configureSigningConfigsInAndroidBlock(
    commonExtension: CommonExtension<*, *, *, *, *>
) {

    commonExtension.apply {
        signingConfigs {
            getByName("debug") {
                storeFile = file("$rootDir/keystore/debug.keystore")
            }
            create("upload") {
                storePassword = System.getenv("UPLOAD_KEY_STORE_PASSWORD")
                keyAlias = System.getenv("UPLOAD_KEY_ALIAS")
                keyPassword = System.getenv("UPLOAD_KEY_PASSWORD")
                storeFile = file("$rootDir/.signing/upload.keystore")
            }
        }
    }
}