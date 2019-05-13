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

package com.mw.beam.beamwallet.screens.address_edit

import com.mw.beam.beamwallet.base_screen.MvpPresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.Category
import com.mw.beam.beamwallet.core.helpers.ExpirePeriod

/**
 * Created by vain onnellinen on 3/5/19.
 */
interface EditAddressContract {
    interface View : MvpView {
        fun getAddress(): WalletAddress
        fun init(address: WalletAddress)
        fun configExpireSpinnerVisibility(shouldShow: Boolean)
        fun configExpireSpinnerTime(shouldExpireNow: Boolean)
        fun configSaveButton(shouldEnable: Boolean)
        fun finishScreen()
        fun configCategory(categories: List<Category>)
    }

    interface Presenter : MvpPresenter<View> {
        fun onSwitchCheckedChange(isChecked: Boolean)
        fun onExpirePeriodChanged(period : ExpirePeriod)
        fun onSavePressed()
        fun onChangeComment(comment: String)
    }

    interface Repository : MvpRepository {
        fun saveAddress(addr: String, name: String, isNever: Boolean, makeActive: Boolean, makeExpired: Boolean)
    }
}
