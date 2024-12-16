@file:Suppress("TYPEALIAS_EXPANSION_DEPRECATION")

package io.verse.convention.plugin.internal.android.androidblock

import AndroidFlavorVariantFieldConfigExtension
import FieldBuilder
import PlaceholderBuilder
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import com.android.build.gradle.internal.dsl.ProductFlavor
import org.gradle.api.Project

internal fun Project.configAllVariantsInAndroidBlock(
    appModuleExtension: BaseAppModuleExtension
) {

    val flavorFieldConfig = extensions.create(
        "flavorVariantFieldConfig",
        AndroidFlavorVariantFieldConfigExtension::class.java
    )

    appModuleExtension.apply {

        val flavorBuildConfigFieldBuilder = flavorFieldConfig.buildConfigFieldBuilder.orNull
            /*extra["flavorBuildConfigFieldBuilder"] as? FieldBuilder*/
        val flavorResFieldBuilder = flavorFieldConfig.resFieldBuilder.orNull
            /*extra["flavorResFieldBuilder"] as? FieldBuilder*/
        val flavorManifestPlaceholderBuilder = flavorFieldConfig.manifestPlaceholderBuilder.orNull
            /*extra["flavorManifestPlaceHolderBuilder"] as? PlaceholderBuilder*/

        productFlavors.all { flavor ->
            val flavorVariants = applicationVariants.filter {
                it.flavorName == flavor.name
            }

            val buildConfigFields = flavorBuildConfigFieldBuilder?.let { builder ->
                flavor.configureFlavorBuildFields(flavorVariants, builder)
            }
            val resFields = flavorResFieldBuilder?.let { builder ->
                flavor.configureFlavorResFields(flavorVariants, builder)
            }
            val manifestPlaceHolders = flavorManifestPlaceholderBuilder?.let { builder ->
                flavor.configureFlavorManifestPlaceholders(flavorVariants, builder)
            }

            flavorVariants.all { flavorVariant ->

                val variantName = flavorVariant.buildType.name

                buildConfigFields?.forEach { (k, v) ->
                    val variantValue = v.second[variantName]
                    flavorVariant.buildConfigField(v.first, k.uppercase(), variantValue as String)
                }
                resFields?.forEach { (k, v) ->
                    val variantValue = v.second[variantName]
                    flavorVariant.resValue(v.first, k, variantValue as String)
                }
                manifestPlaceHolders?.forEach { (k, v) ->
                    val variantValue = v[variantName]!!
                    flavorVariant.mergedFlavor.manifestPlaceholders[k] = variantValue
                }

                true
            }

            true
        }
    }
}

private fun ProductFlavor.configureFlavorBuildFields(
    flavorVariants: List<ApplicationVariant>,
    builder: FieldBuilder
): LinkedHashMap<String, Pair<String, LinkedHashMap<String, Any>>> {

    return builder.invoke(this, flavorVariants)
}

private fun ProductFlavor.configureFlavorResFields(
    flavorVariants: List<ApplicationVariant>,
    builder: FieldBuilder
): LinkedHashMap<String, Pair<String, LinkedHashMap<String, Any>>> {

    return builder.invoke(this, flavorVariants)
}

private fun ProductFlavor.configureFlavorManifestPlaceholders(
    flavorVariants: List<ApplicationVariant>,
    builder: PlaceholderBuilder
): LinkedHashMap<String, LinkedHashMap<String, Any>> {

    return builder.invoke(this, flavorVariants)
}