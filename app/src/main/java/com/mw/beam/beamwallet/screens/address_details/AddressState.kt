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

package com.mw.beam.beamwallet.screens.address_details

import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.entities.WalletAddress

/**
 * Created by vain onnellinen on 3/4/19.
 */
class AddressState {
    var address: WalletAddress? = null

    private val transactions = HashMap<String, TxDescription>()

    fun updateTransactions(tx: List<TxDescription>?): List<TxDescription> {
        tx?.forEach { transaction ->
            if (transaction.myId == address?.walletID || transaction.peerId == address?.walletID) {
                transactions[transaction.id] = transaction
            }
        }

        return transactions.values.sortedByDescending { it.modifyTime }
    }

    fun deleteTransaction(tx: List<TxDescription>?): List<TxDescription> {
        tx?.forEach { transactions.remove(it.id) }
        return transactions.values.sortedByDescending { it.modifyTime }
    }
}
