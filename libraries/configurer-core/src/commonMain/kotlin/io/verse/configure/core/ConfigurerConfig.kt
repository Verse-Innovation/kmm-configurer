package io.verse.configure.core

import io.tagd.arch.datatype.DataObject
import io.tagd.core.ValidateException
import io.tagd.di.Global
import io.tagd.di.Scope
import io.tagd.langx.Context
import io.tagd.langx.isNull

data class ConfigurerConfig(
    val context: Context,
    val name: String,
    val outerScope: Scope = Global,
    val fetchRemoteContext: FetchRemoteConfigContext? = null,
    val accessOnDeviceContext: AccessOnDeviceConfigContext? = null,
    val defaultConfigurationResourceId: Int?,
    val autoLoad: Boolean = false
) : DataObject() {

    override fun validate() {
        val message = if (context.isNull()) {
            "Context must not be null"
        } else if (defaultConfigurationResourceId == null) {
            "Resource Id with default raw json file is required for $name library"
        } else if (name.isBlank()) {
            "Name for Configurer library must not be null for blank"
        } else {
            null
        }

        fetchRemoteContext?.validate()
        accessOnDeviceContext?.validate()

        message?.let {
            throw ValidateException(this, it)
        }
        super.validate()
    }
}