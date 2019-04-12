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

package com.mw.beam.beamwallet.screens.transaction_details

import android.view.Menu
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.helpers.TxSender
import io.reactivex.disposables.Disposable

/**
 * Created by vain onnellinen on 10/18/18.
 */
class TransactionDetailsPresenter(currentView: TransactionDetailsContract.View, currentRepository: TransactionDetailsContract.Repository, private val state: TransactionDetailsState)
    : BasePresenter<TransactionDetailsContract.View, TransactionDetailsContract.Repository>(currentView, currentRepository),
        TransactionDetailsContract.Presenter {
    private lateinit var utxoUpdatedSubscription: Disposable
    private lateinit var txUpdateSubscription: Disposable
    private lateinit var pymentProofSubscription: Disposable

    override fun onCreate() {
        super.onCreate()
        state.txDescription = view?.getTransactionDetails()
    }

    override fun initSubscriptions() {
        super.initSubscriptions()

        utxoUpdatedSubscription = repository.getUtxoUpdated().subscribe { utxos ->
            utxos.filter { it.createTxId == state.txDescription?.id || it.spentTxId == state.txDescription?.id}
                    .let { view?.updateUtxos(it, state.txDescription) }
        }

        txUpdateSubscription = repository.getTxStatus().subscribe { data ->
            data.tx?.firstOrNull { it.id == state.txDescription?.id }?.let {
                state.txDescription = it

                val canRequestProof = canRequestProof()

                view?.init(it, canRequestProof)

                if (canRequestProof) {
                    repository.requestProof(it.id)
                }
            }
        }

        pymentProofSubscription = repository.getPaymetProofs(state.txDescription!!.id, canRequestProof()).subscribe {
            if (it.txId == state.txDescription?.id) {
                state.paymentProof = it
            }
        }
    }

    private fun canRequestProof(): Boolean {
        if (state.txDescription == null) {
            return false
        }
        return state.txDescription!!.sender == TxSender.SENT && !state.txDescription!!.selfTx
    }

    override fun onCopyPaymentProof() {
        state.paymentProof?.let { view?.copePaymetProofToClipboard(it) }
    }

    override fun onShowPaymetProof() {
        state.txDescription?.let { view?.showPaymetProof(it) }
    }

    override fun getSubscriptions(): Array<Disposable>? = arrayOf(utxoUpdatedSubscription, txUpdateSubscription, pymentProofSubscription)

    override fun onMenuCreate(menu: Menu?) {
        view?.configMenuItems(menu, state.txDescription?.status ?: return)
    }

    override fun onCancelTransaction() {
        repository.cancelTransaction(state.txDescription)
        view?.finishScreen()
    }

    override fun onDeleteTransaction() {
        repository.deleteTransaction(state.txDescription)
        view?.finishScreen()
    }

    override fun onStart() {
        super.onStart()
        view?.init(state.txDescription ?: return, canRequestProof())
    }
}
