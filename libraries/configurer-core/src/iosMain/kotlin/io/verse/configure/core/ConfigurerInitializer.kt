@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING",
    "ACTUAL_CLASSIFIER_MUST_HAVE_THE_SAME_MEMBERS_AS_NON_FINAL_EXPECT_CLASSIFIER_WARNING"
)

package io.verse.configure.core

import io.tagd.arch.control.LoadingStateHandler
import io.tagd.arch.infra.UnifiedResource
import io.tagd.arch.scopable.AbstractWithinScopableInitializer
import io.tagd.arch.scopable.Scopable
import io.tagd.core.Dependencies
import io.tagd.langx.Callback
import io.tagd.langx.Context
import io.verse.storage.core.DataObjectFileAccessor

actual open class ConfigurerInitializer<S : Scopable>(within: S) :
    AbstractWithinScopableInitializer<S, Configurer>(within) {

    override fun new(dependencies: Dependencies): Configurer {
        TODO("Not yet implemented")
    }

    override fun <WITHIN : Scopable> registerLoadingSteps(
        handler: LoadingStateHandler<WITHIN, *, *>,
        callback: Callback<Unit>
    ) {
        TODO("Not yet implemented")
    }

    /**
     * Overwrite this, if client is having its own bundle provider implementation
     */
    protected actual open fun newBundleProvider(
        context: Context,
        resource: UnifiedResource
    ): BundleConfigurationProvider {
        TODO("Not yet implemented")
    }

    /**
     * Overwrite this, if client is having its own on device provider / configuration dao
     * implementation
     */
    protected actual open fun newDao(
        filePath: String,
        accessor: DataObjectFileAccessor
    ): IConfigurationDao {
        TODO("Not yet implemented")
    }

    /**
     * Overwrite this, if client is having its own on remote config provider / gateway
     * implementation
     */
    protected actual open fun newGateway(
    ): IConfigurationGateway {
        TODO("Not yet implemented")
    }

    /**
     * Overwrite this, if client is having its own on repository implementation
     */
    protected actual open fun newRepository(): IConfigurationRepository {
        TODO("Not yet implemented")
    }
}