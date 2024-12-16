package io.verse.configure.core

import io.tagd.arch.datatype.DataObject
import io.tagd.core.ValidateException

data class ModuleConfiguration(
    override var name: String,
    override val configuration: Configuration,
    val features: Map<String, FeatureConfiguration> = hashMapOf()
) : DataObject(), Configurable {

    lateinit var product: ProductConfiguration

    val enabled
        get() = configuration.enabled

    fun hasFeature(name: String): Boolean {
        return features.containsKey(name)
    }

    fun feature(name: String): FeatureConfiguration? {
        return features[name]
    }

    fun experiment(feature: String, experiment: String): Experiment? {
        return feature(feature)?.experiment(experiment)
    }

    fun update(config: FeatureConfiguration): ModuleConfiguration {
        val map = hashMapOf<String, FeatureConfiguration>().apply {
            putAll(features)
        }
        map[config.name] = config
        return copy(features = map)
    }

    fun update(config: Experiment) {
        var parent: Experimentable = config.featureOrParentExperiment
        var updatedConfig = config
        while (parent is Experiment) {
            updatedConfig = parent.update(updatedConfig)
            parent = parent.featureOrParentExperiment
        }

        if (parent is FeatureConfiguration) {
            feature(parent.name)?.update(updatedConfig)?.let {
                update(it)
            }
        }
    }

    @Suppress("warnings")
    override fun validate() {
        if (name.isNullOrBlank()) {
            throw ValidateException(this, "Module configuration name should not blank")
        }
        features.forEach { it.value.validate() }
        super.validate()
    }

    override fun release() {
        //no-op
    }
}