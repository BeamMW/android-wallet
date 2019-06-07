package com.mw.beam.beamwallet.screens.fingerprint_dialog

import com.mw.beam.beamwallet.base_screen.BasePresenter

class FingerprintDialogPresenter(view: FingerprintDialogContract.View?, repository: FingerprintDialogContract.Repository)
    : BasePresenter<FingerprintDialogContract.View, FingerprintDialogContract.Repository>(view, repository), FingerprintDialogContract.Presenter {


    override fun onStart() {
        super.onStart()
        view?.init()
    }

    override fun onCancel() {
        view?.cancel()
    }

    override fun onError() {
        view?.error()
    }

    override fun onFailed() {
        view?.showFailed()
    }

    override fun onSuccess() {
        view?.success()
    }

    override fun onStop() {
        view?.clearFingerprintCallback()
        super.onStop()
    }
}