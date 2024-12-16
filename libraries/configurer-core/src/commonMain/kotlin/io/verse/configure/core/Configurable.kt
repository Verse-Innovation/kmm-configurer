package io.verse.configure.core

import io.tagd.arch.datatype.DataObject
import io.tagd.core.Nameable
import io.tagd.core.Service

interface Configurable : Service, Nameable {

    val configuration: Configuration
}

data class Configuration(
    val enabled: Boolean,
    val properties: Map<String, Any> = hashMapOf()
) : DataObject() {

    var checker: ExperimentChecker? = null

    @Suppress("UNCHECKED_CAST")
    fun <T> property(name: String, default: T?): T? {
        return (properties[name] as? T) ?: default
    }

    fun areConditionsMet(): Boolean {
        return checker?.invoke(this) ?: false
    }
}