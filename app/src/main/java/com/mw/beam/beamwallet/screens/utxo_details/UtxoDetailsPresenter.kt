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
import com.mw.beam.beamwallet.core.AppModel
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.helpers.TrashManager
import io.reactivex.disposables.Disposable

/**
 *  12/20/18.
 */
class UtxoDetailsPresenter(currentView: UtxoDetailsContract.View, currentRepository: UtxoDetailsContract.Repository, val state: UtxoDetailsState)
    : BasePresenter<UtxoDetailsContract.View, UtxoDetailsContract.Repository>(currentView, currentRepository),
        UtxoDetailsContract.Presenter {

    private lateinit var utxoUpdatedSubscription: Disposable
    private lateinit var txStatusSubscription: Disposable

    override fun onViewCreated() {
        super.onViewCreated()

        state.utxo = view?.getUtxo()

        view?.init(state.utxo ?: return)
    }

    override fun initSubscriptions() {
        super.initSubscriptions()

        filter()

        utxoUpdatedSubscription = AppModel.instance.subOnUtxosChanged.subscribe(){
            state.utxo = AppModel.instance.getUtxoByID(state?.utxo?.stringId)
            if (state.utxo!=null) {
                view?.init(state.utxo!!)
            }
        }

        txStatusSubscription = AppModel.instance.subOnTransactionsChanged.subscribe(){
            filter()
        }
    }

    private fun filter() {
        val tx = AppModel.instance.getTransactionsByUTXO(state.utxo)

        state.transactions.clear()
        state.transactions.addAll(tx)

        view?.configUtxoHistory(state.utxo!!, state.sortedTransactions())
    }

    override fun onExpandTransactionsPressed() {
        state.shouldExpandTransactions = !state.shouldExpandTransactions
        view?.handleExpandTransactions(state.shouldExpandTransactions)
    }

    override fun onExpandDetailedPressed() {
        state.shouldExpandDetail = !state.shouldExpandDetail
        view?.handleExpandDetails(state.shouldExpandDetail)
    }

    override fun getSubscriptions(): Array<Disposable>? = arrayOf(utxoUpdatedSubscription, txStatusSubscription)
}
