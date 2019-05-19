package com.mw.beam.beamwallet.screens.send.confirmation_dialog

import com.mw.beam.beamwallet.base_screen.BasePresenter

class SendConfirmationPresenter(view: SendConfirmationContract.View?, repository: SendConfirmationContract.Repository)
    : BasePresenter<SendConfirmationContract.View, SendConfirmationContract.Repository>(view, repository),
        SendConfirmationContract.Presenter {
    private val VIBRATION_LENGTH: Long = 100

    override fun onViewCreated() {
        super.onViewCreated()
        view?.apply {
            init(getToken(), getAmount(), getFee(), repository.isFingerPrintEnabled())
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

    override fun onFingerprintError() {
        view?.showFingerprintAuthError()
    }

    override fun onFingerprintSucceeded() {
        view?.confirm()
    }

    override fun onFingerprintFailed() {
        view?.vibrate(VIBRATION_LENGTH)
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