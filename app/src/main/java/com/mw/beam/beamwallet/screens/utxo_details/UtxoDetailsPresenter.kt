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
import io.reactivex.disposables.Disposable

/**
 * Created by vain onnellinen on 12/20/18.
 */
class UtxoDetailsPresenter(currentView: UtxoDetailsContract.View, currentRepository: UtxoDetailsContract.Repository, private val state: UtxoDetailsState)
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

        utxoUpdatedSubscription = repository.getUtxoUpdated().subscribe { utxos ->
            utxos.firstOrNull { it.id == state.utxo?.id }?.let {
                state.utxo = it
                view?.init(it)
            }
        }

        txStatusSubscription = repository.getTxStatus().subscribe { data ->
            data.tx?.filter { it.id == state.utxo?.createTxId || it.id == state.utxo?.spentTxId }?.let {
                state.configTransactions(it)

                if (state.utxo != null) {
                    view?.configUtxoHistory(state.utxo!!, state.configTransactions())
                }
            }
        }
    }

    override fun getSubscriptions(): Array<Disposable>? = arrayOf(utxoUpdatedSubscription, txStatusSubscription)
}
