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

package com.mw.beam.beamwallet.screens.addresses

import com.mw.beam.beamwallet.base_screen.BasePresenter
import com.mw.beam.beamwallet.core.entities.WalletAddress
import com.mw.beam.beamwallet.core.helpers.Category
import com.mw.beam.beamwallet.core.helpers.TrashManager
import io.reactivex.disposables.Disposable

/**
 * Created by vain onnellinen on 2/28/19.
 */
class AddressesPresenter(currentView: AddressesContract.View, currentRepository: AddressesContract.Repository, private val state: AddressesState)
    : BasePresenter<AddressesContract.View, AddressesContract.Repository>(currentView, currentRepository),
        AddressesContract.Presenter {
    private lateinit var addressesSubscription: Disposable
    private lateinit var trashSubscription: Disposable

    override fun onViewCreated() {
        super.onViewCreated()
        view?.init()
    }

    override fun onAddressPressed(address: WalletAddress) {
        view?.showAddressDetails(address)
    }

    override fun initSubscriptions() {
        super.initSubscriptions()

        addressesSubscription = repository.getAddresses().subscribe {
            state.updateAddresses(it.addresses)

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
                    state.updateAddresses(it.data.addresses)
                    updateView()
                }
                TrashManager.ActionType.Removed -> {}
            }
        }
    }

    override fun onAddContactPressed() {
        view?.navigateToAddContactScreen()
    }

    private fun updateView() {
        val addresses = state.getAddresses()

        view?.updateAddresses(Tab.ACTIVE, addresses.filter { !it.isExpired && !it.isContact })
        view?.updateAddresses(Tab.EXPIRED, addresses.filter { it.isExpired && !it.isContact })
        view?.updateAddresses(Tab.CONTACTS, addresses.filter { it.isContact })
    }

    override fun onSearchCategoryForAddress(address: String): Category? = repository.getCategoryForAddress(address)

    override fun getSubscriptions(): Array<Disposable>? = arrayOf(addressesSubscription, trashSubscription)

    override fun hasBackArrow(): Boolean? = true
    override fun hasStatus(): Boolean = true
}
