package io.verse.configure.core

import io.tagd.arch.domain.crosscutting.codec.SerializedName
import io.tagd.core.ValidateException
import io.verse.storage.core.FilableDataObject

data class ApplicationConfigurationDto(
    @SerializedName("version", [])
    val version: Int? = null,

    @SerializedName("name", [])
    val name: String? = null,

    @SerializedName("configuration", [])
    val configuration: Map<String, Any>? = null,

    @SerializedName("products", [])
    val configuredProducts: List<ProductConfigurationDto>? = null,
) : FilableDataObject() {

    override fun validate() {
        super.validate()

        if (name.isNullOrBlank()) {
            throw ValidateException(this, "name should not be null")
        }
        if ((version ?: -1) <= 0) {
            throw ValidateException(this, "Version for $name must be greater than 0")
        }

        configuredProducts?.forEach { it.validate() }
    }

    fun toApplicationConfiguration(): ApplicationConfiguration? {
        validate()

        return name?.let { configName ->
            return ApplicationConfiguration(
                name = configName,
                version = version ?: 1,
                configuration = configuration?.toConfigurationEntity()!!,
                products = toProducts(configuredProducts),
            ).also { application ->
                application.products().forEach {
                    it.application = application
                }

                application.validate()
            }
        }
    }

    private fun toProducts(
        modules: List<ProductConfigurationDto>?
    ): Map<String, ProductConfiguration> {

        val map = hashMapOf<String, ProductConfiguration>()
        modules?.forEach { dto ->
            dto.toProductConfiguration()?.let {
                map[it.name] = it
            }
        }
        return map
    }

    companion object {

        fun from(
            applicationConfiguration: ApplicationConfiguration
        ): ApplicationConfigurationDto {

            return ApplicationConfigurationDto(
                version = applicationConfiguration.version,
                name = applicationConfiguration.name,
                configuration = applicationConfiguration.configuration.properties,
                configuredProducts = applicationConfiguration.products().map {
                    ProductConfigurationDto.from(it)
                }
            )
        }
    }
}

data class ProductConfigurationDto(
    @SerializedName("name", [])
    val name: String? = null,

    @SerializedName("configuration", [])
    val configuration: Map<String, Any>? = null,

    @SerializedName("modules", [])
    val configuredModules: List<ModuleConfigurationDto>? = null,
) : FilableDataObject() {

    override fun validate() {
        super.validate()

        if (name.isNullOrBlank()) {
            throw ValidateException(this, "name should not be null")
        }

        configuredModules?.forEach { it.validate() }
    }

    fun toProductConfiguration(): ProductConfiguration? {
        validate()

        return name?.let { configName ->
            return ProductConfiguration(
                name = configName,
                configuration = configuration.toConfigurationEntity(),
                modules = toModules(configuredModules),
            ).also { product ->
                product.modules().forEach {
                    it.product = product
                }
            }
        }
    }

    private fun toModules(
        modules: List<ModuleConfigurationDto>?
    ): Map<String, ModuleConfiguration> {

        val map = hashMapOf<String, ModuleConfiguration>()
        modules?.forEach { dto ->
            dto.toModuleConfiguration()?.let {
                map[it.name] = it
            }
        }
        return map
    }

    companion object {

        fun from(
            productConfiguration: ProductConfiguration
        ): ProductConfigurationDto {

            return ProductConfigurationDto(
                name = productConfiguration.name,
                configuration = productConfiguration.configuration.properties,
                configuredModules = productConfiguration.modules().map {
                    ModuleConfigurationDto.from(it)
                }
            )
        }
    }
}

