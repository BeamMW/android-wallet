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
import com.mw.beam.beamwallet.core.entities.dto.TransactionParametersDTO
import com.mw.beam.beamwallet.core.entities.dto.WalletAddressDTO

/**
 *  10/2/18.
 */
data class Wallet(val _this: Long) {
    external fun getWalletStatus()
    external fun getTransactions()
    external fun getUtxosStatus()
    external fun syncWithNode()
  //  external fun sendMoney(sender: String, receiver: String, comment: String?, amount: Long, fee: Long)
    external fun calcShieldedCoinSelectionInfo(amount: Long, fee: Long, isShielded: Boolean)

    //-(void):(double)amount fee:(double)fee isShielded:(BOOL) isShielded result:(FeecalculatedBlock _Nonnull )block;
    external fun calcChange(amount: Long)
    external fun getAddresses(own: Boolean)
    external fun generateNewAddress()
    external fun saveAddress(address: WalletAddressDTO, own: Boolean)

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

    external fun switchOnOffExchangeRates(isActive: Boolean)
    external fun switchOnOffNotifications(type: Int, isActive: Boolean)

    external fun getExchangeRates()

    external fun getNotifications()
    external fun markNotificationAsRead(id : String)
    external fun deleteNotification(id : String)

    external fun generateRegularAddress(isPermanentAddress: Boolean, amount: Long, walletId: String): String
    external fun generateOfflineAddress(amount: Long, walletId: String): String
    external fun generateMaxPrivacyAddress(amount: Long, walletId: String)

    external fun isToken(token: String): Boolean
    external fun isAddress(address: String): Boolean
    external fun sendTransaction(sender: String, receiver: String, comment: String?, amount: Long, fee: Long)
    external fun getTransactionParameters(token: String, requestInfo: Boolean): TransactionParametersDTO

    external fun isConnectionTrusted(): Boolean
    external fun callMyMethod()
    external fun getPublicAddress()
    external fun exportTxHistoryToCsv()

    external fun getMaxPrivacyLockTimeLimitHoursAsync()
    external fun getMaxPrivacyLockTimeLimitHours(): Long
    external fun setMaxPrivacyLockTimeLimitHours(hours: Long)

    external fun getMaturityHours(id: Long): Long

    external fun rescan()
    external fun enableBodyRequests(enable: Boolean)
}
