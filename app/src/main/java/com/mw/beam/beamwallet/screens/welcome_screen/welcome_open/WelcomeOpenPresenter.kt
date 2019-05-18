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

package com.mw.beam.beamwallet.screens.welcome_screen.welcome_open

import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.helpers.PreferencesManager
import com.mw.beam.beamwallet.core.helpers.Status

/**
 * Created by vain onnellinen on 10/19/18.
 */
class WelcomeOpenPresenter(currentView: WelcomeOpenContract.View, currentRepository: WelcomeOpenContract.Repository)
    : BasePresenter<WelcomeOpenContract.View, WelcomeOpenContract.Repository>(currentView, currentRepository),
        WelcomeOpenContract.Presenter {
    private val VIBRATION_LENGTH: Long = 100

    override fun onStart() {
        super.onStart()
        view?.init(repository.isFingerPrintEnabled())
    }

    override fun onOpenWallet() {
        view?.hideKeyboard()

        if (view?.hasValidPass() == true) {
            openWallet(view?.getPass())
        }
    }

    override fun onPassChanged() {
        view?.clearError()
    }

    override fun onChangeWallet() {
        view?.clearError()
        view?.showChangeAlert()
    }

    override fun onChangeConfirm() {
        view?.changeWallet()
    }

    override fun onFingerprintError() {
        view?.showFingerprintAuthError()
    }

    override fun onFingerprintSucceeded() {
        openWallet(PreferencesManager.getString(PreferencesManager.KEY_PASSWORD))
    }

    override fun onFingerprintFailed() {
        view?.vibrate(VIBRATION_LENGTH)
    }

    override fun onStop() {
        view?.clearFingerprintCallback()
        super.onStop()
    }

    override fun hasBackArrow(): Boolean? = false

    private fun openWallet(pass: String?) {
        if (Status.STATUS_OK == repository.openWallet(pass)) {
            view?.openWallet(view?.getPass() ?: return)
        } else {
            view?.showOpenWalletError()
        }
    }
}
