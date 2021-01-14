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
import com.mw.beam.beamwallet.core.helpers.Tag
import com.mw.beam.beamwallet.core.helpers.TagHelper
import com.mw.beam.beamwallet.core.listeners.WalletListener
import io.reactivex.subjects.Subject

/**
 *  11/13/18.
 */
class ReceiveRepository : BaseRepository(), ReceiveContract.Repository {

    override fun saveAddress(address: WalletAddress, tags: List<Tag>) {
        getResult("saveAddressChanges") {

            var categories = mutableListOf<String>()

            for (t in tags) {
                categories.add(t.id)
            }

            var ids = categories.joinToString(";")

            var dto = address.toDTO()
            dto.category = ids
            dto.duration = 0L

            wallet?.saveAddress(dto, true)
        }
    }

    override fun getAddressTags(address: String): List<Tag> {
        return TagHelper.getTagsForAddress(address)
    }

    override fun getAllTags(): List<Tag> {
        return TagHelper.getAllTags()
    }
}
