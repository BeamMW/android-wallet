// Copyright 2018 Beam Development
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.mw.beam.beamwallet.utxo

import com.mw.beam.beamwallet.baseScreen.BasePresenter
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.entities.Utxo
import io.reactivex.disposables.Disposable

/**
 * Created by vain onnellinen on 10/2/18.
 */
class UtxoPresenter(currentView: UtxoContract.View, currentRepository: UtxoContract.Repository, private val state: UtxoState)
    : BasePresenter<UtxoContract.View, UtxoContract.Repository>(currentView, currentRepository),
        UtxoContract.Presenter {
    private lateinit var utxoUpdatedSubscription: Disposable
    private lateinit var txStatusSubscription: Disposable

    override fun onViewCreated() {
        super.onViewCreated()
        view?.init()
    }

    override fun onUtxoPressed(utxo: Utxo) {
        view?.showUtxoDetails(utxo, state.configTransactions().filter { it.id == utxo.createTxId || it.id == utxo.spentTxId } as ArrayList<TxDescription>)
    }

    override fun initSubscriptions() {
        super.initSubscriptions()

        utxoUpdatedSubscription = repository.getUtxoUpdated().subscribe { utxos ->
            view?.updateUtxos(utxos)
        }

        txStatusSubscription = repository.getTxStatus().subscribe { data ->
            state.configTransactions(data.tx)
        }
    }

    override fun getSubscriptions(): Array<Disposable>? = arrayOf(utxoUpdatedSubscription, txStatusSubscription)

    override fun hasBackArrow(): Boolean? = null
    override fun hasStatus(): Boolean = true
}
