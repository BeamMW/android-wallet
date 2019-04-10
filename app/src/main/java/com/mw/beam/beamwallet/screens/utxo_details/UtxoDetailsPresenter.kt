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

package com.mw.beam.beamwallet.screens.utxo_details

import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.screens.utxo.UtxoState
import io.reactivex.disposables.Disposable

/**
 * Created by vain onnellinen on 12/20/18.
 */
class UtxoDetailsPresenter(currentView: UtxoDetailsContract.View, currentRepository: UtxoDetailsContract.Repository, private val state: UtxoState)
    : BasePresenter<UtxoDetailsContract.View, UtxoDetailsContract.Repository>(currentView, currentRepository),
        UtxoDetailsContract.Presenter {
    private lateinit var utxoUpdatedSubscription: Disposable
    private lateinit var txStatusSubscription: Disposable

    override fun onCreate() {
        super.onCreate()
        repository.utxo = view?.getUtxoDetails()
        repository.relatedTransactions = view?.getRelatedTransactions()
    }

    override fun initSubscriptions() {
        super.initSubscriptions()

        utxoUpdatedSubscription = repository.getUtxoUpdated().subscribe { utxos ->
            utxos.firstOrNull { it.id == repository.utxo?.id }?.let {
                repository.utxo = it
                updateView()
            }
        }

        txStatusSubscription = repository.getTxStatus().subscribe { data ->
            val utxo = repository.utxo

            data.tx?.filter { it.id == utxo?.createTxId || it.id == utxo?.spentTxId }?.let {
                repository.relatedTransactions = ArrayList(state.configTransactions(it))
                updateView()
            }
        }
    }

    private fun updateView() {
        val utxo = repository.utxo
        val transactions = repository.relatedTransactions
        if (transactions != null && utxo != null) {
            view?.init(utxo, transactions)
        }
    }

    override fun getSubscriptions(): Array<Disposable>? = arrayOf(utxoUpdatedSubscription, txStatusSubscription)

    override fun onStart() {
        super.onStart()
        view?.init(repository.utxo ?: return, repository.relatedTransactions ?: return)
    }
}
