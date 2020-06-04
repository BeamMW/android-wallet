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

package com.mw.beam.beamwallet.screens.unlink

import android.view.Menu
import android.view.MenuInflater
import androidx.lifecycle.LifecycleOwner
import com.mw.beam.beamwallet.base_screen.MvpPresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView


/**
 *  11/13/18.
 */
interface UnlinkContract {

    interface View : MvpView {
        fun getAmount(): Double
        fun getFee(): Long
        fun updateUI(defaultFee: Int, isEnablePrivacyMode: Boolean)
        fun hasErrors(availableAmount: Long, isEnablePrivacyMode: Boolean): Boolean
        fun hasAmountError(amount: Long, fee: Long, availableAmount: Long, isEnablePrivacyMode: Boolean): Boolean
        fun clearErrors()
        fun init(defaultFee: Int, max: Int)
        fun setAmount(amount: Double)
        fun setFee(feeAmount: String)
        fun updateAvailable(available: Long)
        fun isAmountErrorShown() : Boolean
        fun showActivatePrivacyModeDialog()
        fun configPrivacyStatus(isEnable: Boolean)
        fun createOptionsMenu(menu: Menu?, inflater: MenuInflater, isEnablePrivacyMode: Boolean)
        fun showConfirmTransaction(amount: Long, fee: Long)
        fun updateFeeTransactionVisibility()
        fun getLifecycleOwner(): LifecycleOwner
        fun updateFeeViews(clearAmountFocus: Boolean = true)
        fun showFeeDialog()
        fun requestFocusToAmount()
        fun showMinFeeError()
        fun setupMinFee(fee: Int)
    }

    interface Presenter : MvpPresenter<View> {
        fun onNext()
        fun onAmountChanged()
        fun onFeeChanged(rawFee: String?)
        fun onAmountUnfocused()
        fun onChangePrivacyModePressed()
        fun onPrivacyModeActivated()
        fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater)
        fun onSendAllPressed()
        fun onLongPressFee()
        fun onEnterFee(rawFee: String?)
        fun onCancelDialog()
    }

    interface Repository : MvpRepository {
        fun isNeedConfirmEnablePrivacyMode(): Boolean
    }
}
