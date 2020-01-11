package com.mw.beam.beamwallet.screens.confirm

import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.helpers.DelayedTask

class PasswordConfirmPresenter(view: PasswordConfirmContract.View?, repository: PasswordConfirmContract.Repository)
    : BasePresenter<PasswordConfirmContract.View, PasswordConfirmContract.Repository>(view, repository), PasswordConfirmContract.Presenter {

    private var isSuccess = false

    override fun onViewCreated() {
        super.onViewCreated()
        view?.init(repository.isFingerPrintEnabled() || repository.isFaceIDEnabled())
    }

    override fun onCancel() {
        view?.close(false)
    }

    override fun onFailedFingerprint() {
        if (!isSuccess) {
            view?.showFailedFingerprint()
        }
    }

    override fun onErrorFingerprint() {
        if (!isSuccess) {
            view?.showErrorFingerprint()
        }
    }

    override fun onOkPressed(password: String) {
        when {
            repository.checkPassword(password) -> {
                isSuccess = true
                view?.close(true)
            }
            password.isBlank() -> view?.showEmptyPasswordError()
            else -> view?.showWrongPasswordError()
        }
    }

    override fun onPasswordChanged() {
        view?.clearPasswordError()
    }

    override fun onSuccessFingerprint() {
        isSuccess = true
        view?.showSuccessFingerprint()
        DelayedTask.startNew(1, {  view?.close(true) })
    }
}