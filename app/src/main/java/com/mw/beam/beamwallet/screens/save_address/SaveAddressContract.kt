package com.mw.beam.beamwallet.screens.save_address

import com.mw.beam.beamwallet.base_screen.MvpPresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.Category

interface SaveAddressContract {
    interface View: MvpView {
        fun getAddress(): String
        fun init(address: String, category: Category?)
        fun showAddNewCategory()
        fun getName(): String
        fun close()
    }
    interface Presenter: MvpPresenter<View> {
        fun onSelectCategory(category: Category?)
        fun onAddNewCategoryPressed()
        fun onSavePressed()
        fun onCancelPressed()
    }
    interface Repository: MvpRepository {
        fun saveAddress(address: WalletAddress, own: Boolean)
        fun getCategory(address: String): Category?
        fun changeCategoryForAddress(address: String, category: Category?)
    }
}