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

import com.mw.beam.beamwallet.baseScreen.BasePresenter

/**
 * Created by vain onnellinen on 11/5/18.
 */
class WelcomeRestorePresenter(currentView: WelcomeRestoreContract.View, currentRepository: WelcomeRestoreContract.Repository, private val state: WelcomeRestoreState)
    : BasePresenter<WelcomeRestoreContract.View, WelcomeRestoreContract.Repository>(currentView, currentRepository),
        WelcomeRestoreContract.Presenter {

    override fun onStart() {
        super.onStart()
        view?.init()
        view?.configPhrases(state.phrasesCount)
    }

    override fun onStop() {
        view?.clearWindowState()
        super.onStop()
    }

    override fun onRestorePressed() {
        view?.showPasswordsFragment(view?.getPhrase() ?: return)
    }

    override fun onPhraseChanged() {
        view?.handleRestoreButton()
    }
}
