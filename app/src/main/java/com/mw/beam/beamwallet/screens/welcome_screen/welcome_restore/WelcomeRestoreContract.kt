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

package com.mw.beam.beamwallet.screens.welcome_screen.welcome_restore

import com.mw.beam.beamwallet.base_screen.MvpPresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView

/**
 *  11/5/18.
 */
interface WelcomeRestoreContract {
    interface View : MvpView {
        fun init()
        fun configSeed(phrasesCount : Int)
        fun handleRestoreButton()
        fun getSeed() : Array<String>
        fun showPasswordsFragment(seed : Array<String>)
        fun initSuggestions(suggestions: List<String>)
        fun setTextToCurrentView(text: String)
        fun updateSuggestions(text: String)
        fun clearSuggestions()
        fun showRestoreNotification()
    }

    interface Presenter : MvpPresenter<View> {
        fun onRestorePressed()
        fun onSeedChanged(seed: String)
        fun onSuggestionClick(text: String)
        fun onSeedFocusChanged(seed: String, hasFocus: Boolean)
        fun onValidateSeed(seed: String?): Boolean
        fun onUnderstandPressed()
    }

    interface Repository : MvpRepository {
        fun restoreWallet() : Boolean
        fun getSuggestions(): List<String>
    }
}
