package io.verse.configure.core

import io.tagd.arch.datatype.DataObject
import io.tagd.core.ValidateException

typealias ExperimentChecker = ((config: Configuration) -> Boolean)

interface Experimentable : Configurable {

    val experiments: Map<String, Experiment>

    fun <T> property(name: String, default: T? = null): T?

    fun hasExperiment(name: String): Boolean

    fun experiment(name: String): Experiment?

    fun deepExperiment(name: String): Experiment?

    fun experiments(): List<Experiment>

    /**
     *   ifHas("exp_a") {
     *   }?.orElse("exp_b") {
     *   }?.orElse("exp_c") {
     *   }?.orElse("exp_d") {
     *   }?.default {
     *   }?.trigger()
     */
    fun ifHas(
        experiment: String,
        checker: ExperimentChecker,
        flow: (experiment: Experiment) -> Unit
    ): ExperimentFlow

    fun default(): Experiment?

    fun triggerDefault(flow: () -> Unit)
}

open class BaseExperimentable(
    override val name: String,
    override val configuration: Configuration,
    override val experiments: Map<String, Experiment> = hashMapOf()
) : DataObject(), Experimentable {

    val enabled: Boolean
        get() = configuration.enabled

    override fun <T> property(name: String, default: T?): T? {
        return configuration.property(name, default)
    }

    override fun hasExperiment(name: String): Boolean {
        return experiments.containsKey(name)
    }

    override fun experiment(name: String): Experiment? {
        return experiments[name]
    }

    override fun deepExperiment(name: String): Experiment? {
        var result = experiment(name)
        if (result == null) {
            experiments().forEach { experiment ->
                result = experiment.deepExperiment(name)
                if (result != null) {
                    return@forEach
                }
            }
        }
        return result
    }

    override fun experiments(): List<Experiment> {
        return experiments.values.toList()
    }

    override fun default(): Experiment? {
        return experiment("default") ?: experiments().firstOrNull {
            val canDefault = it.configuration.property("default", false)
            canDefault == true
        }
    }

    override fun ifHas(
        experiment: String,
        checker: ExperimentChecker,
        flow: (experiment: Experiment) -> Unit
    ): ExperimentFlow {

        val node = experiment(experiment)?.register(flow)?.also {
            it.configuration.checker = checker
        }
        return ExperimentFlow(this, node)
    }

    override fun triggerDefault(flow: () -> Unit) {
        val result = default()?.register {
            flow.invoke()
        }
        ExperimentFlow(this, result).trigger()
    }

    override fun validate() {
        if (name.isBlank()) {
            throw ValidateException(this, "Name should not be blank")
        }
        experiments().forEach {
            it.validate()
        }
        super.validate()
    }

    override fun release() {
        //no-op
    }
}

