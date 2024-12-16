package io.verse.configure.core

import com.google.gson.Gson
import io.tagd.android.resource.JavaResourceReader
import io.tagd.arch.infra.NamedResource
import org.junit.Test

class ExperimentableTest {

    private lateinit var featureConfiguration: FeatureConfiguration

    init {
        val localConfigResponseString = JavaResourceReader()
            .readNamed(NamedResource("default_application_configuration.json"))

        val appConfig = Gson().fromJson(
            localConfigResponseString,
            ApplicationConfigurationResponse::class.java
        ).configuredApplication?.toApplicationConfiguration()

        appConfig?.let {
            val module = it.module("default", "home module")!!
            featureConfiguration = module.feature("pizza list card")!!
        }
    }

    @Test
    fun `given non existing experiment without default then verify no flow is executed`() {
        var flow = "empty flow"

        featureConfiguration
            .ifHas(experiment = "non existing experiment", checker = { true }) {
                flow = "non existing experiment flow"
            }.trigger()

        assert(flow == "empty flow")
    }

    @Test
    fun `given non existing experiment with default then verify default flow is executed`() {
        var flow = "empty flow"

        featureConfiguration
            .ifHas(experiment = "non existing experiment", checker = { true }) {
                flow = "non existing experiment flow"
            }.orDefault {
                flow = "default flow"
            }.trigger()

        assert(flow == "default flow")
    }

    @Test
    fun `given an existing experiment without default then verify experiment's flow is executed`() {
        var flow = "empty flow"

        featureConfiguration
            .ifHas(experiment = "full page card", checker = { true }) {
                flow = "full page card"
            }.trigger()

        assert(flow == "full page card")
    }

    @Test
    fun `given an existing experiment with default then verify experiment's flow is executed`() {
        var flow = "empty flow"

        featureConfiguration
            .ifHas(experiment = "two cards per page", checker = { true }) {
                flow = "two cards per page"
            }.orDefault {
                flow = "default flow"
            }.trigger()

        assert(flow == "two cards per page")
    }

    @Test
    fun `given multiple experiments without default when found then verify its flow is executed`() {
        var flow = "empty flow"

        featureConfiguration
            .ifHas(experiment = "full page card", checker = { false }) {
                flow = "full page card"
            }.orElseIf(experiment = "two cards per page", checker = { true }) {
                flow = "two cards per page"
            }.trigger()

        assert(flow == "two cards per page")
    }

    @Test
    fun `given multiple experiments with default when found then verify its flow is executed`() {
        var flow = "empty flow"

        featureConfiguration
            .ifHas(experiment = "full page card", checker = { false }) {
                flow = "full page card"
            }.orElseIf(experiment = "two cards per page", checker = { true }) {
                flow = "two cards per page"
            }.orDefault {
                flow = "default flow"
            }.trigger()

        assert(flow == "two cards per page")
    }

    @Test
    fun `given multiple experiments with default when not found then verify default is executed`() {
        var flow = "empty flow"

        featureConfiguration
            .ifHas(experiment = "non existing experiment 1", checker = { false }) {
                flow = "full page card"
            }.orElseIf(experiment = "non existing experiment 2", checker = { true }) {
                flow = "two cards per page"
            }.orDefault {
                flow = "default flow"
            }.trigger()

        assert(flow == "default flow")
    }

    @Test
    fun `given experiments & default when conditions aren't met then verify default is executed`() {
        var flow = "empty flow"

        featureConfiguration
            .ifHas(experiment = "full page card 123", checker = { false }) {
                flow = "full page card"
            }.orElseIf(experiment = "two cards per page 123", checker = { false }) {
                flow = "two cards per page"
            }.orDefault {
                flow = "default flow"
            }.trigger()

        assert(flow == "default flow")
    }

    @Test
    fun `given disabled experiment without default then verify no flow is executed`() {
        var flow = "empty flow"

        featureConfiguration
            .ifHas(experiment = "two cards per page", checker = { it.enabled }) {
                flow = "two cards per page"
            }.trigger()

        assert(flow == "empty flow")
    }

    @Test
    fun `given enabled experiment without default then verify it's flow is executed`() {
        var flow = "empty flow"

        featureConfiguration
            .ifHas(experiment = "full page card", checker = { it.enabled }) {
                flow = "full page card"
            }.trigger()

        assert(flow == "full page card")
    }

    @Test
    fun `given an experiment then verify it's flow is executed only upon meeting its extras`() {
        var flow = "empty flow"
        val maxTimes = 3.0
        val minAppLaunches = 2.0

        featureConfiguration
            .ifHas(experiment = "full page card", checker = {
                val properties = it.properties["properties"] as Map<*, *>
                val maxTimesExtra = properties["max_times"] as? Double
                val minAppLaunchesExtra = properties["min_app_launches"] as? Double
                println("min max $maxTimesExtra, $minAppLaunchesExtra, ${it.properties}")
                maxTimes == maxTimesExtra && minAppLaunches == minAppLaunchesExtra
            }) {
                flow = "full page card"
            }.trigger()

        assert(flow == "full page card")
    }

    @Test
    fun `given an experiment when its extras is not met then verify no flow is executed`() {
        var flow = "empty flow"
        val maxTimes = 4.0
        val minAppLaunches = 2.0

        featureConfiguration
            .ifHas(experiment = "full page card", checker = {
                val maxTimesExtra = it.properties["max_times"] as? Double
                val minAppLaunchesExtra = it.properties["min_app_launches"] as? Double
                maxTimes == maxTimesExtra && minAppLaunches == minAppLaunchesExtra
            }) {
                flow = "full page card"
            }.trigger()

        assert(flow == "empty flow")
    }

    @Test
    fun `given child experiment without default when found then verify it's flow is executed`() {
        var flow = "empty flow"

        featureConfiguration
            .ifHas(experiment = "full page card", checker = { false }) {
                flow = "full page card"
            }.orElseIf(experiment = "three cards per page", checker = { true }) {
                it.ifHas(experiment = "show footer", checker = { true }) {
                    flow = "show footer"
                }.trigger()
            }.trigger()

        assert(flow == "show footer")
    }

    @Test
    fun `given child experiment with default when not found then verify its default is executed`() {
        var flow = "empty flow"

        featureConfiguration
            .ifHas(experiment = "full page card", checker = { false }) {
                flow = "full page card"
            }.orElseIf(experiment = "three cards per page", checker = { true }) {
                it.ifHas(experiment = "non existing child experiment", checker = { true }) {
                    flow = "show footer"
                }.orDefault {
                    flow = "child default flow"
                }.trigger()
                // default
            }.trigger()

        assert(flow == "child default flow")
    }

}