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

import com.mw.beam.beamwallet.base_screen.BaseRepository
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.Tag
import com.mw.beam.beamwallet.core.helpers.TagHelper

/**
 * Created by vain onnellinen on 3/5/19.
 */
class EditAddressRepository : BaseRepository(), EditAddressContract.Repository {

    override fun saveAddressChanges(addr: String, name: String, isNever: Boolean, makeActive: Boolean, makeExpired: Boolean) {
        getResult("saveAddressChanges") {
            wallet?.saveAddressChanges(addr, name, isNever, makeActive, makeExpired)
        }
    }

    override fun saveAddress(address: WalletAddress, own: Boolean) {
        getResult("saveAddress") {
            wallet?.saveAddress(address.toDTO(), own)
        }
    }

//    override fun updateAddress(address: WalletAddress, own: Boolean) {
//        getResult("updateAddress") {
//            val addressExpiration = when {
//                address.isExpired -> WalletAddressDTO.WalletAddressExpirationStatus.Expired
//                address.duration == 0L -> WalletAddressDTO.WalletAddressExpirationStatus.Never
//                else -> WalletAddressDTO.WalletAddressExpirationStatus.OneDay
//            }
//
//            wallet?.updateAddress(address.walletID, address.label, addressExpiration.ordinal)
//        }
//    }

    override fun getCategory(address: String): Tag? {
        return TagHelper.getTagsForAddress(address)
    }

    override fun changeCategoryForAddress(address: String, tag: Tag?) {
        TagHelper.changeTagsForAddress(address, tag)
    }
}
