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

package com.mw.beam.beamwallet.screens.welcome_screen.welcome_phrase

import com.mw.beam.beamwallet.base_screen.MvpPresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView

/**
 * Created by vain onnellinen on 10/30/18.
 */
interface WelcomePhraseContract {
    interface View : MvpView {
        fun showValidationFragment(phrases: Array<String>)
        fun configPhrases(phrases: Array<String>)
        fun copyToClipboard(data: String)
        fun showCopiedAlert()
        fun showSaveAlert()
    }

    interface Presenter : MvpPresenter<View> {
        fun onNextPressed()
        fun onCopyPressed()
        fun onDonePressed()
    }

    interface Repository : MvpRepository {
        val phrases: Array<String>
    }
}