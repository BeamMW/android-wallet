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

package com.mw.beam.beamwallet.screens.change_address

import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.entities.WalletAddress

class ChangeAddressState {
    private val addresses = HashMap<String, WalletAddress>()
    private val transactions = HashMap<String, TxDescription>()
    var scannedAddress : String? = null
    var viewState = ChangeAddressContract.ViewState.Receive

    fun updateAddresses(walletAddresses: List<WalletAddress>?) {
        walletAddresses?.forEach {
            addresses[it.walletID] = it
        }
    }

    fun updateTransactions(transactions: List<TxDescription>?) {
        transactions?.forEach {
            this.transactions[it.id] = it
        }
    }

    fun getAddresses() = addresses.values.toList()

    fun getTransactions() = transactions.values.toList().sortedBy { it.modifyTime }
}