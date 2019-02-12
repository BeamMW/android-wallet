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

package com.mw.beam.beamwallet.screens.welcome_screen.welcome_passwords

import com.mw.beam.beamwallet.base_screen.MvpPresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.helpers.Status
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
        fun getSeed() : Array<String>?
        fun proceedToWallet()
        fun showSeedAlert()
        fun showSeedFragment()
    }

    interface Presenter : MvpPresenter<View> {
        fun onPassChanged(pass : String?)
        fun onConfirmPassChanged()
        fun onProceed()
        fun onBackPressed()
        fun onCreateNewSeed()
    }

    interface Repository : MvpRepository {
        var phrases : Array<String>?
        fun createWallet(pass: String?, phrases : String?): Status
    }
}