data class ModuleConfigurationDto(

    @SerializedName("name", [])
    val name: String?,

    @SerializedName("configuration", [])
    val configuration: Map<String, Any>? = null,

    @SerializedName("features", [])
    val configuredFeatures: List<FeatureConfigurationDto>? = null
) : FilableDataObject() {

    override fun validate() {
        super.validate()

        if (name.isNullOrBlank()) {
            throw ValidateException(this, "Module configuration name should not blank")
        }

        configuredFeatures?.forEach { it.validate() }
    }

    fun toModuleConfiguration(): ModuleConfiguration? {
        validate()

        return name?.let { configName ->
            ModuleConfiguration(
                name = configName,
                configuration = configuration.toConfigurationEntity(),
                features = toFeatures(configuredFeatures),
            ).also { module ->
                module.features.forEach {
                    it.value.module = module
                }
            }
        }
    }

    private fun toFeatures(
        features: List<FeatureConfigurationDto>?
    ): Map<String, FeatureConfiguration> {

        val map = hashMapOf<String, FeatureConfiguration>()
        features?.forEach { dto ->
            dto.toFeatureConfiguration()?.let {
                map[it.name] = it
            }
        }
        return map
    }

    companion object {

        fun from(
            moduleConfiguration: ModuleConfiguration
        ): ModuleConfigurationDto {

            return ModuleConfigurationDto(
                name = moduleConfiguration.name,
                configuration = moduleConfiguration.configuration.properties,
                configuredFeatures = moduleConfiguration.features.map {
                    FeatureConfigurationDto.from(it.value)
                }
            )
        }
    }
}

data class FeatureConfigurationDto(

    @SerializedName("name", [])
    val name: String? = null,

    @SerializedName("configuration", [])
    val configuration: Map<String, Any>? = null,

    @SerializedName("experiments", [])
    val configuredExperiments: List<ExperimentConfigurationDto>? = null,
) : FilableDataObject() {

    override fun validate() {
        super.validate()

        if (name.isNullOrBlank()) {
            throw ValidateException(this, "Name should not be blank")
        }

        configuredExperiments?.forEach { it.validate() }
    }

    fun toFeatureConfiguration(): FeatureConfiguration? {
        validate()

        return name?.let { configName ->
            FeatureConfiguration(
                name = configName,
                configuration = configuration.toConfigurationEntity(),
                experiments = configuredExperiments.toExperimentMap()
            ).also { feature ->
                feature.experiments().forEach { experiment ->
                    experiment.featureOrParentExperiment = feature
                }
            }
        }
    }

    companion object {

        fun from(
            featureConfiguration: FeatureConfiguration
        ): FeatureConfigurationDto {

            return FeatureConfigurationDto(
                name = featureConfiguration.name,
                configuration = featureConfiguration.configuration.properties,
                configuredExperiments = featureConfiguration.experiments.map {
                    ExperimentConfigurationDto.from(it.value)
                }
            )
        }
    }
}

data class ExperimentConfigurationDto(

    @SerializedName("name", [])
    val name: String? = null,

    @SerializedName("configuration", [])
    val configuration: Map<String, Any>? = null,

    @SerializedName("experiments", [])
    val configuredExperiments: List<ExperimentConfigurationDto>? = null,
) : FilableDataObject() {

    override fun validate() {
        super.validate()

        if (name.isNullOrBlank()) {
            throw ValidateException(this, "Name should not be blank")
        }

        configuredExperiments?.forEach { it.validate() }
    }

    fun toExperiment(): Experiment? {
        validate()

        return name?.let { configName ->
            Experiment(
                name = configName,
                configuration = configuration.toConfigurationEntity(),
                experiments = configuredExperiments.toExperimentMap()
            ).also { experiment ->
                experiment.experiments().forEach { childExperiment ->
                    childExperiment.featureOrParentExperiment = experiment
                }
            }
        }
    }

    companion object {

        fun from(experiment: Experiment): ExperimentConfigurationDto {

            return ExperimentConfigurationDto(
                name = experiment.name,
                configuration = experiment.configuration.properties,
                configuredExperiments = experiment.experiments().map {
                    from(it)
                }
            )
        }
    }
}

private fun Map<String, Any>?.toConfigurationEntity(): Configuration {
    return if (this == null) {
        Configuration(enabled = true)
    } else {
        Configuration(enabled = (this["enabled"] as? Boolean) ?: true, properties = this)
    }
}

private fun List<ExperimentConfigurationDto>?.toExperimentMap(): Map<String, Experiment> {
    if (this == null) {
        return emptyMap()
    }

    val map = hashMapOf<String, Experiment>()
    forEach { dto ->
        dto.toExperiment()?.let {
            map[it.name] = it
        }
    }
    return map
}