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

package com.mw.beam.beamwallet.screens.addresses

import com.mw.beam.beamwallet.base_screen.MvpPresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.entities.OnAddressesData
import com.mw.beam.beamwallet.core.entities.OnTxStatusData
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.Category
import io.reactivex.subjects.Subject

/**
 * Created by vain onnellinen on 2/28/19.
 */
interface AddressesContract {
    interface View : MvpView {
        fun init()
        fun updateAddresses(tab: Tab, addresses: List<WalletAddress>)
        fun showAddressDetails(address: WalletAddress)
    }

    interface Presenter : MvpPresenter<View> {
        fun onAddressPressed(address: WalletAddress)
        fun onSearchCategoryForAddress(address: String): Category?
    }

    interface Repository : MvpRepository {
        fun getAddresses(): Subject<OnAddressesData>
        fun getTxStatus(): Subject<OnTxStatusData>
        fun getCategoryForAddress(address: String): Category?
    }
}
