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

package com.mw.beam.beamwallet.screens.welcome_screen.welcome_create

import com.mw.beam.beamwallet.base_screen.MvpPresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.helpers.LocaleHelper

/**
 *  12/4/18.
 */
interface WelcomeCreateContract {
    interface View : MvpView {
        fun createWallet()
        fun restoreWallet()
        fun hasBackArrow(): Boolean
        fun back()
        fun finish()
        fun setupLanguageButton(currentLanguage: LocaleHelper.SupportedLanguage)
        fun navigateToLanguageSettings()
    }

    interface Presenter : MvpPresenter<View> {
        fun onCreateWallet()
        fun onRestoreWallet()
        fun onBackPressed()
        fun onChangeLanguagePressed()
    }

    interface Repository : MvpRepository {
        fun isUnfinishedRestore(): Boolean
        fun clearAllData()
        fun getCurrentLanguage(): LocaleHelper.SupportedLanguage
    }
}
