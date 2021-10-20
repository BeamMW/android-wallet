/*
 * // Copyright 2018 Beam Development
 * //
 * // Licensed under the Apache License, Version 2.0 (the "License");
 * // you may not use this file except in compliance with the License.
 * // You may obtain a copy of the License at
 * //
 * //    http://www.apache.org/licenses/LICENSE-2.0
 * //
 * // Unless required by applicable law or agreed to in writing, software
 * // distributed under the License is distributed on an "AS IS" BASIS,
 * // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * // See the License for the specific language governing permissions and
 * // limitations under the License.
 */

package com.mw.beam.beamwallet.screens.create_password

import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.helpers.Status
import com.mw.beam.beamwallet.core.helpers.WelcomeMode
import com.mw.beam.beamwallet.core.views.PasswordStrengthView

/**
 *  10/23/18.
 */
class PasswordPresenter(currentView: PasswordContract.View, currentRepository: PasswordContract.Repository, val state: PasswordState)
    : BasePresenter<PasswordContract.View, PasswordContract.Repository>(currentView, currentRepository),
        PasswordContract.Presenter {
    private val strengthVeryWeak = Regex("(?=.+)")
    private val strengthWeak = Regex("((?=.{6,})(?=.*[0-9]))|((?=.{6,})(?=.*[A-Z]))|((?=.{6,})(?=.*[a-z]))")
    private val strengthMedium = Regex("((?=.{6,})(?=.*[A-Z])(?=.*[a-z]))|((?=.{6,})(?=.*[0-9])(?=.*[a-z]))")
    private val strengthMediumStrong = Regex("(?=.{8,})(?=.*[0-9])(?=.*[A-Z])(?=.*[a-z])")
    private val strengthStrong = Regex("(?=.{10,})(?=.*[0-9])(?=.*[A-Z])(?=.*[a-z])")
    private val strengthVeryStrong = Regex("(?=.{10,})(?=.*[!@#$%^&*])(?=.*[0-9])(?=.*[A-Z])(?=.*[a-z])")

    override fun onCreate() {
        super.onCreate()

        if (view != null) {
            if (view!!.isModeChangePass()) {
                state.isModeChangePass = true
                state.mode = WelcomeMode.CHANGE_PASS
            } else {
                state.phrases = view?.getSeed()
                state.mode = view?.getWelcomeMode()
            }
        }
    }

    override fun onViewCreated() {
        super.onViewCreated()
        view?.init(state.isModeChangePass, state.mode ?: return)
    }

    override fun onProceed() {
        if (view != null && !view!!.hasErrors()) {
            if (state.isModeChangePass) {
                if (repository.checkPass(view?.getPass())) {
                    view?.showOldPassError()
                } else {
                    repository.changePass(view?.getPass())
                    view?.completePassChanging()
                }
            } else if (state.mode != WelcomeMode.RESTORE) {
                if (AppManager.instance.wallet == null) {
                    if (Status.STATUS_OK == repository.createWallet(view?.getPass(), state.phrases?.joinToString(separator = ";", postfix = ";"), state.mode ?: WelcomeMode.CREATE)) {
                        //if somehow we get mode null here - seems to be better to apply create mode instead of restore one
                        view?.proceedToWallet(state.mode ?: WelcomeMode.CREATE, view?.getPass() ?: return, view?.getSeed() ?: return)
                    } else {
                        view?.showSnackBar(Status.STATUS_ERROR)
                    }
                }
                else {
                    repository.changePass(view?.getPass())
                    view?.proceedToWallet(state.mode ?: WelcomeMode.CREATE, view?.getPass() ?: return, view?.getSeed() ?: return)
                }
            }
            else {
                view?.showRestoreModeChoice(view?.getPass() ?: return, view?.getSeed() ?: return)
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

    override fun onBackPressed() {
        view?.showSeedAlert()
    }

    override fun onCreateNewSeed() {
        view?.showSeedFragment()
    }
}
