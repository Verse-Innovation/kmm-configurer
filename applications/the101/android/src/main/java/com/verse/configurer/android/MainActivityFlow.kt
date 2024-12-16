package com.verse.configurer.android

import android.util.Log
import io.tagd.core.ValidateException
import io.verse.configure.core.ApplicationConfiguration
import io.verse.configure.core.ApplicationConfigurationService
import io.verse.configure.core.Experiment
import io.verse.configure.core.ModuleConfiguration
import io.verse.configure.core.applicationConfigurationService


class MainActivityFlow : ConfigurationFlow {

    private val config by lazy {
        applicationConfigurationService()
            ?.module("default", "home module")
    }

    override fun trigger() {
        try {
            config?.validate()
            applicationConfigurationService()?.add(this)
        } catch (e: ValidateException) {
            e.printStackTrace()
        }
    }

    override fun onObserveConfigurationChange(
        configuration: ApplicationConfiguration,
        service: ApplicationConfigurationService
    ) {

        val moduleConfiguration = configuration.module("default", "home module")
        composeFlowThroughExperiments(moduleConfiguration)
    }

    private fun composeFlowThroughExperiments(config: ModuleConfiguration?) {
        /*
            Note - If remote config keep changing the response based on the defined frequency
            then we can notice the change in the flow here
         */

        /* presentation layer */
        config?.feature("pizza list card")
            ?.ifHas(experiment = "full page card", checker = { it.enabled }) {
                Log.d(TAG, "composeFlowThroughExperiments: flow page card flow")
                println("flow page card flow")
            }?.orElseIf(experiment = "two cards per page", checker = { it.enabled }) {
                Log.d(TAG, "composeFlowThroughExperiments: two cards per page")
                println("two cards per flow")
            }?.orElseIf(experiment = "three cards per page", checker = { it.enabled }) { child ->
                Log.d(TAG, "composeFlowThroughExperiments: three cards per page")
                println("three cards flow -- nested experiments")
                child.ifHas("show footer", checker = { child.enabled }) {
                    println("three cards flow -- nested experiments -- show footer")
                }.orDefault {
                    println("three cards flow -- nested experiments -- default nested")
                }.trigger()
            }?.orDefault {
                println("default")
            }?.trigger()

        config?.feature("non experimentable")?.let { simpleFeature ->
            simpleFeature.triggerDefault {
                println("default")
            }
        }

        config?.feature("feed list card with ::")
            ?.ifHas(experiment = "full page card", checker = { false }, ::abc)
            ?.orElseIf(experiment = "two cards per page", checker = { false }, ::abc)
            ?.orElseIf(experiment = "three cards per page", checker = { true }, ::abc)
            ?.orDefault(::abc)
            ?.trigger()
    }

    private fun abc(experiment: Experiment) {
        Log.d(TAG, "abc: ${experiment.name}")
    }

    override fun release() {
        // no-op
    }

    companion object {
        private const val TAG = "MainPageExperiments"
    }
}