package com.mw.beam.beamwallet.screens.save_address

import com.mw.beam.beamwallet.base_screen.BasePresenter

class SaveAddressPresenter(view: SaveAddressContract.View?, repository: SaveAddressContract.Repository)
    : BasePresenter<SaveAddressContract.View, SaveAddressContract.Repository>(view, repository), SaveAddressContract.Presenter {

    override fun onViewCreated() {
        super.onViewCreated()
        view?.apply {
            init(getAddress())
        }
    }

    override fun onSavePressed() {
        view?.close()
    }

    override fun onCancelPressed() {
        view?.close()
    }

}