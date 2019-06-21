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

package com.mw.beam.beamwallet.screens.send

import android.view.Menu
import android.view.MenuInflater
import androidx.lifecycle.LifecycleOwner
import com.mw.beam.beamwallet.base_screen.MvpPresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.entities.OnAddressesData
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.entities.WalletStatus
import com.mw.beam.beamwallet.core.helpers.Category
import com.mw.beam.beamwallet.core.helpers.ExpirePeriod
import com.mw.beam.beamwallet.core.helpers.PermissionStatus
import io.reactivex.subjects.Subject

/**
 * Created by vain onnellinen on 11/13/18.
 */
interface SendContract {

    interface View : MvpView {
        fun getAmount(): Double
        fun getFee(): Long
        fun getToken(): String
        fun getComment(): String?
        fun updateUI(defaultFee: Int, isEnablePrivacyMode: Boolean)
        fun hasErrors(availableAmount: Long, isEnablePrivacyMode: Boolean): Boolean
        fun configOutgoingAddress(walletAddress: WalletAddress, isGenerated: Boolean)
        fun clearErrors()
        fun clearToken(clearedToken: String?)
        fun init(defaultFee: Int, maxFee: Int)
        fun close()
        fun setAddressError()
        fun clearAddressError()
        fun showCantSendToExpiredError()
        fun showCantPasteError()
        fun showNotBeamAddressError()
        fun setAddress(address: String)
        fun setAmount(amount: Double)
        fun setComment(comment: String)
        fun setFee(feeAmount: String)
        fun scanQR()
        fun updateAvailable(availableString: String)
        fun isAmountErrorShown() : Boolean
        fun isPermissionGranted(): Boolean
        fun showPermissionRequiredAlert()
        fun showActivatePrivacyModeDialog()
        fun configPrivacyStatus(isEnable: Boolean)
        fun createOptionsMenu(menu: Menu?, inflater: MenuInflater, isEnablePrivacyMode: Boolean)
        fun pendingSendMoney(outgoingAddress: String, token: String, comment: String?, amount: Long, fee: Long)
        fun getAddressFromArguments(): String?
        fun getAmountFromArguments(): Long
        fun showChangeAddressFragment()
        fun getLifecycleOwner(): LifecycleOwner
        fun getCommentOutgoingAddress(): String
        fun handleExpandEditAddress(expand: Boolean)
        fun handleExpandAdvanced(expand: Boolean)
        fun configCategory(currentCategory: Category?, categories: List<Category>)
        fun updateFeeViews()
        fun showFeeDialog()
    }

    interface Presenter : MvpPresenter<View> {
        fun onSend()
        fun onTokenChanged(rawToken: String?)
        fun onAmountChanged()
        fun onFeeChanged(rawFee: String?)
        fun onScannedQR(text: String?)
        fun onScanQrPressed()
        fun onRequestPermissionsResult(result: PermissionStatus)
        fun onConfirm()
        fun onChangePrivacyModePressed()
        fun onPrivacyModeActivated()
        fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater)
        fun onCancelDialog()
        fun onSendAllPressed()
        fun onAdvancedPressed()
        fun onEditAddressPressed()
        fun onChangeAddressPressed()
        fun onExpirePeriodChanged(period : ExpirePeriod)
        fun onSelectedCategory(category: Category?)
        fun onAddressChanged(walletAddress: WalletAddress)
        fun onLongPressFee()
        fun onEnterFee(rawFee: String?)
    }

    interface Repository : MvpRepository {
        fun generateNewAddress() : Subject<WalletAddress>
        fun getWalletStatus(): Subject<WalletStatus>
        fun onCantSendToExpired(): Subject<Any>
        fun checkAddress(address: String?): Boolean
        fun isConfirmTransactionEnabled(): Boolean
        fun getAddresses(): Subject<OnAddressesData>
        fun isNeedConfirmEnablePrivacyMode(): Boolean
        fun saveAddress(address: WalletAddress)
        fun getCategory(address: String): Category?
        fun getAllCategory(): List<Category>
        fun changeCategoryForAddress(address: String, category: Category?)
        fun updateAddress(address: WalletAddress)
    }
}
