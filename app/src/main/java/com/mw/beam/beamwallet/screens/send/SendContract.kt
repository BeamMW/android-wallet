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

import com.mw.beam.beamwallet.base_screen.MvpPresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.entities.OnAddressesData
import com.mw.beam.beamwallet.core.entities.WalletStatus
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
        fun updateUI(shouldShowParams: Boolean)
        fun hasErrors(availableAmount: Long): Boolean
        fun clearErrors()
        fun clearToken(clearedToken: String?)
        fun init()
        fun close()
        fun setAddressError()
        fun clearAddressError()
        fun showCantSendToExpiredError()
        fun setAddress(address: String)
        fun scanQR()
        fun isPermissionGranted(): Boolean
        fun showPermissionRequiredAlert()
    }

    interface Presenter : MvpPresenter<View> {
        fun onSend()
        fun onTokenChanged(rawToken: String?)
        fun onAmountChanged()
        fun onScannedQR(address: String?)
        fun onScanQrPressed()
        fun onRequestPermissionsResult(result: PermissionStatus)
    }

    interface Repository : MvpRepository {
        fun sendMoney(token: String, comment: String?, amount: Long, fee: Long)
        fun getWalletStatus(): Subject<WalletStatus>
        fun onCantSendToExpired(): Subject<Any>
        fun checkAddress(address: String?): Boolean
        fun getAddresses(): Subject<OnAddressesData>
    }
}
