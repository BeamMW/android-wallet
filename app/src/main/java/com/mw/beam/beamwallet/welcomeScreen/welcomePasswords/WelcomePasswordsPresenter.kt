package com.mw.beam.beamwallet.welcomeScreen.welcomePasswords

import com.mw.beam.beamwallet.baseScreen.BasePresenter
import com.mw.beam.beamwallet.core.AppConfig
import com.mw.beam.beamwallet.core.views.PasswordStrengthView

/**
 * Created by vain onnellinen on 10/23/18.
 */
class WelcomePasswordsPresenter(currentView: WelcomePasswordsContract.View, private val repository: WelcomePasswordsContract.Repository)
    : BasePresenter<WelcomePasswordsContract.View>(currentView),
        WelcomePasswordsContract.Presenter {
    private val strengthVeryWeak = Regex("(?=.+)")
    private val strengthWeak = Regex("((?=.{6,})(?=.*[0-9]))|((?=.{6,})(?=.*[A-Z]))|((?=.{6,})(?=.*[a-z]))")
    private val strengthMedium = Regex("((?=.{6,})(?=.*[A-Z])(?=.*[a-z]))|((?=.{6,})(?=.*[0-9])(?=.*[a-z]))")
    private val strengthMediumStrong = Regex("(?=.{8,})(?=.*[0-9])(?=.*[A-Z])(?=.*[a-z])")
    private val strengthStrong = Regex("(?=.{10,})(?=.*[0-9])(?=.*[A-Z])(?=.*[a-z])")
    private val strengthVeryStrong = Regex("(?=.{10,})(?=.*[!@#$%^&*])(?=.*[0-9])(?=.*[A-Z])(?=.*[a-z])")

    override fun viewIsReady() {
        view?.init()
    }

    override fun onProceed() {
        if (view != null && !view!!.hasErrors()) {
            if (AppConfig.Status.STATUS_OK == repository.createWallet(view?.getPass())) {
                view?.proceedToWallet()
            } else {
                view?.showSnackBar(AppConfig.Status.STATUS_ERROR)
            }
        }
    }

    override fun onPassChanged(pass: String?) {
        view?.clearErrors()

        if (pass == null) {
            view?.setStrengthLevel(PasswordStrengthView.Strength.EMPTY)
        } else {
            view?.setStrengthLevel(
                    when (true) {
                        strengthVeryStrong.containsMatchIn(pass) -> PasswordStrengthView.Strength.VERY_STRONG
                        strengthStrong.containsMatchIn(pass) -> PasswordStrengthView.Strength.STRONG
                        strengthMediumStrong.containsMatchIn(pass) -> PasswordStrengthView.Strength.MEDIUM_STRONG
                        strengthMedium.containsMatchIn(pass) -> PasswordStrengthView.Strength.MEDIUM
                        strengthWeak.containsMatchIn(pass) -> PasswordStrengthView.Strength.WEAK
                        strengthVeryWeak.containsMatchIn(pass) -> PasswordStrengthView.Strength.VERY_WEAK
                        else -> PasswordStrengthView.Strength.EMPTY
                    }
            )
        }
    }

    override fun onConfirmPassChanged() {
        view?.clearErrors()
    }

    override fun onChangePassVisibility(shouldShow: Boolean) {
        view?.changePassVisibility(shouldShow)
    }
}
