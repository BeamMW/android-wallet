package com.mw.beam.beamwallet.screens.owner_key_verification

import android.content.Context
import com.mw.beam.beamwallet.base_screen.MvpPresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView

interface OwnerKeyVerificationContract {

    interface View: MvpView {
        fun init(isEnableFingerprint: Boolean)
        fun getPassword(): String
        fun getContext(): Context?
        fun showFingerprintDialog()
        fun showEmptyPasswordError()
        fun showWrongPasswordError()
        fun showErrorFingerprintMessage()
        fun clearPasswordError()
        fun showFingerprintDescription()
        fun hideFingerprintDescription()
        fun navigateToOwnerKey()
    }

    interface Presenter: MvpPresenter<View> {
        fun onChangePassword()
        fun onNext()
        fun onFingerprintSuccess()
        fun onFingerprintError()
        fun onCancelFingerprintDialog()
    }

    interface Repository: MvpRepository {
        fun checkPassword(pass: String): Boolean
        fun isEnableFingerprint(): Boolean
    }
}