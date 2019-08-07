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

package com.mw.beam.beamwallet.screens.save_address

import com.mw.beam.beamwallet.base_screen.MvpPresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.Tag

interface SaveAddressContract {
    interface View: MvpView {
        fun getAddress(): String
        fun init(address: String, tag: Tag?)
        fun showAddNewCategory()
        fun getName(): String
        fun close()
    }
    interface Presenter: MvpPresenter<View> {
        fun onSelectCategory(tag: Tag?)
        fun onAddNewCategoryPressed()
        fun onSavePressed()
        fun onCancelPressed()
    }
    interface Repository: MvpRepository {
        fun saveAddress(address: WalletAddress, own: Boolean)
        fun getCategory(address: String): Tag?
        fun changeCategoryForAddress(address: String, tag: Tag?)
    }
}