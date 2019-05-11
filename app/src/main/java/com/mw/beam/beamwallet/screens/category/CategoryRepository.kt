package com.mw.beam.beamwallet.screens.category

import com.mw.beam.beamwallet.base_screen.BaseRepository
import com.mw.beam.beamwallet.core.entities.OnAddressesData
import com.mw.beam.beamwallet.core.helpers.Category
import com.mw.beam.beamwallet.core.helpers.CategoryHelper
import com.mw.beam.beamwallet.core.listeners.WalletListener
import io.reactivex.subjects.Subject

class CategoryRepository: BaseRepository(), CategoryContract.Repository {
    override fun deleteCategory(category: Category) {
        CategoryHelper.deleteCategory(category)
    }

    override fun getCategoryFromId(categoryId: String): Category? {
        return CategoryHelper.getCategory(categoryId)
    }

    override fun getAddresses(): Subject<OnAddressesData> {
        return getResult(WalletListener.subOnAddresses, "getAddresses") {
            wallet?.getAddresses(true)
            wallet?.getAddresses(false)
        }
    }
}