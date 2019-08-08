package com.mw.beam.beamwallet.screens.add_contact

import com.mw.beam.beamwallet.base_screen.BaseRepository
import com.mw.beam.beamwallet.core.entities.dto.WalletAddressDTO
import com.mw.beam.beamwallet.core.helpers.Tag
import com.mw.beam.beamwallet.core.helpers.TagHelper

class AddContactRepository: BaseRepository(), AddContactContract.Repository {

    override fun saveContact(address: String, name: String, tags: List<Tag>) {
        getResult("saveContact") {
            TagHelper.changeTagsForAddress(address, tags)
            wallet?.saveAddress(WalletAddressDTO(address, name, "", System.currentTimeMillis(), 0, 0), false)
        }
    }

    override fun getAddressTags(address: String): List<Tag> {
        return TagHelper.getTagsForAddress(address)
    }

    override fun getAllTags(): List<Tag> {
        return TagHelper.getAllTags()
    }
}