@file:Suppress("TYPEALIAS_EXPANSION_DEPRECATION")

import com.android.build.gradle.api.ApplicationVariant
import org.gradle.api.provider.Property

typealias FieldBuilder = (
    com.android.build.gradle.internal.dsl.ProductFlavor,
    List<ApplicationVariant>
) -> LinkedHashMap<String, Pair<String, LinkedHashMap<String, Any>>>

typealias PlaceholderBuilder = (
    com.android.build.gradle.internal.dsl.ProductFlavor,
    List<ApplicationVariant>
) -> LinkedHashMap<String, LinkedHashMap<String, Any>>

abstract class AndroidFlavorVariantFieldConfigExtension {

    abstract val buildConfigFieldBuilder: Property<FieldBuilder?>

    abstract val resFieldBuilder: Property<FieldBuilder?>

    abstract val manifestPlaceholderBuilder: Property<PlaceholderBuilder?>

}