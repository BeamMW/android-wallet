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

package com.mw.beam.beamwallet.screens.utxo

import android.view.Menu
import android.view.MenuInflater
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.entities.Utxo
import io.reactivex.disposables.Disposable
import org.jetbrains.anko.doAsync
import kotlin.concurrent.thread
import org.jetbrains.anko.uiThread


/**
 *  10/2/18.
 */
class UtxoPresenter(currentView: UtxoContract.View, currentRepository: UtxoContract.Repository, private val state: UtxoState)
    : BasePresenter<UtxoContract.View, UtxoContract.Repository>(currentView, currentRepository),
        UtxoContract.Presenter {

    private lateinit var utxoUpdatedSubscription: Disposable
    private lateinit var blockchainInfoSubscription: Disposable
    private lateinit var txStatusSubscription: Disposable

    private var allUtxos = mutableListOf<Utxo>()

    var utxosCount = 0

    override fun onViewCreated() {
        super.onViewCreated()
        view?.init()

        state.privacyMode = repository.isPrivacyModeEnabled()
    }

    override fun onStart() {
        super.onStart()
        notifyPrivacyStateChange()
    }

    private fun notifyPrivacyStateChange() {
        val privacyModeEnabled = repository.isPrivacyModeEnabled()
        state.privacyMode = privacyModeEnabled
        view?.configPrivacyStatus(privacyModeEnabled)
    }

    override fun onChangePrivacyModePressed() {
        if (!state.privacyMode && repository.isNeedConfirmEnablePrivacyMode()) {
            view?.showActivatePrivacyModeDialog()
        } else {
            repository.setPrivacyModeEnabled(!state.privacyMode)
            notifyPrivacyStateChange()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        view?.createOptionsMenu(menu, inflater, state.privacyMode)
    }

    override fun onCancelDialog() {
        view?.dismissAlert()
    }

    override fun onPrivacyModeActivated() {
        view?.dismissAlert()
        repository.setPrivacyModeEnabled(true)
        notifyPrivacyStateChange()
    }

    override fun onUtxoPressed(utxo: Utxo) {
        view?.showUtxoDetails(utxo)
    }

    override fun initSubscriptions() {
        super.initSubscriptions()

        if(AppManager.instance.isResotred) {
            AppManager.instance.requestUTXO()
        }

        view?.updateBlockchainInfo(AppManager.instance.getStatus().system)

        filter()

        utxoUpdatedSubscription = AppManager.instance.subOnUtxosChanged.subscribe() {
            filter()
        }

        blockchainInfoSubscription = AppManager.instance.subOnStatusChanged.subscribe(){
            view?.updateBlockchainInfo(AppManager.instance.getStatus().system)
        }

        txStatusSubscription = AppManager.instance.subOnTransactionsChanged.subscribe() {
            if (utxosCount > 0) {
                filter()
            }
        }
    }

    private fun filter() {
        doAsync {
            utxosCount = AppManager.instance.getUtxos().count()
            allUtxos.clear()
            allUtxos.addAll(AppManager.instance.getUtxos())

            val transactions = AppManager.instance.getTransactions()

            var sortByDate = false

            allUtxos.forEach {
                var transaction = transactions.filter { s -> s.id == it.createTxId || s.id == it.spentTxId }.firstOrNull()
                if (transaction != null) {
                    sortByDate = true
                    it.transactionDate = transaction.createTime
                    it.transactionComment = transaction.message
                }
            }

            allUtxos.sortByDescending { it.id  }

            if (sortByDate) {
                allUtxos.sortByDescending { it.transactionDate  }
            }

            uiThread {
                view?.updateUtxos(allUtxos)
            }
        }
    }

    override fun onDestroy() {
        view?.dismissAlert()
        super.onDestroy()
    }

    override fun getSubscriptions(): Array<Disposable>? = arrayOf(utxoUpdatedSubscription, blockchainInfoSubscription, txStatusSubscription)

    override fun hasBackArrow(): Boolean? = true
    override fun hasStatus(): Boolean = true
}
