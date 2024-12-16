package io.verse.configure.core

import io.tagd.arch.access.library
import io.tagd.arch.domain.crosscutting.async.AsyncContext
import io.tagd.arch.domain.crosscutting.async.compute
import io.tagd.arch.domain.crosscutting.async.present
import io.tagd.arch.domain.usecase.argsOf
import io.tagd.arch.scopable.library.usecase
import io.tagd.core.BidirectionalDependentOn
import io.tagd.langx.datatype.Serializable

interface ConfigurationChangeObserver {

    /**
     * handle - $application/$product/$module/$feature/$experiment - todo
     */
//    val handle: String

    fun onObserveConfigurationChange(
        configuration: ApplicationConfiguration,
        service: ApplicationConfigurationService
    )
}

/**
    fun access() {
        val m = module("my module")
        val f = ("my module" to "my feature").feature()
        val e = ("my module" to "my feature").experiment("my experiment")
        val de = ("my module" to "my feature").deepExperiment("my deep experiment")
    }

    fun subscribeOrUnsubscribe() {
        applicationConfigurationService()?.observe(theCallee)
        applicationConfigurationService()?.remove(theCallee)
    }
 */
class ApplicationConfigurationService : AsyncContext, BidirectionalDependentOn<Configurer> {

    private var inAppConfigurationProvider: InAppConfigurationProvider? = null
    private var remoteConfigurationProvider: RemoteConfigurationProvider? = null
    private var onDeviceConfigurationProvider: OnDeviceConfigurationProvider? = null
    private var bundleConfigurationProvider: BundleConfigurationProvider? = null

    private var loadUsecase: LoadConfigurationUsecase? = null
    private var fetchUsecase: FetchConfigurationUsecase? = null
    private var observers = ArrayList<ConfigurationChangeObserver?>()

    var configuration: ApplicationConfiguration? = null
        private set

    override fun injectBidirectionalDependent(other: Configurer) {
        inAppConfigurationProvider = other.inAppConfigurationProvider
        remoteConfigurationProvider = other.remoteConfigurationProvider
        onDeviceConfigurationProvider = other.onDeviceConfigurationProvider
        bundleConfigurationProvider = other.bundleConfigurationProvider

        loadUsecase = other.usecase()
        fetchUsecase = other.usecase()
    }

    fun add(observer: ConfigurationChangeObserver): ApplicationConfigurationService {
        if (!observers.contains(observer)) {
            observers.add(observer)

            configuration?.let { configuration ->
                observer.onObserveConfigurationChange(configuration, this)
            }
        }
        return this
    }

    fun remove(observer: ConfigurationChangeObserver): ApplicationConfigurationService {
        observers.remove(observer)
        return this
    }

    fun load() {
        compute {
            loadUsecase?.execute(args = argsOf("context" to this), success = { config ->
                present {
                    notifyConfigurationChange(config)
                }
            })
        }
    }

    fun fetch() {
        compute {
            fetchUsecase?.execute(args = argsOf("context" to this), success = { config ->
                present {
                    notifyConfigurationChange(config)
                }
            })
        }
    }

    fun dispatchConfigurationChange(configuration: ApplicationConfiguration) {
        notifyConfigurationChange(configuration)
    }

    private fun notifyConfigurationChange(configuration: ApplicationConfiguration) {
        this.configuration = configuration
        val listeners = ArrayList(observers)
        listeners.forEach {
            it?.onObserveConfigurationChange(configuration, this)
        }
    }

    fun product(name: String): ProductConfiguration? {
        return inAppConfigurationProvider?.product(name)
            ?: remoteConfigurationProvider?.product(name)
            ?: onDeviceConfigurationProvider?.product(name)
            ?: bundleConfigurationProvider?.product(name)
    }

    fun module(product: String, name: String): ModuleConfiguration? {
        return inAppConfigurationProvider?.module(product, name)
            ?: remoteConfigurationProvider?.module(product, name)
            ?: onDeviceConfigurationProvider?.module(product, name)
            ?: bundleConfigurationProvider?.module(product, name)
    }

    fun feature(
        product: String,
        module: String,
        name: String
    ): FeatureConfiguration? {

        return inAppConfigurationProvider?.feature(product, module, name)
            ?: remoteConfigurationProvider?.feature(product, module, name)
            ?: onDeviceConfigurationProvider?.feature(product, module, name)
            ?: bundleConfigurationProvider?.feature(product, module, name)
    }

    fun experiment(
        product: String,
        module: String,
        feature: String,
        name: String
    ): Experiment? {

        return inAppConfigurationProvider?.experiment(product, module, feature, name)
            ?: remoteConfigurationProvider?.experiment(product, module, feature, name)
            ?: onDeviceConfigurationProvider?.experiment(product, module, feature, name)
            ?: bundleConfigurationProvider?.experiment(product, module, feature, name)
    }

    fun deepExperiment(
        product: String,
        module: String,
        feature: String,
        name: String
    ): Experiment? {

        return inAppConfigurationProvider?.deepExperiment(product, module, feature, name)
            ?: remoteConfigurationProvider?.deepExperiment(product, module, feature, name)
            ?: onDeviceConfigurationProvider?.deepExperiment(product, module, feature, name)
            ?: bundleConfigurationProvider?.deepExperiment(product, module, feature, name)
    }

    override fun release() {
        observers.clear()
        loadUsecase = null
        fetchUsecase = null
        inAppConfigurationProvider = null
        remoteConfigurationProvider = null
        onDeviceConfigurationProvider = null
        bundleConfigurationProvider = null
    }
}

fun applicationConfigurationService(): ApplicationConfigurationService? {
    return library<Configurer>()?.service
}

fun product(name: String): ProductConfiguration? {
    return applicationConfigurationService()?.product(name)
}

fun module(product: String, name: String): ModuleConfiguration? {
    return applicationConfigurationService()?.module(product, name)
}

fun Pair<String, String>.feature(name: String): FeatureConfiguration? {
    return applicationConfigurationService()?.feature(first, second, name)
}

fun Triple<String, String, String>.feature(): FeatureConfiguration? {
    return applicationConfigurationService()?.feature(first, second, third)
}

fun Triple<String, String, String>.experiment(name: String): Experiment? {
    return applicationConfigurationService()?.experiment(first, second, third, name)
}

fun Triple<String, String, String>.deepExperiment(name: String): Experiment? {
    return applicationConfigurationService()?.deepExperiment(first, second, third, name)
}

fun <T> Triple<String, String, String>.property(name: String, default: T? = null): T? {
    return this.feature()?.property(name, default)
}

fun <T> Quadruple<String, String, String, String>.property(name: String, default: T? = null): T? {
    return Triple(first, second, third).experiment(fourth)?.property(name, default)
}

fun <T> Quadruple<String, String, String, String>.deepProperty(
    name: String,
    default: T? = null
): T? {

    return Triple(first, second, third).deepExperiment(fourth)?.property(name, default)
}

data class Quadruple<out A, out B, out C, out D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: C,
) : Serializable {

    /**
     * Returns string representation of the [Triple] including its [first], [second], [third]
     * and [fourth] values.
     */
    override fun toString(): String = "($first, $second, $third, $fourth)"
}