package io.verse.configure.core

import io.tagd.arch.datatype.DataObject
import io.tagd.core.ValidateException
import io.tagd.langx.isNull
import io.verse.storage.core.Storage

data class AccessOnDeviceConfigContext(
    val storedConfigPath: String,
    val storage: Storage? = null,
) : DataObject() {

    override fun validate() {
        if (storedConfigPath.isNull() || storedConfigPath.isBlank()) {
            throw ValidateException(this, "storedConfigPath must not be null or blank")
        }
        super.validate()
    }
}