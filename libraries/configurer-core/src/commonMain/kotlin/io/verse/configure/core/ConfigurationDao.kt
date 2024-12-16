package io.verse.configure.core

import io.tagd.arch.domain.crosscutting.async.daoIO
import io.tagd.langx.Callback
import io.verse.storage.core.DataObjectFileAccessor
import io.verse.storage.core.FilableDao
import io.verse.storage.core.FilableDataAccessObject

interface IConfigurationDao : FilableDataAccessObject<ApplicationConfigurationDto>,
    OnDeviceConfigurationProvider {

    fun writeEntityAsync(
        entity: ApplicationConfiguration,
        success: Callback<Unit>? = null,
        failure: Callback<Throwable>? = null
    )

    fun readEntityAsync(
        success: Callback<ApplicationConfiguration>? = null,
        failure: Callback<Throwable>? = null
    )

    fun readEntity(
        success: Callback<ApplicationConfiguration>? = null,
        failure: Callback<Throwable>? = null
    )
}

open class ConfigurationDao(
    name: String,
    path: String,
    accessor: DataObjectFileAccessor
) : FilableDao<ApplicationConfigurationDto>(name = name, path = path, accessor = accessor),
    IConfigurationDao {

    private var bundleConfigurationProvider: BundleConfigurationProvider? = null

    override lateinit var configuration: ApplicationConfiguration

    override fun injectBidirectionalDependent(other: Configurer) {
        bundleConfigurationProvider = other.bundleConfigurationProvider
    }

    override fun writeEntityAsync(
        entity: ApplicationConfiguration,
        success: Callback<Unit>?,
        failure: Callback<Throwable>?
    ) {

        writeAsync(ApplicationConfigurationDto.from(entity), success, failure)
    }

    override fun readEntityAsync(
        success: Callback<ApplicationConfiguration>?,
        failure: Callback<Throwable>?
    ) {

        readAsync(
            success = {
                // use the cached flavour, because read internally caches the dto as entity
                success?.invoke(configuration)
            },
            failure = failure
        )
    }

    override fun readEntity(
        success: Callback<ApplicationConfiguration>?,
        failure: Callback<Throwable>?
    ) {
        read(
            success = {
                configuration = it.toApplicationConfiguration()!!
                success?.invoke(configuration)
            },
            failure = failure
        )
    }

    override fun writeAsync(
        data: ApplicationConfigurationDto,
        success: Callback<Unit>?,
        failure: Callback<Throwable>?
    ) {

        daoIO {
            cacheAndContinue(
                dto = data,
                success = {
                    super.write(data, success, failure)
                },
                failure = failure
            )
        }
    }

    override fun readAsync(
        success: Callback<ApplicationConfigurationDto>?,
        failure: Callback<Throwable>?
    ) {

        super.readAsync(success = { result ->
            cacheAndContinue(
                dto = result,
                success = {
                    success?.invoke(result)
                },
                failure = failure
            )
        }, failure = { error ->
            cachedDataObject?.let { result ->
                cacheAndContinue(
                    dto = result,
                    success = {
                        success?.invoke(result)
                    },
                    failure = failure
                )
            } ?: kotlin.run {
                failure?.invoke(error)
            }
        })
    }

    private fun cacheAndContinue(
        dto: ApplicationConfigurationDto,
        success: Callback<Unit>? = null,
        failure: Callback<Throwable>? = null
    ) {

        try {
            configuration = dto.toApplicationConfiguration()!!
            success?.invoke(Unit)
        } catch (e: Exception) {
            failure?.invoke(e)
        }
    }

    override fun interceptReadResult(
        t: ApplicationConfigurationDto?
    ): ApplicationConfigurationDto? {

        return t ?: bundleConfigurationProvider?.configuration?.let {
            ApplicationConfigurationDto.from(it)
        }
    }

}