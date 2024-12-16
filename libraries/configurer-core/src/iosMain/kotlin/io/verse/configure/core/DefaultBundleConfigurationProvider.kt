@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.verse.configure.core

import io.tagd.arch.infra.UnifiedResource
import io.tagd.langx.Context

actual class DefaultBundleConfigurationProvider actual constructor(
    context: Context,
    unifiedResource: UnifiedResource
) : BundleConfigurationProvider {

    actual override val resource: UnifiedResource = unifiedResource

    actual override val configuration: ApplicationConfiguration?
        get() = TODO("Not yet implemented")

    actual override fun release() {
    }

}