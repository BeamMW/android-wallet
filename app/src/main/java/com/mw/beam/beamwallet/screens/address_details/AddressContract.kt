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

package com.mw.beam.beamwallet.screens.address_details

import android.view.Menu
import com.mw.beam.beamwallet.base_screen.MvpPresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.entities.OnTxStatusData
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.entities.WalletAddress
import io.reactivex.subjects.Subject

/**
 * Created by vain onnellinen on 3/4/19.
 */
interface AddressContract {
    interface View : MvpView {
        fun getAddress(): WalletAddress
        fun init(address: WalletAddress)
        fun configTransactions(transactions: List<TxDescription>)
        fun showTransactionDetails(txDescription: TxDescription)
        fun showEditAddressScreen(address: WalletAddress)
        fun configMenuItems(menu: Menu?, address: WalletAddress)
        fun finishScreen()
    }

    interface Presenter : MvpPresenter<View> {
        fun onShowQR()
        fun onEditAddress()
        fun onAddressWasEdited()
        fun onDeleteAddress()
        fun onMenuCreate(menu: Menu?)
        fun onTransactionPressed(txDescription: TxDescription)
    }

    interface Repository : MvpRepository {
        fun deleteAddress(addressId: String)
        fun getTxStatus(): Subject<OnTxStatusData>
    }
}
