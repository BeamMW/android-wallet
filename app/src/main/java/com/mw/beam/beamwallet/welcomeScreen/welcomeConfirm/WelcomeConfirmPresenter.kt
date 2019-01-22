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

package com.mw.beam.beamwallet.welcomeScreen.welcomeConfirm

import com.mw.beam.beamwallet.baseScreen.BasePresenter

/**
 * Created by vain onnellinen on 11/1/18.
 */
class WelcomeConfirmPresenter(currentView: WelcomeConfirmContract.View, currentRepository: WelcomeConfirmContract.Repository)
    : BasePresenter<WelcomeConfirmContract.View, WelcomeConfirmContract.Repository>(currentView, currentRepository),
        WelcomeConfirmContract.Presenter {

    override fun onCreate() {
        super.onCreate()
        repository.phrases = view?.getData()
    }

    override fun onViewCreated() {
        super.onViewCreated()

        if (repository.phrases != null) {
            view?.configPhrases(repository.getPhrasesToValidate(), repository.phrases!!)
        }
    }

    override fun onPhraseChanged() {
        view?.handleNextButton()
    }

    override fun onNextPressed() {
        view?.showPasswordsFragment(repository.phrases ?: return)
    }
}
