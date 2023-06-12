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

package com.mw.beam.beamwallet.screens.receive

import androidx.lifecycle.LifecycleOwner

import com.mw.beam.beamwallet.base_screen.MvpPresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.entities.WalletAddress

interface ReceiveContract {
    interface View : MvpView {
        fun init()
        fun initAddress(walletAddress: WalletAddress, transaction: ReceivePresenter.TransactionTypeOptions)
        fun getComment() : String?
        fun showQR(receiveToken: String)
        fun shareToken(receiveToken: String)
        fun copyToken(receiveToken: String)
        fun close()
        fun getAssetId():Int
        fun getAmountFromArguments(): Long
        fun getAmount(): Double?
        fun getTxComment(): String?
        fun setAmount(newAmount: Double)
        fun handleExpandAmount(expand: Boolean)
        fun handleExpandComment(expand: Boolean)
        fun handleExpandAdvanced(expand: Boolean)
        fun getLifecycleOwner(): LifecycleOwner
        fun getWalletAddressFromArguments(): WalletAddress?
        fun showSaveAddressDialog(nextStep: () -> Unit)
        fun showSaveChangesDialog(nextStep: () -> Unit)
        fun showShowToken(receiveToken: String)
        fun updateTokens(walletAddress: WalletAddress, transaction: ReceivePresenter.TransactionTypeOptions)
    }

    interface Presenter : MvpPresenter<View> {
        fun onShareTokenPressed()
        fun onShowQrPressed()
        fun onTokenPressed()
        fun onCopyPressed()
        fun onCommentPressed()
        fun onAmountPressed()
        fun onAdvancedPressed()
        fun onSaveAddressPressed()
        fun onBackPressed()
        fun onRegularPressed()
        fun onMaxPrivacyPressed()
        fun updateToken()
//        fun saveToken()
    }

    interface Repository : MvpRepository {
        fun saveAddress(address: WalletAddress)
    }
}
