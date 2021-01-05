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

import com.mw.beam.beamwallet.base_screen.BaseRepository
import com.mw.beam.beamwallet.core.entities.OnAddressesData
import com.mw.beam.beamwallet.core.entities.OnTxStatusData
import com.mw.beam.beamwallet.core.entities.TxDescription
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.Tag
import com.mw.beam.beamwallet.core.helpers.TagHelper
import com.mw.beam.beamwallet.core.helpers.TrashManager
import com.mw.beam.beamwallet.core.listeners.WalletListener
import io.reactivex.Observable
import io.reactivex.subjects.Subject

/**
 *  3/4/19.
 */
class AddressRepository : BaseRepository(), AddressContract.Repository {

    override fun deleteAddress(walletAddress: WalletAddress, txDescriptions: List<TxDescription>) {
        getResult("deleteAddress") {
            TrashManager.add(walletAddress.id, TrashManager.ActionData(txDescriptions, listOf(walletAddress)))
        }
    }

    override fun getAddressTags(address: String): List<Tag> {
        return TagHelper.getTagsForAddress(address)
    }
}
