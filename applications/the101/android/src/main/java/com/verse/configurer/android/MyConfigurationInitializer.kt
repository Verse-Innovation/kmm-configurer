package com.verse.configurer.android

import io.tagd.arch.scopable.library.Library
import io.tagd.di.Global
import io.tagd.di.bindLazy
import io.tagd.langx.Callback
import io.tagd.langx.time.Millis
import io.verse.configure.core.AccessOnDeviceConfigContext
import io.verse.configure.core.Configurer
import io.verse.configure.core.ConfigurerConfig
import io.verse.configure.core.ConfigurerInitializer
import io.verse.configure.core.FetchRemoteConfigContext
import io.verse.configure.core.RemoteConfigurationProvider
import io.verse.latch.core.Api
import io.verse.latch.core.Server

class MyConfigurationInitializer(within: MyApplication) :
    ConfigurerInitializer<MyApplication>(within) {

    override fun initialize(callback: Callback<Unit>) {
        outerScope.bindLazy<Library, Configurer> {
            val dependencies = newDependencies()
            dependencies.put(ARG_CONFIG, newConfigurerConfig())
            new(dependencies)
        }

        super.initialize(callback)
    }

    private fun newConfigurerConfig(): ConfigurerConfig {
        return ConfigurerConfig(
            context = within,
            name = "my app configurer",
            outerScope = Global,
            fetchRemoteContext = FetchRemoteConfigContext(
                server = Server("https", "https://demo2921399.mockable.io"),
                api = Api("/app-config"),
                syncFrequency = Millis(RemoteConfigurationProvider.DEFAULT_SYNC_FREQUENCY_IN_MS)
            ),
            accessOnDeviceContext = AccessOnDeviceConfigContext(
                storedConfigPath = within.filesDir.path
            ),
            defaultConfigurationResourceId = R.raw.default_application_configuration,
        )
    }
}