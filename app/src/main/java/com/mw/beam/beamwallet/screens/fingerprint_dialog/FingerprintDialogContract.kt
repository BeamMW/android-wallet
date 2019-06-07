package com.mw.beam.beamwallet.screens.fingerprint_dialog

import com.mw.beam.beamwallet.base_screen.MvpPresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView

interface FingerprintDialogContract {

    interface View: MvpView {
        fun init()
        fun error()
        fun cancel()
        fun success()
        fun showFailed()
        fun clearFingerprintCallback()
    }

    interface Presenter: MvpPresenter<View> {
        fun onCancel()
        fun onSuccess()
        fun onFailed()
        fun onError()
    }

    interface Repository: MvpRepository
}