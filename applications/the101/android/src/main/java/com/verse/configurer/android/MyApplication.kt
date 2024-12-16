package com.verse.configurer.android

import io.tagd.android.app.TagdApplication
import io.tagd.android.app.loadingstate.AppLoadingStateHandler
import io.tagd.android.app.loadingstate.AppLoadingStepDispatcher
import io.tagd.arch.control.ApplicationInjector

class MyApplication : TagdApplication() {

    override fun newLoadingStateHandler(
        dispatcher: AppLoadingStepDispatcher<out TagdApplication>
    ): AppLoadingStateHandler {

        return MyAppLoadingStateHandler(this, dispatcher)
    }

    override fun newInjector(): ApplicationInjector<out TagdApplication> {
        return MyInjector(this)
    }
}