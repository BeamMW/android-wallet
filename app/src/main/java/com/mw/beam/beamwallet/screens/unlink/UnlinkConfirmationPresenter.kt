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

package com.mw.beam.beamwallet.screens.unlink

import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.helpers.convertToBeam
import io.reactivex.disposables.Disposable

class UnlinkConfirmationPresenter(view: UnlinkConfirmationContract.View?, repository: UnlinkConfirmationContract.Repository, private val state: UnlinkConfirmationState)
    : BasePresenter<UnlinkConfirmationContract.View, UnlinkConfirmationContract.Repository>(view, repository), UnlinkConfirmationContract.Presenter {
    private lateinit var changeSubscription: Disposable

    override fun onViewCreated() {
        super.onViewCreated()
        view?.apply {
            state.amount = getAmount()
            state.fee = getFee()

            init(state.amount.convertToBeam(), state.fee, AppManager.instance.remainingUnlink(state.amount + state.fee))
        }
    }

    override fun onSendPressed() {
        if (repository.isConfirmTransactionEnabled()) {
            view?.showConfirmDialog()
        } else {
            send()
        }
    }

    override fun onConfirmed() {
        send()
    }

    private fun send() {
        showWallet()
    }

    override fun initSubscriptions() {
        val totalSendAmount = state.amount + state.fee
        changeSubscription = repository.calcChange(totalSendAmount).subscribe {
            view?.configUtxoInfo((it + totalSendAmount).convertToBeam(), it.convertToBeam())
        }
    }

    override fun getSubscriptions(): Array<Disposable>? = arrayOf(changeSubscription)

    private fun showWallet() {
        state.apply { view?.delaySend(amount, fee) }
        view?.showWallet()
    }
}