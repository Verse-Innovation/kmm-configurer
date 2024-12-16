package io.verse.configure.core

import io.tagd.arch.domain.crosscutting.codec.SerializedName
import io.verse.storage.core.FilableDataObject

data class ApplicationConfigurationResponse(
    @SerializedName("application", [])
    val configuredApplication: ApplicationConfigurationDto? = null,
) : FilableDataObject()