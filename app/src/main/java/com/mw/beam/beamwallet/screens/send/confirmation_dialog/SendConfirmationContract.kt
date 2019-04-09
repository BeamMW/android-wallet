package com.mw.beam.beamwallet.screens.send.confirmation_dialog

import com.mw.beam.beamwallet.base_screen.MvpPresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView

interface SendConfirmationContract {
    interface View : MvpView {
        fun init()
        fun confirm()
        fun close()
        fun showPasswordError()
        fun clearPasswordError()
    }
    interface Presenter: MvpPresenter<View> {
        fun onPasswordChanged()
        fun onSend(password: String)
        fun onCloseDialog()
    }
    interface Repository: MvpRepository {
        fun checkPassword(password: String): Boolean
    }
}