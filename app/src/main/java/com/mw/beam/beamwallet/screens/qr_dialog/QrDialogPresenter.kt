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

package com.mw.beam.beamwallet.screens.qr_dialog

import android.graphics.Bitmap
import com.mw.beam.beamwallet.base_screen.BasePresenter

class QrDialogPresenter(view: QrDialogContract.View?, repository: QrDialogContract.Repository, private val state: QrDialogState)
    : BasePresenter<QrDialogContract.View, QrDialogContract.Repository>(view, repository), QrDialogContract.Presenter {


    override fun onViewCreated() {
        super.onViewCreated()
        state.walletAddress = view?.getWalletAddress()
        state.amount = view?.getAmount() ?: 0

        state.walletAddress?.let {
            view?.init(it, state.amount)
        }
    }

    override fun onSharePressed(bitmap: Bitmap) {
        view?.shareQR(repository.saveImage(bitmap))
    }

}