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

package com.mw.beam.beamwallet.screens.welcome_screen

import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.helpers.WelcomeMode

/**
 * Created by vain onnellinen on 10/19/18.
 */
class WelcomePresenter(currentView: WelcomeContract.View, currentRepository: WelcomeContract.Repository)
    : BasePresenter<WelcomeContract.View, WelcomeContract.Repository>(currentView, currentRepository),
        WelcomeContract.Presenter {

    override fun onCreate() {
        super.onCreate()
        view?.finishNotRootTask()
    }

    override fun onViewCreated() {
        super.onViewCreated()

        if (repository.isWalletInitialized()) {
            view?.showOpenFragment()
        } else {
            view?.showCreateFragment()
        }
    }

    override fun onCreateWallet() {
        view?.showDescriptionFragment()
    }

    override fun onGenerateSeed() {
        view?.showSeedFragment()
    }

    override fun isLockScreenEnabled(): Boolean = false

    override fun onOpenWallet(mode: WelcomeMode, pass: String, seed: Array<String>?) {
        when (mode) {
            WelcomeMode.OPEN, WelcomeMode.RESTORE-> view?.showProgressFragment(mode, pass, seed)
            WelcomeMode.CREATE -> view?.showMainActivity()
        }
    }

    override fun onProceedToPasswords(phrases: Array<String>, mode: WelcomeMode) {
        view?.showPasswordsFragment(phrases, mode)
    }

    override fun onProceedToValidation(phrases: Array<String>) {
        view?.showValidationFragment(phrases)
    }

    override fun onRestoreWallet() {
        view?.showRestoreFragment()
    }

    override fun onChangeWallet() {
        view?.showCreateFragment()
    }

    override fun onShowWallet() {
        view?.showMainActivity()
    }

    override fun hasBackArrow(): Boolean? = false
}
