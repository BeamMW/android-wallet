package com.mw.beam.beamwallet.screens.welcome_screen.restore_trusted_node

import com.mw.beam.beamwallet.base_screen.BasePresenter

class RestoreTrustedNodePresenter(view: RestoreTrustedNodeContract.View?, repository: RestoreTrustedNodeContract.Repository)
    : BasePresenter<RestoreTrustedNodeContract.View, RestoreTrustedNodeContract.Repository>(view, repository), RestoreTrustedNodeContract.Presenter {

    override fun onViewCreated() {
        super.onViewCreated()
        view?.init()
    }


    override fun onNextPressed() {
        repository.connectToNode(view?.getNodeAddress() ?: "")
        view?.navigateToProgress()
    }
}