package io.verse.configure.core

import io.tagd.arch.infra.UnifiedResource
import io.tagd.core.BidirectionalDependentOn
import io.tagd.core.Service
import io.tagd.langx.IllegalAccessException

interface ConfigurationProvider : Service, BidirectionalDependentOn<Configurer> {

    val configuration: ApplicationConfiguration?

    override fun injectBidirectionalDependent(other: Configurer) {
        //no-op
    }

    fun products(): List<ProductConfiguration> {
        return configuration?.products() ?: listOf()
    }

    fun product(name: String): ProductConfiguration? {
        return configuration?.product(name)
    }

    fun modules(product: String): List<ModuleConfiguration> {
        return configuration?.product(product)?.modules() ?: listOf()
    }

    fun module(product: String, name: String): ModuleConfiguration? {
        return configuration?.module(product, name)
    }

    fun features(product: String, module: String): List<FeatureConfiguration> {
        return configuration?.features(product, module) ?: listOf()
    }

    fun feature(product: String, module: String, name: String): FeatureConfiguration? {
        return configuration?.module(product, module)?.feature(name)
    }

    fun experiments(product: String, module: String, feature: String): List<Experiment> {
        return configuration?.experiments(product, module, feature) ?: listOf()
    }

    fun experiment(product: String, module: String, feature: String, name: String): Experiment? {
        return configuration?.module(product, module)?.feature(feature)?.experiment(name)
    }

    fun deepExperiment(product: String, module: String, feature: String, name: String): Experiment? {
        return configuration?.module(product, module)?.feature(feature)?.deepExperiment(name)
    }

    fun update(config: Experiment) {
        throw IllegalAccessException()
    }

    fun update(config: FeatureConfiguration) {
        throw IllegalAccessException()
    }

    fun update(config: ModuleConfiguration) {
        throw IllegalAccessException()
    }

    fun update(config: ProductConfiguration) {
        throw IllegalAccessException()
    }
}

/**
 * getBundledValue(key, default -- the data type specific default or null)
 */
interface BundleConfigurationProvider : ConfigurationProvider {

    val resource: UnifiedResource
}

/**
 * getOnDeviceValue(key, default -- look at bundle)
 */
interface OnDeviceConfigurationProvider : ConfigurationProvider

/**
 * getRemoteValue(key, default -- look at onDevice)
 */
interface RemoteConfigurationProvider : ConfigurationProvider {

    val fetchContext: FetchRemoteConfigContext

    companion object {
        const val DEFAULT_SYNC_FREQUENCY_IN_MS = 1 * 60 * 1000L
    }
}

/**
 * getInAppValue(key, default -- look at remote)
 */
interface InAppConfigurationProvider : ConfigurationProvider {

    override var configuration: ApplicationConfiguration

    override fun update(config: ProductConfiguration) {
        configuration.update(config)
    }

    override fun update(config: ModuleConfiguration) {
        configuration.update(config)
    }

    override fun update(config: FeatureConfiguration) {
        configuration.update(config)
    }

    override fun update(config: Experiment) {
        configuration.update(config)
    }
}
