package com.verse.configurer.android

import io.tagd.android.app.TagdApplication
import io.tagd.android.app.loadingstate.AppLoadingStateHandler
import io.tagd.android.app.loadingstate.AppLoadingStepDispatcher
import io.verse.configure.core.ApplicationConfiguration
import io.verse.configure.core.ApplicationConfigurationService
import io.verse.configure.core.ConfigurationChangeObserver
import io.verse.configure.core.applicationConfigurationService

class MyAppLoadingStateHandler(
    application: TagdApplication,
    dispatcher: AppLoadingStepDispatcher<out TagdApplication>
) : AppLoadingStateHandler(dispatcher) {

    override fun onRegisterStep() {
        super.onRegisterStep()
        register(STEP_LOAD_CONFIGURATION, ::loadConfiguration)
    }

    private fun loadConfiguration() {
        applicationConfigurationService()?.add(object : ConfigurationChangeObserver {

            override fun onObserveConfigurationChange(
                configuration: ApplicationConfiguration,
                service: ApplicationConfigurationService
            ) {

                onComplete(STEP_LOAD_CONFIGURATION)
                service.remove(this)
            }
        })?.fetch()
    }

    private companion object {
        private const val STEP_LOAD_CONFIGURATION = "load-configuration"
    }
}