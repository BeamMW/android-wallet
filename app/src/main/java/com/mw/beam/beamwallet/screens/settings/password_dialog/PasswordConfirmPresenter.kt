package com.mw.beam.beamwallet.screens.settings.password_dialog

import com.mw.beam.beamwallet.base_screen.BasePresenter

class PasswordConfirmPresenter(view: PasswordConfirmContract.View?, repository: PasswordConfirmContract.Repository)
    : BasePresenter<PasswordConfirmContract.View, PasswordConfirmContract.Repository>(view, repository), PasswordConfirmContract.Presenter {

    override fun onConfirm(password: String) {
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

    override fun onCancelDialog() {
        view?.cancel()
    }

    override fun onPasswordChanged() {
        view?.clearPasswordError()
    }
}