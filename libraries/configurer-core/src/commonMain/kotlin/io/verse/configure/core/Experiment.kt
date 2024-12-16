package io.verse.configure.core

import io.tagd.core.ValidateException

data class Experiment(
    override val name: String,
    override val configuration: Configuration,
    override val experiments: Map<String, Experiment> = hashMapOf()
) : BaseExperimentable(name = name, configuration = configuration, experiments = experiments) {

    lateinit var featureOrParentExperiment: Experimentable

    internal lateinit var flow: (experiment: Experiment) -> Unit

    fun register(flow: (experiment: Experiment) -> Unit): Experiment {
        this.flow = flow
        return this
    }

    fun update(childExperiment: Experiment): Experiment {
        val map = hashMapOf<String, Experiment>().apply {
            putAll(experiments)
        }
        map[childExperiment.name] = childExperiment
        return copy(experiments = map)
    }

    fun executeFlowIfExists() {
        if (::flow.isInitialized) {
            flow.invoke(this)
        }
    }

    override fun validate() {
        if (::featureOrParentExperiment.isInitialized.not()) {
            val message =
                "Feature or Parent Experiment for a given Experiment $name must be initialized"
            throw ValidateException(this, message)
        }
        super.validate()
    }
}