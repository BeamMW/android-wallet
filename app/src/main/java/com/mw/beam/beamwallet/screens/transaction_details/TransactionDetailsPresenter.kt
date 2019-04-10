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

import android.util.Log
import android.view.Menu
import com.mw.beam.beamwallet.base_screen.BasePresenter
import io.reactivex.disposables.Disposable

/**
 * Created by vain onnellinen on 10/18/18.
 */
class TransactionDetailsPresenter(currentView: TransactionDetailsContract.View, currentRepository: TransactionDetailsContract.Repository)
    : BasePresenter<TransactionDetailsContract.View, TransactionDetailsContract.Repository>(currentView, currentRepository),
        TransactionDetailsContract.Presenter {
    private lateinit var utxoUpdatedSubscription: Disposable
    private lateinit var txUpdateSubscription: Disposable

    override fun onCreate() {
        super.onCreate()
        repository.txDescription = view?.getTransactionDetails()
    }

    override fun initSubscriptions() {
        super.initSubscriptions()

        utxoUpdatedSubscription = repository.getUtxoUpdated().subscribe { utxos ->
            utxos.filter { it.createTxId == repository.txDescription?.id || it.spentTxId == repository.txDescription?.id}
                    .let { view?.updateUtxo(it, repository.txDescription) }
        }

        txUpdateSubscription = repository.getTxStatus().subscribe { data ->
            data.tx?.first { it.id == repository.txDescription?.id }?.let {
                repository.txDescription = it
                view?.init(it)
            }
        }
    }

    override fun getSubscriptions(): Array<Disposable>? = arrayOf(utxoUpdatedSubscription, txUpdateSubscription)

    override fun onMenuCreate(menu: Menu?) {
        view?.configMenuItems(menu, repository.txDescription?.status ?: return)
    }

    override fun onCancelTransaction() {
        repository.cancelTransaction()
        view?.finishScreen()
    }

    override fun onDeleteTransaction() {
        repository.deleteTransaction()
        view?.finishScreen()
    }

    override fun onStart() {
        super.onStart()
        view?.init(repository.txDescription ?: return)
    }
}
