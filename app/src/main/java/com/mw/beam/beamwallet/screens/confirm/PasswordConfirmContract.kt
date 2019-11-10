package com.mw.beam.beamwallet.screens.confirm

import com.mw.beam.beamwallet.base_screen.MvpPresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView

interface PasswordConfirmContract {
    interface View: MvpView {
        fun init(isFingerprintEnable: Boolean)
        fun showFailedFingerprint()
        fun showErrorFingerprint()
        fun showSuccessFingerprint()
        fun showEmptyPasswordError()
        fun showWrongPasswordError()
        fun clearPasswordError()
        fun close(success: Boolean)
    }

    interface Presenter: MvpPresenter<View> {
        fun onCancel()
        fun onSuccessFingerprint()
        fun onFailedFingerprint()
        fun onErrorFingerprint()
        fun onPasswordChanged()
        fun onOkPressed(password: String)
    }

    interface Repository: MvpRepository {
        fun isFingerPrintEnabled(): Boolean
        fun checkPassword(password: String): Boolean
    }
}