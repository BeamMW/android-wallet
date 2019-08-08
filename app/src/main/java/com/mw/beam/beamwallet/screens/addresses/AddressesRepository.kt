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

import com.mw.beam.beamwallet.base_screen.BaseRepository
import com.mw.beam.beamwallet.core.entities.OnAddressesData
import com.mw.beam.beamwallet.core.entities.OnTxStatusData
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.Tag
import com.mw.beam.beamwallet.core.helpers.TagHelper
import com.mw.beam.beamwallet.core.helpers.TrashManager
import com.mw.beam.beamwallet.core.listeners.WalletListener
import io.reactivex.Observable
import io.reactivex.subjects.Subject

/**
 * Created by vain onnellinen on 2/28/19.
 */
class AddressesRepository : BaseRepository(), AddressesContract.Repository {

    override fun getAddresses(): Subject<OnAddressesData> {
        return getResult(WalletListener.subOnAddresses, "getAddresses") {
            wallet?.getAddresses(true)
            wallet?.getAddresses(false)
        }
    }

    override fun getTxStatus(): Observable<OnTxStatusData> {
        return getResult(WalletListener.obsOnTxStatus, "getTxStatus") {
            wallet?.getWalletStatus()
        }
    }

    override fun getAddressTags(address: String): List<Tag> {
        return TagHelper.getTagsForAddress(address)
    }

    override fun getTrashSubject(): Subject<TrashManager.Action> {
        return TrashManager.subOnTrashChanged
    }

    override fun getAllAddressesInTrash(): List<WalletAddress> {
        return TrashManager.getAllData().addresses
    }
}
