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

package com.mw.beam.beamwallet.screens.category

import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.TrashManager
import io.reactivex.disposables.Disposable

class CategoryPresenter(view: CategoryContract.View?, repository: CategoryContract.Repository, private val state: CategoryState)
    : BasePresenter<CategoryContract.View, CategoryContract.Repository>(view, repository), CategoryContract.Presenter {
    private lateinit var addressesSubscription: Disposable
    private lateinit var trashSubscription: Disposable

    override fun onStart() {
        super.onStart()
        state.category = repository.getCategoryFromId(view?.getCategoryId() ?: "")

        state.category?.let {
            view?.init(it)

            val addresses = state.getAddresses().filter { address -> it.addresses.contains(address.walletID) }

            state.setAddresses(addresses)
            view?.updateAddresses(addresses)
        }
    }

    override fun initSubscriptions() {
        super.initSubscriptions()

        addressesSubscription = repository.getAddresses().subscribe {
            val list = it.addresses?.filter(::filterAddresses)

            if (list != null) {
                state.addAddresses(list)
            }

            state.deleteAddresses(repository.getAllAddressesInTrash())

            updateView()
        }

        trashSubscription = repository.getTrashSubject().subscribe {
            when(it.type) {
                TrashManager.ActionType.Added -> {
                    state.deleteAddresses(it.data.addresses)
                    updateView()
                }
                TrashManager.ActionType.Restored -> {
                    state.addAddresses(it.data.addresses.filter(::filterAddresses))
                    updateView()
                }
                TrashManager.ActionType.Removed -> {}
            }
        }
    }

    private fun filterAddresses(walletAddress: WalletAddress) = state.category?.addresses?.contains(walletAddress.walletID) ?: false

    private fun updateView() {
        view?.updateAddresses(state.getAddresses())
    }

    override fun onAddressPressed(address: WalletAddress) {
        view?.showAddressDetails(address)
    }

    override fun getSubscriptions(): Array<Disposable>? = arrayOf(addressesSubscription, trashSubscription)

    override fun onEditCategoryPressed() {
        state.category?.let { view?.navigateToEditCategory(it.id) }
    }

    override fun onDeleteCategoryConfirmed() {
        state.category?.let { repository.deleteCategory(it) }
        view?.finish()
    }

    override fun onDeleteCategoryPressed() {
        view?.showConfirmDeleteDialog(state.category?.name ?: "")
    }
}