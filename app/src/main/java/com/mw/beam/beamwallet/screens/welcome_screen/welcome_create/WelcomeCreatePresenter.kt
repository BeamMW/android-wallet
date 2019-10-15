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

import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.AppConfig
import java.io.File

/**
 *  12/4/18.
 */
class WelcomeCreatePresenter(currentView: WelcomeCreateContract.View, currentRepository: WelcomeCreateContract.Repository)
    : BasePresenter<WelcomeCreateContract.View, WelcomeCreateContract.Repository>(currentView, currentRepository),
        WelcomeCreateContract.Presenter {

    override fun onStart() {
        super.onStart()

        if (repository.isUnfinishedRestore()) {
            repository.clearAllData()
        }

        view?.setupLanguageButton(repository.getCurrentLanguage())
    }

    override fun onBackPressed() {
        if (hasBackArrow() == true) {
            view?.back()
        } else {
            view?.finish()
        }
    }

    override fun onChangeLanguagePressed() {
        view?.navigateToLanguageSettings()
    }

    override fun onCreateWallet() {
        view?.createWallet()
    }

    override fun onRestoreWallet() {
        view?.restoreWallet()
    }

    override fun hasBackArrow(): Boolean? = (view?.hasBackArrow() ?: false) && !repository.isUnfinishedRestore()
}
