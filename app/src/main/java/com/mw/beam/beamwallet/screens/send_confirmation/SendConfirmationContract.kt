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

package com.mw.beam.beamwallet.screens.send_confirmation

import com.mw.beam.beamwallet.base_screen.MvpPresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.entities.OnAddressesData
import com.mw.beam.beamwallet.core.entities.WalletAddress
import io.reactivex.subjects.Subject

interface SendConfirmationContract {
    interface View : MvpView {
        fun getAddress(): String
        fun getOutgoingAddress(): String
        fun getAmount(): Long
        fun getFee(): Long
        fun getChange(): Long
        fun getShieldedInputsFee(): Long
        fun getComment(): String?
        fun getAddressType(): Int
        fun getRemaining(): Int
        fun getIsOffline(): Boolean
        fun getAssetId(): Int
        fun init(address: String, outgoingAddress: String, amount: Double, fee: Long, addressType: Int)
        fun configureContact(walletAddress: WalletAddress)
        fun configureOutAddress(walletAddress: WalletAddress)
        fun configUtxoInfo(usedUtxo: Double, changedUtxo: Double)
        fun showSaveAddressFragment(address: String)
        fun delaySend(outgoingAddress: String, token: String, comment: String?, amount: Long, fee: Long, isOffline:Boolean?)
        fun showWallet()
        fun showConfirmDialog()
    }

    interface Presenter : MvpPresenter<View> {
        fun onSendPressed()
        fun onConfirmed()
    }

    interface Repository : MvpRepository {
        fun isConfirmTransactionEnabled(): Boolean
        fun getAddresses(): Subject<OnAddressesData>
        fun calcChange(amount: Long, assetId:Int): Subject<Long>
    }
}