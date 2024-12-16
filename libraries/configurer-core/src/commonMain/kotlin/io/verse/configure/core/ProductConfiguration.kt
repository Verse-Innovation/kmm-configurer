package io.verse.configure.core

import io.tagd.arch.datatype.DataObject
import io.tagd.core.ValidateException

data class ProductConfiguration(
    override val name: String,
    override val configuration: Configuration,
    private val modules: Map<String, ModuleConfiguration> = hashMapOf()
) : DataObject(), Configurable {

    lateinit var application: ApplicationConfiguration

    val enabled
        get() = configuration.enabled

    fun hasModule(name: String): Boolean {
        return modules.containsKey(name)
    }

    fun modules(): List<ModuleConfiguration> {
        return modules.values.toList()
    }

    fun module(name: String): ModuleConfiguration? {
        return modules[name]
    }

    fun features(module: String): List<FeatureConfiguration> {
        return modules[module]?.features?.values?.toList() ?: listOf()
    }

    @Suppress("unused")
    fun feature(module: String, feature: String): FeatureConfiguration? {
        return modules[module]?.feature(feature)
    }

    fun experiments(module: String, feature: String): List<Experiment> {
        return modules[module]?.feature(feature)?.experiments() ?: listOf()
    }

    fun experiment(module: String, feature: String, experiment: String): Experiment? {
        return modules[module]?.feature(feature)?.experiment(experiment)
    }

    fun update(config: ModuleConfiguration): ProductConfiguration {
        val map = hashMapOf<String, ModuleConfiguration>().apply {
            putAll(modules)
        }
        map[config.name] = config
        return copy(modules = map)
    }

    fun update(config: FeatureConfiguration): ProductConfiguration {
        return modules[config.module.name]?.update(config)?.let {
            update(it)
        } ?: this
    }

    fun update(config: Experiment) {
        var parent: Experimentable? = config.featureOrParentExperiment
        while (parent != null) {
            if (parent is Experiment) {
                parent = parent.featureOrParentExperiment
            } else if (parent is FeatureConfiguration) {
                modules[parent.module.name]?.update(config)
                break
            }
        }
    }

    @Suppress("warnings")
    override fun validate() {
        if (name.isNullOrBlank()) {
            throw ValidateException(this, "Application configuration name should not blank")
        }
        if (!enabled) {
            throw ValidateException(this, "Application configuration: $name must be enabled")
        }
        modules.forEach { it.value.validate() }
        super.validate()
    }

    override fun release() {
        //no-op
    }
}



