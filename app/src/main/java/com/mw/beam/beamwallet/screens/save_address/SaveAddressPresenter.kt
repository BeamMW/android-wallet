package com.mw.beam.beamwallet.screens.save_address

import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.entities.dto.WalletAddressDTO
import com.mw.beam.beamwallet.core.helpers.Category

class SaveAddressPresenter(view: SaveAddressContract.View?, repository: SaveAddressContract.Repository, private val state: SaveAddressState)
    : BasePresenter<SaveAddressContract.View, SaveAddressContract.Repository>(view, repository), SaveAddressContract.Presenter {

    override fun onViewCreated() {
        super.onViewCreated()
        view?.apply {
            state.address = getAddress()
            state.category = repository.getCategory(state.address)

            init(state.address, state.category)
        }
    }

    override fun onSavePressed() {
        view?.apply {
            val address = WalletAddress(WalletAddressDTO(state.address, getName(), "", System.currentTimeMillis(), 0, 0))
            repository.saveAddress(address, false)
            repository.changeCategoryForAddress(state.address, state.category)
            close()
        }
    }

    override fun onSelectCategory(category: Category?) {
        state.category = category
    }

    override fun onAddNewCategoryPressed() {
        view?.showAddNewCategory()
    }

    override fun onCancelPressed() {
        view?.close()
    }

}