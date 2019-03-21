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

package com.mw.beam.beamwallet.screens.receive

import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.helpers.ExpirePeriod
import io.reactivex.disposables.Disposable

/**
 * Created by vain onnellinen on 11/13/18.
 */
class ReceivePresenter(currentView: ReceiveContract.View, currentRepository: ReceiveContract.Repository, private val state: ReceiveState)
    : BasePresenter<ReceiveContract.View, ReceiveContract.Repository>(currentView, currentRepository),
        ReceiveContract.Presenter {
    private lateinit var walletIdSubscription: Disposable

    override fun onViewCreated() {
        super.onViewCreated()
        view?.init()
    }

    override fun onCopyTokenPressed() {
        saveAddress()

        if (state.address != null) {
            view?.copyToClipboard(state.address!!.walletID)
            view?.close()
        }
    }

    override fun onShowQrPressed() {
        saveAddress()
        view?.showQR(state.address!!.walletID)
    }

    override fun onDialogCopyPressed() {
        if (state.address != null) {
            view?.copyToClipboard(state.address!!.walletID)
            view?.dismissDialog()
            view?.close()
        }
    }

    override fun onDialogClosePressed() {
        view?.dismissDialog()
    }

    override fun onExpirePeriodChanged(period: ExpirePeriod) {
        state.expirePeriod = period
    }

    override fun onBackPressed() {
        saveAddress()
    }

    override fun onDestroy() {
        view?.dismissDialog()
        super.onDestroy()
    }

    override fun initSubscriptions() {
        super.initSubscriptions()

        walletIdSubscription = repository.generateNewAddress().subscribe {
            if (state.address == null) {
                state.address = it
                view?.showToken(state.address!!.walletID)
            }
        }
    }

    private fun saveAddress() {
        if (state.address != null && !state.wasAddressSaved) {
            state.address!!.duration = state.expirePeriod.value

            val comment = view?.getComment()

            if (!comment.isNullOrBlank()) {
                state.address!!.label = comment
            }

            repository.saveAddress(state.address!!)
            state.wasAddressSaved = true
        }
    }

    override fun getSubscriptions(): Array<Disposable>? = arrayOf(walletIdSubscription)

    override fun hasStatus(): Boolean = true
}
