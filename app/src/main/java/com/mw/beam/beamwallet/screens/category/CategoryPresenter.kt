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
import com.mw.beam.beamwallet.core.AppModel
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.TrashManager
import io.reactivex.disposables.Disposable

class CategoryPresenter(view: CategoryContract.View?, repository: CategoryContract.Repository, private val state: CategoryState)
    : BasePresenter<CategoryContract.View, CategoryContract.Repository>(view, repository), CategoryContract.Presenter {

    private lateinit var addressesSubscription: Disposable

    override fun onStart() {
        super.onStart()

        state.tag = repository.getCategoryFromId(view?.getCategoryId() ?: "")

        state.tag?.let {
            view?.init(it)
            updateView()
        }
    }

    override fun initSubscriptions() {
        super.initSubscriptions()

        addressesSubscription = AppModel.instance.subOnAddressesChanged.subscribe{
            updateView()
        }
    }


    private fun updateView() {
        view?.displayTabs(state.allCount() > 0)
        view?.updateAddresses(Tab.ADDRESSES,state.getAddresses(Tab.ADDRESSES))
        view?.updateAddresses(Tab.CONTACTS,state.getAddresses(Tab.CONTACTS))
    }

    override fun onAddressPressed(address: WalletAddress) {
        view?.showAddressDetails(address)
    }

    override fun getSubscriptions(): Array<Disposable>? = arrayOf(addressesSubscription)

    override fun onEditCategoryPressed() {
        state.tag?.let { view?.navigateToEditCategory(it.id) }
    }

    override fun onDeleteCategoryConfirmed() {
        state.tag?.let { repository.deleteCategory(it) }
        view?.finish()
    }

    override fun onDeleteCategoryPressed() {
        view?.showConfirmDeleteDialog(state.tag?.name ?: "")
    }
}