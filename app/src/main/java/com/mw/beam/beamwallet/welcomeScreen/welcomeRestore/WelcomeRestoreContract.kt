// Copyright 2018 Beam Development
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.mw.beam.beamwallet.welcomeScreen.welcomeRestore

import com.mw.beam.beamwallet.baseScreen.MvpPresenter
import com.mw.beam.beamwallet.baseScreen.MvpRepository
import com.mw.beam.beamwallet.baseScreen.MvpView

/**
 * Created by vain onnellinen on 11/5/18.
 */
interface WelcomeRestoreContract {
    interface View : MvpView {
        fun init()
        fun configPhrases(phrasesCount : Int)
        fun handleRestoreButton()
        fun getPhrase() : Array<String>
        fun showPasswordsFragment(phrases : Array<String>)
        fun clearWindowState()
    }

    interface Presenter : MvpPresenter<View> {
        fun onRestorePressed()
        fun onPhraseChanged()
    }

    interface Repository : MvpRepository {
        fun restoreWallet() : Boolean
    }
}
