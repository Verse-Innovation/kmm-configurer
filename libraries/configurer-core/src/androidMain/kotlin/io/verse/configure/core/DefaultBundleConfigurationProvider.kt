@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.verse.configure.core

import io.tagd.android.resource.ContextResourceReader
import io.tagd.arch.infra.UnifiedResource
import io.tagd.arch.infra.readCompressedJson
import io.tagd.langx.Context
import io.tagd.langx.ref.WeakReference

actual class DefaultBundleConfigurationProvider actual constructor(
    context: Context,
    unifiedResource: UnifiedResource
) : BundleConfigurationProvider {

    private var weakContext: WeakReference<Context>? = WeakReference(context)

    actual override val resource: UnifiedResource = unifiedResource

    private var bundledConfiguration: ApplicationConfiguration? = null

    actual override val configuration: ApplicationConfiguration?
        get() = bundledConfiguration ?: readBundleConfiguration() ?: throw NullPointerException()

    private fun readBundleConfiguration(): ApplicationConfiguration? {
        bundledConfiguration = weakContext?.get()?.let { context ->
            val response = ContextResourceReader(context)
                .readCompressedJson<ApplicationConfigurationResponse>(resource.identifier)
            response?.configuredApplication?.toApplicationConfiguration()
        }
        return bundledConfiguration
    }

    actual override fun release() {
        weakContext?.clear()
        weakContext = null
        bundledConfiguration = null
    }

}