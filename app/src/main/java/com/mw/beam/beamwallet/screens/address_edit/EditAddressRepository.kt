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
import com.mw.beam.beamwallet.core.AppManager
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.entities.dto.WalletAddressDTO
import com.mw.beam.beamwallet.core.helpers.TrashManager

/**
 *  3/5/19.
 */
class EditAddressRepository : BaseRepository(), EditAddressContract.Repository {

    override fun deleteAddress(walletAddress: WalletAddress, txDescriptions: List<TxDescription>) {
        getResult("deleteAddress") {
            TrashManager.add(walletAddress.id, TrashManager.ActionData(txDescriptions, listOf(walletAddress)))
        }
    }

    override fun saveAddressChanges(addr: String, name: String, makeExpired: Boolean, makeActive: Boolean, isExtend: Boolean) {
        getResult("saveAddressChanges") {
            var addressExpiration = WalletAddressDTO.WalletAddressExpirationStatus.AsIs

            if(makeExpired) {
                addressExpiration = WalletAddressDTO.WalletAddressExpirationStatus.Expired
            }
            else if(makeActive || isExtend) {
                addressExpiration = WalletAddressDTO.WalletAddressExpirationStatus.Auto
            }

            wallet?.updateAddress(addr, name, addressExpiration.ordinal)
        }
    }

    override fun saveAddress(address: WalletAddress, own: Boolean) {
        getResult("saveAddress") {
            wallet?.saveAddress(address.toDTO(), own)
        }
    }
}
