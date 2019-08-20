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

package com.mw.beam.beamwallet.screens.addresses

import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.entities.WalletAddress

class AddressesState {

    var addresses = HashMap<String, WalletAddress>()
    var transactions = mutableListOf<TxDescription>()

    fun updateAddresses(addresses: List<WalletAddress>?){
        addresses?.forEach {
            this.addresses[it.walletID] = it
        }
    }

    fun getAddresses() = addresses.values.toList()

    fun getTransactions(walletID:String) : List<TxDescription> {
        var addressTransactions = mutableListOf<TxDescription>()
        transactions?.forEach { transaction ->
            if (transaction.myId == walletID || transaction.peerId == walletID) {
                addressTransactions.add(transaction)
            }
        }

        return addressTransactions
    }

    fun deleteAddresses(addresses: List<WalletAddress>?) {
        addresses?.forEach {
            this.addresses.remove(it.walletID)
        }
    }

    fun deleteRemovedAddresses(addresses: List<String>?) {
        addresses?.forEach {
            this.addresses.remove(it)
        }
    }

    fun deleteTransaction(tx: List<TxDescription>?) {
        tx?.forEach { transactions.removeAll { x -> it.id == x.id  } }
    }
}