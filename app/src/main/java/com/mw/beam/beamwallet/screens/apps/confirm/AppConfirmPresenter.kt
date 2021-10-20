package com.mw.beam.beamwallet.screens.apps.confirm

import com.mw.beam.beamwallet.base_screen.BasePresenter

class AppConfirmPresenter(view: AppConfirmContract.View?, repository: AppConfirmContract.Repository)
    : BasePresenter<AppConfirmContract.View, AppConfirmContract.Repository>(view, repository), AppConfirmContract.Presenter {


    override fun onViewCreated() {
        super.onViewCreated()
        view?.init()
    }

    override fun hasBackArrow(): Boolean? {
        return true
    }

    override fun hasStatus(): Boolean {
        return true
    }
}