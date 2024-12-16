@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.verse.configure.core

import io.tagd.arch.data.dao.DataAccessObject
import io.tagd.arch.infra.UnifiedResource
import io.tagd.arch.scopable.AbstractWithinScopableInitializer
import io.tagd.arch.scopable.Scopable
import io.tagd.arch.scopable.bind
import io.tagd.arch.scopable.library.Library
import io.tagd.core.Dependencies
import io.tagd.core.dependencies
import io.tagd.di.bindLazy
import io.tagd.langx.Callback
import io.tagd.langx.Context
import io.tagd.langx.isNull
import io.verse.latch.core.Latch
import io.verse.latch.core.LatchInitializer
import io.verse.storage.core.DataObjectFileAccessor
import io.verse.storage.core.Storage
import io.verse.storage.core.StorageInitializer

actual open class ConfigurerInitializer<S : Scopable>(within: S) :
    AbstractWithinScopableInitializer<S, Configurer>(within) {

    override fun new(dependencies: Dependencies): Configurer {
        assert(dependencies)

        val config = dependencies.get<ConfigurerConfig>(ARG_CONFIG)!!
        val defaultConfigurationResource = UnifiedResource(
            identifier = config.defaultConfigurationResourceId!!,
            `package` = config.context.packageName
        )

        val bundleConfigurationProvider = newBundleProvider(
            config.context,
            defaultConfigurationResource
        )
        
        val fetchRemoteConfigContext = getFetchRemoteConfigContext(config)
        val configurationDao = newDao(config)
        val configurationGateway = newGateway()
        val configurationRepository = newRepository()

        return newConfigurationBuilder(
            name = config.name,
            outerScope = config.outerScope,
            fetchContext = fetchRemoteConfigContext,
            bundleConfigurationProvider = bundleConfigurationProvider,
            configurationDao = configurationDao,
            configurationGateway = configurationGateway,
            inAppConfigurationProvider = null,
            configurationRepository = configurationRepository
        ).build().also { configurer ->

            configurer.initIfNull(config, fetchRemoteConfigContext, configurationDao)

            if (config.autoLoad) {
                configurationDao?.readEntity(success = { entity ->
                    configurer.service?.dispatchConfigurationChange(entity)
                })
            }
        }
    }

    protected open fun Configurer.initIfNull(
        config: ConfigurerConfig,
        configuredContext: FetchRemoteConfigContext?,
        preparedDaoUsingConfig: IConfigurationDao?
    ) {

        if (configuredContext == null) {
            fetchContext = config.fetchRemoteContext?.copy(latch = newLatch(config))
        }
        if (preparedDaoUsingConfig == null) {
            val accessOnDeviceConfigContext =
                config.accessOnDeviceContext?.copy(storage = newStorage())

            val newConfigurationDao = newDao(accessOnDeviceConfigContext)!!
            bind<DataAccessObject, IConfigurationDao>(instance = newConfigurationDao)
            newConfigurationDao.injectBidirectionalDependent(this)
        }
    }

    protected open fun assert(dependencies: Dependencies) {
        val configurerConfig = dependencies.get<ConfigurerConfig>(ARG_CONFIG)
        assert(configurerConfig != null)
        configurerConfig?.validate()
    }

    /**
     * Overwrite this, if client is having its own bundle provider implementation
     */
    protected actual open fun newBundleProvider(
        context: Context,
        resource: UnifiedResource
    ): BundleConfigurationProvider {

        return DefaultBundleConfigurationProvider(
            context = context,
            unifiedResource = resource
        )
    }

    private fun newDao(config: ConfigurerConfig): IConfigurationDao? {
        val accessOnDeviceConfigContext = getAccessOnDeviceConfigContext(config)
        return newDao(accessOnDeviceConfigContext)
    }

    private fun newDao(
        accessOnDeviceConfigContext: AccessOnDeviceConfigContext?
    ): IConfigurationDao? {

        return accessOnDeviceConfigContext?.storage?.let {
            newDao(accessOnDeviceConfigContext.storedConfigPath, it.dataObjectFileAccessor)
        }
    }

    /**
     * Overwrite this, if client is having its own on device provider / configuration dao
     * implementation
     */
    protected actual open fun newDao(
        filePath: String,
        accessor: DataObjectFileAccessor
    ): IConfigurationDao {

        return ConfigurationDao(
            name = ApplicationConfiguration::class.java.simpleName,
            path = filePath,
            accessor = accessor
        )
    }

    /**
     * Overwrite this, if client is having its own on remote config provider / gateway
     * implementation
     */
    protected actual open fun newGateway(): IConfigurationGateway {
        return ConfigurationGateway()
    }

    /**
     * Overwrite this, if client is having its own on repository implementation
     */
    protected actual open fun newRepository(): IConfigurationRepository {
        return ConfigurationRepository()
    }

    private fun getFetchRemoteConfigContext(config: ConfigurerConfig): FetchRemoteConfigContext? {
        return config.fetchRemoteContext?.takeIf { it.latch != null }
    }

    protected open fun Configurer.newLatch(config: ConfigurerConfig): Latch? {
        return ConfigurerLatchInitializer(
            config.context,
            this,
            "configurer",
            config
        ).new(
            dependencies = Dependencies().also {
                it.put(LatchInitializer.ARG_CONTEXT, config.context)
            }
        )
    }

    private fun getAccessOnDeviceConfigContext(
        config: ConfigurerConfig
    ): AccessOnDeviceConfigContext? {

        return config.accessOnDeviceContext?.takeIf { !it.storage.isNull() }
    }

    protected open fun Configurer.newStorage(): Storage {
        val dependencies = dependencies(
            StorageInitializer.ARG_OUTER_SCOPE to thisScope
        )
        return StorageInitializer(this).new(dependencies)
    }

    companion object {
        const val ARG_CONFIG = "config"
    }
}
