package com.mw.beam.beamwallet.screens.owner_key_verification

import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.helpers.FingerprintManager

class OwnerKeyVerificationPresenter(view: OwnerKeyVerificationContract.View?, repository: OwnerKeyVerificationContract.Repository)
    : BasePresenter<OwnerKeyVerificationContract.View, OwnerKeyVerificationContract.Repository>(view, repository), OwnerKeyVerificationContract.Presenter {

    override fun onViewCreated() {
        super.onViewCreated()
        view?.init(isEnableFingerprint())
    }

    override fun onFingerprintSuccess() {
        view?.navigateToOwnerKey()
    }

    override fun onFingerprintError() {
        view?.showErrorFingerprintMessage()
    }

    override fun onCancelFingerprintDialog() {
        view?.showFingerprintDescription()
    }

    override fun onNext() {
        val password = view?.getPassword()

        view?.hideFingerprintDescription()

        when {
            password.isNullOrBlank() -> view?.showEmptyPasswordError()
            repository.checkPassword(password) -> {
                if (isEnableFingerprint()) {
                    view?.showFingerprintDialog()
                } else {
                    view?.navigateToOwnerKey()
                }
            }
            else -> view?.showWrongPasswordError()
        }
    }

    private fun isEnableFingerprint(): Boolean {
        return repository.isEnableFingerprint() && FingerprintManager.SensorState.READY == FingerprintManager.checkSensorState(view?.getContext() ?: return false)
    }

    override fun onChangePassword() {
        view?.clearPasswordError()
    }
}