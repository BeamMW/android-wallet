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

import com.mw.beam.beamwallet.base_screen.BaseRepository
import com.mw.beam.beamwallet.core.entities.OnTxStatusData
import com.mw.beam.beamwallet.core.entities.PaymentProof
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.entities.Utxo
import com.mw.beam.beamwallet.core.helpers.PreferencesManager
import com.mw.beam.beamwallet.core.listeners.WalletListener
import io.reactivex.subjects.Subject

/**
 * Created by vain onnellinen on 10/18/18.
 */
class TransactionDetailsRepository : BaseRepository(), TransactionDetailsContract.Repository {

    override fun deleteTransaction(txDescription: TxDescription?) {
        if (txDescription != null) {
            getResult("deleteTransaction", "kernelID = ${txDescription.kernelId}") {
                wallet?.deleteTx(txDescription.id)
            }
        }
    }

    override fun getUtxoByTx(txId: String): Subject<List<Utxo>?> {
        return getResult(WalletListener.subOnCoinsByTx, "getUtxoByTx") { wallet?.getCoinsByTx(txId) }
    }

    override fun getTxStatus(): Subject<OnTxStatusData> {
        return getResult(WalletListener.subOnTxStatus, "getTxStatus") { wallet?.getWalletStatus() }
    }

    override fun cancelTransaction(txDescription: TxDescription?) {
        if (txDescription != null) {
            getResult("cancelTransaction", "kernelID = ${txDescription.kernelId}") {
                wallet?.cancelTx(txDescription.id)
            }
        }
    }

    override fun getPaymentProof(txId: String, canRequestProof: Boolean): Subject<PaymentProof> {
        return getResult(WalletListener.subOnPaymentProofExported, "getPaymentProof") {
            if (canRequestProof) {
                requestProof(txId)
            }
        }
    }

    override fun requestProof(txId: String) {
        getResult("requestProof") {
            wallet?.getPaymentInfo(txId)
        }
    }

    override fun isAllowOpenExternalLink(): Boolean {
        return PreferencesManager.getBoolean(PreferencesManager.KEY_ALWAYS_OPEN_LINK, false)
    }
}
