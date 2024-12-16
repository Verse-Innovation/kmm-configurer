@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.verse.configure.core

import io.tagd.arch.infra.UnifiedResource
import io.tagd.arch.scopable.AbstractWithinScopableInitializer
import io.tagd.arch.scopable.Scopable
import io.tagd.langx.Context
import io.verse.storage.core.DataObjectFileAccessor

expect open class ConfigurerInitializer<S : Scopable> :
    AbstractWithinScopableInitializer<S, Configurer> {

    /**
     * Overwrite this, if client is having its own bundle provider implementation
     */
    protected open fun newBundleProvider(
        context: Context,
        resource: UnifiedResource
    ): BundleConfigurationProvider

    /**
     * Overwrite this, if client is having its own on device provider / configuration dao
     * implementation
     */
    protected open fun newDao(filePath: String, accessor: DataObjectFileAccessor): IConfigurationDao

    /**
     * Overwrite this, if client is having its own on remote config provider / gateway
     * implementation
     */
    protected open fun newGateway(): IConfigurationGateway


    /**
     * Overwrite this, if client is having its own on repository implementation
     */
    protected open fun newRepository(): IConfigurationRepository
}

