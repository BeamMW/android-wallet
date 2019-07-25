package com.mw.beam.beamwallet.screens.add_contact

import com.mw.beam.beamwallet.base_screen.MvpPresenter
import com.mw.beam.beamwallet.base_screen.MvpRepository
import com.mw.beam.beamwallet.base_screen.MvpView
import com.mw.beam.beamwallet.core.helpers.Category

interface AddContactContract {
    interface View : MvpView {
        fun getAddress(): String
        fun getName(): String
        fun showTokenError()
        fun hideTokenError()
        fun close()
        fun navigateToScanQr()
        fun navigateToAddNewCategory()
        fun setAddress(address: String)
    }

    interface Presenter : MvpPresenter<View> {
        fun onTokenChanged()
        fun onCancelPressed()
        fun onSelectCategory(category: Category?)
        fun onAddNewCategoryPressed()
        fun onSavePressed()
        fun onScanPressed()
        fun onScannedQR(text: String?)
    }

    interface Repository: MvpRepository {
        fun saveContact(address: String, name: String, category: Category?)
    }
}