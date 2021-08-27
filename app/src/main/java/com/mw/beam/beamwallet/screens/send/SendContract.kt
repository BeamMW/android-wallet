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
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.ExpirePeriod
import com.mw.beam.beamwallet.core.helpers.PermissionStatus
import io.reactivex.subjects.Subject

/**
 *  11/13/18.
 */
interface SendContract {

    interface View : MvpView {
        fun getAmountText():String
        fun getAmount(): Double
        fun getFee(): Long
        fun isOffline(): Boolean
        fun getComment(): String?
        fun getToken(): String
        fun updateUI(defaultFee: Int, isEnablePrivacyMode: Boolean)
        fun hasErrors(availableAmount: Long, isEnablePrivacyMode: Boolean): Boolean
        fun hasAmountError(amount: Long, fee: Long, availableAmount: Long, isEnablePrivacyMode: Boolean): Boolean
        fun configOutgoingAddress(walletAddress: WalletAddress, isGenerated: Boolean)
        fun clearErrors()
        fun clearToken(clearedToken: String?)
        fun init(defaultFee: Int, max: Int)
        fun updateMaxPrivacyCount(count: Int)
       // fun setAddressError()
        fun clearAddressError()
        fun showCantSendToExpiredError()
        fun showCantPasteError()
        fun showNotBeamAddressError()
        fun setAddress(address: String)
        fun setAmount(amount: Double)
        fun setComment(comment: String)
        fun setFee(feeAmount: String)
        fun scanQR()
        fun updateAvailable(available: Long)
        fun isAmountErrorShown() : Boolean
        fun isPermissionGranted(): Boolean
        fun showPermissionRequiredAlert()
        fun showActivatePrivacyModeDialog()
        fun configPrivacyStatus(isEnable: Boolean)
        fun createOptionsMenu(menu: Menu?, inflater: MenuInflater, isEnablePrivacyMode: Boolean)
        fun showConfirmTransaction(outgoingAddress: String, token: String, comment: String?, amount: Long, fee: Long)
        fun getAddressFromArguments(): String?
        fun getAmountFromArguments(): Long
        fun showChangeAddressFragment(generatedAddress: WalletAddress?)
        fun updateFeeTransactionVisibility()
        fun getLifecycleOwner(): LifecycleOwner
        fun getCommentOutgoingAddress(): String
        fun handleExpandEditAddress(expand: Boolean)
        fun handleExpandAdvanced(expand: Boolean)
        fun updateFeeViews(clearAmountFocus: Boolean = true)
        fun showFeeDialog()
        fun setSendContact(walletAddress: WalletAddress?)
        fun handleAddressSuggestions(addresses: List<WalletAddress>?, showSuggestions: Boolean = true)
        fun requestFocusToAmount()
        fun showMinFeeError()
        fun setupMinFee(fee: Int)
        fun setupMaxFee(max: Int, min:Int)
        fun onTrimAddress()
        fun showTokenFragment()
        fun onCalcFeeDone()
        fun isIgnoreAmountWatcher():Boolean
    }

    interface Presenter : MvpPresenter<View> {
        fun onNext()
        fun onMaxPrivacy(value:Boolean)
        fun onTokenChanged(rawToken: String?, searchAddress: Boolean = true)
        fun onAmountChanged()
        fun onFeeChanged(rawFee: String?)
        fun requestFee()
        fun onAmountUnfocused()
        fun onScannedQR(text: String?, requestFocus: Boolean)
        fun onScanQrPressed()
        fun onRequestPermissionsResult(result: PermissionStatus)
        fun onChangePrivacyModePressed()
        fun showTokenFragmentPressed()
        fun onPrivacyModeActivated()
        fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater)
        fun onCancelDialog()
        fun onSendAllPressed()
        fun onAdvancedPressed()
        fun onEditAddressPressed()
        fun onChangeAddressPressed()
        fun onExpirePeriodChanged(period : ExpirePeriod)
        fun onLabelAddressChanged(text: String)
        fun onAddressChanged(walletAddress: WalletAddress)
        fun onLongPressFee()
        fun onEnterFee(rawFee: String?)
        fun onSelectAddress(walletAddress: WalletAddress)
        fun onPaste()
    }

    interface Repository : MvpRepository {
        fun generateNewAddress()
        fun onCantSendToExpired(): Subject<Any>
        fun checkAddress(address: String?): Boolean
        fun isConfirmTransactionEnabled(): Boolean
        fun isNeedConfirmEnablePrivacyMode(): Boolean
        fun saveAddress(address: WalletAddress)
        fun updateAddress(address: WalletAddress)
    }
}
