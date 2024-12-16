import com.android.build.api.dsl.ProductFlavor
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property

abstract class AndroidVariantFieldConfig {

    abstract val flavor: Property<ProductFlavor>

    abstract val buildFields: MapProperty<String, Pair<String, LinkedHashMap<String, Any>>>

    abstract val resFields: MapProperty<String, Pair<String, LinkedHashMap<String, Any>>>

    abstract val manifestPlaceHolders: MapProperty<String, LinkedHashMap<String, Any>>

}