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

package com.mw.beam.beamwallet.screens.settings.password_dialog

import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.helpers.DelayedTask

class ConfirmRemoveWalletPresenter(view: ConfirmRemoveWalletContract.View?, repository: ConfirmRemoveWalletContract.Repository)
    : BasePresenter<ConfirmRemoveWalletContract.View, ConfirmRemoveWalletContract.Repository>(view, repository), ConfirmRemoveWalletContract.Presenter {
    private var isSuccess = false

    override fun onViewCreated() {
        super.onViewCreated()
        view?.init(repository.isFingerPrintEnabled())
    }

    override fun onCancel() {
        view?.close(false)
    }

    override fun onFailedFingerprint() {
        if (!isSuccess) {
            view?.showFailedFingerprint()
        }
    }

    override fun onErrorFingerprint() {
        if (!isSuccess) {
            view?.showErrorFingerprint()
        }
    }

    override fun onOkPressed(password: String) {
        when {
            repository.checkPassword(password) -> {
                isSuccess = true
                view?.close(true)
            }
            password.isBlank() -> view?.showEmptyPasswordError()
            else -> view?.showWrongPasswordError()
        }
    }

    override fun onPasswordChanged() {
        view?.clearPasswordError()
    }

    override fun onSuccessFingerprint() {
        isSuccess = true
        view?.showSuccessFingerprint()
        DelayedTask.startNew(1, {  view?.close(true) })
    }

}