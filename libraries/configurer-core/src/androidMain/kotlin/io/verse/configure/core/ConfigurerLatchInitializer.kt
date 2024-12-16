package io.verse.configure.core

import io.tagd.arch.scopable.Scopable
import io.tagd.di.Scope
import io.tagd.langx.Context
import io.verse.latch.core.Latch
import io.verse.latch.core.LatchInitializer
import io.verse.latch.core.converter.gson.JsonCodecContentConverterFactory
import io.verse.latch.core.okhttp.OkHttpProtocolGateway

class ConfigurerLatchInitializer<S : Scopable>(
    context: Context,
    within: S,
    name: String?,
    private val config: ConfigurerConfig
) : LatchInitializer<S>(context, within, name) {


    override fun initLatch(context: Context, outerScope: Scope, name: String?): Latch {
        assertConfig()

        val fetchContext = config.fetchRemoteContext!!
        //todo connectivity, bandwidth meter

        return Latch.Builder()
            .name("configurer/latch")
            .context(context)
            .scope(outerScope)
            .addBaseUrl(fetchContext.server.scheme, fetchContext.server.host)
            .register(fetchContext.server.scheme, OkHttpProtocolGateway())
            .addPayloadConverterFactory(JsonCodecContentConverterFactory.new())
            .build()
    }

    private fun assertConfig() {
        val fetchContext = config.fetchRemoteContext ?: kotlin.run {
            val message = "${FetchRemoteConfigContext::class.simpleName} is required in  " +
                    "${ConfigurerConfig::class.simpleName} to initialize ${Latch::class.simpleName}"
            throw IllegalArgumentException(message)
        }
        assert(fetchContext.server.scheme.startsWith("https")) //todo allow all protocols
    }

    override fun release() {
        // no-op
    }
}