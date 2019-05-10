package com.mw.beam.beamwallet.screens.category

import com.mw.beam.beamwallet.base_screen.MvpPresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.entities.OnAddressesData
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.Category
import io.reactivex.subjects.Subject

interface CategoryContract {
    interface View: MvpView {
        fun getCategoryId(): String
        fun init(category: Category)
        fun updateAddresses(addresses: List<WalletAddress>)
        fun navigateToEditCategory(categoryId: String)
        fun finish()
    }

    interface Presenter: MvpPresenter<View> {
        fun onEditCategoryPressed()
        fun onDeleteCategoryPressed()
    }

    interface Repository: MvpRepository {
        fun getAddresses(): Subject<OnAddressesData>
        fun deleteCategory(category: Category)
        fun getCategoryFromId(categoryId: String): Category?
    }
}