package com.mw.beam.beamwallet.screens.add_contact

import com.mw.beam.beamwallet.base_screen.BaseRepository
import com.mw.beam.beamwallet.core.entities.dto.WalletAddressDTO
import com.mw.beam.beamwallet.core.helpers.Category
import com.mw.beam.beamwallet.core.helpers.CategoryHelper

class AddContactRepository: BaseRepository(), AddContactContract.Repository {

    override fun saveContact(address: String, name: String, category: Category?) {
        getResult("saveContact") {
            CategoryHelper.changeCategoryForAddress(address, category)
            wallet?.saveAddress(WalletAddressDTO(address, name, "", System.currentTimeMillis(), 0, 0), false)
        }
    }

}