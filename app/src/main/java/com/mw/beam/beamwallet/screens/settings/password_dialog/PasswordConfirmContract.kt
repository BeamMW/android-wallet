package com.mw.beam.beamwallet.screens.settings.password_dialog

import com.mw.beam.beamwallet.base_screen.MvpPresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView

interface PasswordConfirmContract {
    interface View: MvpView {
        fun init()
        fun confirm()
        fun cancel()
        fun showEmptyPasswordError()
        fun showWrongPasswordError()
        fun clearPasswordError()
    }

    interface Presenter: MvpPresenter<View> {
        fun onPasswordChanged()
        fun onConfirm(password: String)
        fun onCancelDialog()
    }

    interface Repository: MvpRepository {
        fun checkPassword(password: String): Boolean
    }
}