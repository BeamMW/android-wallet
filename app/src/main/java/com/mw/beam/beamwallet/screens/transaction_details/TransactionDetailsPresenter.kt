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
import android.view.MenuInflater
import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.AppConfig
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.entities.Utxo
import com.mw.beam.beamwallet.core.helpers.TxSender
import com.mw.beam.beamwallet.core.helpers.TxStatus
import io.reactivex.disposables.Disposable

/**
 *  10/18/18.
 */
class TransactionDetailsPresenter(currentView: TransactionDetailsContract.View, currentRepository: TransactionDetailsContract.Repository, val state: TransactionDetailsState)
    : BasePresenter<TransactionDetailsContract.View, TransactionDetailsContract.Repository>(currentView, currentRepository),
        TransactionDetailsContract.Presenter {

    private val COPY_TAG = "PROOF"

    private lateinit var paymentProofSubscription: Disposable
    private lateinit var txSubscription: Disposable
    private lateinit var addressesSubscription: Disposable

    override fun onCreate() {
        super.onCreate()
        state.txID = view?.getTransactionId()
    }

    override fun initSubscriptions() {
        super.initSubscriptions()

        state.txDescription = AppManager.instance.getTransaction(state.txID!!)

        if (state.txDescription != null) {
            view?.init(state.txDescription!!, repository.isPrivacyModeEnabled())
            view?.updateAddresses(state.txDescription!!)

            if (canRequestProof()) {
                repository.requestProof(state.txID!!)
            }

            repository.getUtxoByTx(state.txID!!)

            updateUtxos(AppManager.instance.getUTXOByTransaction(state.txDescription!!))
        }

        paymentProofSubscription = repository.getPaymentProof(state.txID!!, canRequestProof()).subscribe {
            if (it.txId == state.txID) {
                state.paymentProof = it
                view?.updatePaymentProof(it)
            }
        }

        txSubscription = AppManager.instance.subOnTransactionsChanged.subscribe {
            state.txDescription = AppManager.instance.getTransaction(state.txID!!)
            if (state.txDescription != null) {
                view?.init(state.txDescription!!, repository.isPrivacyModeEnabled())
            }
        }

        addressesSubscription = AppManager.instance.subOnAddressesChanged.subscribe(){
            if (it == false) {
                state.txDescription = AppManager.instance.getTransaction(state.txID!!)
                if (state.txDescription != null) {
                    view?.updateAddresses(state.txDescription!!)
                }
            }
        }
    }

    override fun onOpenInBlockExplorerPressed() {
        if (state.txDescription == null) {
            return
        }

        if (repository.isAllowOpenExternalLink()) {
            view?.openExternalLink(AppConfig.buildTransactionLink(state.txDescription!!.kernelId))
        } else {
            view?.showOpenLinkAlert()
        }
    }

    override fun onOpenLinkPressed() {
        if (state.txDescription == null) {
            return
        }

        view?.openExternalLink(AppConfig.buildTransactionLink(state.txDescription!!.kernelId))
    }

    private fun updateUtxos(utxos: List<Utxo>) {
        view?.updateUtxos(utxos.map { utxo ->
            var type = UtxoType.Exchange

            if (state.txDescription?.selfTx == false) {
                type = if (state.txID == utxo.createTxId) {
                    UtxoType.Receive
                } else {
                    UtxoType.Send
                }
            }

            UtxoInfoItem(type, utxo.amount, utxo.assetId)
        }, repository.isPrivacyModeEnabled())
    }

    private fun canRequestProof(): Boolean {
        if (state.txDescription == null) {
            return false
        }

        return state.txDescription!!.sender == TxSender.SENT && !state.txDescription!!.selfTx && state.txDescription!!.status == TxStatus.Completed
    }

    override fun onCopyPaymentProof() {
        state.paymentProof?.let {
            view?.copyToClipboard(it.rawProof, COPY_TAG)
            view?.showCopiedAlert()
        }
    }

    override fun onShowPaymentProof() {
        if (state.paymentProof == null) return
        view?.showPaymentProof(state.paymentProof!!)
    }

    override fun getSubscriptions(): Array<Disposable>? = arrayOf(paymentProofSubscription, txSubscription, addressesSubscription)

    override fun onMenuCreate(menu: Menu?, inflater: MenuInflater) {
        view?.configMenuItems(menu, inflater,state.txDescription)
    }

    override fun onRepeatTransaction() {
        state.txDescription?.let { txDescription ->
            if (txDescription.sender.value) {
                view?.showSendFragment(txDescription.peerId, txDescription.amount)
            } else {
                val address = AppManager.instance.getAddress(txDescription.myId)
                view?.showReceiveFragment(txDescription.amount, address)
            }
        }
    }

    override fun onSaveContact() {
        state.txDescription?.let { txDescription ->
            view?.showSaveContact(txDescription.peerId)
        }
    }

    override fun onCancelTransaction() {
        view?.showCancelAlert()
    }

    override fun onDeleteTransaction() {
        view?.showDeleteAlert()
    }

    override fun onStart() {
        super.onStart()
        view?.init(state.txDescription ?: return, repository.isPrivacyModeEnabled())
    }

    override fun onSharePressed() {
        view?.shareTransactionDetails(repository.saveImage(view?.convertViewIntoBitmap()))
    }

    override fun onExpandDetailedPressed() {
        state.shouldExpandDetail = !state.shouldExpandDetail
        view?.handleExpandDetails(state.shouldExpandDetail)
    }

    override fun onExpandUtxosPressed() {
        state.shouldExpandUtxos = !state.shouldExpandUtxos
        view?.handleExpandUtxos(state.shouldExpandUtxos)
    }

    override fun onExpandProofPressed() {
        state.shouldExpandProof = !state.shouldExpandProof
        view?.handleExpandProof(state.shouldExpandProof)
    }

    override fun onCancelTransactionConfirm() {
        repository.cancelTransaction(state.txDescription)
        view?.finishScreen()
    }

    override fun onDeleteTransactionsPressed() {
        state.txDescription?.let { view?.showDeleteSnackBar(it) }
        repository.deleteTransaction(state.txDescription)
        view?.finishScreen()
    }

    override fun onCopyDetailsPressed() {
        view?.copyDetails()
    }
}
