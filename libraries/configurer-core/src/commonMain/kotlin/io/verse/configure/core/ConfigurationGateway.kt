package io.verse.configure.core

import io.tagd.arch.data.gateway.Gateway
import io.tagd.arch.domain.crosscutting.async.AsyncContext
import io.tagd.arch.domain.crosscutting.async.compute
import io.tagd.arch.domain.crosscutting.async.networkIO
import io.tagd.langx.Callback
import io.tagd.arch.scopable.library.dao
import io.tagd.langx.IllegalAccessException
import io.verse.latch.core.ExecutionException
import io.verse.latch.core.FrequencyPolicy
import io.verse.latch.core.InterceptorGateway
import io.verse.latch.core.Request
import io.verse.latch.core.ResultContext
import io.verse.latch.core.newHttpsGetRequestBuilder

interface IConfigurationGateway : Gateway, RemoteConfigurationProvider, AsyncContext {

    fun cached(
        success: Callback<ApplicationConfiguration>? = null,
        failure: Callback<Throwable>? = null
    )

    fun fetch(
        success: Callback<ApplicationConfiguration>? = null,
        failure: Callback<Throwable>? = null
    )
}

open class ConfigurationGateway :
    InterceptorGateway<String, ApplicationConfigurationResponse, ApplicationConfiguration>(),
    IConfigurationGateway {

    private var library: Configurer? = null

    override val fetchContext: FetchRemoteConfigContext
        get() = library?.fetchContext!!

    private var dao: IConfigurationDao? = null

    override var configuration: ApplicationConfiguration? = null

    override fun injectBidirectionalDependent(other: Configurer) {
        library = other
        dao = other.dao()
    }

    override fun cached(
        success: Callback<ApplicationConfiguration>?,
        failure: Callback<Throwable>?
    ) {

        notifyExisting(success, failure)
    }

    override fun fetch(
        success: Callback<ApplicationConfiguration>?,
        failure: Callback<Throwable>?
    ) {

        if (configuration == null) {
            notifyExisting(success, failure)
        }

        networkIO {
            val request = newHttpsGetJsonRequest()
            request?.let {
                fire(request, success, failure)
            } ?: throw IllegalAccessException("failed to create request")
        }
    }

    private fun newHttpsGetJsonRequest(): Request<String, ApplicationConfigurationResponse>? {
        return library?.let {
            fetchContext.latch
                ?.newHttpsGetRequestBuilder(fetchContext.api.url, this)
                ?.frequencyPolicy(FrequencyPolicy(fetchContext.syncFrequency))
                ?.build()
        }
    }

    override fun success(
        context: ResultContext<String, ApplicationConfigurationResponse>,
        result: ApplicationConfigurationResponse
    ) {

        val requestContext = requestContext(context.identifier)
        notifyNew(result, requestContext?.success, requestContext?.failure)
    }

    private fun notifyNew(
        response: ApplicationConfigurationResponse,
        success: Callback<ApplicationConfiguration>?,
        failure: Callback<Throwable>?
    ) {

        response.configuredApplication?.let { dto ->
            try {
                val entity = dto.toApplicationConfiguration()!!
                dao?.writeEntityAsync(entity)
                configuration = entity
                success?.invoke(entity)
            } catch (e: Exception) {
                e.printStackTrace()
                notifyExisting(success, failure)
            }
        } ?: notifyExisting(success, failure)
    }

    override fun failure(exception: ExecutionException) {
        val requestContext = requestContext(exception.identifier)
        notifyExisting(requestContext?.success, requestContext?.failure)
    }

    private fun notifyExisting(
        success: Callback<ApplicationConfiguration>?,
        failure: Callback<Throwable>?
    ) {

        compute {
            dao?.readEntityAsync(success = { entity ->
                configuration = entity
                success?.invoke(entity)
            }, failure = {
                notifyDefault(success, failure)
            }) ?: kotlin.run {
                notifyDefault(success, failure)
            }
        }
    }

    private fun notifyDefault(
        success: Callback<ApplicationConfiguration>?,
        failure: Callback<Throwable>?
    ) {

        library?.bundleConfigurationProvider?.configuration?.let {
            configuration = it
            success?.invoke(it)
        } ?: kotlin.run {
            failure?.invoke(NullPointerException("Bundle Configuration is null"))
        }
    }

    override fun release() {
        library = null
        dao = null
        configuration = null
        super.release()
    }
}


