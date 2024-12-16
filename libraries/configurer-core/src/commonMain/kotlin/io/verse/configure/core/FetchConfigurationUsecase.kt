package io.verse.configure.core

import io.tagd.arch.domain.usecase.Args
import io.tagd.arch.domain.usecase.LiveUseCase
import io.tagd.arch.scopable.library.repository
import io.tagd.core.BidirectionalDependentOn

class FetchConfigurationUsecase : LiveUseCase<ApplicationConfiguration>(),
    BidirectionalDependentOn<Configurer> {

    private var repository: IConfigurationRepository? = null

    override fun injectBidirectionalDependent(other: Configurer) {
        repository = other.repository()
    }

    override fun trigger(args: Args) {
        repository?.fetchConfiguration(success = {
            setValue(args, it)
        }, failure = {
            setError(args, it)
        })
    }

    override fun release() {
        repository = null
        super.release()
    }
}