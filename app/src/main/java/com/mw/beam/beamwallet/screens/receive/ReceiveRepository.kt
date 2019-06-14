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

import com.mw.beam.beamwallet.base_screen.BaseRepository
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.Category
import com.mw.beam.beamwallet.core.helpers.CategoryHelper
import com.mw.beam.beamwallet.core.listeners.WalletListener
import io.reactivex.subjects.Subject

/**
 * Created by vain onnellinen on 11/13/18.
 */
class ReceiveRepository : BaseRepository(), ReceiveContract.Repository {

    override fun generateNewAddress(): Subject<WalletAddress> {
        return getResult(WalletListener.subOnGeneratedNewAddress, "generateNewAddress") {
            wallet?.generateNewAddress()
        }
    }

    override fun updateAddress(address: WalletAddress) {
        getResult("updateAddress") {
            val isNever = address.duration == 0L
            wallet?.saveAddressChanges(address.walletID, address.label, isNever, makeActive = !isNever, makeExpired = false)
        }
    }

    override fun saveAddress(address: WalletAddress) {
        getResult("saveAddress") {
            wallet?.saveAddress(address.toDTO(), true)
        }
    }

    override fun getAllCategory(): List<Category> {
        return CategoryHelper.getAllCategory()
    }

    override fun getCategory(address: String): Category? {
        return CategoryHelper.getCategoryForAddress(address)
    }

    override fun changeCategoryForAddress(address: String, category: Category?) {
        CategoryHelper.changeCategoryForAddress(address, category)
    }
}
