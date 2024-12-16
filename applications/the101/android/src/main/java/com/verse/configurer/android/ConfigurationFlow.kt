package com.verse.configurer.android

import io.tagd.arch.flow.Flow
import io.tagd.core.Releasable
import io.verse.configure.core.ConfigurationChangeObserver

interface ConfigurationFlow : Flow, ConfigurationChangeObserver, Releasable