package io.verse.configure.core

import io.tagd.arch.data.repo.AbstractRepository
import io.tagd.arch.data.repo.Repository
import io.tagd.arch.domain.crosscutting.async.AsyncContext
import io.tagd.arch.domain.crosscutting.async.cancelComputations
import io.tagd.arch.domain.crosscutting.async.compute
import io.tagd.langx.Callback
import io.tagd.arch.scopable.library.gateway
import io.tagd.core.BidirectionalDependentOn
import io.tagd.langx.IllegalAccessException

interface IConfigurationRepository : Repository, AsyncContext,
    BidirectionalDependentOn<Configurer> {

    fun loadConfiguration(
        success: Callback<ApplicationConfiguration>? = null,
        failure: Callback<Throwable>? = null
    )

    fun fetchConfiguration(
        success: Callback<ApplicationConfiguration>? = null,
        failure: Callback<Throwable>? = null
    )

    fun setConfiguration(
        configuration: ApplicationConfiguration,
        success: Callback<Unit>? = null,
        failure: Callback<Throwable>? = null
    )
}

@Suppress("unused")
class ConfigurationRepository : AbstractRepository(), IConfigurationRepository {

    private var library: Configurer? = null
    private var gateway: IConfigurationGateway? = null

    override fun injectBidirectionalDependent(other: Configurer) {
        library = other
        gateway = other.gateway()
    }

    override fun loadConfiguration(
        success: Callback<ApplicationConfiguration>?,
        failure: Callback<Throwable>?
    ) {

        gateway?.cached(success, failure)
    }

    override fun fetchConfiguration(
        success: Callback<ApplicationConfiguration>?,
        failure: Callback<Throwable>?
    ) {

        gateway?.fetch(success, failure)
    }

    override fun setConfiguration(
        configuration: ApplicationConfiguration,
        success: Callback<Unit>?,
        failure: Callback<Throwable>?
    ) {

        //todo - test it as part of the inapp configuration provider implementation through UI
        compute {
            library?.inAppConfigurationProvider?.let {
                it.configuration = configuration
                /*
                 * todo
                 * we may end up having an [InAppConfigDao] to persist user's config change
                 * and this must take at most precedence while reading it back.
                 */
                success?.invoke(Unit)
            } ?: failure?.invoke(IllegalAccessException())
        }
    }

    override fun release() {
        library = null
        gateway = null
        cancelComputations()
        super.release()
    }
}