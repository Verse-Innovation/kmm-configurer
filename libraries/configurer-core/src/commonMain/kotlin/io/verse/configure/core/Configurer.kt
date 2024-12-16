package io.verse.configure.core

import io.tagd.arch.data.dao.DataAccessObject
import io.tagd.arch.data.gateway.Gateway
import io.tagd.arch.data.repo.Repository
import io.tagd.arch.domain.usecase.Command
import io.tagd.arch.scopable.library.AbstractLibrary
import io.tagd.arch.scopable.library.Library
import io.tagd.di.Scope
import io.tagd.di.bind
import io.tagd.di.key
import io.tagd.di.layer
import io.tagd.langx.assert

class Configurer private constructor(
    name: String,
    outerScope: Scope,
) : AbstractLibrary(name, outerScope) {

    var fetchContext: FetchRemoteConfigContext? = null
        internal set

    val latch
        get() = fetchContext?.latch

    lateinit var bundleConfigurationProvider: BundleConfigurationProvider
        private set

    var onDeviceConfigurationProvider: OnDeviceConfigurationProvider? = null
        private set

    var remoteConfigurationProvider: RemoteConfigurationProvider? = null
        private set

    var inAppConfigurationProvider: InAppConfigurationProvider? = null
        private set

    var service: ApplicationConfigurationService? = null
        private set

    val configuration: ApplicationConfiguration?
        get() = service?.configuration

    @Suppress("unused")
    fun loaded(): Boolean {
        return service?.configuration != null
    }

    @Suppress("unused")
    fun update(fetchContext: FetchRemoteConfigContext) {
        this.fetchContext = fetchContext
    }

    override fun release() {
        fetchContext = null

        service?.release()
        service = null

        onDeviceConfigurationProvider = null
        remoteConfigurationProvider = null
        inAppConfigurationProvider = null
        super.release()
    }

    class Builder : Library.Builder<Configurer>() {

        private var fetchContext: FetchRemoteConfigContext? = null

        private lateinit var bundleConfigProvider: BundleConfigurationProvider
        private var onDeviceConfigProvider: OnDeviceConfigurationProvider? = null
        private var remoteConfigProvider: RemoteConfigurationProvider? = null
        private var inAppConfigProvider: InAppConfigurationProvider? = null

        override fun name(name: String?): Builder {
            super.name(name)
            return this
        }

        override fun scope(outer: Scope?): Builder {
            super.scope(outer)
            return this
        }

        fun fetchContext(context: FetchRemoteConfigContext?): Builder {
            this.fetchContext = context
            return this
        }

        override fun inject(
            bindings: Scope.(Configurer) -> Unit
        ): Builder {

            super.inject(bindings)
            return this
        }

        fun bundleConfigurationProvider(provider: BundleConfigurationProvider): Builder {
            this.bundleConfigProvider = provider
            return this
        }

        fun onDeviceConfigurationProvider(provider: OnDeviceConfigurationProvider?): Builder {
            this.onDeviceConfigProvider = provider
            return this
        }

        fun remoteConfigurationProvider(provider: RemoteConfigurationProvider?): Builder {
            this.remoteConfigProvider = provider
            return this
        }

        fun inAppConfigurationProvider(provider: InAppConfigurationProvider?): Builder {
            this.inAppConfigProvider = provider
            return this
        }

        override fun buildLibrary(): Configurer {
            assertInjections()

            return Configurer(name ?: "${outerScope.name}/$NAME", outerScope).also { lib ->
                lib.fetchContext = fetchContext
                lib.bundleConfigurationProvider = bundleConfigProvider
                lib.onDeviceConfigurationProvider = onDeviceConfigProvider
                lib.inAppConfigurationProvider = inAppConfigProvider
                lib.remoteConfigurationProvider = remoteConfigProvider
                lib.service = ApplicationConfigurationService()

                outerScope.bind<Library, Configurer>(instance = lib)
            }
        }

        private fun assertInjections() {
            assert(::bundleConfigProvider.isInitialized)
        }

        companion object {
            const val NAME = "configurer"
        }
    }
}

fun newConfigurationBuilder(
    name: String?,
    outerScope: Scope?,
    fetchContext: FetchRemoteConfigContext?,
    bundleConfigurationProvider: BundleConfigurationProvider,
    configurationDao: IConfigurationDao? = null,
    configurationGateway: IConfigurationGateway? = null,
    inAppConfigurationProvider: InAppConfigurationProvider? = null,
    configurationRepository: IConfigurationRepository? = null
): Configurer.Builder {

    return Configurer.Builder()
        .name(name)
        .scope(outerScope)
        .fetchContext(fetchContext)
        .bundleConfigurationProvider(bundleConfigurationProvider)
        .onDeviceConfigurationProvider(configurationDao)
        .remoteConfigurationProvider(configurationGateway)
        .inAppConfigurationProvider(inAppConfigurationProvider)
        .inject {library ->
            layer<DataAccessObject> {
                configurationDao?.let {
                    bind(key(), configurationDao)
                    it.injectBidirectionalDependent(library)
                }
            }
            layer<Gateway> {
                configurationGateway?.let {
                    bind(key(), configurationGateway)
                    it.injectBidirectionalDependent(library)
                }
            }
            layer<Repository> {
                configurationRepository?.let {
                    bind(key(), configurationRepository)
                    it.injectBidirectionalDependent(library)
                }
            }
            layer<Command<*, *>> {
                bind(key(), LoadConfigurationUsecase().also {
                    it.injectBidirectionalDependent(library)
                })
                bind(key(), FetchConfigurationUsecase().also {
                    it.injectBidirectionalDependent(library)
                })
            }

            library.service?.injectBidirectionalDependent(library)
        }
}

@Suppress("unused")
fun Configurer.haveInternalLatch(): Boolean {
    return latch?.outerScope?.name == name
}