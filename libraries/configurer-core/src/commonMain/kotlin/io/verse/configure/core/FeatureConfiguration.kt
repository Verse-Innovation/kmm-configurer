package io.verse.configure.core

import io.tagd.core.ValidateException

data class FeatureConfiguration(
    override val name: String,
    override val configuration: Configuration,
    override val experiments: Map<String, Experiment> = hashMapOf()
) : BaseExperimentable(name = name, configuration = configuration, experiments = experiments) {

    lateinit var module: ModuleConfiguration

    fun update(config: Experiment): FeatureConfiguration {
        var experiment = config
        if (config.featureOrParentExperiment is Experiment) {
            experiment = (config.featureOrParentExperiment as Experiment).update(config)
        }
        val map = hashMapOf<String, Experiment>().apply {
            putAll(experiments)
        }
        map[config.name] = experiment
        return copy(experiments = map)
    }

    override fun validate() {
        if (::module.isInitialized.not()) {
            val message = "Module for Feature configuration  $name must be initialized"
            throw ValidateException(this, message)
        }
        super.validate()
    }
}