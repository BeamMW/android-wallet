package com.mw.beam.beamwallet.screens.send.confirmation_dialog

import com.mw.beam.beamwallet.base_screen.BasePresenter

class SendConfirmationPresenter(view: SendConfirmationContract.View?, repository: SendConfirmationContract.Repository)
    : BasePresenter<SendConfirmationContract.View, SendConfirmationContract.Repository>(view, repository),
        SendConfirmationContract.Presenter {

    override fun onViewCreated() {
        super.onViewCreated()
        view?.apply {
            init(getToken(), getAmount(), getFee())
        }
    }

    override fun onPasswordChanged() {
        view?.clearPasswordError()
    }

    override fun onSend(password: String) {
        if (!hasError(password)) {
            view?.confirm()
        }
    }

    private fun hasError(password: String): Boolean {
        var hasError = false

        if (password.isBlank()) {
            view?.showEmptyPasswordError()
            hasError = true
        }

        if (password.isNotBlank() && !repository.checkPassword(password)) {
            view?.showWrongPasswordError()
            hasError = true
        }

        return hasError
    }

    override fun onCloseDialog() {
        view?.close()
    }
}