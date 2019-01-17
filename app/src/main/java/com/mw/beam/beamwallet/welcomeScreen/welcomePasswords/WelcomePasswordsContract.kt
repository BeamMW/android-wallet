package com.mw.beam.beamwallet.welcomeScreen.welcomePasswords

import com.mw.beam.beamwallet.baseScreen.MvpPresenter
import com.mw.beam.beamwallet.baseScreen.MvpRepository
import com.mw.beam.beamwallet.baseScreen.MvpView
import com.mw.beam.beamwallet.core.AppConfig
import com.mw.beam.beamwallet.core.views.PasswordStrengthView

/**
 * Created by vain onnellinen on 10/23/18.
 */
interface WelcomePasswordsContract {
    interface View : MvpView {
        fun hasErrors() : Boolean
        fun setStrengthLevel(strength : PasswordStrengthView.Strength)
        fun clearErrors()
        fun getPass(): String
        fun getPhrases() : Array<String>?
        fun proceedToWallet()
    }

    interface Presenter : MvpPresenter<View> {
        fun onPassChanged(pass : String?)
        fun onConfirmPassChanged()
        fun onProceed()
    }

    interface Repository : MvpRepository {
        var phrases : Array<String>?
        fun createWallet(pass: String?, phrases : String?): AppConfig.Status
    }
}
