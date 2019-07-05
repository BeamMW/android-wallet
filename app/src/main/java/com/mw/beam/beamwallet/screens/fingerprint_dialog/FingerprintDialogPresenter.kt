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

package com.mw.beam.beamwallet.screens.fingerprint_dialog

import com.mw.beam.beamwallet.base_screen.BasePresenter

class FingerprintDialogPresenter(view: FingerprintDialogContract.View?, repository: FingerprintDialogContract.Repository)
    : BasePresenter<FingerprintDialogContract.View, FingerprintDialogContract.Repository>(view, repository), FingerprintDialogContract.Presenter {


    override fun onStart() {
        super.onStart()
        view?.init()
    }

    override fun onCancel() {
        view?.cancel()
    }

    override fun onError() {
        view?.error()
    }

    override fun onFailed() {
        view?.showFailed()
    }

    override fun onSuccess() {
        view?.success()
    }

    override fun onStop() {
        view?.clearFingerprintCallback()
        super.onStop()
    }
}