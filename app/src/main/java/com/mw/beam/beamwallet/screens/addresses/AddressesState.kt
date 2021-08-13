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

import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.entities.WalletAddress

class AddressesState {

    var addresses = mutableListOf<WalletAddress>()

    fun filteredAddresses(item:Int): List<WalletAddress> {
        return when (item) {
            0 -> {
                addresses.filter { !it.isExpired && !it.isContact }
            }
            1 -> {
                addresses.filter { it.isExpired && !it.isContact }
            }
            else -> {
                addresses.filter { it.isContact }
            }
        }
    }

    fun getTransactions(walletID:String) : List<TxDescription> {
        return AppManager.instance.getTransactionsByAddress(walletID)
    }
}