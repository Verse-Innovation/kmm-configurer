package com.verse.configurer.android

import io.tagd.android.app.TagdApplicationInjector
import io.tagd.arch.scopable.WithinScopableInitializer

class MyInjector(application: MyApplication) : TagdApplicationInjector<MyApplication>(application) {

    override fun load(initializers: ArrayList<WithinScopableInitializer<MyApplication, *>>) {
        super.load(initializers)
        initializers.add(MyConfigurationInitializer(within))
    }
}
