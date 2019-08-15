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

import android.graphics.Bitmap
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.helpers.TrashManager
import io.reactivex.disposables.Disposable

/**
 * Created by vain onnellinen on 12/20/18.
 */
class UtxoDetailsPresenter(currentView: UtxoDetailsContract.View, currentRepository: UtxoDetailsContract.Repository, private val state: UtxoDetailsState)
    : BasePresenter<UtxoDetailsContract.View, UtxoDetailsContract.Repository>(currentView, currentRepository),
        UtxoDetailsContract.Presenter {
    private lateinit var utxoUpdatedSubscription: Disposable
    private lateinit var txStatusSubscription: Disposable
    private lateinit var trashSubscription: Disposable

    override fun onViewCreated() {
        super.onViewCreated()
        state.utxo = view?.getUtxo()
        view?.init(state.utxo ?: return)
    }

    override fun initSubscriptions() {
        super.initSubscriptions()

        utxoUpdatedSubscription = repository.getUtxoUpdated().subscribe { utxos ->
            utxos.firstOrNull { it.id == state.utxo?.id }?.let {
                state.utxo = it
                view?.init(it)
            }
        }

        txStatusSubscription = repository.getTxStatus().subscribe { data ->
            data.tx?.filter(::transactionFilter)?.let {
                state.updateTransactions(it)
                state.deleteTransactions(repository.getAllTransactionInTrash().filter(::transactionFilter))

                updateUtxoHistory()
            }

            var kernelID = state.transactions.values.find { it.id == state.utxo?.spentTxId }?.kernelId

            if (kernelID.isNullOrEmpty()) {
                kernelID = state.transactions.values.find { it.id == state.utxo?.createTxId }?.kernelId
            }

            view?.configUtxoKernel(kernelID)
        }

        trashSubscription = repository.getTrashSubject().subscribe {
            when (it.type) {
                TrashManager.ActionType.Added -> {
                    state.deleteTransactions(it.data.transactions)
                    updateUtxoHistory()
                }
                TrashManager.ActionType.Restored -> {
                    state.updateTransactions(it.data.transactions.filter(::transactionFilter))
                    updateUtxoHistory()
                }
                TrashManager.ActionType.Removed -> {}
            }
        }
    }

    private fun transactionFilter(txDescription: TxDescription): Boolean {
        return txDescription.id == state.utxo?.createTxId || txDescription.id == state.utxo?.spentTxId
    }

    private fun updateUtxoHistory() {
        if (state.utxo != null) {
            view?.configUtxoHistory(state.utxo!!, state.getTransactions())
        }
    }

    override fun getSubscriptions(): Array<Disposable>? = arrayOf(utxoUpdatedSubscription, txStatusSubscription, trashSubscription)

}
