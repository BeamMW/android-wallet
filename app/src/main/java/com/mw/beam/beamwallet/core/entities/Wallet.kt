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

package com.mw.beam.beamwallet.core.entities

import com.mw.beam.beamwallet.core.entities.dto.PaymentInfoDTO
import com.mw.beam.beamwallet.core.entities.dto.WalletAddressDTO

/**
 *  10/2/18.
 */
data class Wallet(val _this: Long) {
    external fun getWalletStatus()
    external fun getUtxosStatus()
    external fun syncWithNode()
    external fun sendMoney(sender: String, receiver: String, comment: String?, amount: Long, fee: Long)
    external fun calcChange(amount: Long)
    external fun getAddresses(own: Boolean)
    external fun generateNewAddress()
    external fun saveAddress(address: WalletAddressDTO, own: Boolean)

    @Deprecated("Need usage 'updateAddress'")
    external fun saveAddressChanges(addr: String, name: String, isNever: Boolean, makeActive: Boolean, makeExpired: Boolean)
    external fun updateAddress(addr: String, name: String, addressExpirationEnum: Int)
    external fun cancelTx(id: String)
    external fun deleteTx(id: String)
    external fun deleteAddress(walletID: String)
    external fun changeWalletPassword(password: String)
    external fun checkWalletPassword(password: String): Boolean
    external fun getPaymentInfo(txID: String)
    external fun verifyPaymentInfo(paymentInfo: String): PaymentInfoDTO
    external fun getCoinsByTx(txID : String)
    external fun changeNodeAddress(address: String)
    external fun exportOwnerKey(pass: String): String
    external fun importRecovery(path: String)
    external fun importDataFromJson(data: String)
    external fun exportDataToJson()
}
