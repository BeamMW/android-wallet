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
import com.mw.beam.beamwallet.core.helpers.Tag
import com.mw.beam.beamwallet.core.helpers.ExpirePeriod
import io.reactivex.subjects.Subject

/**
 *  11/13/18.
 */
interface ReceiveContract {
    interface View : MvpView {
        fun init()
        fun initAddress(walletAddress: WalletAddress, transaction: ReceivePresenter.TransactionTypeOptions, expire: ReceivePresenter.TokenExpireOptions)
        fun getComment() : String?
        fun showQR(receiveToken: String)
        fun shareToken(receiveToken: String)
        fun close()
        fun getAmountFromArguments(): Long
        fun getAmount(): Double?
        fun getTxComment(): String?
        fun copyAddress(address: String)
        fun setAmount(newAmount: Double)
        fun handleExpandEditAddress(expand: Boolean)
        fun handleExpandAdvanced(expand: Boolean)
        fun showAddNewCategory()
        fun getLifecycleOwner(): LifecycleOwner
        fun getWalletAddressFromArguments(): WalletAddress?
        fun showSaveAddressDialog(nextStep: () -> Unit)
        fun showSaveChangesDialog(nextStep: () -> Unit)
        fun setupTagAction(isEmptyTags: Boolean)
        fun showTagsDialog(selectedTags: List<Tag>)
        fun showCreateTagDialog()
        fun setTags(tags: List<Tag>)
        fun showShowToken(receiveToken: String)
        fun showShareDialog(option1:String, option2:String)
        fun updateTokens(walletAddress: WalletAddress)
    }

    interface Presenter : MvpPresenter<View> {
        fun onShareTokenPressed()
        fun onShowQrPressed(receiveToken: String)
        fun onTokenPressed(receiveToken: String)
        fun onAdvancedPressed()
        fun onEditAddressPressed()
        fun onSaveAddressPressed()
        fun onBackPressed()
        fun onAddressLongPressed()
        fun onTagActionPressed()
        fun onSelectTags(tags: List<Tag>)
        fun onCreateNewTagPressed()
        fun onPermanentPressed()
        fun onOneTimePressed()
        fun onRegularPressed()
        fun onMaxPrivacyPressed()
        fun onSwitchPressed()
        fun updateToken()
    }

    interface Repository : MvpRepository {
        fun saveAddress(address: WalletAddress, tags: List<Tag>)
        fun getAddressTags(address: String): List<Tag>
        fun getAllTags(): List<Tag>
        fun generateNewAddress(): Subject<WalletAddress>
    }
}
