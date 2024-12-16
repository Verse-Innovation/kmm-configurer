import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import io.verse.convention.plugin.internal.android.androidblock.configureBuildFeaturesInAndroidBlock
import io.verse.convention.plugin.internal.android.androidblock.configureBuildTypesInAndroidApplicationBlock
import io.verse.convention.plugin.internal.android.androidblock.configureKotlinInAndroidBlock
import io.verse.convention.plugin.internal.android.androidblock.configurePackageOptionsInAndroidBlock
import io.verse.convention.plugin.internal.android.androidblock.configureSigningConfigsInAndroidBlock
import io.verse.convention.plugin.internal.android.androidblock.configAllVariantsInAndroidBlock
import io.verse.convention.plugin.internal.android.dependenciesblock.configureAndroidApplicationDependencies
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
                apply("org.jetbrains.kotlin.android")
            }

            extensions.configure<BaseAppModuleExtension> {
                defaultConfig.targetSdk = 33
                configureKotlinInAndroidBlock(this)
                configurePackageOptionsInAndroidBlock(this)
                configureBuildFeaturesInAndroidBlock(this)
                configureSigningConfigsInAndroidBlock(this)
                configureBuildTypesInAndroidApplicationBlock(this)
                configAllVariantsInAndroidBlock(this)
            }
            configureAndroidApplicationDependencies()
        }
    }
}

