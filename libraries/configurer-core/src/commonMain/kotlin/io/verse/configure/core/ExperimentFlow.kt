package io.verse.configure.core

import io.tagd.arch.flow.Flow
import io.tagd.langx.IllegalAccessException
import io.tagd.langx.assert

class ExperimentFlow(
    private val featureOrParentExperiment: Experimentable,
    private val node: Experiment?
) : Flow {

    private var head: ExperimentFlow? = null
    private var orElse: ExperimentFlow? = null

    fun orElseIf(
        experiment: String,
        checker: ExperimentChecker,
        flow: (experiment: Experiment) -> Unit,
    ): ExperimentFlow {

        var elseFlow = this
        featureOrParentExperiment.experiment(experiment)?.let { other ->
            assert(other.featureOrParentExperiment === this.featureOrParentExperiment)

            other.flow = flow
            other.configuration.checker = checker

            elseFlow = ExperimentFlow(featureOrParentExperiment, other)
            elseFlow.head = this
            this.orElse = elseFlow
        }
        return elseFlow
    }

    fun orDefault(flow: (experiment: Experiment) -> Unit): ExperimentFlow {
        var defaultFlow = this
        featureOrParentExperiment.default()?.let { other ->
            assert(other.featureOrParentExperiment === this.featureOrParentExperiment)

            other.flow = flow
            other.configuration.checker = { true }

            defaultFlow = ExperimentFlow(featureOrParentExperiment, other)
            defaultFlow.head = this
            this.orElse = defaultFlow
        }
        return defaultFlow
    }

    @Suppress("LocalVariableName")
    override fun trigger() {
        var _head: ExperimentFlow? = head ?: this

        if (_head?.isUndefinedFlow() == true) {
            if (featureOrParentExperiment.default() == null) {
                throw IllegalAccessException(
                    "the ${featureOrParentExperiment.name} must have at least default flow defined"
                )
            } else {
                _head =
                    ExperimentFlow(featureOrParentExperiment, featureOrParentExperiment.default())
            }
        }

        while (_head != null) {
            if (_head.head != null) {
                _head = _head.head
            } else {
                _head.execute()
                break
            }
        }
    }

    private fun isUndefinedFlow() =
        isEndOfTheFlow() && node == null

    private fun isEndOfTheFlow() = (orElse == null)

    private fun execute() {
        if (node?.configuration?.areConditionsMet() == true) {
            node.executeFlowIfExists()
        } else {
            orElse?.execute() ?: executeDefaultIfExists()
        }
    }
    private fun executeDefaultIfExists() {
        featureOrParentExperiment.default()?.executeFlowIfExists()
    }

    override fun release() {
        //no-op
    }
}