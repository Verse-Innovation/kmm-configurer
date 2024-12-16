package io.verse.configure.core

import io.tagd.arch.datatype.DataObject
import io.tagd.core.ValidateException

/**
 *    {
 *      "configuration": {
 *       "modules": [
 *         {
 *           "name": "module_A",
 *           "enabled": true,
 *           "features": [
 *             {
 *               "name": "feature_1",
 *                "configuration": {
 *                  "enabled": true,
 *                  "experiments": [
 *                    {
 *                      "name": "experiment_1",
 *                      "configuration": {
 *                        "enabled": true,
 *                        "max_times": 3,
 *                        "min_app_launches": 2
 *                      }
 *                    },
 *                    {
 *                      "name": "experiment_2",
 *                      "configuration": {
 *                        "enabled": true,
 *                        "max_times": 1,
 *                        "min_app_launches": 0
 *                      }
 *                    },
 *                    {
 *                      "name": "experiment_3",
 *                      "configuration": {
 *                        "enabled": true,
 *                        "for_app_launches": "even",
 *                        "experiments": [
 *                          {
 *                            "name": "experiment_3A",
 *                            "configuration": {
 *                              "enabled": true
 *                            }
 *                          }
 *                        ]
 *                      }
 *                    },
 *                    {
 *                      "name": "experiment_4",
 *                      "configuration": {
 *                        "enabled": false
 *                      },
 *                      "CRUDOperation": "D"
 *                    },
 *                    {
 *                      "name": "default"
 *                    }
 *                  ]
 *                }
 *              },
 *              {
 *                "...": "..."
 *              }
 *            ]
 *          },
 *          {
 *            "...": "..."
 *          }
 *        ]
 *      }
 *    }
 */
data class ApplicationConfiguration(
    override val name: String,
    val version: Int,
    override val configuration: Configuration,
    private val products: Map<String, ProductConfiguration> = hashMapOf()
) : DataObject(), Configurable {

    val enabled
        get() = configuration.enabled

    val flavor
        get() = configuration.properties["flavor"]

    fun hasProduct(name: String): Boolean {
        return products.containsKey(name)
    }

    fun products(): List<ProductConfiguration> {
        return products.values.toList()
    }

    fun product(name: String): ProductConfiguration? {
        return products[name]
    }

    fun modules(product: String): List<ModuleConfiguration> {
        return products[product]?.modules() ?: listOf()
    }

    fun module(product: String, name: String): ModuleConfiguration? {
        return products[product]?.module(name)
    }

    fun features(product: String, module: String): List<FeatureConfiguration> {
        return products[product]?.module(module)?.features?.values?.toList() ?: listOf()
    }

    @Suppress("unused")
    fun feature(product: String, module: String, feature: String): FeatureConfiguration? {
        return products[product]?.module(module)?.feature(feature)
    }

    fun experiments(product: String, module: String, feature: String): List<Experiment> {
        return products[product]?.module(module)?.feature(feature)?.experiments() ?: listOf()
    }

    fun experiment(
        product: String,
        module: String,
        feature: String,
        experiment: String
    ): Experiment? {

        return products[product]?.module(module)?.feature(feature)?.experiment(experiment)
    }

    fun update(config: ProductConfiguration): ApplicationConfiguration {
        val map = hashMapOf<String, ProductConfiguration>().apply {
            putAll(products)
        }
        map[config.name] = config
        return copy(products = map, version = version + 1)
    }

    fun update(config: ModuleConfiguration): ApplicationConfiguration {
        return products[config.product.name]?.update(config)?.let {
            update(it)
        } ?: this
    }

    fun update(config: FeatureConfiguration): ApplicationConfiguration {
        return products[config.module.product.name]?.update(config)?.let {
            update(it)
        } ?: this
    }

    fun update(config: Experiment) {
        var parent: Experimentable? = config.featureOrParentExperiment
        while (parent != null) {
            if (parent is Experiment) {
                parent = parent.featureOrParentExperiment
            } else if (parent is FeatureConfiguration) {
                products[parent.module.product.name]?.update(config)
                break
            }
        }
    }

    @Suppress("warnings")
    override fun validate() {
        if (name.isNullOrBlank()) {
            throw ValidateException(this, "Application configuration name should not blank")
        }
        if (!enabled) {
            throw ValidateException(this, "Application configuration: $name must be enabled")
        }
        if (version <= 0) {
            throw ValidateException(this, "Version for $name must be greater than 0")
        }
        products.forEach { it.value.validate() }
        super.validate()
    }

    override fun release() {
        //no-op
    }
}



