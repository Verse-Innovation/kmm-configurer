@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.verse.configure.core

import io.tagd.arch.infra.UnifiedResource
import io.tagd.langx.Context

expect class DefaultBundleConfigurationProvider(
    context: Context,
    unifiedResource: UnifiedResource
) : BundleConfigurationProvider {

    override val resource: UnifiedResource

    override val configuration: ApplicationConfiguration?

    override fun release()
}