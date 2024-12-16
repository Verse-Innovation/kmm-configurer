package io.verse.configure.core

import io.tagd.arch.datatype.DataObject
import io.tagd.core.ValidateException
import io.tagd.langx.time.Millis
import io.verse.latch.core.Api
import io.verse.latch.core.Latch
import io.verse.latch.core.Server

data class FetchRemoteConfigContext(
    val server: Server,
    val api: Api,
    val syncFrequency: Millis,
    val latch: Latch? = null,
) : DataObject() {

    override fun validate() {
        val message = if (syncFrequency.millis < 1000L) {
            "SyncFrequency (in Ms) must be greater than 1000 milliseconds"
        } else {
            server.validate()
            api.validate()
            null
        }
        message?.let {
            throw ValidateException(this, it)
        }
        super.validate()
    }
}