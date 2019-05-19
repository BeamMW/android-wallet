package com.mw.beam.beamwallet.screens.send.confirmation_dialog

import com.mw.beam.beamwallet.base_screen.MvpPresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView

interface SendConfirmationContract {

    interface View : MvpView {
        fun init(token: String?, amount: Double?, fee: Long?, shouldInitFingerprint: Boolean)
        fun getToken(): String?
        fun getAmount(): Double?
        fun getFee(): Long?
        fun confirm()
        fun close()
        fun showWrongPasswordError()
        fun clearPasswordError()
        fun showEmptyPasswordError()
        fun showFingerprintAuthError()
    }

    interface Presenter: MvpPresenter<View> {
        fun onPasswordChanged()
        fun onSend(password: String)
        fun onCloseDialog()
        fun onFingerprintError()
        fun onFingerprintSucceeded()
        fun onFingerprintFailed()
    }

    interface Repository: MvpRepository {
        fun checkPassword(password: String): Boolean
        fun isFingerPrintEnabled(): Boolean
    }
}