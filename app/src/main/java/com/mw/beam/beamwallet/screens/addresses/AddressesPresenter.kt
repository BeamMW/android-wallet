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
import com.mw.beam.beamwallet.core.helpers.ChangeAction
import com.mw.beam.beamwallet.core.helpers.Tag
import com.mw.beam.beamwallet.core.helpers.TrashManager
import io.reactivex.disposables.Disposable

/**
 * Created by vain onnellinen on 2/28/19.
 */
class AddressesPresenter(currentView: AddressesContract.View, currentRepository: AddressesContract.Repository, val state: AddressesState)
    : BasePresenter<AddressesContract.View, AddressesContract.Repository>(currentView, currentRepository),
        AddressesContract.Presenter {

    private lateinit var addressesSubscription: Disposable
    private lateinit var trashSubscription: Disposable
    private lateinit var txStatusSubscription: Disposable

    var removedAddresses = mutableListOf<String>()

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
            state.deleteRemovedAddresses(removedAddresses.toList())

            updateView()
        }


        trashSubscription = repository.getTrashSubject().subscribe {
            when (it.type) {
                TrashManager.ActionType.Added -> {
                    state.deleteAddresses(it.data.addresses)
                    updateView()
                }
                TrashManager.ActionType.Restored -> {
                    state.updateAddresses(it.data.addresses)
                    updateView()
                }
                TrashManager.ActionType.Removed -> {
                }
            }
        }

        txStatusSubscription = repository?.getTxStatus()?.subscribe { data ->
            if (data.action == ChangeAction.RESET || data.action == ChangeAction.ADDED) {
                if (data.action == ChangeAction.RESET) {
                    state.transactions.clear()
                }

                if (data.tx != null) {
                    state.transactions.addAll(data.tx!!)
                }

                state.deleteTransaction(repository.getAllTransactionInTrash())
            } else if (data.action == ChangeAction.REMOVED) {
                state.deleteTransaction(data.tx)
                state.deleteTransaction(repository.getAllTransactionInTrash())
            }

        }
    }

    override fun onAddContactPressed() {
        view?.navigateToAddContactScreen()
    }

    override fun onEditAddressPressed() {
        view?.navigateToEditAddressScreen()
    }

    override fun onCopyAddressPressed() {
        view?.copyAddress()
    }

    override fun onDeleteAddressesPressed() {
        view?.deleteAddresses()
    }

    override fun onDeleteAddress(selected: List<String>) {
        var showTransactionsAlert = false

        selected?.forEach { walletID ->
            if (state.getTransactions(walletID).count() > 0) {
                showTransactionsAlert = true
                return@forEach
            }
        }

        if (showTransactionsAlert) {
            view?.showDeleteAddressesDialog()
        } else {
            view?.showDeleteAddressesSnackBar(false)
        }
    }

    override fun onConfirmDeleteAddresses(withTransactions: Boolean, addresses: List<String>) {
        removedAddresses.clear()
        removedAddresses.addAll(addresses)

        for (i in 0 until addresses.count()) {
            val id = addresses[i]
            val address = state?.getAddresses()?.find { it.walletID == id }
            if (address != null) {
                repository.deleteAddress(address, if (withTransactions) state?.getTransactions(id) else listOf())
            }
        }
    }

    private fun updateView() {
        val addresses = state.getAddresses()

        view?.updateAddresses(Tab.ACTIVE, addresses.filter { !it.isExpired && !it.isContact })
        view?.updateAddresses(Tab.EXPIRED, addresses.filter { it.isExpired && !it.isContact })
        view?.updateAddresses(Tab.CONTACTS, addresses.filter { it.isContact })

        view?.updatePlaceholder(addresses.count() == 0)
    }

    override fun onSearchTagsForAddress(address: String): List<Tag> {
        return repository.getAddressTags(address)
    }

    override fun getSubscriptions(): Array<Disposable>? = arrayOf(addressesSubscription, trashSubscription)

    override fun hasBackArrow(): Boolean? = true
    override fun hasStatus(): Boolean = true
}
