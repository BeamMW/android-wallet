package com.mw.beam.beamwallet.screens.save_address

import com.mw.beam.beamwallet.base_screen.BaseRepository
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.Category
import com.mw.beam.beamwallet.core.helpers.CategoryHelper

class SaveAddressRepository: BaseRepository(), SaveAddressContract.Repository {
    override fun saveAddress(address: WalletAddress, own: Boolean) {
        getResult("saveAddress") {
            wallet?.saveAddress(address.toDTO(), own)
        }
    }

    override fun getCategory(address: String): Category? {
        return CategoryHelper.getCategoryForAddress(address)
    }

    override fun changeCategoryForAddress(address: String, category: Category?) {
        CategoryHelper.changeCategoryForAddress(address, category)
    }
}