package com.mw.beam.beamwallet.screens.add_contact

import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.helpers.Category
import com.mw.beam.beamwallet.core.helpers.QrHelper

class AddContactPresenter(view: AddContactContract.View?, repository: AddContactContract.Repository, private val state: AddContactState):
        BasePresenter<AddContactContract.View, AddContactContract.Repository>(view, repository), AddContactContract.Presenter {

    override fun onCancelPressed() {
        view?.close()
    }

    override fun onSavePressed() {
        val address = view?.getAddress() ?: ""
        val name = view?.getName() ?: ""
        val category = state.category

        if (QrHelper.isValidAddress(address)) {
            repository.saveContact(address, name.trim(), category)
            view?.close()
        } else {
            view?.showTokenError()
        }
    }

    override fun onScannedQR(text: String?) {
        if (text == null) return

        val scannedAddress = QrHelper.getScannedAddress(text)

        if (QrHelper.isValidAddress(scannedAddress)) {
            view?.setAddress(scannedAddress)
        } else {
            view?.showTokenError()
        }
    }

    override fun onSelectCategory(category: Category?) {
        state.category = category
    }

    override fun onAddNewCategoryPressed() {
        view?.navigateToAddNewCategory()
    }

    override fun onScanPressed() {
        view?.navigateToScanQr()
    }

    override fun onTokenChanged() {
        view?.hideTokenError()
    }
}