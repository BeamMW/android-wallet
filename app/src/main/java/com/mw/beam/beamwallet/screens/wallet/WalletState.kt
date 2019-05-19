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

package com.mw.beam.beamwallet.screens.wallet

import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.entities.WalletStatus

/**
 * Created by vain onnellinen on 12/4/18.
 */
class WalletState {
    var walletStatus: WalletStatus? = null

    var shouldExpandAvailable = false
    var shouldExpandInProgress = false
    var privacyMode = false

    val transactions = HashMap<String, TxDescription>()

    fun updateTransactions(tx: List<TxDescription>?): List<TxDescription> {
        tx?.forEach { transaction ->
            transactions[transaction.id] = transaction
        }

        return transactions.values.sortedByDescending { it.modifyTime }
    }

    fun deleteTransaction(tx: List<TxDescription>?): List<TxDescription> {
        tx?.forEach { transactions.remove(it.id) }
        return transactions.values.sortedByDescending { it.modifyTime }
    }
}
