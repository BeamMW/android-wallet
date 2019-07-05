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

package com.mw.beam.beamwallet.screens.owner_key_verification

import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.helpers.FingerprintManager

class OwnerKeyVerificationPresenter(view: OwnerKeyVerificationContract.View?, repository: OwnerKeyVerificationContract.Repository)
    : BasePresenter<OwnerKeyVerificationContract.View, OwnerKeyVerificationContract.Repository>(view, repository), OwnerKeyVerificationContract.Presenter {

    override fun onViewCreated() {
        super.onViewCreated()
        view?.init(isEnableFingerprint())
    }

    override fun onFingerprintSuccess() {
        view?.navigateToOwnerKey()
    }

    override fun onFingerprintError() {
        view?.showErrorFingerprintMessage()
    }

    override fun onCancelFingerprintDialog() {
        view?.showFingerprintDescription()
    }

    override fun onNext() {
        val password = view?.getPassword()

        view?.hideFingerprintDescription()

        when {
            password.isNullOrBlank() -> view?.showEmptyPasswordError()
            repository.checkPassword(password) -> {
                if (isEnableFingerprint()) {
                    view?.showFingerprintDialog()
                } else {
                    view?.navigateToOwnerKey()
                }
            }
            else -> view?.showWrongPasswordError()
        }
    }

    private fun isEnableFingerprint(): Boolean {
        return repository.isEnableFingerprint() && FingerprintManager.SensorState.READY == FingerprintManager.checkSensorState(view?.getContext() ?: return false)
    }

    override fun onChangePassword() {
        view?.clearPasswordError()
    }
}