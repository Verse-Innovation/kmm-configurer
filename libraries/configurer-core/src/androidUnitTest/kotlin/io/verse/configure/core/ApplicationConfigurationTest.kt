package io.verse.configure.core

import com.google.gson.Gson
import io.tagd.android.resource.JavaResourceReader
import io.tagd.arch.infra.NamedResource
import io.tagd.core.ValidateException
import org.junit.Test
import kotlin.test.assertEquals

class ApplicationConfigurationTest {

    private lateinit var applicationConfiguration: ApplicationConfiguration

    init {
        val localConfigResponseString = JavaResourceReader()
            .readNamed(NamedResource("default_application_configuration.json"))

        val appConfig = Gson().fromJson(
            localConfigResponseString,
            ApplicationConfigurationResponse::class.java
        )

        appConfig.configuredApplication?.toApplicationConfiguration()?.let {
            applicationConfiguration = it
        }
    }

    @Test
    fun `given application configuration DTO then verify domain is Valid`() {
        val localConfigResponseString = JavaResourceReader()
            .readNamed(NamedResource("default_application_configuration.json"))

        val appConfig = Gson().fromJson(
            localConfigResponseString,
            ApplicationConfigurationResponse::class.java
        )

        appConfig.configuredApplication?.toApplicationConfiguration()
    }

    @Test
    fun `given application configuration then verify it is notnull`() {
        assert(::applicationConfiguration.isInitialized)
    }

    @Test
    fun `given application configuration then verify it is valid`() {
        try {
            applicationConfiguration.validate()
            assert(true)
        } catch (e: ValidateException) {
            assert(false) { e.message ?: "$e" }
        }
    }

    @Test
    fun `given application configuration then verify it has the expected product`() {
        // given
        val productName = "default"
        val homeModule = applicationConfiguration.product(productName)

        // then execute
        val hasModule = applicationConfiguration.hasProduct(productName)

        // verify
        assert(hasModule && homeModule != null)
    }

    @Test
    fun `given application configuration then verify it doesn't have unexpected product`() {
        // given
        val productName = "some unexpected product"

        // then execute
        val hasModule = applicationConfiguration.hasProduct(productName)

        // verify
        assert(!hasModule)
    }

    @Test
    fun `given product configuration then verify it has the expected module`() {
        // given
        val product = applicationConfiguration.product("default")!!
        val moduleName = "home module"
        val module = product.module(moduleName)

        // then execute
        val hasModule = product.hasModule(moduleName)

        // verify
        assert(hasModule && module != null)
    }

    @Test
    fun `given product configuration then verify it doesn't have unexpected module`() {
        // given
        val product = applicationConfiguration.product("default")

        // then execute
        val hasModule = product!!.hasModule("unexpected module")

        // verify
        assert(!hasModule)
    }

    @Test
    fun `given module configuration then verify it has the expected feature`() {
        // given
        val module = applicationConfiguration.module("default", "home module")
        val featureName = "pizza list card"
        val feature = module!!.feature(featureName)

        // then execute
        val hasFeature = module.hasFeature(featureName)

        // verify
        assert(hasFeature && feature != null)
    }

    @Test
    fun `given module configuration then verify it doesn't have unexpected feature`() {
        // given
        val module = applicationConfiguration.module("default", "home module")

        // then execute
        val hasFeature = module!!.hasFeature("unexpected feature")

        // verify
        assert(!hasFeature)
    }

    @Test
    fun `given feature configuration then verify it has the expected experiment`() {
        // given
        val module = applicationConfiguration.module("default", "home module")
        val feature = module!!.feature("pizza list card")
        val experimentName = "three cards per page"
        val experiment = feature!!.experiment(experimentName)

        // then execute
        val hasExperiment = feature.hasExperiment(experimentName)

        // verify
        assert(hasExperiment && experiment != null)
    }

    @Test
    fun `given feature configuration then verify it doesn't have unexpected experiment`() {
        // given
        val module = applicationConfiguration.module("default", "home module")
        val feature = module!!.feature("pizza list card")

        // then execute
        val hasExperiment = !feature!!.hasExperiment("unexpected experiment")

        // verify
        assert(hasExperiment)
    }

    @Test
    fun `given experiment then verify it has the expected child experiment`() {
        // given
        val module = applicationConfiguration.module("default", "home module")
        val feature = module!!.feature("pizza list card")
        val experiment = feature!!.experiment("three cards per page")

        val childExperimentName = "show footer"
        val childExperiment = experiment!!.experiment(childExperimentName)

        // expected
        val expectedChildrenCount = 2

        // actual
        val actualChildrenCount = experiment.experiments().size
        val hasChildExperiment = experiment.hasExperiment(childExperimentName)

        // verify
        assert(hasChildExperiment && childExperiment != null)
        assertEquals(expectedChildrenCount, actualChildrenCount)
    }

    @Test
    fun `given experiment then verify it doesn't have unexpected child experiment`() {
        // given
        val module = applicationConfiguration.module("default", "home module")
        val feature = module!!.feature("pizza list card")
        val experiment = feature!!.experiment("three cards per page")

        // then execute
        val childExperimentName = "unexpected child experiment"
        val hasChildExperiment = experiment!!.hasExperiment(childExperimentName)

        // verify
        assert(!hasChildExperiment)
    }

    @Test
    fun `given module configuration then verify it is enabled`() {
        // given
        val module = applicationConfiguration.module("default", "home module")!!

        // then execute
        val isConfigEnabled = module.enabled

        // verify
        assert(isConfigEnabled)
    }

    @Test
    fun `given experiment then verify it is disabled`() {
        // given
        val module = applicationConfiguration.module("default", "home module")
        val experiment = module?.experiment("pizza list card", "two cards per page")!!

        // then execute
        val isExperimentConfigDisabled = !experiment.configuration.enabled

        // verify
        assert(isExperimentConfigDisabled)
    }

    @Test
    fun `given feature configuration then verify it is enabled`() {
        // given
        val module = applicationConfiguration.module("default", "home module")
        val feature = module!!.feature("pizza list card")

        // then execute
        val isFeatureConfigEnabled = feature?.configuration?.enabled == true

        // verify
        assert(isFeatureConfigEnabled)
    }

    @Test
    fun `given configuration then verify it is expected extras`() {
        // given
        val module = applicationConfiguration.module("default", "home module")
        val feature = module!!.feature("pizza list card")
        val experiment = feature!!.experiment("full page card")!!

        // expected
        val expectedExtrasCount = 2
        val expectedProperties = mapOf(
            "max_times" to 3.0,
            "min_app_launches" to 2.0
        )

        // then execute
        val actualExtrasSize = experiment.configuration.properties.size
        val actualProperties = experiment.configuration.properties["properties"] as? Map<*, *>

        // verify
        assertEquals(expectedExtrasCount, actualExtrasSize)
        assertEquals(expectedProperties, actualProperties)
    }

}